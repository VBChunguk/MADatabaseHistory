package com.zotca.vbc.dbhistory;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

public class AboutActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		try {
			TextView versionView = (TextView) findViewById(R.id.version);
			String version = getPackageManager()
					.getPackageInfo(getPackageName(), 0).versionName;
			versionView.setText(version);
		} catch (NameNotFoundException e) {
		}
		// Show the Up button in the action bar.
		setupActionBar();
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void viewCoderTwitter(View v)
	{
		this.startActivity(
				new Intent(Intent.ACTION_VIEW)
				.setData(Uri.parse("http://twitter.com/VBHimesama")));
	}
	
}
