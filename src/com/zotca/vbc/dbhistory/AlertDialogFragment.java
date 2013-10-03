package com.zotca.vbc.dbhistory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {

	public static final String ARG_TITLE = "title";
	public static final String ARG_MESSAGE = "message";
	
	public static final String ARG_NEUTRAL = "neutral";
	public static final String ARG_POSITIVE = "positive";
	public static final String ARG_NEGATIVE = "negative";
	
	public static interface NotifyDialogListener {
		public void onDialogNeutralClick(DialogFragment dialog);
	}
	public static interface ConfirmDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	private NotifyDialogListener notifyListener;
	private ConfirmDialogListener confirmListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		notifyListener = null;
		confirmListener = null;
		
		if (activity instanceof NotifyDialogListener) {
			notifyListener = (NotifyDialogListener) activity;
		}
		
		if (activity instanceof ConfirmDialogListener) {
			confirmListener = (ConfirmDialogListener) activity;
		}
		
		if (notifyListener == null && confirmListener == null) {
			throw new ClassCastException(activity.toString() +
					" must implement either NotifyDialogListener or ConfirmDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		Bundle args = this.getArguments();
		final String title = args.getString(ARG_TITLE);
		final String message = args.getString(ARG_MESSAGE);
		if (title != null) builder.setTitle(title);
		if (message != null) builder.setMessage(message);
		
		if (notifyListener != null)
		{
			String neutralButton = args.getString(ARG_NEUTRAL);
			if (neutralButton == null)
				neutralButton = getResources().getString(android.R.string.ok);
			
			builder.setNeutralButton(neutralButton, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					notifyListener.onDialogNeutralClick(AlertDialogFragment.this);
				}
			});
		}
		if (confirmListener != null)
		{
			String positiveButton = args.getString(ARG_POSITIVE);
			String negativeButton = args.getString(ARG_NEGATIVE);
			if (positiveButton == null)
				positiveButton = getResources().getString(android.R.string.yes);
			if (negativeButton == null)
				negativeButton = getResources().getString(android.R.string.no);
			
			builder.setPositiveButton(positiveButton, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					confirmListener.onDialogPositiveClick(AlertDialogFragment.this);
				}
			}).setNegativeButton(negativeButton, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					confirmListener.onDialogNegativeClick(AlertDialogFragment.this);
				}
			});
		}
		return builder.create();
	}
}
