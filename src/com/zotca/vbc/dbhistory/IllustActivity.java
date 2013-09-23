package com.zotca.vbc.dbhistory;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class IllustActivity extends FragmentActivity {
	public static final String ARG_ID = "id";
	public static final String ARG_ID_AROUSAL = "id_arousal";
	
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

	private int id, idArousal;
	
	int mControlsHeight;
	int mShortAnimTime;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void setControlsVisible(View controlsView, boolean visible) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			// If the ViewPropertyAnimator API is available
			// (Honeycomb MR2 and later), use it to animate the
			// in-layout UI controls at the bottom of the
			// screen.
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
			controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
		if (visible) delayedHide(AUTO_HIDE_DELAY_MILLIS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null)
		{
			id = savedInstanceState.getInt(ARG_ID);
			idArousal = savedInstanceState.getInt(ARG_ID_AROUSAL);
		}
		else
		{
			Intent args = this.getIntent();
			id = args.getIntExtra(ARG_ID, 0);
			idArousal = args.getIntExtra(ARG_ID_AROUSAL, 0);
		}

		setContentView(R.layout.activity_illust);
		
		final View controlsView = findViewById(R.id.pager_title_strip);
		final ViewPager contentView = (ViewPager) findViewById(R.id.pager);

		contentView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			boolean settleFlag;
			
			@Override
			public void onPageSelected(int position) {
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onPageScrollStateChanged(int state) {
				switch (state)
				{
				case ViewPager.SCROLL_STATE_IDLE:
					if (!settleFlag)
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							getActionBar().show();
						}
						setControlsVisible(controlsView, true);
					}
					settleFlag = false;
					break;
				case ViewPager.SCROLL_STATE_DRAGGING:
					setControlsVisible(controlsView, true);
					break;
				case ViewPager.SCROLL_STATE_SETTLING:
					settleFlag = true;
					break;
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.pager_title_strip).setOnTouchListener(
				mDelayHideTouchListener);
		
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

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
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
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void run() {
			final View controlsView = findViewById(R.id.pager_title_strip);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				getActionBar().hide();
			}
			setControlsVisible(controlsView, false);
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_ID, id);
		outState.putInt(ARG_ID_AROUSAL, idArousal);
	}
}
