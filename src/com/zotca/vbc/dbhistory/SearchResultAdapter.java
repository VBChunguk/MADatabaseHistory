package com.zotca.vbc.dbhistory;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;
import com.zotca.vbc.dbhistory.core.CardDatabase;
import com.zotca.vbc.dbhistory.core.MyCardManager;
import com.zotca.vbc.dbhistory.core.CardDatabase.Card;
import com.zotca.vbc.dbhistory.core.MyCardManager.ServerCard;
import com.zotca.vbc.dbhistory.core.MyCardManager.ServerCard.Attributes;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		
		MyCardManager cards = MyCardManager.getInstance();
		if (cards != null)
		{
			final TextView statView = (TextView) v.findViewById(R.id.stat);
			final ServerCard serverCardData = cards.getBestCard(id);
			if (serverCardData != null)
			{
				final int level = serverCardData.getIntAttribute(Attributes.LEVEL);
				final int hp = serverCardData.getIntAttribute(Attributes.HP);
				final int atk = serverCardData.getIntAttribute(Attributes.ATK);
				final boolean holo = serverCardData.getBooleanAttribute(Attributes.HOLOGRAPHY);
				
				final String statString = String.format(Locale.getDefault(),
						"%sLv. %d HP %d ATK %d", holo?"☆ ":"", level, hp, atk);
				statView.setText(statString);
			}
			else
				statView.setText("");
		}
		
		v.setTag(id);
		File icon = new File(iconDir, String.format(Locale.getDefault(), "face_%d", id));
		String iconPath = icon.getAbsolutePath();
		ImageView imageView = (ImageView) v.findViewById(R.id.icon);
		BitmapLoader.loadBitmap(mResources, iconPath, imageView);
		return v;
	}
}
