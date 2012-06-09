package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;

/**
 * The primary loader for IVLE data.
 * @author yjwong
 */
public class DataLoader implements LoaderManager.LoaderCallbacks<Cursor> {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "DataLoader";
	
	/** Loader IDs */
	public static final int MODULE_INFO_FRAGMENT_LOADER = 1;
	
	/** The context */
	private Activity mActivity;
	
	// }}}
	// {{{ methods
	
	public DataLoader(Activity activity) {
		mActivity = activity;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.v(TAG, "onCreateLoader");
		
		// Select the correct loader based on the provided ID.
		switch (id) {
			case MODULE_INFO_FRAGMENT_LOADER:
				// Obtain the current account.
				Account activeAccount = AccountUtils.getActiveAccount(mActivity, true);
				if (activeAccount == null) {
					// Launch activity to add account.
					Log.e(TAG, "Error loading accounts");
					return null;
				}
				
				// Obtain the account name.
				String accountName = activeAccount.name;
				Log.d(TAG, "Account found, using " + accountName);
				
				// Obtain the module ID.
				long moduleId = args.getLong("moduleId", -1);
				if (moduleId == -1) {
					throw new IllegalStateException("No module ID was passed to DataLoader");
				}

				// Set up our query parameters.
				String[] projection = {
						ModulesContract.ID,
						ModulesContract.COURSE_CODE,
						ModulesContract.COURSE_NAME,
						ModulesContract.COURSE_ACAD_YEAR
				};
				String selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
				String[] selectionArgs = { accountName };
				
				// Set up the cursor loader.
				CursorLoader loader = new CursorLoader(mActivity);
				Log.d(TAG, "Setting up cursorLoader");
				loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId));
				loader.setProjection(projection);
				loader.setSelection(selection);
				loader.setSelectionArgs(selectionArgs);
				return loader;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Select the correct action based on ID.
		switch (loader.getId()) {
			case MODULE_INFO_FRAGMENT_LOADER:
				// Reset the cursor.
				cursor.moveToFirst();
				
				// Set the title.
				if (Build.VERSION.SDK_INT >= 11) {
					ActionBar actionBar = mActivity.getActionBar();
					
					// Set the title appropriately.
					actionBar.setTitle(cursor.getString(1));
				}
				
				// Set the view data.
				TextView tvCourseName = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_name);
				tvCourseName.setText(cursor.getString(2));
				TextView tvCourseCode = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_code);
				tvCourseCode.setText(cursor.getString(1));
				TextView tvCourseAcadYear = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_acad_year);
				tvCourseAcadYear.setText(cursor.getString(3));
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Select the correct action based on ID.
		switch (loader.getId()) {
			case MODULE_INFO_FRAGMENT_LOADER:
				// Do nothing.
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}
	
	// }}}
}
