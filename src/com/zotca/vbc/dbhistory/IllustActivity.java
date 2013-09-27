package com.zotca.vbc.dbhistory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import com.zotca.vbc.dbhistory.bitmap.MemoryBitmapCache;
import com.zotca.vbc.dbhistory.bitmap.ShareIllustProcessor;
import com.zotca.vbc.dbhistory.core.CardDatabase.Card;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class IllustActivity extends ActionBarActivity {
	public static final String ARG_CARD = "card";
	
	private static final boolean AUTO_HIDE = true;
	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

	private int id, idArousal;
	private Card card;
	
	int mControlsHeight;
	int mShortAnimTime;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void setControlsVisible(View controlsView, boolean visible) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			if (mControlsHeight == 0) {
				mControlsHeight = controlsView.getHeight();
			}
			if (mShortAnimTime == 0) {
				mShortAnimTime = getResources().getInteger(
						android.R.integer.config_shortAnimTime);
			}
			controlsView
					.animate()
					.translationY(visible ? 0 : mControlsHeight)
					.setDuration(mShortAnimTime);
		} else {
			// If the ViewPropertyAnimator APIs aren't
			// available, simply show or hide the in-layout UI
			// controls.
			controlsView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}
	
	private MemoryBitmapCache customCache;
	public MemoryBitmapCache getIllustCache() {
		return customCache;
	}
	
	private ViewPager contentView;
	private GestureDetectorCompat gestureDetector;
	private boolean controlsVisible;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null)
		{
			card = (Card) savedInstanceState.getSerializable(ARG_CARD);
			id = card.getNormalIllustId();
			idArousal = card.getArousalIllustId();
		}
		else
		{
			Intent args = this.getIntent();
			card = (Card) args.getSerializableExtra(ARG_CARD);
			id = card.getNormalIllustId();
			idArousal = card.getArousalIllustId();
		}

		setContentView(R.layout.activity_illust);
		
		customCache = new MemoryBitmapCache(10 * 1024); // 10MB
		
		final View controlsView = findViewById(R.id.pager_title_strip);
		contentView = (ViewPager) findViewById(R.id.pager);
		gestureDetector = new GestureDetectorCompat(
				this, new GestureDetector.SimpleOnGestureListener() {
					
					@Override
					public boolean onDown(MotionEvent e) {
						Log.d("", "onDown");
						return true;
					}
					
					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						Log.d("", "onSigleTapUp");
						getSupportActionBar().show();
						setControlsVisible(controlsView, true);
						delayedHide(AUTO_HIDE_DELAY_MILLIS);
						return true;
					}
					
					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						if (!controlsVisible) setControlsVisible(controlsView, true);
						controlsVisible = true;
						return true;
					}
		});

		contentView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_UP)
				{
					controlsVisible = false;
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});
		
		PagerAdapter adapter = new IllustPagerAdapter(getSupportFragmentManager(), id, idArousal);
		contentView.setAdapter(adapter);
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
	
	private static final String PREF_CONNECT = "internet_connect";
	private boolean mConnectInternet;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.illust, menu);
		
		mConnectInternet = getPreferences(MODE_PRIVATE).getBoolean(PREF_CONNECT, true);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId)
		{
		case R.id.share:
		{
			int page = contentView.getCurrentItem();
			boolean isHoro = false;
			boolean isArousal = false;
			if (page % 2 == 1) isArousal = true;
			if (page >= 2) isHoro = true;
			int id = isArousal ? idArousal : this.id;
			String fileName = String.format(Locale.getDefault(),
					"thumbnail_chara_%d%s", id, isHoro ? "_horo" : "");
			String urlName;
			try {
				urlName = new URL(IllustFragment.cardUrl, String.format(Locale.getDefault(),
						"card_full%s%s/full_%s", isHoro?"_h":"", id>5000?"_max":"", fileName)
						).toString();
			} catch (MalformedURLException e) {
				return true;
			}
			String filePath = new File(IllustFragment.cardDir, fileName).getAbsolutePath();
			Bitmap bitmap = customCache.get(urlName);
			if (bitmap == null) bitmap = MemoryBitmapCache.getCache().get(filePath);
			if (bitmap != null)
			{
				String title = String.format(Locale.getDefault(), "[%s] %s",
						Card.getRareLevelString(card.getRareLevel(), true), card.getName());
				String desc = String.format(Locale.getDefault(), "종류: %s %s",
						isHoro?"홀로그램":"노멀", isArousal?"각성":"일반");
				new ShareIllustProcessor(this).execute(bitmap, title, desc);
			}
		}
		}
		return false;
	}
	
	public boolean isConnectedToInternet() {
		return mConnectInternet;
	}
	
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			final View controlsView = findViewById(R.id.pager_title_strip);
			getSupportActionBar().hide();
			setControlsVisible(controlsView, false);
		}
	};

	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(ARG_CARD, card);
	}
}
