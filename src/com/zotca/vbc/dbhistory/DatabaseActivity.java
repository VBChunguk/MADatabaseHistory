package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class DatabaseActivity extends FragmentActivity {

	private CardListPagerAdapter mPagerAdapter;
	private DatabaseFileManager mFileManager;
	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		mFileManager = new DatabaseFileManager(this.getFilesDir());
		mPagerAdapter = new CardListPagerAdapter(this.getSupportFragmentManager(), mFileManager);
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
	}

}
