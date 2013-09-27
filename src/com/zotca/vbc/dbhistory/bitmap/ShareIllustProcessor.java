package com.zotca.vbc.dbhistory.bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.zotca.vbc.dbhistory.ProgressDialogFragment;
import com.zotca.vbc.dbhistory.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;

public class ShareIllustProcessor extends AsyncTask<Object, Void, Uri> {

	private final FragmentActivity mActivity;
	private final ProgressDialogFragment mDialog;
	
	public ShareIllustProcessor(FragmentActivity activity) {
		mActivity = activity;
		mDialog = new ProgressDialogFragment();
	}
	
	@Override
	protected void onPreExecute() {
		Bundle args = new Bundle();
		args.putInt(ProgressDialogFragment.ARG_MESSAGE_ID, R.string.share_prepare);
		args.putString(ProgressDialogFragment.ARG_TITLE, "");
		mDialog.setArguments(args);
		mDialog.show(mActivity.getSupportFragmentManager(), "illust_progress");
	}
	
	private String title, desc;
	@Override
	protected Uri doInBackground(Object... args) {
		try {
			title = (String) args[1];
			desc = (String) args[2];
			OutputStream os = mActivity.openFileOutput("temp", Context.MODE_PRIVATE);
			((Bitmap) args[0]).compress(CompressFormat.PNG, 9, os);
			os.close();
			
			File tempFile = mActivity.getFileStreamPath("temp");
			String uri = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(),
					tempFile.getAbsolutePath(), title, desc);
			mActivity.deleteFile("temp");
			return Uri.parse(uri);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Uri result) {
		if (this.isCancelled()) result = null;
		
		if (result != null)
		{
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_STREAM, result);
			intent.setType("image/png");
			mDialog.dismissAllowingStateLoss();
			mActivity.startActivity(Intent.createChooser(intent,
					mActivity.getResources().getString(R.string.share_with)));
		}
	}
}
