package com.zotca.vbc.dbhistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CardListPagerAdapter extends FragmentStatePagerAdapter {

	private DatabaseFileManager mFileManager;
	private long[] mPointers;
	
	public CardListPagerAdapter(FragmentManager fm, DatabaseFileManager dm) {
		super(fm);
		mFileManager = dm;
		int len = mFileManager.getChain().size();
		mPointers = new long[len];
		
		int i = 0;
		for (long pointer : mFileManager.getChain())
		{
			mPointers[i++] = pointer;
		}
	}

	@Override
	public Fragment getItem(int i) {
		Fragment f = new CardListFragment();
		Bundle args = new Bundle();
		args.putLong(CardListFragment.ARG_ID, mPointers[i]);
		args.putSerializable(CardListFragment.ARG_DELTA, mFileManager.getDelta(mPointers[i]));
		f.setArguments(args);
		return f;
	}

	@Override
	public int getCount() {
		return mPointers.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		long p = mPointers[position];
		Date date = new Date(p);
		return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, Locale.getDefault())
							   .format(date);
	}
}
