package com.nuscomputing.ivle;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;

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
    		default:
    			return false;
    	}
    }
    
	// }}}
}
