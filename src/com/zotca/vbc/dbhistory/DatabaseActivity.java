package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
import com.zotca.vbc.dbhistory.core.RuntimeTestUtility;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class DatabaseActivity extends LoadCardActivityBase {

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

	@Override
	protected void onResume() {
		super.onResume();
		int ret = RuntimeTestUtility.test(getApplicationContext(), false);
		if (ret != 0) finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.database, menu);
		
		SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    MenuItem searchMenuItem = menu.findItem(R.id.search);
	    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id)
		{
		case R.id.settings:
			this.startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.search_help:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(
					Uri.parse("http://vbchunguk.github.io/MADatabaseHistory/help/search.html"));
			this.startActivity(intent);
			break;
		default:
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void reloadAfterLoad() {
		super.reloadAfterLoad();
		mPagerAdapter.notifyDataSetChanged();
	}
}
