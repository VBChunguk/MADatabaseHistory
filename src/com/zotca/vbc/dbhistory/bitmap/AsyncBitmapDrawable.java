package com.zotca.vbc.dbhistory.bitmap;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AsyncBitmapDrawable extends BitmapDrawable {

	private final WeakReference<BitmapLoader> mLoaderRef;
	
	public AsyncBitmapDrawable(Resources res, Bitmap bitmap, BitmapLoader loader)
	{
		super(res, bitmap);
		mLoaderRef = new WeakReference<BitmapLoader>(loader);
	}
	
	public BitmapLoader getBitmapLoader()
	{
		return mLoaderRef.get();
	}
}
