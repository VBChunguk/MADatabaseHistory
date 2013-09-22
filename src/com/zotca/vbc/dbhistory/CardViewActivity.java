package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

public class CardViewActivity extends FragmentActivity {

	public static String ARG_ID = "id";
	
	private int mId;
	private DatabaseFileManager mFileManager;
	private ViewPager mViewPager;
	private CardHistoryPagerAdapter mPagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		setupActionBar();
		
		Intent args = this.getIntent();
		mId = args.getIntExtra(ARG_ID, 0);
		
		mFileManager = DatabaseFileManager.getManager(this,
				new DatabaseFileManager.PostProcessHandler() {
			
			@Override
			public void onPostProcess(DatabaseFileManager manager) {
				mFileManager = manager;
				mPagerAdapter = new CardHistoryPagerAdapter(
						getSupportFragmentManager(), manager, mId);
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mPagerAdapter);
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
}
