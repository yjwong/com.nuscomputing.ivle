package com.nuscomputing.ivle;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Legacy activity to show the dialog fragment.
 * @author yjwong
 */
public class AboutApplicationActivityLegacy extends SherlockFragmentActivity {
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the version.
		String version = MainApplication.getVersionString();
		
		// Inflate the view and set the contents.
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.about_application_dialog_fragment, null);
		TextView textview = (TextView) view.findViewById(R.id.about_application_dialog_fragment_text);
		textview.setText(Html.fromHtml(getString(R.string.about_application_text, version)));
		textview.setLinksClickable(true);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Set the title.
		setTitle(getString(R.string.about_app_name, getString(R.string.app_name)));
		
		// Set up the view.
		setContentView(view);
	}
	
	// }}}
}
