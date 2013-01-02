package com.nuscomputing.ivle;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ViewAnnouncementActivity extends IVLEFragmentActivity {
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
    	ActionBar bar = getActionBar();
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

	// }}}
}
