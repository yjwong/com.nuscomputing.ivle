package com.nuscomputing.ivle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * A dialog for "About". 
 * @author yjwong
 */
public class AboutApplicationDialogFragment extends DialogFragment {
	// {{{ methods
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get the information about this package.
		String version = "Unknown";
		try {
			PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			// Do nothing... Let the version remain unknown.
		}
		
		// Inflate the view and set the contents.
		LayoutInflater inflater = (LayoutInflater) new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.about_application_dialog_fragment, null);
		TextView textview = (TextView) view.findViewById(R.id.about_application_dialog_fragment_text);
		textview.setText(Html.fromHtml(getString(R.string.about_application_text, version)));
		textview.setLinksClickable(true);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Create the dialog.
		return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog))
			.setIcon(R.drawable.ic_launcher)
			.setTitle(getString(R.string.about_app_name, getString(R.string.app_name)))
			.setView(view)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();
	}
	
	// }}}
}
