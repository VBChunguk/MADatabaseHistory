package com.zotca.vbc.dbhistory.net;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import com.zotca.vbc.dbhistory.bitmap.MemoryBitmapCache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class HttpBitmapDownloader extends AsyncTask<String, Void, InputStream> {

	private final WeakReference<ImageView> mWeakRef;
	private String mUrl;
	private final MemoryBitmapCache mCustomCache;
	
	public HttpBitmapDownloader(ImageView imageView, MemoryBitmapCache customCache)
	{
		mWeakRef = new WeakReference<ImageView>(imageView);
		if (customCache == null) mCustomCache = MemoryBitmapCache.getCache();
		else mCustomCache = customCache;
	}
	
	@Override
	protected InputStream doInBackground(String... args) {
		mUrl = args[0];
		try {
			return HttpDownloader.downloadGet(new URL(mUrl));
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

}
