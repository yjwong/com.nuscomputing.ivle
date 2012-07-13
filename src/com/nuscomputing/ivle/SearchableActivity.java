package com.nuscomputing.ivle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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
public class SearchableActivity extends SherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "SearchableActivity";
	
	/** The search view */
	private SearchView mSearchView;
	
	/** The search query */
	private String mSearchQuery;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchable_activity);
		
		// Set the up button.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		// Get the intent, verify the action and get the query.
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
		}
		
		if (savedInstanceState == null) {
			// Define fragment arguments.
			Bundle args = new Bundle();
			args.putString(SearchManager.QUERY, mSearchQuery);
			
			// Add the fragment.
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			Fragment fragment = new SearchableFragment();
			fragment.setArguments(args);
			transaction.add(R.id.searchable_activity_fragment_container, fragment);
			transaction.commit();
		}
	}
	
    @TargetApi(11)
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.global, menu);
    	inflater.inflate(R.menu.searchable_activity_menu, menu);
    	
    	// Get the SearchView and set the searchable configuration
    	if (Build.VERSION.SDK_INT >= 11) {
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
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
        	switch (item.getItemId()) {
        		case android.R.id.home:
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
}
