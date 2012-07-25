package com.nuscomputing.ivle;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
@TargetApi(11)
public class AboutApplicationDialogFragment extends DialogFragment {
	// {{{ methods
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get the version.
		String version = MainApplication.getVersionString();
		
		// Determine the context theme.
		final Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_Sherlock_Light_Dialog);
		
		// Inflate the view and set the contents.
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.about_application_dialog_fragment, null);
		TextView textview = (TextView) view.findViewById(R.id.about_application_dialog_fragment_text);
		textview.setText(Html.fromHtml(getString(R.string.about_application_text, version)));
		textview.setLinksClickable(true);
		textview.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Create the dialog.
		return new AlertDialog.Builder(context)
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
