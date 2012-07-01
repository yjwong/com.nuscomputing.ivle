package com.nuscomputing.ivle;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
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
	
	/**
	 * Method: getVersionString
	 * <p>
	 * Utility method to get the version string of this application.
	 */
	public static String getVersionString() {
		// Get the information about this package.
		String version = "Unknown";
		try {
			PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			// Do nothing... Let the version remain unknown.
		}
		
		return version;
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
