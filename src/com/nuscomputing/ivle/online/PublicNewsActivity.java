package com.nuscomputing.ivle.online;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nuscomputing.ivle.MainApplication;
import com.nuscomputing.ivle.R;

/**
 * Activity to display public IVLE news.
 * @author yjwong
 */
public class PublicNewsActivity extends SherlockFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "PublicNewsActivity";
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the action bar.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(getString(R.string.ivle_news));
		
		// Set up our view.
		setContentView(R.layout.public_news_activity);
		if (savedInstanceState == null) {
			// Prepare the fragment.
			Fragment fragment = new PublicNewsFragment();
			
			// Add the fragment.
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.public_news_activity_fragment_container, fragment);
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
