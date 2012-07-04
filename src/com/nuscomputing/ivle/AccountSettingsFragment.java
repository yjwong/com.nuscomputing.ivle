package com.nuscomputing.ivle;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

@TargetApi(11)
public class AccountSettingsFragment extends PreferenceFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "AccountSettingsFragment";
	
	/** The account being edited */
	private Account mAccount;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the account.
        Bundle args = getArguments();
        mAccount = args.getParcelable("account");
        
        // Set the shared preference file to use.
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName("account_" + mAccount.name);
        
        // Set up the preferences tree.
        addPreferencesFromResource(R.layout.account_settings_fragment);
        
        // Set up some options in the preference tree.
        setUpSyncInterval();
    }
    
    /**
     * Method: setUpSyncInterval
     * Sets up the sync interval preference item.
     */
    private void setUpSyncInterval() {
    	// Create a new list preference item.
    	ListPreference pref = (ListPreference) findPreference("sync_interval");
    	pref.setSummary(pref.getEntry());
    	
    	// Set up the periodic sync when this account is changed.
    	pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				int freqHours = Integer.parseInt(newValue.toString());
				if (freqHours > 0) {
					long freq = freqHours * 60 * 60; 
					ContentResolver.addPeriodicSync(mAccount, Constants.PROVIDER_AUTHORITY, new Bundle(), freq);
				} else {
					// "None" has been selected.
					ContentResolver.removePeriodicSync(mAccount, Constants.PROVIDER_AUTHORITY, new Bundle());
				}
				
				// Set the new summary.
				int newValueEntry = ((ListPreference) preference).findIndexOfValue(newValue.toString());
				preference.setSummary(((ListPreference) preference).getEntries()[newValueEntry]);
				return true;
			}
    	});
    }
    
    // }}}
}
