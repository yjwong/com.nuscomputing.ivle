package com.nuscomputing.ivle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ViewWebcastActivity extends SherlockFragmentActivity
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastActivity";
	
	/** The webcast ID */
	private long mWebcastId;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested announcement ID.
        Intent intent = getIntent();
        mWebcastId = intent.getLongExtra("webcastId", -1);
        if (mWebcastId == -1) {
        	throw new IllegalStateException("No webcast ID was passed to ViewWebcastActivity");
        }
        
        // Set action bar parameters.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Set up our view.
        setContentView(R.layout.view_webcast_activity);
        
        // Set up arguments.
        Bundle args = new Bundle();
        args.putLong("webcastId", mWebcastId);
        
        // Setup the fragment.
        Fragment fragment = new ViewWebcastFragment();
        fragment.setArguments(args);
        
        // Add the fragment.
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.view_webcast_activity_fragment_container, fragment);
        transaction.commit();
        
        // Load the action bar title.
        DataLoader loader = new DataLoader(this, this);
        getSupportLoaderManager().initLoader(DataLoader.LOADER_VIEW_WEBCAST_ACTIVITY, args, loader).forceLoad();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
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
		getSupportActionBar().setTitle(result.getString("title"));
    }
	
	// }}}
}
