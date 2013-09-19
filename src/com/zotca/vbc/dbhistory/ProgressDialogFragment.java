package com.zotca.vbc.dbhistory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

	public static final String ARG_TITLE_ID = "title_id";
	public static final String ARG_MESSAGE_ID = "message_id";
	public static final String ARG_TITLE = "title";
	public static final String ARG_MESSAGE = "message";
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = this.getArguments();
		int title_id = args.getInt(ARG_TITLE_ID);
		int message_id = args.getInt(ARG_MESSAGE_ID);
		String title = args.getString(ARG_TITLE);
		String message = args.getString(ARG_MESSAGE);
		if (title == null)
		{
			if (title_id != 0) title = this.getResources().getString(title_id);
			else title = "";
		}
		if (message == null)
		{
			if (message_id != 0) message = this.getResources().getString(message_id);
			else message = "";
		}
		ProgressDialog dialog = new ProgressDialog(this.getActivity(), ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(true);
		dialog.setTitle(title);
		dialog.setMessage(message);
		return dialog;
	}
	
	public void setTitle(int resid) {
		ProgressDialog dialog = (ProgressDialog) this.getDialog();
		dialog.setTitle(resid);
	}
	public void setTitle(String message) {
		ProgressDialog dialog = (ProgressDialog) this.getDialog();
		dialog.setTitle(message);
	}
	public void setMessage(int resid) {
		setMessage(this.getResources().getString(resid));
	}
	public void setMessage(String message) {
		ProgressDialog dialog = (ProgressDialog) this.getDialog();
		dialog.setMessage(message);
	}
}
