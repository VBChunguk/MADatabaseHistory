package com.zotca.vbc.dbhistory;

import com.zotca.vbc.dbhistory.core.DatabaseFileManager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class DatabaseActivity extends FragmentActivity {

	public class DialogModifier {
		
		protected ProgressDialogFragment mDialog;
		protected Handler mHandler;
		
		public DialogModifier(ProgressDialogFragment d, Handler h) {
			mDialog = d;
			mHandler = h;
		}
		
		public void setTitle(final int resid) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mDialog.setTitle(resid);
				}
				
			});
		}
		public void setTitle(final String title) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mDialog.setTitle(title);
				}
				
			});
		}
		public void setMessage(final int resid) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					setMessage(getResources().getString(resid));
				}
				
			});
		}
		public void setMessage(final String message) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mDialog.setMessage(message);
				}
				
			});
		}
	}
	
	private CardListPagerAdapter mPagerAdapter;
	private DatabaseFileManager mFileManager;
	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		
		final ProgressDialogFragment df = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ProgressDialogFragment.ARG_TITLE_ID, R.string.progress_init_db);
		df.setArguments(args);
		class MyRunnable implements Runnable {

			private Handler mHandler;
			
			public MyRunnable(Handler h) {
				mHandler = h;
			}
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mFileManager = new DatabaseFileManager(getFilesDir(),
						new DialogModifier(df, mHandler));
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mPagerAdapter = new CardListPagerAdapter(getSupportFragmentManager(),
								mFileManager);
						mViewPager = (ViewPager) findViewById(R.id.pager);
						mViewPager.setAdapter(mPagerAdapter);
						df.dismissAllowingStateLoss();
					}
					
				});
			}
			
		};
		Thread th = new Thread(new MyRunnable(new Handler()));
		df.show(this.getSupportFragmentManager(), "db_progress");
		th.start();
	}

}
