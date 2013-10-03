package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.AlertDialogFragment.ConfirmDialogListener;
import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
import com.zotca.vbc.dbhistory.core.MyCardManager;
import com.zotca.vbc.dbhistory.core.RuntimeTestUtility;
import com.zotca.vbc.dbhistory.net.ArthurLoginHelper;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest.OnDownloadCompleted;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class DatabaseActivity extends ActionBarActivity implements ConfirmDialogListener {

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
	public void onDialogPositiveClick(DialogFragment dialog) {
		dialog.dismiss();
		
		final ProgressDialogFragment df = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ProgressDialogFragment.ARG_TITLE_ID, R.string.dialog_inprogress_mycard);
		df.setArguments(args);
		df.show(getSupportFragmentManager(), "mycard_prog");
		ArthurLoginHelper.initializeClient(getApplicationContext(), new OnDownloadCompleted() {
			
			@Override
			public DownloadRequest onDownloadSucceeded(byte[] data) throws Exception {
				new MyCardManager(data);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						df.dismissAllowingStateLoss();
						mPagerAdapter.notifyDataSetChanged();
					}
				});
				return null;
			}
			
			@Override
			public void onDownloadFailed(Exception e) {
				e.printStackTrace();
			}
			
			@Override
			public void onDownloadCompleted() {
			}
		}, false);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
}
