package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.CardDatabase.Card;
import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class CardViewActivity extends ActionBarActivity {

	public static final String ARG_ID = "id";
	public static final String ARG_PAGE = "defaultPage";
	
	private int mId;
	private ViewPager mViewPager;
	private CardHistoryPagerAdapter mPagerAdapter;
	private DatabaseFileManager mManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mId = -1;
		if (savedInstanceState != null) mId = savedInstanceState.getInt(ARG_ID, -1);
		setContentView(R.layout.activity_database);
		setupActionBar();
		
		Intent args = this.getIntent();
		if (mId == -1) mId = args.getIntExtra(ARG_ID, 0);
		final int page;
		if (savedInstanceState != null) page = savedInstanceState.getInt(ARG_PAGE, -1);
		else page = -1;
		final long time = args.getLongExtra(ARG_PAGE, -1);
		mManager = DatabaseFileManager.getManager(this,
				new DatabaseFileManager.PostProcessHandler() {
			
			@Override
			public void onPostProcess(DatabaseFileManager manager) {
				mManager = manager;
				mPagerAdapter = new CardHistoryPagerAdapter(
						getSupportFragmentManager(), manager, mId);
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mPagerAdapter);
				if (page != -1)
					mViewPager.setCurrentItem(page, false);
				else if (time != -1)
					mViewPager.setCurrentItem(mPagerAdapter.getIndexOf(time), false);
				{
					String title = mPagerAdapter.getPageCardName(mViewPager.getCurrentItem());
					char code = title.charAt(0);
					final String male = getResources().getString(R.string.history_male);
					final String female = getResources().getString(R.string.history_female);
					if (code == 'm') title = "[" + male + "] " + title.substring(1);
					else if (code == 'f') title = "[" + female + "] " + title.substring(1);
					setTitle(title);
				}
				mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageSelected(int position) {
						String title = mPagerAdapter.getPageCardName(position);
						char code = title.charAt(0);
						final String male = getResources().getString(R.string.history_male);
						final String female = getResources().getString(R.string.history_female);
						if (code == 'm') title = "[" + male + "] " + title.substring(1);
						else if (code == 'f') title = "[" + female + "] " + title.substring(1);
						setTitle(title);
					}
					
				});
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_ID, mId);
		outState.putInt(ARG_PAGE, mViewPager.getCurrentItem());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}
	
	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
		{
			StringBuilder builder = new StringBuilder();
			Card card = mManager.getCard(mId).get(mViewPager.getCurrentItem()).second;
			final String categoryStr;
			switch (card.getCategory())
			{
			case Card.BLADE:
				categoryStr = "검술";
				break;
			case Card.TECHNIQUE:
				categoryStr = "기교";
				break;
			case Card.MAGIC:
				categoryStr = "마법";
				break;
			case Card.FAIRY:
				categoryStr = "요정";
				break;
			default:
				categoryStr = "";
				break;
			}
			final String subSkillName =
					card.getSubSkillName()=="0"?"":" ("+card.getSubSkillName()+")";
			String skillDesc = card.getSkillDescription();
			if (skillDesc.length() <= 0) skillDesc = "";
			else skillDesc = "\n" + skillDesc;
			
			builder
			.append('[').append(categoryStr).append(' ')
			.append(Card.getRareLevelString(card.getRareLevel(), true)).append("] ")
			.append(card.getName()).append("\nCost: ").append(card.getCost())
			.append("\nIllustrated by ").append(card.getIllustrator()).append("\n\n[스킬]\n")
			.append(card.getSkillName()).append(subSkillName).append(skillDesc).append("\n\n")
			.append(card.getDescription());
			
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
			intent.setType("text/plain");
			startActivity(Intent.createChooser(
					intent, getResources().getString(R.string.share_with)));
		}
			return true;
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
}
