package com.zotca.vbc.dbhistory.core;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public final class RuntimeTestUtility {

	public static final int EXTERNAL_STORAGE_NOT_AVAILABLE = -1;
	public static final int ARTHUR_NOT_INSTALLED = -2;
	public static final int GOOGLE_PLAY_ERROR = -3;
	public static final int SUCCESS = 0;
	
	private static final String ARTHUR_PACKAGE = "com.square_enix.million_kr";
	private static final String DEBUG_TAG = "RuntimeTestUtility";
	
	public static int test(Context ctx, boolean useGCM) {
		// External storage test
		final String extState = Environment.getExternalStorageState();
		if (!extState.equals(Environment.MEDIA_MOUNTED))
		{
			Log.e(DEBUG_TAG, "External storage not available");
			return EXTERNAL_STORAGE_NOT_AVAILABLE;
		}
		
		// Install test
		try {
			PackageInfo info = ctx.getPackageManager().getPackageInfo(ARTHUR_PACKAGE, 0);
			Log.d(DEBUG_TAG,
					"Installed MA version: " + info.versionName + " (" + info.versionCode + ")");
		} catch (NameNotFoundException e) {
			Log.e(DEBUG_TAG, "MA not installed");
			return ARTHUR_NOT_INSTALLED;
		}
		
		if (useGCM)
		{
			// Google Play service test
			int playServiceRet = testPlayService(ctx);
			if (playServiceRet != 0)
			{
				Log.e(DEBUG_TAG, "Google Play service error occured: " + playServiceRet);
				if (playServiceRet > 0)
					return playServiceRet;
				else
					return GOOGLE_PLAY_ERROR;
			}
		}
		
		return SUCCESS;
	}
	
	private static int testPlayService(Context ctx) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
		if (resultCode == ConnectionResult.SUCCESS)
		{
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				return resultCode;
			else
				return GOOGLE_PLAY_ERROR;
		}
		return 0;
	}
}
