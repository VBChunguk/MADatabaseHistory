package com.zotca.vbc.dbhistory.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.zotca.vbc.dbhistory.net.HttpDownloader;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest.SimpleOnDownloadCompletedHandler;

public final class ArthurLoginHelper {

	private static final String CHECK_INSPECTION = "/connect/app/check_inspection";
	private static final String POST_DEVICETOKEN = "/connect/app/notification/post_devicetoken";
	private static final String LOGIN = "/connect/app/login";
	
	private static final String PREF_REGID = "regid";
	private static final String ARTHUR_SENDER_ID = "432099844534";
	private static final String ARTHUR_APPDATA = "Android/data/com.square_enix.million_kr/files/save/appdata/save_appdata";
	
	private static final String DEBUG_TAG = "ArthurLoginHelper";
	
	public synchronized static HttpClientHelper initializeClient(
			final Context ctx, final DownloadRequest.OnDownloadCompleted completeHandler) {
		HttpClientHelper helper = new HttpClientHelper(ctx);
		DownloadRequest startReq = new DownloadRequest(CHECK_INSPECTION, true)
				.setClientHelper(helper)
				.setOnDownloadCompletedListener(new SimpleOnDownloadCompletedHandler() {
					@Override
					public DownloadRequest onDownloadSucceeded(byte[] data) throws Exception {
						String regId = getRegistrationId(ctx);
						if (regId == null) throw new Exception();
						regId = Base64.encodeToString(regId.getBytes(), Base64.DEFAULT);
						String id = getId();
						String pw = getPassword();
						Log.d(DEBUG_TAG, id + "," + pw);
						Log.d(DEBUG_TAG, regId);
						
						Map<String, String> args = new HashMap<String, String>();
						args.put("S", "nosession");
						args.put("login_id", id);
						args.put("password", pw);
						args.put("app", "and");
						args.put("token", regId);
						
						Map<String, String> loginArgs = new HashMap<String, String>();
						loginArgs.put("login_id", id);
						loginArgs.put("password", pw);
						DownloadRequest nextReq =
								new DownloadRequest(POST_DEVICETOKEN, true, args)
								.setNext(new DownloadRequest(LOGIN, true, loginArgs)
								.setOnDownloadCompletedListener(completeHandler) );
						return nextReq;
					}
				});
		
		HttpDownloader downloader = new HttpDownloader(startReq);
		downloader.start();
		return helper;
	}
	
	private static GoogleCloudMessaging gcm;
	public static String getRegistrationId(Context ctx) {
		final SharedPreferences pref =
				ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
		String ret = pref.getString(PREF_REGID, null);
		if (ret != null) return ret;
		
		try {
			if (gcm == null) gcm = GoogleCloudMessaging.getInstance(ctx);
			ret = gcm.register(ARTHUR_SENDER_ID);
			pref.edit().putString(PREF_REGID, ret).commit();
		} catch (IOException e) {
			return null;
		}
		return ret;
	}
	
	private static String id, password;
	private static String getId() {
		if (id == null) loadAccountInfo();
		return id;
	}
	private static String getPassword() {
		if (password == null) loadAccountInfo();
		return password;
	}
	
	private static void loadAccountInfo() {
		File baseDir = Environment.getExternalStorageDirectory();
		File appData = new File(baseDir, ARTHUR_APPDATA);
		byte[] idBytes = new byte[0x40];
		byte[] pwBytes = new byte[0x40];
		try {
			FileInputStream stream = new FileInputStream(appData);
			stream.skip(0x124);
			stream.read(idBytes);
			stream.read(pwBytes);
			stream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		int idLen = findNull(idBytes);
		int pwLen = findNull(pwBytes);
		
		id = new String(idBytes, 0, idLen);
		password = new String(pwBytes, 0, pwLen);
	}
	
	private static int findNull(byte[] data) {
		int len = data.length;
		for (int i = 0; i < len; i++)
		{
			if (data[i] == 0) return i;
		}
		return len;
	}
}
