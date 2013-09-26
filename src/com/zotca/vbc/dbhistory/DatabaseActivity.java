package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class DatabaseActivity extends ActionBarActivity {

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
		case R.id.about:
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
		default:
			return false;
		}
		return super.onOptionsItemSelected(item);
	}
}
