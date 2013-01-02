package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.nuscomputing.ivle.online.ModuleLecturersFragment;
import com.nuscomputing.ivle.online.ModuleWeblinksFragment;

/**
 * A fragment that hosts most of the module options.
 * @author yjwong
 */
public class ModuleFragment extends Fragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleFragment";
	
	/** The module ID */
	public long moduleId;
	
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
	
	/** Arguments for the fragment */
	private Bundle mFragmentArgs;
	
	/** Handler to run stuff on the UI */
	private Handler mHandler = new Handler();
	
	// }}}
	// {{{ methods
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup view,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_fragment, null);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Obtain the requested module ID.
        Bundle args = getArguments();
        moduleCourseName = args.getString("moduleCourseName");
        moduleIvleId = args.getString("moduleIvleId");
        moduleId = args.getLong("moduleId", -1);
        if (moduleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleActivity");
        }
        
        // Find the view pager.
        mViewPager = (ViewPager) getActivity().findViewById(R.id.module_activity_view_pager);
        
    	// Configure the action bar.
    	ActionBar bar = getActivity().getActionBar();
    	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    	bar.setDisplayHomeAsUpEnabled(true);
    	bar.setDisplayShowTitleEnabled(false);
    	
    	// Create a new spinner adapter.
    	ArrayList<String> spinnerItems = new ArrayList<String>();
    	spinnerItems.addAll(Arrays.asList(
    		getString(R.string.module_activity_info),
    		getString(R.string.module_activity_announcements),
    		getString(R.string.module_activity_lecturers),
    		getString(R.string.module_activity_webcasts),
    		getString(R.string.module_activity_weblinks),
    		getString(R.string.module_activity_workbins)
    	));
    	mSpinnerAdapter = new ModuleActivitySpinnerAdapter(bar.getThemedContext(), spinnerItems);
    	bar.setListNavigationCallbacks(mSpinnerAdapter, new ModuleActivityOnNavigationListener());
    	
    	// Save fragment arguments.
    	mFragmentArgs = args;
    }
	
	@Override
	public void onResume() {
		super.onResume();

        // Plug the pager tabs.
    	ArrayList<String> fragmentList = new ArrayList<String>();
    	fragmentList.add(ModuleInfoFragment.class.getName());
    	fragmentList.add(ModuleAnnouncementsFragment.class.getName());
    	fragmentList.add(ModuleLecturersFragment.class.getName());
    	fragmentList.add(ModuleWebcastsFragment.class.getName());
    	fragmentList.add(ModuleWeblinksFragment.class.getName());
    	fragmentList.add(ModuleWorkbinsFragment.class.getName());
    	mPagerAdapter = new ModuleFragmentPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList, mFragmentArgs);
    	
    	// Configure the view pager.
    	mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActivity().getActionBar().setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
			
			@Override
			public void onPageScrollStateChanged(int state) { }
    	});
    	
    	// HACK: Fragments inside fragments.
    	mHandler.post(new Runnable() {
    		@Override
    		public void run() {
    			mViewPager.setAdapter(mPagerAdapter);
    		}
    	});
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
    	
    	ModuleActivitySpinnerAdapter(Context context, ArrayList<String> items) {
    		super(context, android.R.layout.simple_list_item_1, android.R.id.text1, items);
    		mContext = context;
    		mItems = items;
    	}
    	
    	@Override
    	public Context getContext() {
    		return mContext;
    	}
    	
    	@Override
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
    public class ModuleFragmentPagerAdapter extends FragmentStatePagerAdapter {
    	// {{{ properties
    	
    	/** The list of fragments */
    	private ArrayList<String> mFragmentList = new ArrayList<String>();
    	
    	/** The arguments */
    	private Bundle mArgs;
    	
    	// }}}
    	// {{{ methods
    	
    	ModuleFragmentPagerAdapter(FragmentManager fm, ArrayList<String> fragmentList, Bundle args) {
    		super(fm);
    		mFragmentList = fragmentList;
    		mArgs = args;
    	}
    	
    	@Override
    	public int getCount() {
    		return mFragmentList.size();
    	}
    	
    	@Override
    	public Fragment getItem(int position) {
    		return Fragment.instantiate(getActivity(), mFragmentList.get(position), mArgs);
    	}
    	
    	// }}}
    }
    
    // }}}
}
