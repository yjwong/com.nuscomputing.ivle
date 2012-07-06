package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ViewWorkbinActivity extends FragmentActivity 
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWorkbinActivity";
	
	/** The workbin ID */
	public long workbinId;
	
	/** The workbin folder ID */
	public long workbinFolderId;
	
	/** The tab listener */
	private ActionBar.TabListener mTabListener;
	
	/** The view pager */
	private ViewPager mViewPager;
	
	/** The pager adapter */
	private PagerAdapter mPagerAdapter;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested workbin ID.
        Intent intent = getIntent();
        workbinId = intent.getLongExtra("workbinId", -1);
        if (workbinId == -1) {
        	throw new IllegalStateException("No workbin ID was passed to ViewWorkbinActivity");
        }
        workbinFolderId = intent.getLongExtra("workbinFolderId", -1);
        
        // Create the pager adapter.
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong("workbinId", workbinId);
        fragmentArgs.putLong("workbinFolderId", workbinFolderId);
        List<TabData> fragmentList = new ArrayList<TabData>();
        fragmentList.addAll(Arrays.asList(
        		new TabData(new ViewWorkbinFoldersFragment(), fragmentArgs),
        		new TabData(new ViewWorkbinFilesFragment(), fragmentArgs)
        ));
        mPagerAdapter = new ViewWorkbinActivityPagerAdapter(getSupportFragmentManager(), fragmentList);
        
        // Create the view pager.
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.view_workbin_activity_view_pager);
        mViewPager.setOnPageChangeListener(new cOnPageChangeListener());
        mViewPager.setAdapter(mPagerAdapter);
        
        // Set action bar parameters.
        if (Build.VERSION.SDK_INT >= 11) {
        	ActionBar bar = getActionBar();
        	bar.setDisplayHomeAsUpEnabled(true);
        	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        	mTabListener = new ViewWorkbinActivityTabListener();
        	
        	// Create a new tab for folders.
        	ActionBar.Tab tabFolders = bar.newTab();
        	tabFolders
        		.setText("Folders")
        		.setTabListener(mTabListener);
        	
        	// Create a new tab for files.
        	ActionBar.Tab tabFiles = bar.newTab();
        	tabFiles
        		.setText("Files")
        		.setTabListener(mTabListener);
        	
        	// Plug in the tabs.
        	bar.addTab(tabFolders, 0, true);
        	bar.addTab(tabFiles, 1, false);
        }
        
        // Set up our view.
        setContentView(mViewPager);
        
        // Load the action bar title.
        Bundle args = new Bundle();
        args.putLong("workbinId", workbinId);
        DataLoader loader = new DataLoader(this, this);
        getSupportLoaderManager().initLoader(DataLoader.LOADER_VIEW_WORKBIN_ACTIVITY, args, loader);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.global, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
	    	// Handle item selection.
	    	switch (item.getItemId()) {
				case android.R.id.home:
					// Up pressed, go back to previous screen.
					finish();
					return true;
					
	    		default:
	    			return super.onOptionsItemSelected(item);
	    	}
	    	
    	} else {
    		return true;
    	}
    }
    
    public void onLoaderFinished(Bundle result) {
    	// Set the title.
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setTitle(result.getString("title"));
		}
    }
	
	// }}}
    // {{{ classes
    
    /**
     * Listener for action bar tabs.
     * @author yjwong
     */
    class ViewWorkbinActivityTabListener implements ActionBar.TabListener {
    	// {{{ methods
    	
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction DO_NOT_USE) { }

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction DO_NOT_USE) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction DO_NOT_USE) { }
    	
		// }}}
    }
    
    /**
     * View pager adapter.
     * @author yjwong
     */
    class ViewWorkbinActivityPagerAdapter extends FragmentPagerAdapter {
    	// {{{ properties
    	
    	/** The list of fragments */
    	private List<TabData> mFragmentList = new ArrayList<TabData>();
    	
    	// }}}
    	// {{{ methods
    	
    	ViewWorkbinActivityPagerAdapter(FragmentManager fm, 
    			List<TabData> fragmentList) {
    		super(fm);
    		mFragmentList = fragmentList;
    	}
    	
    	@Override
    	public int getCount() {
    		return mFragmentList.size();
    	}
    	
    	@Override
    	public Fragment getItem(int position) {
    		return mFragmentList.get(position).getFragment();
    	}
    	
    	// }}}
    }
    
    /**
     * The tab listener for the page.
     * @author yjwong
     */
    class cOnPageChangeListener implements ViewPager.OnPageChangeListener {
    	// {{{ methods
    	
		@Override
		public void onPageScrollStateChanged(int state) { }

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

		@Override
		public void onPageSelected(int position) {
			ActionBar bar = getActionBar();
			bar.selectTab(bar.getTabAt(position));			
		}
    	
		// }}}
    }
    
    /**
     * Tab data.
     * @author yjwong
     */
    class TabData {
    	// {{{ properties
    	
    	/** The fragment associated with this tab */
    	Fragment mFragment;
    	
    	/** Arguments for the fragment associated with this tab */
    	Bundle mArgs;
    	
    	// }}}
    	// {{{ methods
    	
    	TabData (Fragment fragment, Bundle args) {
    		mFragment = fragment;
    		mArgs = args;
    		mFragment.setArguments(args);
    	}
    	
    	/**
    	 * Returns the fragment associated with this tab.
    	 * @return Fragment
    	 */
    	Fragment getFragment() {
    		return mFragment;
    	}
    	
    	/**
    	 * Returns the arguments for the fragment associated with this tab.
    	 * @return Bundle
    	 */
    	Bundle getArgs() {
    		return mArgs;
    	}
    	
    	// }}}
    }
    
    // }}}
}
