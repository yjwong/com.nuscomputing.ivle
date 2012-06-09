package com.nuscomputing.ivle;

import android.app.Application;
import android.content.Context;

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
	
	// }}}
}
