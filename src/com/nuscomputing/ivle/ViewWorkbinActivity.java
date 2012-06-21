package com.nuscomputing.ivle;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
        
        // Set action bar parameters.
        if (Build.VERSION.SDK_INT >= 11) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Set up our view.
        setContentView(R.layout.view_workbin_activity);
        
        // Add the fragment.
        Fragment fragment = new ViewWorkbinFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.view_workbin_activity_fragment_container, fragment);
        transaction.commit();
        
        // Load the action bar title.
        Bundle args = new Bundle();
        args.putLong("workbinId", workbinId);
        DataLoader loader = new DataLoader(this);
        getSupportLoaderManager().initLoader(DataLoader.VIEW_WORKBIN_ACTIVITY_LOADER, args, loader);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
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
	
	// }}}
}
