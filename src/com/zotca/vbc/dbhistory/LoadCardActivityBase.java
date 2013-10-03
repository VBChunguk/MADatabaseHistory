package com.zotca.vbc.dbhistory;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;

import com.zotca.vbc.dbhistory.AlertDialogFragment.ConfirmDialogListener;
import com.zotca.vbc.dbhistory.core.MyCardManager;
import com.zotca.vbc.dbhistory.net.ArthurLoginHelper;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest;
import com.zotca.vbc.dbhistory.net.HttpDownloader.DownloadRequest.OnDownloadCompleted;

public class LoadCardActivityBase extends ActionBarActivity implements
		ConfirmDialogListener {

	protected void reloadAfterLoad() {
	}
	
	private boolean retry;
	private Object retryLock = new Object();
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		if (dialog.getTag().equals("retry_ask"))
		{
			retry = true;
			dialog.dismiss();
			retryLock.notify();
			return;
		}
		dialog.dismiss();
		
		final ProgressDialogFragment df = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString(ProgressDialogFragment.ARG_TITLE, "");
		args.putInt(ProgressDialogFragment.ARG_MESSAGE_ID, R.string.dialog_inprogress_mycard);
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
						reloadAfterLoad();
					}
				});
				return null;
			}
			
			@Override
			public boolean onDownloadFailed(Exception e) {
				e.printStackTrace();
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						final DialogFragment confirm = new AlertDialogFragment();
						Bundle confirmArgs = new Bundle();
						confirmArgs.putString(
								AlertDialogFragment.ARG_TITLE,
								getString(R.string.dialog_title_commerror));
						confirmArgs.putString(
								AlertDialogFragment.ARG_MESSAGE,
								getString(R.string.dialog_message_commerror));
						confirmArgs.putString(
								AlertDialogFragment.ARG_POSITIVE,
								getString(R.string.dialog_positive_commerror));
						confirmArgs.putString(
								AlertDialogFragment.ARG_NEGATIVE,
								getString(android.R.string.cancel));
						confirm.setArguments(confirmArgs);
						confirm.show(getSupportFragmentManager(), "retry_ask");
					}
				});
				retry = false;
				try {
					retryLock.wait();
				} catch (InterruptedException e1) {
					retry = false;
				}
				
				if (!retry)
				{
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							df.dismissAllowingStateLoss();
						}
					});
				}
				return retry;
			}
			
			@Override
			public void onDownloadCompleted() {
			}
		}, false);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		if (dialog.getTag().equals("retry_ask"))
		{
			retry = false;
			dialog.dismiss();
			retryLock.notify();
			return;
		}
		dialog.dismiss();
	}

}
