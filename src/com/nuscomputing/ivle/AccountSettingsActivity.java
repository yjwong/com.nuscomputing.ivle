package com.nuscomputing.ivle;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

/**
 * Account settings activity.
 * Allows users to view and update account settings for NUS IVLE.
 * @author yjwong
 */
@TargetApi(11)
public class AccountSettingsActivity extends Activity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "AccountSettingsActivity";
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings_activity);
        
        // Obtain the account we want.
        Intent intent = getIntent();
        Account account = intent.getParcelableExtra("account");
        
        // Set title for action bar.
        ActionBar bar = getActionBar();
        bar.setTitle(getString(R.string.account_settings_activity_account_settings));
        bar.setSubtitle(account.name);
        
        // Get hold of the fragment and set the argument.
        Fragment fragment = new AccountSettingsFragment();
        fragment.setArguments(intent.getExtras());
        
        // Insert the fragment.
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.account_settings_activity_fragment_container, fragment, null);
        transaction.commit();
    }
    
    // }}}
}
