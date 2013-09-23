package com.zotca.vbc.dbhistory;

import java.io.File;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class IllustFragment extends Fragment {

	private static File cardDir;
	
	static
	{
		File extstorage = Environment.getExternalStorageDirectory();
		cardDir = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/download/image/card/");
	}
	
	public static final String ARG_HORO = "is_horo";
	public static final String ARG_ID = "id";
	
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
		BitmapLoader.loadBitmap(this.getResources(), file.getAbsolutePath(), view);
		return view;
	}
}
