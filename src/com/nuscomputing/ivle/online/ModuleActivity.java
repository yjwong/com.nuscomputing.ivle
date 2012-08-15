package com.nuscomputing.ivle.online;

import com.nuscomputing.ivle.IVLESherlockFragmentActivity;
import com.nuscomputing.ivle.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Main IVLE application activity.
 * @author yjwong
 */
public class ModuleActivity extends IVLESherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleActivity";
	
	/** The module ID */
	public long moduleId;
	
	/** The module IVLE ID */
	public String moduleIvleId;
	
	/** The module name */
	public String moduleCourseName;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_activity);
        
        // Instantiate the fragment if it doesn't exist.
        if (savedInstanceState == null) {
        	// Create the fragment.
        	Fragment fragment = new ModuleFragment();
        	Bundle args = getIntent().getExtras();
        	fragment.setArguments(args);
        	
        	// Add the fragment.
        	FragmentManager manager = getSupportFragmentManager();
        	FragmentTransaction transaction = manager.beginTransaction();
        	transaction.add(R.id.module_activity_fragment_container, fragment);
        	transaction.commit();
        }
    }

    // }}}
}