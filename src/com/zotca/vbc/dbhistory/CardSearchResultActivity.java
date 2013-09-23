package com.zotca.vbc.dbhistory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.zotca.vbc.dbhistory.core.CardDatabase.Card;
import com.zotca.vbc.dbhistory.core.DatabaseDelta;
import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.os.Bundle;
import android.os.Handler;
import android.app.SearchManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class CardSearchResultActivity extends FragmentActivity {

	private DatabaseFileManager mFileManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("CardSearchResultActivity", "onCreate");
		setContentView(R.layout.fragment_cardlist);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mFileManager = DatabaseFileManager.getManager(this,
				new DatabaseFileManager.PostProcessHandler() {
			
			@Override
			public void onPostProcess(DatabaseFileManager manager) {
				mFileManager = manager;
				handleIntent(getIntent());
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}
	
	private void handleIntent(final Intent intent) {
		if (mFileManager == null) return;
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			final Handler handler = new Handler();
			final Context ctx = this;
			new Thread(new Runnable() {

				@Override
				public void run() {
					final String query = intent.getStringExtra(SearchManager.QUERY);
					final LinkedList<Long> chain = mFileManager.getChain();
					final ArrayList<Card> queryResult = new ArrayList<Card>();
					final HashSet<Integer> resultIds = new HashSet<Integer>();
					for (long time : chain)
					{
						final DatabaseDelta delta = mFileManager.getDelta(time);
						final Set<Integer> cards = delta.getCardIdSet();
						for (int id : cards)
						{
							if (queryResult.contains(id)) continue;
							
							final Card card = delta.getCard(id);
							if (matchesAdvancedQuery(card.getName(), query))
							{
								queryResult.add(card);
								resultIds.add(id);
							}
						}
					}
					
					handler.post(new Runnable() {

						@Override
						public void run() {
							ListView listView = (ListView) findViewById(android.R.id.list);
							SearchResultAdapter adapter = new SearchResultAdapter(ctx, queryResult);
							listView.setAdapter(adapter);
						}
						
					});
				}
			}).start();
		}
	}
	
	private static boolean matchesAdvancedQuery(String text, String query)
	{
		boolean inQuotes = false;
		boolean failed = false, passNext = false;
		StringBuilder inProcessString = new StringBuilder();
		boolean wasInQuote = false;
		for (char c : query.toCharArray())
		{
			if (c == '"')
			{
				if (inQuotes) wasInQuote = true;
				inQuotes = !inQuotes;
				continue;
			}
			if (inQuotes) inProcessString.append(c);
			else
			{
				if (c == ' ')
				{
					String res = inProcessString.toString().trim();
					inProcessString = new StringBuilder();
					if (!wasInQuote && res.equalsIgnoreCase("or"))
					{
						if (!failed) passNext = true;
						else failed = false;
						continue;
					}
					if (passNext)
					{
						passNext = false;
						continue;
					}
					if (failed) return false;
					if (!text.contains(res))
						failed = true;
				}
				else inProcessString.append(c);
			}
			wasInQuote = false;
		}
		String res = inProcessString.toString().trim();
		if (!wasInQuote && res.equalsIgnoreCase("or"))
		{
		}
		else
		{
			if (passNext) return true;
			if (failed) return false;
			if (!text.contains(res))
				failed = true;
		}
		return !failed;
	}
}
