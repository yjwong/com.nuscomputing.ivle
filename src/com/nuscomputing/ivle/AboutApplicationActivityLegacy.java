package com.nuscomputing.ivle;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockDialogFragment;
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
		
		// Get the fragment manager.
		FragmentManager manager = getSupportFragmentManager();
		SherlockDialogFragment fragment = new AboutApplicationDialogFragmentLegacy();
		fragment.show(manager, null);
		fragment.setStyle(SherlockDialogFragment.STYLE_NORMAL, R.style.Theme_Sherlock_Light_Dialog);
	}
	
	// }}}
}
