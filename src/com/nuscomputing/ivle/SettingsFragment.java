package com.nuscomputing.ivle;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

@TargetApi(11)
public class SettingsFragment extends PreferenceFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "SettingsFragment";
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the preferences tree.
        addPreferencesFromResource(R.layout.settings_fragment);
        
        // Set up some options in the preferences tree.
        setUpAccount();
        setUpAddAccount();
        setUpManageAccounts();
        setUpAbout();
        setUpSendFeedback();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Reload the accounts list.
    	setUpAccount();
    }

    /**
     * Method: setUpAccounts
     * Populates the accounts list.
     */
    private void setUpAccount() {
    	// Obtain an instance of AccountManager.
        AccountManager am = AccountManager.get(getActivity());
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        
        // Obtain the accounts preference.
        ListPreference accountsPreference = (ListPreference) findPreference("account");
        
        // Reset the state of the preference.
        accountsPreference.setEnabled(true);
        
        // Populate the list.
        String[] accountEntries = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
        	accountEntries[i] = accounts[i].name;
        }
        accountsPreference.setEntries(accountEntries);
        accountsPreference.setEntryValues(accountEntries);
        
    	// There are no accounts, disable this.
    	if (accounts.length == 0) {
    		accountsPreference.setSummary(getString(R.string.settings_fragment_no_accounts_configured));
    		accountsPreference.setEnabled(false);
    	}
        
        // If there is only one account, it should be the current and default one.
        if (accounts.length == 1) {
        	accountsPreference.setSummary(accounts[0].name);
        }
        
        // If is more than one account, search for the current active one.
        if (accounts.length > 1) {
        	Account currentAccount = AccountUtils.getActiveAccount(getActivity());
        	if (currentAccount == null) {
        		accountsPreference.setSummary(accounts[0].name);
        	} else {
        		accountsPreference.setSummary(currentAccount.name);
        	}
        }
        
        // When the preference is changed, update the summary too.
        final Context context = getActivity();
        accountsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String oldPref = preference.getSharedPreferences().getString("account", null);
				if (oldPref.equals(newValue)) {
					return false;
				}
				
				// Change the summary value.
				preference.setSummary(newValue.toString());
				
				// Build a dialog to inform the user that we need to restart.
				AlertDialog dialog = new AlertDialog.Builder(context).create();
				dialog.setMessage(getString(R.string.settings_fragment_app_will_restart));
				
	    		// Set button parameters and show the dialog.
	    		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						// Restart the application.
						Intent intent = new Intent(getActivity(), MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				});
	    		dialog.show();
				return true;
			}
        });
    }
    
    /**
     * Method: setUpAddAccount
     * Action for adding an account.
     */
    private void setUpAddAccount() {
    	// Set up new intent to launch the authenticator activity.
    	Intent intent = new Intent();
    	intent.setClass(getActivity(), AuthenticatorActivity.class);
    	
        // Obtain the add_account preference.
        Preference addAccountPreference = findPreference("add_account");
        addAccountPreference.setIntent(intent);
    }
    
    /**
     * Method: setUpManageAccounts
     * Action for managing accounts.
     */
    private void setUpManageAccounts() {
    	// Set up new intent to launch the "Accounts and Sync Settings" screen.
    	Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
    	intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] { Constants.PROVIDER_AUTHORITY });
    	
    	// Obtain the manage_accounts preference.
    	Preference manageAccountsPreference = findPreference("manage_accounts");
    	manageAccountsPreference.setIntent(intent);
    }
    
    /**
     * Method: setUpAbout
     * Action for the about dialog.
     */
    private void setUpAbout() {
    	Preference aboutPreference = findPreference("about");
    	aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	// Get the fragment manager.
		    	DialogFragment fragment = new AboutApplicationDialogFragment();
		    	FragmentManager manager = getActivity().getFragmentManager();
		    	fragment.show(manager, null);
		    	return true;
			}
    	});
    	
		// Get the information about this package.
		String version = MainApplication.getVersionString();
    	aboutPreference.setTitle(getString(R.string.settings_fragment_about_title, version));
    	aboutPreference.setSummary(getString(R.string.settings_fragment_about_summary));
    }
    
    /**
     * Method: setUpSendFeedback
     * Action for sending feedback.
     */
    private void setUpSendFeedback() {
    	// Get the application version.
    	String version = MainApplication.getVersionString();
    	
    	// Set up the email intent.
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType("message/rfc822");
    	intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_fragment_send_feedback_subject, getString(R.string.app_name), version));
    	intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "yjwong92@gmail.com" });
    	
    	// Find the preference.
    	Preference sendFeedbackPreference = findPreference("send_feedback");
    	sendFeedbackPreference.setIntent(Intent.createChooser(intent, getString(R.string.settings_fragment_send_feedback_via)));
    }
    
    // }}}
}
