package com.nuscomputing.ivle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.SearchView;

/**
 * The main universal search activity.
 * @author yjwong
 */
public class SearchableActivity extends IVLESherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "SearchableActivity";
	
	/** The search view */
	private SearchView mSearchView;
	
	/** The search query */
	private String mSearchQuery;
	
	/** The fragment manager */
	private FragmentManager mFragmentManager;
	
	/** The search fragment */
	private Fragment mFragment;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchable_activity);
		
		// Set the up button.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		// Handle the search intent.
		this.handleIntent(getIntent());

		// Display the query for Android < HONEYCOMB.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			bar.setTitle(getString(R.string.searchable_activity_results_for, mSearchQuery));
		}
		
		// Get the fragment manager.
		mFragmentManager = getSupportFragmentManager();
		if (savedInstanceState == null) {
			// Define fragment arguments.
			Bundle args = new Bundle();
			args.putString(SearchManager.QUERY, mSearchQuery);
			
			// Add the fragment.
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			mFragment = new SearchableFragment();
			mFragment.setArguments(args);
			transaction.add(R.id.searchable_activity_fragment_container, mFragment, "SEARCH_FRAGMENT");
			transaction.commit();
		} else {
			mFragment = mFragmentManager.findFragmentByTag("SEARCH_FRAGMENT");
		}
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		this.handleIntent(intent);
		
		// Remove the old fragment.
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.remove(mFragment);
		
		// Add the new fragment.
		// Define fragment arguments.
		Bundle args = new Bundle();
		args.putString(SearchManager.QUERY, mSearchQuery);
		
		// Add the fragment.
		mFragment = new SearchableFragment();
		mFragment.setArguments(args);
		transaction.add(R.id.searchable_activity_fragment_container, mFragment, "SEARCH_FRAGMENT");
		transaction.commit();
	}
	
	private void handleIntent(Intent intent) {
		// Get the intent, verify the action and get the query.
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
		}
	}
	
    @TargetApi(11)
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Create the global menu.
    	super.onCreateOptionsMenu(menu);
    	
    	// Create the searchable activity menu.
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.searchable_activity_menu, menu);
    	
    	// Get the SearchView and set the searchable configuration
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    	mSearchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
	    	mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    	mSearchView.setQueryHint(getString(R.string.searchable_hint));
	    	mSearchView.setQuery(mSearchQuery, false);
	    	mSearchView.setIconifiedByDefault(false);
    	}
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	switch (item.getItemId()) {
    		case R.id.main_menu_search:
    			onSearchRequested();
    			return true;
    			
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
	
	// }}}
}
