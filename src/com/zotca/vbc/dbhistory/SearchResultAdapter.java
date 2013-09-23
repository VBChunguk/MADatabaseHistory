package com.zotca.vbc.dbhistory;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;
import com.zotca.vbc.dbhistory.core.CardDatabase;
import com.zotca.vbc.dbhistory.core.CardDatabase.Card;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class SearchResultAdapter extends ArrayAdapter<Card> {

	private static File iconDir;
	
	static
	{
		File extstorage = Environment.getExternalStorageDirectory();
		iconDir = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/download/image/face/");
	}
	
	private final Resources mResources;
	
	public SearchResultAdapter(Context context, Collection<? extends Card> list) {
		super(context, R.layout.item_cardlist, R.id.name);
		mResources = context.getResources();
		for (Card card : list) this.add(card);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		CardDatabase.Card card = this.getItem(position);
		int id = card.getId();
		v.setTag(id);
		File icon = new File(iconDir, String.format(Locale.getDefault(), "face_%d", id));
		String iconPath = icon.getAbsolutePath();
		ImageView imageView = (ImageView) v.findViewById(R.id.icon);
		BitmapLoader.loadBitmap(mResources, iconPath, imageView);
		return v;
	}
}
