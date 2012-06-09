package com.nuscomputing.ivle;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AccountSettingsFragment extends PreferenceFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "AccountSettingsFragment";
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the preferences tree.
        addPreferencesFromResource(R.layout.account_settings_fragment);
    }
    
    // }}}
}
