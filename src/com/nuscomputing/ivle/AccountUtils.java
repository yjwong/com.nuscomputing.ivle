package com.nuscomputing.ivle;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Utility class to manage IVLE accounts.
 * @author yjwong
 */
public class AccountUtils {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "AccountUtils";
	
	// }}}
	// {{{ methods
	
	/**
	 * Method: getAllAccounts
	 * <p>
	 * Gets all the accounts.
	 */
	public static Account[] getAllAccounts(Context context) {
		// Create an account manager.
		AccountManager am = AccountManager.get(context.getApplicationContext());
		Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		return accounts;
	}
	
	/**
	 * Method: setActiveAccount
	 * <p>
	 * Sets the active account and stores it into the preferences.
	 */
	public static void setActiveAccount(Context context, String accountName) {
		// Obtain the preference manager.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		// Create an account manager.
		Account[] accounts = AccountUtils.getAllAccounts(context);
		
		// Check if the specified account exists.
		if (accountName != null) {
			// Find the account.
			boolean accountFound = false;
			for (Account account : accounts) {
				if (account.name.equals(accountName)) {
					accountFound = true;
				}
			}
			
			if (!accountFound) {
				throw new IllegalArgumentException("The specified account cannot be found");
			} else {
				// Set the preference.
				Editor prefsEditor = prefs.edit();
				prefsEditor.putString("account", accountName);
				prefsEditor.commit();
			}
			
		} else {
			throw new NullPointerException("accountName was null");
		}
	}
	
	/**
	 * Method: getActiveAccount
	 * <p>
	 * Gets the current active account as specified in the preferences.
	 * If there is no currently defined active account, this returns the
	 * first account found via AccountManager. If there are no accounts
	 * stored in the AccountManager, this method returns null.
	 * <p>
	 * If the set parameter is true, then the chosen account will be
	 * stored into the preferences.
	 */
	public static Account getActiveAccount(Context context, boolean set) {
		// Obtain the preference manager.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accountName = prefs.getString("account", null);
		
		// Create an account manager.
		Account[] accounts = AccountUtils.getAllAccounts(context);
		
		// Act based on prior preference.
		if (accountName != null) {
			// Find the account.
			for (Account account : accounts) {
				if (account.name.equals(accountName)) {
					Log.v(TAG, "Using account found in preferences");
					return account;
				}
			}
			
			// Is the account we want missing?
			// XXX: compiler is stupid, so we need to replicate this code.
			if (set && accounts.length != 0) {
				Editor prefsEditor = prefs.edit();
				prefsEditor.putString("account", accounts[0].name);
				prefsEditor.commit();
			}

			return (accounts.length == 0) ? null : accounts[0];
			
		} else {
			if (set && accounts.length != 0) {
				Editor prefsEditor = prefs.edit();
				prefsEditor.putString("account", accounts[0].name);
				prefsEditor.commit();
			}

			return (accounts.length == 0) ? null : accounts[0];
			
		}
	}
	
	public static Account getActiveAccount(Context context) {
		return AccountUtils.getActiveAccount(context, false);
	}
	
	// }}}
}
