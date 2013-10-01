package com.zotca.vbc.dbhistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;
import com.zotca.vbc.dbhistory.bitmap.MemoryBitmapCache;
import com.zotca.vbc.dbhistory.net.HttpBitmapDownloader;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class IllustFragment extends Fragment {

	public static File cardDir;
	public static URL cardUrl;
	
	static
	{
		File extstorage = Environment.getExternalStorageDirectory();
		cardDir = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/download/image/card/");
		
		int version = 0;
		try {
			FileInputStream fis = new FileInputStream(new File(extstorage,
					"Android/data/com.square_enix.million_kr/files/save/appdata/save_version"));
			byte[] buf = new byte[4];
			fis.read(buf);
			fis.close();
			version = ByteBuffer.wrap(buf).getInt();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		if (version != 0)
		{
			try {
				cardUrl = new URL(String.format(Locale.getDefault(),
						"http://dn.actoz.hscdn.com/marthur/contents/%d/", version));
			} catch (MalformedURLException e) {
				cardUrl = null;
			}
		}
		else
			cardUrl = null;
	}
	
	public static final String ARG_HORO = "is_horo";
	public static final String ARG_ID = "id";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = this.getArguments();
		boolean isHoro = args.getBoolean(ARG_HORO);
		int id = args.getInt(ARG_ID);
		ImageView view = new ImageView(inflater.getContext());
		view.setContentDescription(this.getResources().getString(R.string.icon_description));
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		String fileName = String.format(Locale.getDefault(),
				"thumbnail_chara_%d%s", id, isHoro ? "_horo" : "");
		File file = new File(cardDir, fileName);
		
		String urlName = null;
		if (cardUrl != null)
		{
			try {
				urlName = new URL(cardUrl, String.format(Locale.getDefault(),
						"card_full%s%s/full_%s", isHoro ? "_h" : "", id>5000 ? "_max" : "", fileName)
						).toString();
			} catch (MalformedURLException e) {
			}
		}
		if (urlName != null)
		{
			final MemoryBitmapCache cache = ((IllustActivity) this.getActivity()).getIllustCache();
			final Bitmap bitmapCached = cache.get(urlName);
			if (bitmapCached != null)
			{
				view.setImageBitmap(bitmapCached);
			}
			else
			{
				BitmapLoader.loadBitmap(this.getResources(), file.getAbsolutePath(), view);
				boolean onlyWifi = PreferenceManager
						.getDefaultSharedPreferences(this.getActivity())
						.getBoolean("pref_onlywifi", false);
				if (checkNetworkState(onlyWifi))
				{
					final HttpBitmapDownloader downloader = new HttpBitmapDownloader(this.getActivity(), view, cache);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					{
						downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlName);
					}
					else
					{
						downloader.execute(urlName);
					}
				}
			}
		}
		else
			BitmapLoader.loadBitmap(this.getResources(), file.getAbsolutePath(), view);
		return view;
	}
	
	private boolean checkNetworkState(boolean acceptMobileNetwork) {
		final ConnectivityManager manager = (ConnectivityManager)
				this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
