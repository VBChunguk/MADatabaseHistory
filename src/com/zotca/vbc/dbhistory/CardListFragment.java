package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseDelta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CardListFragment extends Fragment {

	public static final String ARG_ID = "id";
	public static final String ARG_DELTA = "delta";
	
	private long mId;
	private DatabaseDelta mDelta;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_cardlist, container, false);
		Bundle args = this.getArguments();
		mId = args.getLong(ARG_ID);
		mDelta = (DatabaseDelta) args.getSerializable(ARG_DELTA);
		ListView listView = (ListView) rootView;
		listView.setAdapter(new CardListAdapter(inflater.getContext(), mDelta));
		final Context ctx = inflater.getContext();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Intent args = new Intent(ctx, CardViewActivity.class);
				int cardId = (Integer) v.getTag();
				args.putExtra(CardViewActivity.ARG_ID, cardId);
				args.putExtra(CardViewActivity.ARG_PAGE, mId);
				startActivity(args);
			}
			
		});
		return rootView;
	}
}
