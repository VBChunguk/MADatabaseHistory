package com.zotca.vbc.dbhistory.bitmap;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;

import com.zotca.vbc.crypt.ArthurDecodeStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapLoader extends AsyncTask<String, Void, Bitmap> {

	private final WeakReference<ImageView> mWeakRef;
	private String mPath;
	
	public BitmapLoader(ImageView view)
	{
		mWeakRef = new WeakReference<ImageView>(view);
		mPath = null;
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		mPath = params[0];
		try {
			return BitmapFactory.decodeStream(new ArthurDecodeStream(new FileInputStream(mPath), ArthurDecodeStream.KEY_IMAGE));
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		if (this.isCancelled())
			result = null;
		
		if (mWeakRef != null && result != null)
		{
			final MemoryBitmapCache cache = MemoryBitmapCache.getCache();
			cache.putChecked(mPath, result);
			
			final ImageView view = mWeakRef.get();
			final BitmapLoader bitmapLoader = getBitmapLoader(view);
			if (this == bitmapLoader && view != null)
			{
				view.setImageBitmap(result);
			}
		}
	}

	
	public static void loadBitmap(Resources res, String path, ImageView view)
	{
		Bitmap placeholder = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
		placeholder.setPixel(0, 0, Color.GRAY);
		loadBitmap(res, path, placeholder, view);
	}
	public static void loadBitmap(Resources res, String path, Bitmap placeholder, ImageView view)
	{
		if (cancelPotentialWork(path, view))
		{
			final MemoryBitmapCache cache = MemoryBitmapCache.getCache();
			final Bitmap bitmapCached = cache.get(path);
			if (bitmapCached != null)
			{
				view.setImageBitmap(bitmapCached);
			}
			else
			{
				final BitmapLoader bitmapLoader = new BitmapLoader(view);
				final AsyncBitmapDrawable asyncDrawable = 
						new AsyncBitmapDrawable(res, placeholder, bitmapLoader);
				view.setImageDrawable(asyncDrawable);
				bitmapLoader.execute(path);
			}
		}
	}
	public static boolean cancelPotentialWork(String path, ImageView view)
	{
		final BitmapLoader bitmapLoader = getBitmapLoader(view);
		
		if (bitmapLoader != null)
		{
			final String bitmapPath = bitmapLoader.mPath;
			if (bitmapPath != path)
				bitmapLoader.cancel(true);
			else
				return false;
		}
		return true;
	}
	
	private static BitmapLoader getBitmapLoader(ImageView view)
	{
		if (view != null)
		{
			final Drawable drawable = view.getDrawable();
			if (drawable instanceof AsyncBitmapDrawable)
			{
				final AsyncBitmapDrawable asyncDrawable = (AsyncBitmapDrawable) drawable;
				return asyncDrawable.getBitmapLoader();
			}
		}
		return null;
	}
}
