package com.zotca.vbc.dbhistory.net;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

import com.zotca.vbc.dbhistory.bitmap.MemoryBitmapCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class HttpBitmapDownloader extends AsyncTask<String, Void, InputStream> {

	private final WeakReference<ImageView> mWeakRef;
	private String mUrl;
	private final MemoryBitmapCache mCustomCache;
	private final HttpClientHelper mHelper;
	
	public HttpBitmapDownloader(Context ctx, ImageView imageView, MemoryBitmapCache customCache)
	{
		mWeakRef = new WeakReference<ImageView>(imageView);
		if (customCache == null) mCustomCache = MemoryBitmapCache.getCache();
		else mCustomCache = customCache;
		mHelper = new HttpClientHelper(ctx);
	}
	
	@Override
	protected InputStream doInBackground(String... args) {
		mUrl = args[0];
		try {
			return mHelper.downloadGet(mUrl, false, true);
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
