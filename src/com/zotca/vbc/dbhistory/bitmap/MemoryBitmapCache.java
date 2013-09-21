package com.zotca.vbc.dbhistory.bitmap;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

public class MemoryBitmapCache extends LruCache<String, Bitmap> {

	private static MemoryBitmapCache mObject;
	
	public static MemoryBitmapCache getCache()
	{
		return mObject;
	}
	
	static
	{
		mObject = new MemoryBitmapCache();
	}
	
	
	public MemoryBitmapCache() {
		super((int) (Runtime.getRuntime().maxMemory() / 1024 / 10)); // 1/10th of max memory
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	protected int sizeOf(String key, Bitmap value) {
		final int size;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			size = value.getByteCount();
		else
		{
			size = value.getRowBytes() * value.getHeight();
		}
		return size / 1024;
	}
	
	public Bitmap putChecked(String key, Bitmap value) {
		final Bitmap keyVal = this.get(key);
		if (keyVal == null)
			return this.put(key, value);
		return keyVal;
	}
}
