package com.nuscomputing.ivle;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * A convenience class for a fragment activity.
 * @author yjwong
 */
public class IVLESherlockFragmentActivity extends SherlockFragmentActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.global, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			finish();
    			return true;
    			
    		case R.id.main_menu_settings:
    			Intent intent = new Intent();
    			if (Build.VERSION.SDK_INT >= 11) {
    				intent.setClass(this, SettingsActivity.class);
    			} else {
    				intent.setClass(this, SettingsActivityLegacy.class);
    			}
    			intent.setAction(Intent.ACTION_MAIN);
    			intent.addCategory(Intent.CATEGORY_PREFERENCE);
    			startActivity(intent);
    			return true;
    			
    		case R.id.main_menu_help:
    			// TODO: Implement a real help system here.
    			Uri uri = Uri.parse("https://ivle.nus.edu.sg/");
    			intent = new Intent(Intent.ACTION_VIEW, uri);
    			startActivity(intent);
    			Toast.makeText(this, getString(R.string.temp_help_not_available_yet), Toast.LENGTH_SHORT).show();
    			return true;

    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
}
