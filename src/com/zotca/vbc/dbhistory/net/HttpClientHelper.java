package com.zotca.vbc.dbhistory.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.zotca.vbc.crypt.ArthurDecodeStream;
import com.zotca.vbc.crypt.ArthurEncodeStream;
import com.zotca.vbc.crypt.Base64;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class HttpClientHelper {

	private static final String DEBUG_TAG = "HttpClientHelper";
	private static final String TARGET_SERVER = "http://ma.actoz.com:10001/";
	private static final int APP_VERSION = 103;
	
	private static final String AUTH_USERNAME = "iW7B5MWJ";
	private static final String AUTH_PASSWORD = "8KdtjVfX";
	private static final String USER_AGENT;
	
	static {
		StringBuilder builder = new StringBuilder();
		builder
		.append("Million/").append(APP_VERSION).append(" (")
		.append(Build.DEVICE).append("; ").append(Build.PRODUCT).append("; ")
		.append(Build.VERSION.RELEASE).append(") ").append(Build.FINGERPRINT);
		USER_AGENT = builder.toString();
	}
	
	private static List<NameValuePair> encodeArgs(Map<String, String> args)
			throws NoSuchAlgorithmException, IOException {
		class EncodedNameValuePair implements NameValuePair {

			private String name, codedValue;
			public EncodedNameValuePair(String name, String value) {
				try {
					this.name = name;
					byte[] valueBytes = value.getBytes();
					codedValue = Base64.encodeFromStream(
							new ArthurEncodeStream(
									new ByteArrayInputStream(valueBytes),
									ArthurEncodeStream.KEY_TEXT));
				} catch (NoSuchAlgorithmException e) {
				} catch (IOException e) {
				}
			}
			
			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getValue() {
				return codedValue;
			}
		}
		
		List<NameValuePair> ret = new ArrayList<NameValuePair>();
		if (args != null)
		{
			for (Entry<String, String> item : args.entrySet())
			{
				String key = item.getKey();
				String value = item.getValue();
				NameValuePair tempPair = new EncodedNameValuePair(key, value);
				Log.d(DEBUG_TAG, key + "=" + value + " ==> " + tempPair.getValue());
				ret.add(tempPair);
			}
		}
		return ret;
	}
	
	private DefaultHttpClient client;
	private Context ctx;
	
	public HttpClientHelper(Context ctx) {
		this.ctx = ctx;
		
		client = new DefaultHttpClient();
		client.getParams().setIntParameter("http.socket.timeout", 20000);
		client.getParams().setIntParameter("http.connection.timeout", 20000);
		
		URL url;
		try {
			url = new URL(TARGET_SERVER);
		} catch (MalformedURLException e) {
			Log.wtf(DEBUG_TAG, "Error parsing " + TARGET_SERVER + "???");
			return;
		}
		client.getCredentialsProvider().setCredentials(
				new AuthScope(url.getHost(), url.getPort()),
				new UsernamePasswordCredentials(AUTH_USERNAME, AUTH_PASSWORD));
	}
	
	public InputStream downloadGet(URI uri, boolean isCrypted, boolean isImage)
			throws IOException {
		if (!checkNetworkState(!isImage)) return null;
		
		InputStream in = null;
		Log.d(DEBUG_TAG,
				"GET Download from " + uri.toString() + (isCrypted?" - crypted connection":""));
		if (isCrypted) uri = URI.create(uri.toString() + "?cyt=1");
		try {
			HttpGet request = new HttpGet(uri);
			request.setHeader("User-Agent", USER_AGENT);
			request.setHeader("Accept-Encoding", "gzip, deflate");
			
			HttpResponse response = client.execute(request);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w(DEBUG_TAG,
						"GET Failed: " + response + " " + statusLine.getReasonPhrase());
				return null;
			}
			in = response.getEntity().getContent();
			if (isCrypted) in = new ArthurDecodeStream(in, ArthurDecodeStream.KEY_TEXT);
			
			Header encoding = response.getEntity().getContentEncoding();
			if (encoding != null && encoding.getValue().contains("gzip"))
				in = new GZIPInputStream(in);
			
			if (isImage) in = new ArthurDecodeStream(in, ArthurDecodeStream.KEY_IMAGE);
			return in;
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
	
	public InputStream downloadPost(URI uri, Map<String, String> args, boolean isImage)
			throws IOException {
		if (!checkNetworkState(!isImage)) return null;
		
		InputStream in = null;
		Log.d(DEBUG_TAG, "POST Download from " + uri.toString() + " - crypted connection");
		uri = URI.create(uri.toString() + "?cyt=1");
		try {
			HttpPost request = new HttpPost(uri);
			request.setEntity(new UrlEncodedFormEntity(encodeArgs(args)));
			request.setHeader("User-Agent", USER_AGENT);
			request.setHeader("Accept-Encoding", "gzip, deflate");
			
			HttpResponse response = client.execute(request);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w(DEBUG_TAG,
						"POST Failed: " + response + " " + statusLine.getReasonPhrase());
				return null;
			}
			in = response.getEntity().getContent();
			in = new ArthurDecodeStream(in, ArthurDecodeStream.KEY_TEXT);
			
			Header encoding = response.getEntity().getContentEncoding();
			if (encoding != null && encoding.getValue().contains("gzip"))
				in = new GZIPInputStream(in);
			
			if (isImage) in = new ArthurDecodeStream(in, ArthurDecodeStream.KEY_IMAGE);
			return in;
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
	
	private boolean checkNetworkState(boolean acceptMobileNetwork) {
		final ConnectivityManager manager = (ConnectivityManager)
				ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected())
		{
			if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				return acceptMobileNetwork;
			}
			else return true;
		}
		return false;
	}
}
