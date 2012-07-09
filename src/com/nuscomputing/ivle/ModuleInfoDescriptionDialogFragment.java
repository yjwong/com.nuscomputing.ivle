package com.nuscomputing.ivle;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.RelativeLayout;

/**
 * Displays module information description.
 * @author yjwong
 */
public class ModuleInfoDescriptionDialogFragment extends SherlockDialogFragment {
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 11) {
			setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
		} else {
			setStyle(SherlockDialogFragment.STYLE_NORMAL, R.style.Theme_Sherlock_Light_Dialog);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Obtain the arguments.
		Bundle args = getArguments();
		String description = args.getString("description");
		String title = args.getString("title");
		
		// Set the title.
		getDialog().setTitle(title);
		
		// Create the linear layout.
		RelativeLayout layout = new RelativeLayout(getActivity());
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		layout.setMinimumWidth(450);
		
		// Create the webview.
		WebView webview = new WebView(getActivity());
		webview.getSettings().setBuiltInZoomControls(true);
		webview.loadData(description, "text/html", null);
		
		// Set the layout parameters.
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		webview.setLayoutParams(params);
		
		// Add the webview.
		layout.addView(webview);
		return layout;
	}
	
	// }}}
}
