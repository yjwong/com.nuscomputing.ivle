package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.UsersContract;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.Adapter;
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
	public static final int MODULES_FRAGMENT_LOADER = 1;
	public static final int MODULE_INFO_FRAGMENT_LOADER = 2;
	public static final int MODULE_ANNOUNCEMENT_FRAGMENT_LOADER = 3;
	
	/** The context */
	private Activity mActivity;
	
	/** The adapter, if any */
	private Adapter mAdapter;
	
	// }}}
	// {{{ methods
	
	public DataLoader(Activity activity) {
		mActivity = activity;
	}
	
	public DataLoader(Activity activity, Adapter adapter) {
		mActivity = activity;
		mAdapter = adapter;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.v(TAG, "onCreateLoader");
		
		// Select the correct loader based on the provided ID.
		String accountName = null;
		long moduleId = -1;
		switch (id) {
			case MODULE_INFO_FRAGMENT_LOADER:
			case MODULE_ANNOUNCEMENT_FRAGMENT_LOADER:
				// Obtain the module ID.
				moduleId = args.getLong("moduleId", -1);
				if (moduleId == -1) {
					throw new IllegalStateException("No module ID was passed to DataLoader");
				}
				
			case MODULES_FRAGMENT_LOADER: // Fall through.
				// Obtain the current account.
				Account activeAccount = AccountUtils.getActiveAccount(mActivity, true);
				if (activeAccount == null) {
					// Launch activity to add account.
					Log.e(TAG, "Error loading accounts");
					return null;
				}
				
				// Obtain the account name.
				accountName = activeAccount.name;
				if (accountName == null) {
					throw new IllegalStateException("Account name cannot be retrieved");
				}
				Log.d(TAG, "Account found, using " + accountName);
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
		
		// Set up query parameters.
		CursorLoader loader = new CursorLoader(mActivity);
		List<String> projectionList = new ArrayList<String>();
		List<String> selectionArgsList = new ArrayList<String>();
		String selection;
		
		Log.d(TAG, "loader " + id + " setting up cursorLoader");
		if (id == MODULES_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					ModulesContract.ID,
					ModulesContract.COURSE_CODE,
					ModulesContract.COURSE_NAME
			));
			selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(ModulesContract.CONTENT_URI);
			
		} else if (id == MODULE_INFO_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					ModulesContract.ID,
					ModulesContract.COURSE_CODE,
					ModulesContract.COURSE_NAME,
					ModulesContract.COURSE_ACAD_YEAR
			));
			selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId));

		} else if (id == MODULE_ANNOUNCEMENT_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					AnnouncementsContract.ID,
					AnnouncementsContract.TITLE,
					AnnouncementsContract.DESCRIPTION,
					AnnouncementsContract.CREATED_DATE,
					"creator_" + UsersContract.NAME
			));
			selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/announcements"));
			
		} else {
			throw new IllegalArgumentException("No such loader");
		}
		
		loader.setProjection(projectionList.toArray(new String[]{}));
		loader.setSelection(selection);
		loader.setSelectionArgs(selectionArgsList.toArray(new String[]{}));
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Select the correct action based on ID.
		switch (loader.getId()) {
			case MODULES_FRAGMENT_LOADER:
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				((SimpleCursorAdapter) mAdapter).notifyDataSetChanged();
				break;
				
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
			
			case MODULE_ANNOUNCEMENT_FRAGMENT_LOADER:
				// Change the visibility message.
				TextView tvNoAnnouncements = (TextView) mActivity.findViewById(R.id.module_announcements_fragment_no_announcements);
				tvNoAnnouncements.setVisibility((cursor.getCount() == 0) ? TextView.VISIBLE : TextView.GONE);
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Select the correct action based on ID.
		switch (loader.getId()) {
			case MODULES_FRAGMENT_LOADER:
			case MODULE_ANNOUNCEMENT_FRAGMENT_LOADER:
				((SimpleCursorAdapter) mAdapter).swapCursor(null);
				break;
				
			case MODULE_INFO_FRAGMENT_LOADER:
				// Do nothing.
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}
	
	// }}}
}
