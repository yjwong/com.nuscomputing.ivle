package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.UsersContract;
import com.nuscomputing.ivle.providers.WebcastFilesContract;
import com.nuscomputing.ivle.providers.WebcastItemGroupsContract;
import com.nuscomputing.ivle.providers.WebcastsContract;

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
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.VideoView;

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
	public static final int MODULE_ACTIVITY_LOADER = 2;
	public static final int MODULE_INFO_FRAGMENT_LOADER = 3;
	public static final int MODULE_ANNOUNCEMENT_FRAGMENT_LOADER = 4;
	public static final int MODULE_WEBCAST_FRAGMENT_LOADER = 5;
	public static final int VIEW_ANNOUNCEMENT_FRAGMENT_LOADER = 6;
	public static final int VIEW_WEBCAST_ACTIVITY_LOADER = 7;
	public static final int VIEW_WEBCAST_FRAGMENT_LOADER = 8;
	public static final int VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER = 9;
	public static final int VIEW_WEBCAST_FILE_ACTIVITY_LOADER = 10;
	
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
		
		// Obtain the current account.
		Account activeAccount = AccountUtils.getActiveAccount(mActivity, true);
		if (activeAccount == null) {
			// Launch activity to add account.
			Log.e(TAG, "Error loading accounts");
			return null;
		}
		
		// Obtain the account name.
		String accountName = activeAccount.name;
		if (accountName == null) {
			throw new IllegalStateException("Account name cannot be retrieved");
		}
		Log.d(TAG, "Account found, using " + accountName);
		
		// Select the correct loader based on the provided ID.
		long moduleId = -1;
		long announcementId = -1;
		long webcastId = -1;
		long webcastItemGroupId = -1;
		long webcastFileId = -1;
		switch (id) {
			case MODULE_ACTIVITY_LOADER:
			case MODULE_INFO_FRAGMENT_LOADER:
			case MODULE_ANNOUNCEMENT_FRAGMENT_LOADER:
			case MODULE_WEBCAST_FRAGMENT_LOADER:
				// Obtain the module ID.
				moduleId = args.getLong("moduleId", -1);
				if (moduleId == -1) {
					throw new IllegalStateException("No module ID was passed to DataLoader");
				}
				break;

			case VIEW_ANNOUNCEMENT_FRAGMENT_LOADER:
				// Obtain the announcement ID.
				announcementId = args.getLong("announcementId", -1);
				if (announcementId == -1) {
					throw new IllegalStateException("No announcement ID was passed to DataLoader");
				}
				break;
			
			case VIEW_WEBCAST_ACTIVITY_LOADER:
			case VIEW_WEBCAST_FRAGMENT_LOADER:
				// Obtain the webcast ID.
				webcastId = args.getLong("webcastId", -1);
				if (webcastId == -1) {
					throw new IllegalStateException("No webcast ID was passed to DataLoader");
				}
				break;
			
			case VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER:
				// Obtain the webcast item group ID.
				webcastItemGroupId = args.getLong("webcastItemGroupId", -1);
				if (webcastItemGroupId == -1) {
					throw new IllegalStateException("No webcast group ID was passed to DataLoader");
				}
				break;
				
			case VIEW_WEBCAST_FILE_ACTIVITY_LOADER:
				// Obtain the webcast file ID.
				webcastFileId = args.getLong("webcastFileId", -1);
				if (webcastFileId == -1) {
					throw new IllegalStateException("No webcast file ID was passed to DataLoader");
				}
				break;
				
			case MODULES_FRAGMENT_LOADER:
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
			
		} else if (id == MODULE_ACTIVITY_LOADER) {
			// Set up our query parameters.
			projectionList.add(ModulesContract.COURSE_NAME);
			selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId));
			
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
					AnnouncementsContract.CREATED_DATE
			));
			selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/announcements"));
			
		} else if (id == MODULE_WEBCAST_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastsContract.ID,
					WebcastsContract.TITLE
			));
			selection = DatabaseHelper.WEBCASTS_TABLE_NAME + "." + WebcastsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/webcasts"));
			
		} else if (id == VIEW_ANNOUNCEMENT_FRAGMENT_LOADER) {
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
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/announcements/" + announcementId));
			
		} else if (id == VIEW_WEBCAST_ACTIVITY_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastsContract.MODULE_ID,
					WebcastsContract.TITLE
			));
			selection = DatabaseHelper.WEBCASTS_TABLE_NAME + "." + WebcastsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcasts/" + webcastId));
			
		} else if (id == VIEW_WEBCAST_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastItemGroupsContract.ID,
					WebcastItemGroupsContract.ITEM_GROUP_TITLE
			));
			selection = DatabaseHelper.WEBCAST_ITEM_GROUPS_TABLE_NAME + "." + WebcastItemGroupsContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WEBCAST_ITEM_GROUPS_TABLE_NAME + "." + WebcastItemGroupsContract.WEBCAST_ID + " = ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(webcastId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcast_item_groups"));
			
		} else if (id == VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastFilesContract.ID,
					WebcastFilesContract.FILE_TITLE,
					WebcastFilesContract.FILE_DESCRIPTION
			));
			selection = DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.WEBCAST_ITEM_GROUP_ID + " = ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(webcastItemGroupId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcast_files"));
			
		} else if (id == VIEW_WEBCAST_FILE_ACTIVITY_LOADER) { 
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastFilesContract.MP4,
					WebcastFilesContract.FILE_TITLE,
					WebcastFilesContract.FILE_NAME
			));
			selection = DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.WEBCAST_ITEM_GROUP_ID + " = ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(webcastFileId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcast_files"));
			
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
				
			case MODULE_ACTIVITY_LOADER:
				cursor.moveToFirst();
				if (Build.VERSION.SDK_INT >= 11) {
					ActionBar actionBar = mActivity.getActionBar();
					actionBar.setTitle(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
				}
				break;
				
			case MODULE_INFO_FRAGMENT_LOADER:
				// Reset the cursor.
				cursor.moveToFirst();
				
				// Set the view data.
				TextView tvCourseName = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_name);
				tvCourseName.setText(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
				TextView tvCourseCode = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_code);
				tvCourseCode.setText(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_CODE)));
				TextView tvCourseAcadYear = (TextView) mActivity.findViewById(R.id.module_info_fragment_course_acad_year);
				tvCourseAcadYear.setText(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_ACAD_YEAR)));
				break;
			
			case MODULE_ANNOUNCEMENT_FRAGMENT_LOADER:
				// Change the visibility message.
				TextView tvNoAnnouncements = (TextView) mActivity.findViewById(R.id.module_announcements_fragment_no_announcements);
				tvNoAnnouncements.setVisibility((cursor.getCount() == 0) ? TextView.VISIBLE : TextView.GONE);
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
				
			case MODULE_WEBCAST_FRAGMENT_LOADER:
				TextView tvNoWebcasts = (TextView) mActivity.findViewById(R.id.module_webcasts_fragment_no_webcasts);
				tvNoWebcasts.setVisibility((cursor.getCount() == 0) ? TextView.VISIBLE : TextView.GONE);
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
			
			case VIEW_ANNOUNCEMENT_FRAGMENT_LOADER:
				cursor.moveToFirst();
				
				// Set the title and subtitle.
				TextView tvTitle = (TextView) mActivity.findViewById(R.id.view_announcement_fragment_title);
				tvTitle.setText(cursor.getString(cursor.getColumnIndex(AnnouncementsContract.TITLE)));
				tvTitle.setSelected(true);
				TextView tvSubtitle = (TextView) mActivity.findViewById(R.id.view_announcement_fragment_subtitle);
				tvSubtitle.setText(cursor.getString(cursor.getColumnIndex(UsersContract.NAME)));
				
				// Set the content for the webview.
				WebView wvDescription = (WebView) mActivity.findViewById(R.id.view_announcement_fragment_webview);
				wvDescription.loadData(cursor.getString(cursor.getColumnIndex(AnnouncementsContract.DESCRIPTION)), "text/html", null);
				break;
			
			case VIEW_WEBCAST_ACTIVITY_LOADER:
				cursor.moveToFirst();
				if (Build.VERSION.SDK_INT >= 11) {
					ActionBar actionBar = mActivity.getActionBar();
					actionBar.setTitle(cursor.getString(cursor.getColumnIndex(WebcastsContract.TITLE)));
				}
				break;
				
			case VIEW_WEBCAST_FRAGMENT_LOADER:
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
			
			case VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER:
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
			
			case VIEW_WEBCAST_FILE_ACTIVITY_LOADER:
				// Reset the cursor.
				cursor.moveToFirst();
				
				// Set the title.
				if (Build.VERSION.SDK_INT >= 11) {
					mActivity.getActionBar().setTitle(cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_TITLE)));
				}
				
				// Start the video playback.
				Log.v(TAG, "video = " + cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MP4)));
				Uri videoUri = Uri.parse(cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MP4)));
				((ViewWebcastFileActivity) mActivity).setVideoUri(videoUri);
				VideoView videoView = (VideoView) mActivity.findViewById(R.id.view_webcast_file_video_view);
				videoView.setVideoURI(videoUri);
				videoView.start();
				
				// Set the file name.
				String fileName = cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_NAME));
				((ViewWebcastFileActivity) mActivity).setVideoFileName(fileName);
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
			case MODULE_WEBCAST_FRAGMENT_LOADER:
			case VIEW_WEBCAST_FRAGMENT_LOADER:
			case VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER:
				((SimpleCursorAdapter) mAdapter).swapCursor(null);
				break;
				
			case MODULE_ACTIVITY_LOADER:
			case MODULE_INFO_FRAGMENT_LOADER:
			case VIEW_WEBCAST_ACTIVITY_LOADER:
			case VIEW_ANNOUNCEMENT_FRAGMENT_LOADER:
			case VIEW_WEBCAST_FILE_ACTIVITY_LOADER:
				// Do nothing.
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}
	
	// }}}
}
