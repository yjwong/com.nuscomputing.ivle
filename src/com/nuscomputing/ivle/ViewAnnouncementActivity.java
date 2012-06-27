package com.nuscomputing.ivle;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ViewAnnouncementActivity extends FragmentActivity {
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
        if (Build.VERSION.SDK_INT >= 11) {
        	ActionBar bar = getActionBar();
        	bar.setDisplayHomeAsUpEnabled(true);
        }
        
        // Set up our view.
        setContentView(R.layout.view_announcement_activity);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
