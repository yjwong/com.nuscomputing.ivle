package com.nuscomputing.ivle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class SettingsFragment extends PreferenceFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "SettingsFragment";
	
	/** The "Check for Updates" alert dialog */
	private AlertDialog mCheckForUpdatesDialog;
	
	/** The "Check for Updates" preference */
	private Preference mCheckForUpdatesPreference;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Set up the preferences tree.
        addPreferencesFromResource(R.layout.settings_fragment);
        
        // Set up some options in the preferences tree.
        setUpAccount();
        setUpAddAccount();
        setUpManageAccounts();
        setUpAbout();
        setUpCheckForUpdates();
        setUpSendFeedback();
        
        // Create an AlertDialog for "Check for Updates".
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.settings_fragment_update_available_body)
			.setCancelable(true)
			.setTitle(R.string.settings_fragment_update_available)
			.setNegativeButton(R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		mCheckForUpdatesDialog = builder.create();
		
		// Was the "Check for Updates" dialog being shown?
		if (savedInstanceState != null && savedInstanceState.getBoolean("checkForUpdatesDialog", false)) {
			getLoaderManager().initLoader(DataLoader.LOADER_CHECK_FOR_UPDATES, new Bundle(), new CheckForUpdatesLoaderCallbacks());
		}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	// Is the "Check for Updates" dialog being shown?
    	outState.putBoolean("checkForUpdatesDialog", mCheckForUpdatesDialog.isShowing());
    	mCheckForUpdatesDialog.dismiss();
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
     * Method: setUpCheckForUpdates
     * Action for checking updates.
     */
    private void setUpCheckForUpdates() {
    	mCheckForUpdatesPreference = findPreference("check_updates");
    	mCheckForUpdatesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
    		@Override
    		public boolean onPreferenceClick(Preference preference) {
    			preference.setSummary(getString(R.string.settings_fragment_checking_for_updates));
    			
    			// Load the update information.
    			getLoaderManager().initLoader(DataLoader.LOADER_CHECK_FOR_UPDATES, new Bundle(), new CheckForUpdatesLoaderCallbacks());
    			return true;
    		}
    	});
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
    // {{{ classes
    
    /**
     * Loader callbacks to check for updates.
     * @author yjwong
     */
    class CheckForUpdatesLoaderCallbacks implements LoaderManager.LoaderCallbacks<UpdateInfo> {
    	// {{{ methods
    	
		@Override
		public Loader<UpdateInfo> onCreateLoader(int id, Bundle args) {
			return new CheckForUpdatesLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<UpdateInfo> loader, final UpdateInfo updateInfo) {
			// Hide any progress.
			mCheckForUpdatesPreference.setSummary("");
			
			// We now have the update info.
			if (updateInfo == null) {
				mCheckForUpdatesPreference.setSummary(R.string.settings_fragment_update_check_failed);
			} else if (updateInfo.currentVersionCode >= updateInfo.updateVersionCode) {
				mCheckForUpdatesPreference.setSummary(R.string.settings_fragment_update_latest_version);
			} else {
				// Update the dialog with newest URL.
				mCheckForUpdatesDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						// Open a browser to download the update.
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(updateInfo.updateAPK));
						startActivity(Intent.createChooser(intent, getString(R.string.settings_fragment_update_download_via)));
					}
				});
				
				// Prompt to download newer version.
				mCheckForUpdatesDialog.show();
			}
		}

		@Override
		public void onLoaderReset(Loader<UpdateInfo> loader) {
			// Do nothing.
		}
    	
		// }}}
    }
    
    /**
     * AsyncTaskLoader to check for updates.
     * @author yjwong
     */
    static class CheckForUpdatesLoader extends AsyncTaskLoader<UpdateInfo> {
    	// {{{ properties
    	
    	/** The context */
    	private Context mContext;
    	
    	/** The update information */
    	private UpdateInfo mUpdateInfo;
    	
    	// }}}
    	// {{{ methods

		public CheckForUpdatesLoader(Context context) {
			super(context);
			mContext = context;
		}
		
		@Override
		public void onStartLoading() {
			if (mUpdateInfo != null) {
				deliverResult(mUpdateInfo);
			}
			if (takeContentChanged() || mUpdateInfo == null) {
				forceLoad();
			}
		}

		@Override
		public UpdateInfo loadInBackground() {
			try {
				// Get the current version code.
				PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
				int versionCode = info.versionCode;
				
				// Get the online source.
				URL url = new URL("http://ivle.nuscomputing.com/versionCode");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder response = new StringBuilder(); 
				String buf = null;
				while ((buf = reader.readLine()) != null) {
					response.append(buf);
				}
				reader.close();
				
				// Parse the new version code.
				int updateVersionCode = Integer.parseInt(response.toString());
				
				// Create new update info.
				UpdateInfo updateInfo = new UpdateInfo();
				updateInfo.currentVersionCode = versionCode;
				updateInfo.updateVersionCode = updateVersionCode;
				updateInfo.updateAPK = "http://ivle.nuscomputing.com/com.nuscomputing.ivle-".concat(Integer.toString(updateVersionCode)).concat(".apk");
				return updateInfo;
				
			} catch (NameNotFoundException e) {
				return null;
			} catch (MalformedURLException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}
    	
		// }}}
    }
    
    /**
     * Class containing update information.
     * @author yjwong
     */
    static class UpdateInfo {
    	// {{{ properties
    	
    	/** The current version code */
    	public int currentVersionCode;
    	
    	/** The update version code */
    	public int updateVersionCode;
    	
    	/** The URL to the update APK */
    	public String updateAPK;
    	
    	// }}}
    }
    
    // }}}
}
