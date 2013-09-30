package com.zotca.vbc.dbhistory.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.zotca.vbc.crypt.ArthurDecodeStream;
import com.zotca.vbc.crypt.ArthurEncodeStream;
import com.zotca.vbc.crypt.Base64;

public class HttpDownloader extends Thread {

	private static final String DEBUG_TAG = "HttpDownloader";
	
	public static class DownloadRequest implements Runnable {
		
		public static interface OnDownloadCompleted {
			public void onDownloadSucceeded(byte[] data);
			public void onDownloadFailed(Exception e);
			public void onDownloadCompleted();
		}
		
		private String url;
		private boolean isPost;
		private Map<String, String> args;
		private DownloadRequest next;
		private OnDownloadCompleted handler;
		private byte[] result;
		private int sleepLen;
		
		public DownloadRequest(String url, boolean isPost) {
			this.url = url;
			this.isPost = isPost;
			this.args = null;
		}
		public DownloadRequest(String url, boolean isPost, Map<String, String> args) {
			this.url = url;
			this.isPost = isPost;
			this.args = args;
		}
		
		public DownloadRequest setNext(DownloadRequest next) {
			this.next = next;
			return this;
		}
		
		public DownloadRequest setOnDownloadCompletedListener(OnDownloadCompleted l) {
			handler = l;
			return this;
		}
		
		public DownloadRequest setSleepLength(int sleepLen) {
			this.sleepLen = sleepLen;
			return this;
		}
		
		@Override
		public void run() {
			try {
				URL url = new URL(this.url);
				InputStream resStream;
				if (!isPost) resStream = downloadGet(url);
				else resStream = downloadPost(url, args);
				
				byte[] buffer = new byte[1024];
				ByteBuffer ret = ByteBuffer.allocate(1024);
				ret.mark();
				while (true)
				{
					int read = resStream.read(buffer);
					if (read < 0) break;
					ret.put(buffer, 0, read);
				}
				
				ret.limit(ret.position()).reset();
				int len = ret.remaining();
				result = new byte[len];
				ret.get(result);
				
				if (handler != null) handler.onDownloadSucceeded(result);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				if (handler != null) handler.onDownloadFailed(e);
			} catch (IOException e) {
				e.printStackTrace();
				if (handler != null) handler.onDownloadFailed(e);
			} finally {
				if (handler != null) handler.onDownloadCompleted();
			}
		}
	}
	
	private DownloadRequest start;
	
	public HttpDownloader(DownloadRequest start) {
		super();
		this.start = start;
	}
	
	boolean sleeping;
	boolean stopSignal;
	@Override
	public void run() {
		DownloadRequest current = start;
		while (!stopSignal && current != null)
		{
			current.run();
			if (current.sleepLen > 0)
			{
				try {
					sleeping = true;
					Thread.sleep(current.sleepLen);
				} catch (InterruptedException e) {
				} finally {
					sleeping = false;
				}
			}
			current = current.next; // chain
		}
	}
	
	public void quit() {
		stopSignal = true;
		if (sleeping) this.interrupt();
	}
	
	public static InputStream downloadGet(URL url) throws IOException
	{
		InputStream in = null;
		Log.d(DEBUG_TAG, "GET Download from " + url.toString());
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			int response = conn.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK)
			{
				Log.w(DEBUG_TAG,
						"GET Failed: " + response + " " + conn.getResponseMessage());
			}
			in = conn.getInputStream();
			return new ArthurDecodeStream(in, ArthurDecodeStream.KEY_IMAGE);
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	public static InputStream downloadPost(URL url, Map<String, String> args) throws IOException
	{
		InputStream in = null;
		Log.d(DEBUG_TAG, "POST Download from " + url.toString());
		url = new URL(url.toString() + "?cyt=1");
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			StringBuilder argsString = new StringBuilder();
			if (args != null)
			{
				boolean first = true;
				for (Entry<String, String> item : args.entrySet())
				{
					String key = item.getKey();
					String value = item.getValue();
					byte[] valueBytes = value.getBytes();
					String codedValue = Base64.encodeFromStream(
							new ArthurEncodeStream(
									new ByteArrayInputStream(valueBytes),
									ArthurEncodeStream.KEY_TEXT));
					Log.d(DEBUG_TAG, key + "=" + value + " ==> " + codedValue);
					if (!first) argsString.append('&');
					argsString.append(key).append('=').append(codedValue);
					first = false;
				}
			}
			byte[] argsBytes = argsString.toString().getBytes();
			conn.getOutputStream().write(argsBytes);
			
			int response = conn.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK)
			{
				Log.w(DEBUG_TAG,
						"POST Failed: " + response + " " + conn.getResponseMessage());
			}
			in = conn.getInputStream();
			return new ArthurDecodeStream(in, ArthurDecodeStream.KEY_TEXT);
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null)
				in.close();
		}
	}
}
