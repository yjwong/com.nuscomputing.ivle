package com.nuscomputing.ivle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager.LayoutParams;

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
        
        // Set up our view.
        setContentView(R.layout.view_announcement_activity);
        getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        
        // Add the fragment.
        Fragment fragment = new ViewAnnouncementFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.view_announcement_activity_fragment_container, fragment);
        transaction.commit();
    }
	
	// }}}
}
