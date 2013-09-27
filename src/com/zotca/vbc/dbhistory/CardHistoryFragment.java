package com.zotca.vbc.dbhistory;

import java.io.File;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.BitmapLoader;
import com.zotca.vbc.dbhistory.core.CardDatabase.Card;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CardHistoryFragment extends Fragment {

	private static File cardDir;
	
	static
	{
		File extstorage = Environment.getExternalStorageDirectory();
		cardDir = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/download/image/card/");
	}
	
	public static final String ARG_CARD = "card";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_cardinfo, container, false);
		final Bundle args = this.getArguments();
		final Card card = (Card) args.getSerializable(ARG_CARD);
		
		final TextView illustratorView = (TextView) v.findViewById(R.id.illustrator);
		final TextView descriptionView = (TextView) v.findViewById(R.id.description);
		illustratorView.setText(this.getResources().getString(
				R.string.history_illustrated,
				card.getIllustrator()));
		descriptionView.setText(card.getDescription());
		
		final TextView mainSkillView = (TextView) v.findViewById(R.id.main_skill_name);
		final TextView subSkillView = (TextView) v.findViewById(R.id.sub_skill_name);
		final TextView skillDescView = (TextView) v.findViewById(R.id.skill_description);
		mainSkillView.setText(card.getSkillName());
		if (!card.getSubSkillName().equals("0"))
			subSkillView.setText("(" + card.getSubSkillName() + ")");
		skillDescView.setText(card.getSkillDescription());
		
		final File illust = new File(cardDir,
				String.format(Locale.getDefault(), "thumbnail_chara_%d", card.getId()));
		final ImageView illustView = (ImageView) v.findViewById(R.id.illust);
		final Context ctx = this.getActivity();
		illustView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent args = new Intent(ctx, IllustActivity.class);
				args.putExtra(IllustActivity.ARG_CARD, card);
				startActivity(args);
			}
			
		});
		BitmapLoader.loadBitmap(this.getResources(), illust.getAbsolutePath(), illustView);
		return v;
	}
}
