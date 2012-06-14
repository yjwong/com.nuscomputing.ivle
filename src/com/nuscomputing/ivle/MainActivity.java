package com.nuscomputing.ivle;

import java.util.ArrayList;

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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
	
	/** Is there a refresh in progress? */
	private boolean mRefreshInProgress;
	
	/** The refresh menu item */
	private MenuItem mRefreshMenuItem;
	
	/** The view pager */
	private ViewPager mViewPager;
	
	/** Tabs adapter for view pager */
	private TabsAdapter mTabsAdapter;
	
	/** The refresh receiver */
	private boolean mIsReceiverRegistered = false;
	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Reset the refresh button.
			mRefreshMenuItem.setActionView(null);
			mRefreshInProgress = false;
			
			// Unregister the broadcast receiver.
			mIsReceiverRegistered = false;
			unregisterReceiver(this);
		}
	};
	
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
        
        // Create the view pager.
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.main_view_pager);
		
        // Newer versions of Android: Action Bar
        if (Build.VERSION.SDK_INT >= 11) {
        	// Configure the action bar.
        	ActionBar actionBar = getActionBar();
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        	
        	// Set the title appropriately.
        	actionBar.setTitle("NUS IVLE (" + mActiveAccount.name + ")");
        	
            // Plug the pager tabs.
            mTabsAdapter = new TabsAdapter(this, mViewPager);
            mTabsAdapter.addTab(actionBar.newTab()
            		.setText("Modules"), ModulesFragment.class, null);
            mTabsAdapter.addTab(actionBar.newTab()
            		.setText("What's New"), WhatsNewFragment.class, null);
        }
        
        setContentView(mViewPager);
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
    				this.performRefresh(null);
    			}
    	}
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	// Restore the active tab.
    	int currentTabPosition = savedInstanceState.getInt("currentTab", 0);
    	getActionBar().setSelectedNavigationItem(currentTabPosition);
    	Log.v(TAG, "onRestoreInstanceState: Restoring action bar tab, currently selected = " + currentTabPosition);
    	
    	// Restore the state of the refresh.
    	mRefreshInProgress = savedInstanceState.getBoolean("refreshInProgress", false);
    	Log.v(TAG, "onRestoreInstanceState: Restoring refresh state, currently = " + mRefreshInProgress);
    	if (mRefreshInProgress) {
    		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    		final View progressView = layoutInflater.inflate(R.layout.refresh_view, null);	
    		mRefreshMenuItem.setActionView(progressView);
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	// Save the currently being viewed tab.
    	ActionBar actionBar = getActionBar();
    	int currentTabPosition = actionBar.getSelectedNavigationIndex();
    	outState.putInt("currentTab", currentTabPosition);
    	Log.v(TAG, "onSaveInstanceState: Saving action bar tab, currently selected = " + currentTabPosition);
    	
    	// Save the state of the refresh.
    	outState.putBoolean("refreshInProgress", mRefreshInProgress);
    	Log.v(TAG, "onSaveInstanceState: Saving refresh state, currently = " + mRefreshInProgress);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (mIsReceiverRegistered) {
    		mIsReceiverRegistered = false;
    		unregisterReceiver(mRefreshReceiver);
    		Log.v(TAG, "onPause: refresh receiver stopped");
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (!mIsReceiverRegistered && mRefreshInProgress) {
    		registerReceiver(mRefreshReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
    		Log.v(TAG, "onResume: refresh receiver resumed");
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	inflater.inflate(R.menu.global, menu);
    	// Find the refresh item, since we do need when the state is restored.
    	mRefreshMenuItem = menu.findItem(R.id.main_menu_refresh);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
        	switch (item.getItemId()) {
	    		case R.id.main_menu_refresh:
	    			this.performRefresh(null);
	    			return true;
	    			
	    		default:
	    			return super.onOptionsItemSelected(item);
	    	}
        	
    	} else {
    		return true;
    	}
    }
    
    private void performRefresh(Account account) {
		// Inflate custom layout for refresh animation.
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View progressView = inflater.inflate(R.layout.refresh_view, null);
		mRefreshMenuItem.setActionView(progressView);
		
		// Setup a ContentReceiver to receive sync completion events.
		account = (account == null) ? AccountUtils.getActiveAccount(this) : account;
		registerReceiver(mRefreshReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
		mIsReceiverRegistered = true;
		
		// Request a sync.
		ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, new Bundle());
		
		// Set refresh in progress.
		mRefreshInProgress = true;
    }

    // }}}
    // {{{ classes
    
    /**
     * Helper class for tab management.
     * @author yjwong
     */
    public static class TabsAdapter extends FragmentPagerAdapter
    		implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    	// {{{ properties
    	
    	private final Context mContext;
    	private final ActionBar mActionBar;
    	private final ViewPager mViewPager;
    	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    	
    	// }}}
    	// {{{ methods
    	
    	public TabsAdapter(Activity activity, ViewPager pager) {
    		super(((FragmentActivity) activity).getSupportFragmentManager());
    		mContext = activity;
    		mActionBar = activity.getActionBar();
    		mViewPager = pager;
    		mViewPager.setAdapter(this);
    		mViewPager.setOnPageChangeListener(this);
    	}
    	
    	public void addTab(ActionBar.Tab tab, Class<?> clazz, Bundle args) {
    		TabInfo info = new TabInfo(clazz, args);
    		tab.setTag(info);
    		tab.setTabListener(this);
    		mTabs.add(info);
    		mActionBar.addTab(tab);
    		notifyDataSetChanged();
    	}
    	
    	@Override
    	public int getCount() {
    		return mTabs.size();
    	}
    	
    	@Override
    	public Fragment getItem(int position) {
    		TabInfo info = mTabs.get(position);
    		return Fragment.instantiate(mContext, info.mClass.getName(), info.mArgs);
    	}
    	
    	@Override
    	public void onPageScrolled(int position, float positionOffset,
    			int positionOffsetPixels) {
    		
    	}
    	
    	@Override
    	public void onPageSelected(int position) {
    		mActionBar.setSelectedNavigationItem(position);
    	}
    	
    	@Override
    	public void onPageScrollStateChanged(int state) {
    		
    	}
    	
    	public void onTabSelected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
    		Object tag = tab.getTag();
    		for (int i = 0; i < mTabs.size(); i++) {
    			if (mTabs.get(i) == tag) {
    				mViewPager.setCurrentItem(i);
    			}
    		}
    	}
    	
    	public void onTabReselected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
    		
    	}
    	
    	public void onTabUnselected(Tab tab, android.app.FragmentTransaction DO_NOT_USE) {
    		
    	}
    	
    	// }}}
    	// {{{ classes
    	
    	static final class TabInfo {
    		private final Class<?> mClass;
    		private final Bundle mArgs;
    		
    		TabInfo(Class<?> clazz, Bundle args) {
    			mClass = clazz;
    			mArgs = args;
    		}
    	}
    	
    	// }}}
    }
    
    // }}}
}