package com.nuscomputing.ivle;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Main IVLE application activity.
 * @author yjwong
 */
public class MainActivity extends FragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "MainActivity";
	
	/** The currently active account */
	public Account mActiveAccount;
	
	/** Intent request code */
	public static final int REQUEST_AUTH = 1;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        
        // Check if there's an active account.
		mActiveAccount = AccountUtils.getActiveAccount(this, true);
		if (mActiveAccount == null) {
			// Launch activity to add account.
			Log.d(TAG, "No accounts defined, starting AuthenticatorActivity");
			Intent intent = new Intent();
			intent.setClass(this, AuthenticatorActivity.class);
			startActivityForResult(intent, REQUEST_AUTH);
			return;
		}
        
        // Newer versions of Android: Action Bar
        if (Build.VERSION.SDK_INT >= 11) {
        	// Configure the action bar.
        	ActionBar actionBar = getActionBar();
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        	
        	// Set the title appropriately.
        	actionBar.setTitle("NUS IVLE (" + mActiveAccount.name + ")");
        	
        	// Create the actionbar tabs.
        	Log.v(TAG, "creating action bar tabs");
        	actionBar.addTab(actionBar.newTab()
        			.setText("Modules")
        			.setTabListener(new TabListener<ModulesFragment>(
        					this, "TAG_MODULES", ModulesFragment.class)));
        	
        	actionBar.addTab(actionBar.newTab()
        			.setText("What's New")
        			.setTabListener(new TabListener<WhatsNewFragment>(
        					this, "TAG_WHATS_NEW", WhatsNewFragment.class)));
        }
        
        setContentView(R.layout.main);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
    		Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	// Handle the authentication result in here.
    	switch (requestCode) {
    		case REQUEST_AUTH:
    			// If the initial authentication was canceled, return to the
    			// activity.
    			if (resultCode == RESULT_CANCELED) {
    				Log.v(TAG, "Authentication was canceled");
    				finish();
    			}
    			
    			// If it passes, perform an initial sync.
    			if (resultCode == RESULT_OK) {
        			// Setup a ContentReceiver to receive sync completion events.
    				Log.v(TAG, "Authentication suceeded");
        			Account account = AccountUtils.getActiveAccount(this);
        			registerReceiver(new BroadcastReceiver() {
        				@Override
        				public void onReceive(Context context, Intent intent) {
        					Log.v(TAG, "Account added, performing initial sync");
        					unregisterReceiver(this);
        				}
        			}, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
        			
        			// Request a sync.
        			ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, new Bundle());
    			}
    	}
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	// Restore the active tab.
    	int currentTabPosition = savedInstanceState.getInt("currentTab", 0);
    	ActionBar actionBar = getActionBar();
    	actionBar.setSelectedNavigationItem(currentTabPosition);
    	Log.v(TAG, "Restoring action bar tab, currently selected: " + currentTabPosition);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	// Save the currently being viewed tab.
    	ActionBar actionBar = getActionBar();
    	int currentTabPosition = actionBar.getSelectedNavigationIndex();
    	outState.putInt("currentTab", currentTabPosition);
    	Log.v(TAG, "Saving action bar tab, currently selected: " + currentTabPosition);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	inflater.inflate(R.menu.global, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
        	switch (item.getItemId()) {
	    		case R.id.main_menu_refresh:
	    			// Setup a ContentReceiver to receive sync completion events.
	    			Account account = AccountUtils.getActiveAccount(this);
	    			registerReceiver(new BroadcastReceiver() {
	    				@Override
	    				public void onReceive(Context context, Intent intent) {
	    					Log.v(TAG, "Received broadcast");
	    					
	    					// Reload the fragment.
	    					FragmentManager manager = getSupportFragmentManager();
	    					ModulesFragment fragment = (ModulesFragment) manager.findFragmentByTag("TAG_MODULES");
	    					if (fragment != null) {
	    						fragment.restartLoader();
	    					}
	    					
	    					unregisterReceiver(this);
	    				}
	    			}, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
	    			
	    			// Request a sync.
	    			ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, new Bundle());
	    			return true;
	    			
	    		default:
	    			return super.onOptionsItemSelected(item);
	    	}
        	
    	} else {
    		return true;
    	}
    }

    // }}}
    // {{{ classes
    
    /**
     * Listener for tabs in the UI.
     * @author Wong Yong Jie
     */
    private class TabListener<T extends Fragment> implements ActionBar.TabListener {
    	// {{{ properties
    	
    	private Fragment mFragment;
    	private final Activity mActivity;
    	private final String mTag;
    	private final Class<T> mClass;
    	
    	// }}}
    	// {{{ methods
    	
    	/**
    	 * Constructor used each time a new time is created.
    	 * 
    	 * @param activity The host Activity, used to instantiate the fragment.
    	 * @param tag      The identifier tag for the fragment.
    	 * @param clazz    The fragment's class, used to instantiate the
    	 *                  fragment.
    	 */
    	public TabListener(Activity activity, String tag, Class<T> clazz) {
    		mActivity = activity;
    		mTag = tag;
    		mClass = clazz;
    	}
    	
		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
			// User selected the already selected tab. Do nothing.
		}

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
			// Create a FragmentTransaction.
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			Log.v(TAG, "onTabSelected: " + mClass.getName());
			
			// Check if the fragment is already instantiated.
			mFragment = manager.findFragmentByTag(mTag);
			if (mFragment == null) {
				// If not, instantiate it.
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				transaction.add(R.id.main_activity_fragment_container, mFragment, mTag);
				Log.v(TAG, "Created new fragment " + mTag);
			} else {
				transaction.attach(mFragment);
				Log.v(TAG, "Attached old fragment " + mTag);
			}
			
			// We can call commit because we're not using the passed in
			// FragmentTransaction.
			transaction.commit();
		}

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
			// Create a FragmentTransaction.
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			Log.v(TAG, "onTabUnselected: " + mClass.getName());
			
			if (mFragment != null) {
				transaction.detach(mFragment);
			}
			
			transaction.commit();
		}
    	
		// }}}
    }
    
    // }}}
}