package com.zotca.vbc.dbhistory;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class IllustPagerAdapter extends FragmentStatePagerAdapter {

	private final int mId, mIdArousal;
	
	public IllustPagerAdapter(FragmentManager fm, int id, int idArousal) {
		super(fm);
		mId = id;
		mIdArousal = idArousal;
	}

	@Override
	public Fragment getItem(int i) {
		boolean isHoro;
		boolean isArousal;
		isArousal = i % 2 == 1;
		isHoro = i >= 2;
		Bundle args = new Bundle();
		args.putBoolean(IllustFragment.ARG_HORO, isHoro);
		args.putInt(IllustFragment.ARG_ID, isArousal ? mIdArousal : mId);
		IllustFragment f = new IllustFragment();
		f.setArguments(args);
		return f;
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		boolean isHoro;
		boolean isArousal;
		isArousal = position % 2 == 1;
		isHoro = position >= 2;
		
		return String.format(Locale.getDefault(),
				"%s %s", isHoro ? "홀로그램" : "노멀", isArousal ? "각성" : "일반");
	}
}
