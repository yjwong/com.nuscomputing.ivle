package com.nuscomputing.ivle;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Main settings activity (for Android < 3.0).
 * Allows users to view and update preferences for NUS IVLE.
 * @author yjwong
 */
public class SettingsActivityLegacy extends PreferenceActivity {
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings_activity_legacy);
        
        // Set up some options in the preferences tree.
        setUpAccount();
        setUpAddAccount();
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
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        
        // Obtain the accounts preference.
        ListPreference accountsPreference = (ListPreference) findPreference("account");
        String currentAccountName = accountsPreference.getValue();
        boolean foundCurrentAccount = false;
        
        // Reset the state of the preference.
        accountsPreference.setEnabled(true);
        
        // Populate the list.
        String[] accountEntries = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
        	accountEntries[i] = accounts[i].name;
        	if (currentAccountName == accounts[i].name) {
        		foundCurrentAccount = true;
        	}
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
            if (foundCurrentAccount) {
            	accountsPreference.setSummary(currentAccountName);
            } else {
            	// Revert to first entry.
            	accountsPreference.setSummary(accounts[0].name);
            }
        }
    }
    
    /**
     * Method: setUpAddAccount
     * Action for adding an account.
     */
    private void setUpAddAccount() {
    	// Set up new intent to launch the authenticator activity.
    	Intent intent = new Intent();
    	intent.setClass(this, AuthenticatorActivity.class);
    	
        // Obtain the add_account preference.
        Preference addAccountPreference = findPreference("add_account");
        addAccountPreference.setIntent(intent);
    }
    
    // }}}
}
