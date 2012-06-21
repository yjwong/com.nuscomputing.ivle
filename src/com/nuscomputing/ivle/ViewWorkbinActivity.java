package com.nuscomputing.ivle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager.LayoutParams;

public class ViewWorkbinActivity extends FragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWorkbinActivity";
	
	/** The workbin ID */
	public long workbinId;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtain the requested announcement ID.
        Intent intent = getIntent();
        workbinId = intent.getLongExtra("workbinId", -1);
        if (workbinId == -1) {
        	throw new IllegalStateException("No workbin ID was passed to ViewWorkbinActivity");
        }
        
        // Set up our view.
        setContentView(R.layout.view_workbin_activity);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        // Add the fragment.
        Fragment fragment = new ViewWorkbinFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.view_workbin_activity_fragment_container, fragment);
        transaction.commit();
    }
	
	// }}}
}
