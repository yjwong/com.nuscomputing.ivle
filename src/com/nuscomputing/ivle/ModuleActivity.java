package com.nuscomputing.ivle;

import java.util.ArrayList;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.Tab;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Main IVLE application activity.
 * @author yjwong
 */
public class ModuleActivity extends FragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleActivity";
	
	/** The module ID */
	public long moduleId;
	
	/** The view pager */
	private ViewPager mViewPager;
	
	/** Tabs adapter for view pager */
	private TabsAdapter mTabsAdapter;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested module ID.
        Intent intent = getIntent();
        moduleId = intent.getLongExtra("moduleId", -1);
        if (moduleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleActivity");
        }
        
        // Create the view pager.
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.module_activity_view_pager);
        
        // Newer versions of Android: Action Bar
        if (Build.VERSION.SDK_INT >= 11) {
        	// Configure the action bar.
        	ActionBar actionBar = getActionBar();
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        	actionBar.setDisplayHomeAsUpEnabled(true);
        	
            // Plug the pager tabs.
            mTabsAdapter = new TabsAdapter(this, mViewPager);
            mTabsAdapter.addTab(actionBar.newTab()
            		.setText("Info"), ModuleInfoFragment.class, null);
            mTabsAdapter.addTab(actionBar.newTab()
            		.setText("Announcements"), ModuleAnnouncementsFragment.class, null);
            mTabsAdapter.addTab(actionBar.newTab()
            		.setText("Webcasts"), ModuleWebcastsFragment.class, null);
        }
        
        // Load the action bar title.
        Bundle args = new Bundle();
        args.putLong("moduleId", moduleId);
        DataLoader loader = new DataLoader(this);
        getSupportLoaderManager().initLoader(DataLoader.MODULE_ACTIVITY_LOADER, args, loader).forceLoad();
        
        // Set the content view.
        setContentView(mViewPager);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	switch (item.getItemId()) {
    		case R.id.main_menu_settings:
    			Intent intent = new Intent();
    			if (Build.VERSION.SDK_INT >= 11) {
    				intent.setClass(this, SettingsActivity.class);
    			} else {
    				intent.setClass(this, SettingsActivityLegacy.class);
    			}
    			intent.setAction(Intent.ACTION_MAIN);
    			intent.addCategory(Intent.CATEGORY_PREFERENCE);
    			startActivity(intent);
    			return true;
    		
    		case R.id.main_menu_refresh:
    			// Setup a ContentReceiver to receive sync completion events.
    			Account account = AccountUtils.getActiveAccount(this);
    			registerReceiver(new BroadcastReceiver() {
    				@Override
    				public void onReceive(Context context, Intent intent) {
    					Log.v(TAG, "Received broadcast");
    					unregisterReceiver(this);
    				}
    			}, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
    			
    			// Request a sync.
    			ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, new Bundle());
    			return true;
    			
    		case android.R.id.home:
    			// App icon tapped, go home.
    			intent = new Intent(this, MainActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intent);
    			return true;
    			
    		default:
    			return super.onOptionsItemSelected(item);
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