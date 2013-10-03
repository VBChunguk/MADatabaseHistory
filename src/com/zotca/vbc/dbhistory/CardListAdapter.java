package com.zotca.vbc.dbhistory;

import java.io.File;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;
import com.zotca.vbc.dbhistory.core.CardDatabase;
import com.zotca.vbc.dbhistory.core.DatabaseDelta;
import com.zotca.vbc.dbhistory.core.MyCardManager;
import com.zotca.vbc.dbhistory.core.MyCardManager.ServerCard;
import com.zotca.vbc.dbhistory.core.MyCardManager.ServerCard.Attributes;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<CardDatabase.Card> {

	private static File iconDir;
	
	static
	{
		File extstorage = Environment.getExternalStorageDirectory();
		iconDir = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/download/image/face/");
	}
	
	private DatabaseDelta mDelta;
	private SparseArray<DatabaseDelta.DeltaType> mDeltaType;
	private final Resources mResources;
	
	public CardListAdapter(Context context, DatabaseDelta delta) {
		super(context, R.layout.item_cardlist, R.id.name);
		mResources = context.getResources();
		mDelta = delta;
		mDeltaType = new SparseArray<DatabaseDelta.DeltaType>();
		for (int id : mDelta.getCardIdSet())
		{
			this.add(mDelta.getCard(id));
			mDeltaType.put(id, mDelta.getDeltaType(id));
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		TextView deltaInfo = (TextView) v.findViewById(R.id.delta_info);
		CardDatabase.Card card = this.getItem(position);
		int id = card.getId();
		switch (mDeltaType.get(id))
		{
		case MODIFIED:
			deltaInfo.setText(R.string.delta_modified);
			break;
		case ADDED:
			deltaInfo.setText(R.string.delta_added);
			break;
		case DELETED:
			deltaInfo.setText(R.string.delta_deleted);
			break;
		case INIT:
			deltaInfo.setText(R.string.delta_init);
			break;
		}
		
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
		}
		
		v.setTag(id);
		File icon = new File(iconDir, String.format(Locale.getDefault(), "face_%d", id));
		String iconPath = icon.getAbsolutePath();
		ImageView imageView = (ImageView) v.findViewById(R.id.icon);
		BitmapLoader.loadBitmap(mResources, iconPath, imageView);
		return v;
	}
}
