package com.zotca.vbc.dbhistory.net;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import com.zotca.vbc.crypt.ArthurDecodeStream;
import com.zotca.vbc.dbhistory.bitmap.MemoryBitmapCache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class HttpDownloader extends AsyncTask<String, Void, InputStream> {

	private final WeakReference<ImageView> mWeakRef;
	private String mUrl;
	private final MemoryBitmapCache mCustomCache;
	
	public HttpDownloader(ImageView imageView, MemoryBitmapCache customCache)
	{
		mWeakRef = new WeakReference<ImageView>(imageView);
		if (customCache == null) mCustomCache = MemoryBitmapCache.getCache();
		else mCustomCache = customCache;
	}
	
	@Override
	protected InputStream doInBackground(String... args) {
		mUrl = args[0];
		try {
			return download(new URL(mUrl));
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(InputStream result) {
		if (this.isCancelled()) result = null;
		
		if (mWeakRef != null && result != null)
		{
			final Bitmap bitmap = BitmapFactory.decodeStream(result);
			mCustomCache.putChecked(mUrl, bitmap);
			
			final ImageView view = mWeakRef.get();
			if (view != null)
			{
				view.setImageBitmap(bitmap);
			}
		}
	}
	
	private static InputStream download(URL url) throws IOException
	{
		InputStream in = null;
		Log.d("HttpDownloader", "GET Download from " + url.toString());
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
				Log.w("HttpDownloader",
						"GET Failed: " + response + " " + conn.getResponseMessage());
			}
			in = conn.getInputStream();
			return new ArthurDecodeStream(in);
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
