package com.nuscomputing.ivle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ViewAnnouncementActivity extends SherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewAnnouncementActivity";
	
	/** The announcement ID */
	public long announcementId;

	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested announcement ID.
        Intent intent = getIntent();
        announcementId = intent.getLongExtra("announcementId", -1);
        if (announcementId == -1) {
        	throw new IllegalStateException("No announcement ID was passed to ViewAnnouncementActivity");
        }
        
        // Set up the action bar.
    	ActionBar bar = getSupportActionBar();
    	bar.setDisplayHomeAsUpEnabled(true);
        
        // Set up our view.
    	setContentView(R.layout.view_announcement_activity);
    	if (savedInstanceState == null) {
    		// Prepare the fragment.
    		Bundle args = new Bundle(intent.getExtras());
    		Fragment fragment = new ViewAnnouncementFragment();
    		fragment.setArguments(args);
    		
    		// Add the fragment.
    		FragmentManager manager = getSupportFragmentManager();
    		FragmentTransaction transaction = manager.beginTransaction();
    		transaction.add(R.id.view_announcement_activity_fragment_container, fragment);
    		transaction.commit();
    	}
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
