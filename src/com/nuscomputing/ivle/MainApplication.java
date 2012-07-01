package com.nuscomputing.ivle;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The entire application.
 * @author yjwong
 */
public class MainApplication extends Application {
	// {{{ properties
	
	/** The application context */
	private static Context context;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}
	
	/**
	 * Method: getContext
	 * <p>
	 * Returns the context of this application.
	 */
	public static Context getContext() {
		return MainApplication.context;
	}
	
    public static boolean onOptionsItemSelected(Context context, MenuItem item) {
    	// Handle item selection.
    	switch (item.getItemId()) {
    		case R.id.main_menu_settings:
    			Intent intent = new Intent();
    			if (Build.VERSION.SDK_INT >= 11) {
    				intent.setClass(context, SettingsActivity.class);
    			} else {
    				intent.setClass(context, SettingsActivityLegacy.class);
    			}
    			intent.setAction(Intent.ACTION_MAIN);
    			intent.addCategory(Intent.CATEGORY_PREFERENCE);
    			context.startActivity(intent);
    			return true;
    			
    		case R.id.main_menu_about:
				FragmentActivity activity = (FragmentActivity) context;
				FragmentManager manager = activity.getSupportFragmentManager();
				DialogFragment fragment = new AboutApplicationDialogFragment();
				fragment.show(manager, null);
    			return true;
    			
    		case R.id.main_menu_help:
    			// TODO: Implement a real help system here.
    			Uri uri = Uri.parse("https://ivle.nus.edu.sg/");
    			intent = new Intent(Intent.ACTION_VIEW, uri);
    			context.startActivity(intent);
    			Toast.makeText(context, context.getString(R.string.temp_help_not_available_yet), Toast.LENGTH_SHORT).show();
    			return true;
    			
    		default:
    			return false;
    	}
    }
    
	// }}}
}
