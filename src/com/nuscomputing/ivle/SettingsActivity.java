package com.nuscomputing.ivle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

/**
 * Main settings activity.
 * Allows users to view and update preferences for NUS IVLE.
 * @author yjwong
 */
@TargetApi(11)
public class SettingsActivity extends Activity {
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        
        if (savedInstanceState == null) {
        	Fragment fragment = new SettingsFragment();
        	FragmentManager manager = getFragmentManager();
        	FragmentTransaction transaction = manager.beginTransaction();
        	transaction.add(R.id.settings_activity_fragment_container, fragment);
        	transaction.commit();
        }
    }
    
    // }}}
}
