package com.nuscomputing.ivle.online;

import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nuscomputing.ivle.MainApplication;
import com.nuscomputing.ivle.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Main IVLE application activity.
 * @author yjwong
 */
public class ModuleActivity extends SherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleActivity";
	
	/** The module IVLE ID */
	public String moduleIvleId;
	
	/** The module name */
	public String moduleCourseName;
	
	/** The view pager */
	private ViewPager mViewPager;
	
	/** Pager adapter for the pager */
	private PagerAdapter mPagerAdapter;
	
	/** The spinner adapter for the drop down */
	private SpinnerAdapter mSpinnerAdapter;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @TargetApi(11)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested module ID.
        Intent intent = getIntent();
        moduleCourseName = intent.getStringExtra("moduleCourseName");
        moduleIvleId = intent.getStringExtra("moduleIvleId");
        if (moduleIvleId == null) {
        	throw new IllegalStateException("No module IVLE ID was passed to ModuleActivity");
        }
        
        // Create the view pager.
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.module_activity_view_pager);
        
    	// Configure the action bar.
    	ActionBar bar = getSupportActionBar();
    	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    	bar.setDisplayHomeAsUpEnabled(true);
    	bar.setDisplayShowTitleEnabled(false);
    	
    	// Create a new spinner adapter.
    	ArrayList<String> spinnerItems = new ArrayList<String>();
    	spinnerItems.addAll(Arrays.asList(
    		getString(R.string.module_activity_info),
    		getString(R.string.module_activity_lecturers)//,
    		//getString(R.string.module_activity_announcements),
    		//getString(R.string.module_activity_webcasts),
    		//getString(R.string.module_activity_workbins)
    	));
    	mSpinnerAdapter = new ModuleActivitySpinnerAdapter(this, R.id.module_activity_spinner_subtitle, spinnerItems);
    	bar.setListNavigationCallbacks(mSpinnerAdapter, new ModuleActivityOnNavigationListener());
    	
    	// Define the fragment arguments.
    	Bundle args = new Bundle();
    	args.putString("moduleIvleId", moduleIvleId);
    	
        // Plug the pager tabs.
    	ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
    	
    	Fragment fragment = new ModuleInfoFragment();
    	fragment.setArguments(args);
    	fragmentList.add(fragment);
    	
    	fragment = new ModuleLecturersFragment();
    	fragment.setArguments(args);
    	fragmentList.add(fragment);
    	
    	/*
    	fragment = new ModuleAnnouncementsFragment();
    	fragment.setArguments(args);
    	fragmentList.add(fragment);
    	
    	fragment = new ModuleWebcastsFragment();
    	fragment.setArguments(args);
    	fragmentList.add(fragment);
    	
    	fragment = new ModuleWorkbinsFragment();
    	fragment.setArguments(args);
    	fragmentList.add(fragment);
    	*/
    	
    	mPagerAdapter = new ModuleActivityPagerAdapter(getSupportFragmentManager(), fragmentList);
    	mViewPager.setAdapter(mPagerAdapter);
    	mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
			
			@Override
			public void onPageScrollStateChanged(int state) { }
    	});
        
        // Set the content view.
        setContentView(mViewPager);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
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
	    			// App icon tapped, go back.
	    			finish();
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
     * Helper class for the activity spinner.
     * @author yjwong
     */
    public class ModuleActivitySpinnerAdapter extends ArrayAdapter<String>
    		implements SpinnerAdapter {
    	// {{{ properties
    	
    	/** The list of items */
    	private ArrayList<String> mItems;
    	
    	/** The context */
    	private Context mContext;
    	
    	// }}}
    	// {{{ methods
    	
    	ModuleActivitySpinnerAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
    		super(context, textViewResourceId, items);
    		mContext = context;
    		mItems = items;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// Inflate the layout for the item.
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.module_activity_spinner, null);
    		}
    		
    		String item = mItems.get(position);
    		if (item != null) {
    			TextView tvItem = (TextView) convertView.findViewById(R.id.module_activity_spinner_subtitle);
    			if (tvItem != null) {
    				tvItem.setText(item);
    			}
    			
    			TextView tvTitle = (TextView) convertView.findViewById(R.id.module_activity_spinner_title);
    			if (tvTitle != null) {
    				tvTitle.setText(moduleCourseName);
    			}
    		}
    		
    		return convertView;
    	}
    	
    	public View getDropDownView(int position, View convertView, ViewGroup parent) {
    		// Inflate the layout.
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.module_activity_spinner_item, null);
    		}
    		
    		String item = mItems.get(position);
    		if (item != null) {
    			TextView tvItem = (TextView) convertView.findViewById(R.id.module_activity_spinner_item_title);
    			if (tvItem != null) {
    				tvItem.setText(item);
    			}
    		}
    		
    		return convertView;
    	}
    	
    	// }}}
    }
    
    /**
     * Helper class to switch between items in the spinner.
     * @author yjwong
     */
    public class ModuleActivityOnNavigationListener implements
    		ActionBar.OnNavigationListener {
    	// {{{ methods

		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			mViewPager.setCurrentItem(itemPosition);
			return true;
		}
    	
		// }}}
    }
    
    /** 
     * Helper class for pager adapter.
     * @author yjwong
     */
    public class ModuleActivityPagerAdapter extends FragmentPagerAdapter {
    	// {{{ properties
    	
    	/** The list of fragments */
    	private ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();
    	
    	// }}}
    	// {{{ methods
    	
    	ModuleActivityPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
    		super(fm);
    		mFragmentList = fragmentList;
    	}
    	
    	@Override
    	public int getCount() {
    		return mFragmentList.size();
    	}
    	
    	@Override
    	public Fragment getItem(int position) {
    		return mFragmentList.get(position);
    	}
    	
    	// }}}
    }
    
    // }}}
}