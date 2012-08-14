package com.nuscomputing.ivle;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

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
	
	/** The action bar */
	private ActionBar mActionBar;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings_activity);
        
        // Set title for action bar.
        mActionBar = getActionBar();
        mActionBar.setTitle(getString(R.string.account_settings_activity_account_settings));
        
        // Jellybean now removes per-account settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        	// Obtain all the accounts.
        	final Account[] accounts = AccountUtils.getAllAccounts(this);
        	ArrayAdapter<Account> adapter = new AccountsSpinnerAdapter(mActionBar.getThemedContext(), android.R.layout.simple_list_item_1, accounts);
        	
        	// Use a spinner action bar.
        	mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    	mActionBar.setDisplayShowTitleEnabled(false);
        	mActionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					// Set the subtitle.
					mActionBar.setSubtitle(accounts[itemPosition].name);
					
					// Create a fragment.
					Bundle args = new Bundle();
					args.putParcelable("account", accounts[itemPosition]);
					Fragment fragment = new AccountSettingsFragment();
					fragment.setArguments(args);
					
					// Replace the fragment.
					FragmentManager manager = getFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					transaction.replace(R.id.account_settings_activity_fragment_container, fragment);
					transaction.commit();
					return true;
				}
			});
        	
        } else {
            // Obtain the account we want.
            Intent intent = getIntent();
            Account account = intent.getParcelableExtra("account");
            
            // Set title for action bar.
            mActionBar.setSubtitle(account.name);
            
            // Get hold of the fragment and set the argument.
            Fragment fragment = new AccountSettingsFragment();
            fragment.setArguments(intent.getExtras());
            
            // Insert the fragment.
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.account_settings_activity_fragment_container, fragment, null);
            transaction.commit();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	// Jellybean: save the fragment we're on.
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    		outState.putInt("position", mActionBar.getSelectedNavigationIndex());
    	}
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	// Jellybean: restore the fragment we're on.
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    		int barPosition = savedInstanceState.getInt("position", 0);
    		mActionBar.setSelectedNavigationItem(barPosition);
    	}
    }
    
    // }}}
    // {{{ classes
    
    /**
     * An adapter for the spinner accounts.
     * @author yjwong
     */
    class AccountsSpinnerAdapter extends ArrayAdapter<Account> implements SpinnerAdapter {
    	// {{{ properties
    	
    	/** The context */
    	private Context mContext;
    	
    	/** The list of accounts */
    	private Account[] mAccounts;
    	
    	// }}}
    	// {{{ methods
    	
		AccountsSpinnerAdapter(Context context, int textViewResourceId,
				Account[] accounts) {
			super(context, textViewResourceId, accounts);
			mContext = context;
			mAccounts = accounts;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// View for the spinner.
			LayoutInflater inflater = getLayoutInflater();
			convertView = inflater.inflate(R.layout.account_settings_activity_spinner, null);
			
			// Insert the title.
			TextView tvTitle = (TextView) convertView.findViewById(R.id.account_settings_activity_spinner_title);
			tvTitle.setText(mActionBar.getTitle());
			
			// Insert the subtitle.
			TextView tvSubtitle = (TextView) convertView.findViewById(R.id.account_settings_activity_spinner_subtitle);
			tvSubtitle.setText(mAccounts[position].name);
			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			// Create a view to contain the name of the account.
			LayoutInflater inflater = getLayoutInflater();
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			
			// Insert the name.
			TextView tvAccountName = (TextView) convertView.findViewById(android.R.id.text1);
			tvAccountName.setTextAppearance(mContext, android.R.style.TextAppearance_Holo_Medium_Inverse);
			tvAccountName.setText(mAccounts[position].name);
			return convertView;
		}
    	
		// }}}
    }
    
    // }}}
}
