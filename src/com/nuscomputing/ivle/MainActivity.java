package com.nuscomputing.ivle;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

/**
 * Main IVLE application activity.
 * @author yjwong
 */
@TargetApi(14)
public class MainActivity extends SherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "MainActivity";
	
	/** The currently active account */
	public Account mActiveAccount;
	
	/** Intent request code */
	public static final int REQUEST_AUTH = 1;
	
	/** The refresh menu item */
	private MenuItem mRefreshMenuItem;
	
	/** The view pager */
	private ViewPager mViewPager;
	
	/** Tabs adapter for view pager */
	private TabsAdapter mTabsAdapter;
	
	/** The shared preferences */
	private SharedPreferences mPrefs;
	
	/** Is a sync currently in progress? */
	private boolean mSyncInProgress;
	
	/** The search view */
	private SearchView mSearchView;
	
	/** Sync started broadcast receiver */
	private BroadcastReceiver mSyncStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Account account = intent.getParcelableExtra("com.nuscomputing.ivle.Account");
			if (account.name.equals(mActiveAccount.name)) {
				showSyncInProgress();
			}
		}
	};
	
	/** Sync success broadcast receiver */
	private BroadcastReceiver mSyncSuccessReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Account account = intent.getParcelableExtra("com.nuscomputing.ivle.Account");
			if (account.name.equals(mActiveAccount.name)) {
				hideSyncInProgress();
			}
		}
	};
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        // Create the view pager.
		setContentView(R.layout.main);
		Log.v(TAG, "Testing GB");
		mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
		
        // Check if there's an active account.
		mActiveAccount = AccountUtils.getActiveAccount(this, true);
		if (mActiveAccount == null) {
			// Launch activity to add account.
			Log.d(TAG, "No accounts defined, starting AuthenticatorActivity");
			Intent intent = new Intent();
			intent.setClass(this, AuthenticatorActivity.class);
			startActivityForResult(intent, REQUEST_AUTH);
		}
		
    	// Configure the action bar.
    	ActionBar bar = getSupportActionBar();
    	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    	
        // Plug the pager tabs.
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab()
        		.setText(getString(R.string.main_activity_modules)), ModulesFragment.class, null);
        mTabsAdapter.addTab(bar.newTab()
        		.setText(getString(R.string.main_activity_my_agenda)), MyAgendaFragment.class, null);
        
    	// Set the title appropriately.
    	if (mActiveAccount != null) {
    		bar.setTitle(getString(R.string.app_name_with_active_account, mActiveAccount.name));
    	}
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
    				Log.v(TAG, "Authentication suceeded");
    				if (mActiveAccount == null) {
    					showSyncInProgress();
    				}    				
    				this.performRefresh();
    			}
    	}
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	// Restore the active tab.
    	if (Build.VERSION.SDK_INT >= 11) {
	    	int currentTabPosition = savedInstanceState.getInt("currentTab", 0);
	    	getActionBar().setSelectedNavigationItem(currentTabPosition);
	    	Log.v(TAG, "onRestoreInstanceState: Restoring action bar tab, currently selected = " + currentTabPosition);
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	// Save the currently being viewed tab.
    	if (Build.VERSION.SDK_INT >= 11) {
	    	ActionBar actionBar = getSupportActionBar();
	    	int currentTabPosition = actionBar.getSelectedNavigationIndex();
	    	outState.putInt("currentTab", currentTabPosition);
	    	Log.v(TAG, "onSaveInstanceState: Saving action bar tab, currently selected = " + currentTabPosition);
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Unregister receiving sync events.
    	unregisterReceiver(mSyncStartedReceiver);
    	unregisterReceiver(mSyncSuccessReceiver);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Register receiving sync events.
    	registerReceiver(mSyncStartedReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_STARTED));
    	registerReceiver(mSyncSuccessReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_SUCCESS));
    	
    	// Obtain the shared preferences.
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	if (mActiveAccount != null) {
        	mSyncInProgress = mPrefs.getBoolean(IVLESyncService.KEY_SYNC_IN_PROGRESS + "_" + mActiveAccount.name, false);
        	if (mSyncInProgress) {
        		showSyncInProgress();
        	} else {
        		hideSyncInProgress();
        	}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	inflater.inflate(R.menu.global, menu);
    	
    	// Get the SearchView and set the searchable configuration
    	if (Build.VERSION.SDK_INT >= 11) {
	    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    	mSearchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
	    	mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    	mSearchView.setQueryHint(getString(R.string.searchable_hint));
    	}
    	
    	// Restore the state of the refresh item.
    	mRefreshMenuItem = menu.findItem(R.id.main_menu_refresh);
    	if (mSyncInProgress) {
			showSyncInProgress();
    	} else {
    		hideSyncInProgress();
    	}
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
        	switch (item.getItemId()) {
	    		case R.id.main_menu_refresh:
	    			this.performRefresh();
	    			return true;
	    			
	    		default:
	    			return super.onOptionsItemSelected(item);
	    	}
        	
    	} else {
    		return true;
    	}
    }
    
    @Override
    public void onBackPressed() {
    	// Close the search view if one is open.
    	if (!mSearchView.isIconified() && Build.VERSION.SDK_INT >= 14) {
    		mSearchView.onActionViewCollapsed();
    		mSearchView.setQuery("", false);
    	} else {
    		super.onBackPressed();
    	}
    }
    
    private void showSyncInProgress() {
		// Change the refresh button to a ProgressBar action view.
    	if (Build.VERSION.SDK_INT >= 11) {
	    	if (mRefreshMenuItem != null) {
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				final View progressView = layoutInflater.inflate(R.layout.refresh_view, null);	
				mRefreshMenuItem.setActionView(progressView);
	    	}
    	}
		
		// Hide the view pager.
		mViewPager.setVisibility(View.GONE);
		
		// Show the sync in progress notice.
		LinearLayout viewWaitingForSync = (LinearLayout) findViewById(R.id.main_waiting_for_sync_linear_layout);
		viewWaitingForSync.setVisibility(View.VISIBLE);
    }
    
    private void hideSyncInProgress() {
		// Reset the refresh button.
    	if (Build.VERSION.SDK_INT >= 11) {
	    	if (mRefreshMenuItem != null) {
	    		mRefreshMenuItem.setActionView(null);
	    	}
    	}
		
		// Show the view pager.
		mViewPager.setVisibility(View.VISIBLE);
		
		// Hide the sync in progress notice.
		LinearLayout viewWaitingForSync = (LinearLayout) findViewById(R.id.main_waiting_for_sync_linear_layout);
		viewWaitingForSync.setVisibility(View.GONE);
    }
    
    private void performRefresh() {
    	mActiveAccount = AccountUtils.getActiveAccount(this);
    	
		// Update the title.
		ActionBar bar = getSupportActionBar();
		bar.setTitle(getString(R.string.app_name_with_active_account, mActiveAccount.name));
		
		// Request a sync.
    	if (!IVLESyncService.isSyncInProgress(getApplicationContext(), mActiveAccount)) {
    		Bundle args = new Bundle();
    		args.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    		args.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(mActiveAccount, Constants.PROVIDER_AUTHORITY, new Bundle());
    	}
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
    	
    	public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
    		super(activity.getSupportFragmentManager());
    		mContext = activity;
    		mActionBar = activity.getSupportActionBar();
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
    	
    	public void onTabSelected(Tab tab, FragmentTransaction ft) {
    		Object tag = tab.getTag();
    		for (int i = 0; i < mTabs.size(); i++) {
    			if (mTabs.get(i) == tag) {
    				mViewPager.setCurrentItem(i);
    			}
    		}
    	}
    	
    	public void onTabReselected(Tab tab, FragmentTransaction ft) {
    		
    	}
    	
    	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    		
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