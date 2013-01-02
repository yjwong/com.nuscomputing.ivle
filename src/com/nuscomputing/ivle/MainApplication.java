package com.nuscomputing.ivle;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

/**
 * The entire application.
 * @author yjwong
 */
@ReportsCrashes(formKey = "dGdaRXE1NkpWZlpLN3REUUVLMnEySnc6MQ",
		mode = ReportingInteractionMode.NOTIFICATION,
		resToastText = R.string.crash_toast_text,
		resNotifTickerText = R.string.crash_notif_ticker_text,
		resNotifTitle = R.string.crash_notif_title,
		resNotifText = R.string.crash_notif_text,
		resNotifIcon = android.R.drawable.stat_notify_error,
		resDialogText = R.string.crash_dialog_text,
		resDialogIcon = android.R.drawable.ic_dialog_info,
		resDialogTitle = R.string.crash_dialog_title,
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
		resDialogOkToast = R.string.crash_dialog_ok_toast
)
public class MainApplication extends Application {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "MainApplication";
	
	/** The application context */
	private static Context context;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate() {
		// Initialize the crash reporter.
		ACRA.init(this);
		
		// Proceed with the creation of the app.
		super.onCreate();
		context = getApplicationContext();
	}
	
	/**
	 * Method: isTablet
	 * <p>
	 * Determines if this application is running on a tablet
	 * configuration.
	 */
	public static boolean isTablet() {
		try {
			// Compute screen size.
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			float width = dm.widthPixels / dm.xdpi;
			float height = dm.heightPixels / dm.ydpi;
			double size = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
			return size >= 6;
		} catch (Throwable t) {
			Log.w(TAG, "failed to determine if this is a tablet");
			return false;
		}
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
    	Log.e(TAG, "should not call this anymore");
    	return false;
    }
    
	// }}}
}
