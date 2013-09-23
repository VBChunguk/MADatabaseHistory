package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class DatabaseActivity extends FragmentActivity {

	private CardListPagerAdapter mPagerAdapter;
	private DatabaseFileManager mFileManager;
	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		this.setTitle(R.string.title_activity_database);
		
		mFileManager = DatabaseFileManager.getManager(this,
				new DatabaseFileManager.PostProcessHandler() {
			
			@Override
			public void onPostProcess(DatabaseFileManager manager) {
				mFileManager = manager;
				mPagerAdapter = new CardListPagerAdapter(getSupportFragmentManager(),
						mFileManager);
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mPagerAdapter);
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.database, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			SearchManager searchManager =
		           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		    SearchView searchView =
		            (SearchView) menu.findItem(R.id.search).getActionView();
		    searchView.setSearchableInfo(
		            searchManager.getSearchableInfo(getComponentName()));
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id)
		{
		case R.id.about:
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.search:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				this.onSearchRequested();
			break;
		default:
			return false;
		}
		return super.onOptionsItemSelected(item);
	}
}
