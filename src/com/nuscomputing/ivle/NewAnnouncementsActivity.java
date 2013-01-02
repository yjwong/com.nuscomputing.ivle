package com.nuscomputing.ivle;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * An activity to display new announcements.
 * @author yjwong
 */
public class NewAnnouncementsActivity extends IVLEFragmentActivity {
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the action bar parameters.
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle("New Announcements");
		
		// Inflate the layout.
		setContentView(R.layout.new_announcements_activity);
		if (savedInstanceState == null) {
			Fragment fragment = new NewAnnouncementsFragment();
			
			// Add the fragment.
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.new_announcements_activity_fragment_container, fragment);
			transaction.commit();
		}
	}
	
	// }}}
}
