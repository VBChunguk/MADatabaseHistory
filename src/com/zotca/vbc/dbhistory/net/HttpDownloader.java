package com.zotca.vbc.dbhistory.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpDownloader extends Thread {
	
	public static class DownloadRequest implements Runnable {
		
		private static final String ARG_CRYPTED = "crypted";
		private static final String ARG_IMAGE = "image";
		
		public static interface OnDownloadCompleted {
			public DownloadRequest onDownloadSucceeded(byte[] data) throws Exception;
			public void onDownloadFailed(Exception e);
			public void onDownloadCompleted();
		}
		
		public static class SimpleOnDownloadCompletedHandler implements OnDownloadCompleted {

			@Override
			public DownloadRequest onDownloadSucceeded(byte[] data) throws Exception {
				return null;
			}

			@Override
			public void onDownloadFailed(Exception e) {
			}

			@Override
			public void onDownloadCompleted() {
			}
		}
		
		private String endpoint;
		private boolean isPost;
		private HttpClientHelper helper;
		private Map<String, String> args;
		private Map<String, Object> internalArgs;
		private DownloadRequest next;
		private OnDownloadCompleted handler;
		private byte[] result;
		private int sleepLen;
		
		public DownloadRequest(String endpoint, boolean isPost) {
			this.endpoint = endpoint;
			this.isPost = isPost;
			this.args = null;
			this.internalArgs = new HashMap<String, Object>();
		}
		public DownloadRequest(String endpoint, boolean isPost, Map<String, String> args) {
			this.endpoint = endpoint;
			this.isPost = isPost;
			this.args = args;
			this.internalArgs = new HashMap<String, Object>();
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
		
		public DownloadRequest setClientHelper(HttpClientHelper helper) {
			this.helper = helper;
			return this;
		}
		
		public DownloadRequest setInternalArgument(String name, Object value) {
			this.internalArgs.put(name, value);
			return this;
		}
		
		private boolean isCrypted() {
			Boolean ret = (Boolean) internalArgs.get(ARG_CRYPTED);
			if (ret == null) return isPost;
			return ret.booleanValue();
		}
		private boolean isImage() {
			Boolean ret = (Boolean) internalArgs.get(ARG_IMAGE);
			if (ret == null) return !isPost;
			return ret.booleanValue();
		}
		
		@Override
		public void run() {
			try {
				InputStream resStream;
				if (!isPost) resStream = helper.downloadGet(endpoint, isCrypted(), isImage());
				else resStream = helper.downloadPost(endpoint, args, isImage());
				if (resStream == null) throw new IOException();
				
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				while (true)
				{
					int read = resStream.read(buffer);
					if (read < 0) break;
					os.write(buffer, 0, read);
				}
				result = os.toByteArray();
				os.close();
				
				if (handler != null)
				{
					DownloadRequest tempNext = handler.onDownloadSucceeded(result);
					if (tempNext != null) next = tempNext;
				}
			} catch (Exception e) {
				e.printStackTrace();
				next = null;
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
			if (current.next != null && current.next.helper == null)
				current.next.helper = current.helper;
			current = current.next; // chain
		}
	}
	
	public void quit() {
		stopSignal = true;
		if (sleeping) this.interrupt();
	}
}
