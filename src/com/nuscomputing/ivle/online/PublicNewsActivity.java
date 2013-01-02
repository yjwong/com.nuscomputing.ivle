package com.nuscomputing.ivle.online;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.nuscomputing.ivle.IVLEFragmentActivity;
import com.nuscomputing.ivle.R;

/**
 * Activity to display public IVLE news.
 * @author yjwong
 */
public class PublicNewsActivity extends IVLEFragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "PublicNewsActivity";
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the action bar.
		ActionBar bar = getActionBar();
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
	
	// }}}
}
