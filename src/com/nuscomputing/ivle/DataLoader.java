package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nuscomputing.ivle.ModulesFragment.ModulesCursorAdapter;
import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.UsersContract;
import com.nuscomputing.ivle.providers.WebcastFilesContract;
import com.nuscomputing.ivle.providers.WebcastItemGroupsContract;
import com.nuscomputing.ivle.providers.WebcastsContract;
import com.nuscomputing.ivle.providers.WorkbinFilesContract;
import com.nuscomputing.ivle.providers.WorkbinFoldersContract;
import com.nuscomputing.ivle.providers.WorkbinsContract;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.Adapter;

/**
 * The primary loader for IVLE data.
 * @author yjwong
 */
public class DataLoader implements LoaderManager.LoaderCallbacks<Cursor> {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "DataLoader";
	
	/** Loader IDs */
	public static final int LOADER_MODULES_FRAGMENT = 1;
	public static final int LOADER_MODULE_INFO_FRAGMENT = 2;
	public static final int LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT = 3;
	public static final int LOADER_MODULE_WEBCASTS_FRAGMENT = 4;
	public static final int LOADER_MODULE_WORKBINS_FRAGMENT = 5;
	public static final int LOADER_VIEW_ANNOUNCEMENT_FRAGMENT = 6;
	public static final int LOADER_VIEW_WEBCAST_ACTIVITY = 7;
	public static final int LOADER_VIEW_WEBCAST_FRAGMENT = 8;
	public static final int LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT = 9;
	public static final int LOADER_VIEW_WEBCAST_FILE_ACTIVITY = 10;
	public static final int LOADER_VIEW_WORKBIN_ACTIVITY = 11;
	public static final int LOADER_VIEW_WORKBIN_FRAGMENT = 12;
	public static final int LOADER_VIEW_WORKBIN_FILES_FRAGMENT = 13;
	public static final int LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT = 14;
	public static final int LOADER_NEW_ANNOUNCEMENTS_FRAGMENT = 15;
	
	/** Loader IDs for AsyncTaskLoaders that are not handled by this loader */
	public static final int LOADER_MODULE_INFO_FRAGMENT_INFO = 15;
	public static final int LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS = 16;
	public static final int LOADER_MODULE_LECTURERS_FRAGMENT = 17;
	public static final int LOADER_MODULE_WEBLINKS_FRAGMENT = 18;
	public static final int LOADER_SEARCHABLE_FRAGMENT = 19;
	public static final int LOADER_CHECK_FOR_UPDATES = 20;
	public static final int LOADER_PUBLIC_NEWS_FRAGMENT = 21;
	
	/** Listener for data loader events */
	private DataLoaderListener mListener;
	
	/** The context */
	private Context mContext;
	
	/** The adapter, if any */
	private Adapter mAdapter;
	
	// }}}
	// {{{ methods
	
	public DataLoader(Context context) {
		mContext = context;
		mListener = null;
	}
	
	public DataLoader(Context context, DataLoaderListener listener) {
		mContext = context;
		mListener = listener;
	}
	
	public DataLoader(Context context, Adapter adapter) {
		mContext = context;
		mAdapter = adapter;
		mListener = null;
	}
	
	public DataLoader(Context context, Adapter adapter, DataLoaderListener listener) {
		mContext = context;
		mAdapter = adapter;
		mListener = listener;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.v(TAG, "onCreateLoader");
		
		// Obtain the current account.
		Account activeAccount = AccountUtils.getActiveAccount(mContext, true);
		if (activeAccount == null) {
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
		long workbinId = -1;
		long workbinFolderId = -1;
		switch (id) {
			case LOADER_MODULE_INFO_FRAGMENT:
			case LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT:
			case LOADER_MODULE_WEBCASTS_FRAGMENT:
			case LOADER_MODULE_WORKBINS_FRAGMENT:
				// Obtain the module ID.
				moduleId = args.getLong("moduleId", -1);
				if (moduleId == -1) {
					throw new IllegalStateException("No module ID was passed to DataLoader");
				}
				break;

			case LOADER_VIEW_ANNOUNCEMENT_FRAGMENT:
				// Obtain the announcement ID.
				announcementId = args.getLong("announcementId", -1);
				if (announcementId == -1) {
					throw new IllegalStateException("No announcement ID was passed to DataLoader");
				}
				break;
			
			case LOADER_VIEW_WEBCAST_ACTIVITY:
			case LOADER_VIEW_WEBCAST_FRAGMENT:
				// Obtain the webcast ID.
				webcastId = args.getLong("webcastId", -1);
				if (webcastId == -1) {
					throw new IllegalStateException("No webcast ID was passed to DataLoader");
				}
				break;
			
			case LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT:
				// Obtain the webcast item group ID.
				webcastItemGroupId = args.getLong("webcastItemGroupId", -1);
				if (webcastItemGroupId == -1) {
					throw new IllegalStateException("No webcast group ID was passed to DataLoader");
				}
				break;
				
			case LOADER_VIEW_WEBCAST_FILE_ACTIVITY:
				// Obtain the webcast file ID.
				webcastFileId = args.getLong("webcastFileId", -1);
				if (webcastFileId == -1) {
					throw new IllegalStateException("No webcast file ID was passed to DataLoader");
				}
				break;
			
			case LOADER_VIEW_WORKBIN_ACTIVITY:
			case LOADER_VIEW_WORKBIN_FRAGMENT:
				// Obtain the workbin ID.
				workbinId = args.getLong("workbinId", -1);
				if (workbinId == -1) {
					throw new IllegalStateException("No workbin ID was passed to DataLoader");
				}
				break;
				
			case LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT:
				// Obtain the workbin ID.
				workbinId = args.getLong("workbinId", -1);
				if (workbinId == -1) {
					throw new IllegalStateException("No workbin ID was passed to DataLoader");
				}
			
			case LOADER_VIEW_WORKBIN_FILES_FRAGMENT:// Fall through.
				// Obtain the workbin folder ID.
				workbinFolderId = args.getLong("workbinFolderId", -1);
				break;
				
			case LOADER_MODULES_FRAGMENT:
			case LOADER_NEW_ANNOUNCEMENTS_FRAGMENT:
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
		
		// Set up query parameters.
		CursorLoader loader = new CursorLoader(mContext);
		List<String> projectionList = new ArrayList<String>();
		List<String> selectionArgsList = new ArrayList<String>();
		String selection = null;
		String sortOrder = null;
		
		Log.d(TAG, "loader " + id + " setting up cursorLoader");
		if (id == LOADER_MODULES_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					ModulesContract.ID,
					ModulesContract.IVLE_ID,
					ModulesContract.COURSE_CODE,
					ModulesContract.COURSE_NAME
			));
			selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(ModulesContract.CONTENT_URI);
			
		} else if (id == LOADER_MODULE_INFO_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					ModulesContract.ID,
					ModulesContract.COURSE_CODE,
					ModulesContract.COURSE_NAME,
					ModulesContract.COURSE_ACAD_YEAR,
					ModulesContract.COURSE_SEMESTER
			));
			selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId));

		} else if (id == LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					AnnouncementsContract.ID,
					AnnouncementsContract.TITLE,
					AnnouncementsContract._DESCRIPTION_NOHTML,
					AnnouncementsContract.CREATED_DATE,
					AnnouncementsContract.IS_READ
			));
			selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			sortOrder = AnnouncementsContract.CREATED_DATE.concat(" DESC");
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/announcements"));
			
		} else if (id == LOADER_MODULE_WEBCASTS_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastsContract.ID,
					WebcastsContract.TITLE
			));
			selection = DatabaseHelper.WEBCASTS_TABLE_NAME + "." + WebcastsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/webcasts"));
			
		} else if (id == LOADER_MODULE_WORKBINS_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WorkbinsContract.ID,
					WorkbinsContract.TITLE
			));
			selection = DatabaseHelper.WORKBINS_TABLE_NAME + "." + WorkbinsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + moduleId + "/workbins"));
			
			
		} else if (id == LOADER_VIEW_ANNOUNCEMENT_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					AnnouncementsContract.ID,
					AnnouncementsContract.IVLE_ID,
					AnnouncementsContract.TITLE,
					AnnouncementsContract.DESCRIPTION,
					AnnouncementsContract.CREATED_DATE,
					AnnouncementsContract.EXPIRY_DATE,
					AnnouncementsContract.URL,
					AnnouncementsContract.IS_READ,
					AnnouncementsContract.CREATOR_PREFIX + UsersContract.NAME
			));
			selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/announcements/" + announcementId));
			
		} else if (id == LOADER_VIEW_WEBCAST_ACTIVITY) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastsContract.MODULE_ID,
					WebcastsContract.TITLE
			));
			selection = DatabaseHelper.WEBCASTS_TABLE_NAME + "." + WebcastsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcasts/" + webcastId));
			
		} else if (id == LOADER_VIEW_WEBCAST_FRAGMENT) {
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
			
		} else if (id == LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastFilesContract.ID,
					WebcastFilesContract.FILE_TITLE,
					WebcastFilesContract.FILE_NAME,
					WebcastFilesContract.FILE_DESCRIPTION,
					WebcastFilesContract.CREATE_DATE,
					WebcastFilesContract.MEDIA_FORMAT
			));
			selection = DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.WEBCAST_ITEM_GROUP_ID + " = ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(webcastItemGroupId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcast_files"));
			
		} else if (id == LOADER_VIEW_WEBCAST_FILE_ACTIVITY) { 
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WebcastFilesContract.CREATE_DATE,
					WebcastFilesContract.FILE_TITLE,
					WebcastFilesContract.FILE_NAME,
					WebcastFilesContract.FILE_DESCRIPTION,
					WebcastFilesContract.MEDIA_FORMAT,
					WebcastFilesContract.MP3,
					WebcastFilesContract.MP4
			));
			selection = DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ID + " = ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(webcastFileId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/webcast_files"));
			
		} else if (id == LOADER_VIEW_WORKBIN_ACTIVITY) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WorkbinsContract.TITLE
			));
			selection = DatabaseHelper.WORKBINS_TABLE_NAME + "." + WorkbinsContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/workbins/" + workbinId));
			
		} else if (id == LOADER_VIEW_WORKBIN_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WorkbinFoldersContract.ID,
					WorkbinFoldersContract.FOLDER_NAME
			));
			selection = DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.WORKBIN_ID + " = ?";
			selection += " AND " + DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.WORKBIN_FOLDER_ID + " IS NULL";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(workbinId));
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/workbin_folders"));
			
		} else if (id == LOADER_VIEW_WORKBIN_FILES_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WorkbinFilesContract.ID,
					WorkbinFilesContract.FILE_NAME,
					WorkbinFilesContract.DOWNLOAD_URL,
					WorkbinFilesContract.FILE_SIZE
			));
			selection = DatabaseHelper.WORKBIN_FILES_TABLE_NAME + "." + WorkbinFilesContract.ACCOUNT + " = ?";
			selectionArgsList.add(accountName);
			
			// Check if we're at the root.
			if (workbinFolderId == -1) {
				selection += " AND " + DatabaseHelper.WORKBIN_FILES_TABLE_NAME + "." + WorkbinFilesContract.WORKBIN_FOLDER_ID + " IS NULL";
			} else {
				selection += " AND " + DatabaseHelper.WORKBIN_FILES_TABLE_NAME + "." + WorkbinFilesContract.WORKBIN_FOLDER_ID + " = ?";
				selectionArgsList.add(Long.toString(workbinFolderId));
			}
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/workbin_files"));
			
		} else if (id == LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					WorkbinFoldersContract.ID,
					WorkbinFoldersContract.FOLDER_NAME,
					WorkbinFoldersContract.FILE_COUNT
			));
			selection = DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.ACCOUNT + " = ?";
			selection += " AND " + DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.WORKBIN_ID + "= ?";
			selectionArgsList.add(accountName);
			selectionArgsList.add(Long.toString(workbinId));
			
			// Check if we're at the root.
			if (workbinFolderId == -1) {
				selection += " AND " + DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.WORKBIN_FOLDER_ID + " IS NULL";
			} else {
				selection += " AND " + DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME + "." + WorkbinFoldersContract.WORKBIN_FOLDER_ID + " = ?";
				selectionArgsList.add(Long.toString(workbinFolderId));
			}
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/workbin_folders"));
			
		} else if (id == LOADER_NEW_ANNOUNCEMENTS_FRAGMENT) {
			// Set up our query parameters.
			projectionList.addAll(Arrays.asList(
					AnnouncementsContract.ID,
					AnnouncementsContract.TITLE,
					AnnouncementsContract._DESCRIPTION_NOHTML,
					AnnouncementsContract.CREATED_DATE,
					AnnouncementsContract.IS_READ,
					AnnouncementsContract.MODULE_PREFIX + ModulesContract.COURSE_CODE
			));
			selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ? AND ";
			selection += DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.IS_READ + " = ? AND ";
			selection += DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.MODULE_ID + " = " + DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ID;
			selectionArgsList.add(accountName);
			selectionArgsList.add("0");
			sortOrder = AnnouncementsContract.CREATED_DATE.concat(" DESC");
			
			// Set up the cursor loader.
			loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/announcements"));
			
		} else {
			throw new IllegalArgumentException("No such loader");
		}
		
		loader.setProjection(projectionList.toArray(new String[]{}));
		loader.setSelection(selection);
		loader.setSelectionArgs(selectionArgsList.toArray(new String[]{}));
		loader.setSortOrder(sortOrder);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Select the correct action based on ID.
		Bundle result = new Bundle();
		switch (loader.getId()) {
			case LOADER_MODULES_FRAGMENT:
				result.putInt("cursorCount", cursor.getCount());
				((ModulesCursorAdapter) mAdapter).swapCursor(cursor);
				((ModulesCursorAdapter) mAdapter).notifyDataSetChanged();
				break;

			case LOADER_MODULE_INFO_FRAGMENT:
				cursor.moveToFirst();
				result.putString("courseName", cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
				result.putString("courseCode", cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_CODE)));
				result.putString("courseAcadYear", cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_ACAD_YEAR)));
				result.putString("courseSemester", cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_SEMESTER)));
				break;
			
			case LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT:
			case LOADER_NEW_ANNOUNCEMENTS_FRAGMENT:
				result.putInt("cursorCount", cursor.getCount());
				((CursorAdapter) mAdapter).swapCursor(cursor);
				((CursorAdapter) mAdapter).notifyDataSetChanged();
				break;
				
			case LOADER_MODULE_WEBCASTS_FRAGMENT:
			case LOADER_MODULE_WORKBINS_FRAGMENT:
				result.putInt("cursorCount", cursor.getCount());
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				((SimpleCursorAdapter) mAdapter).notifyDataSetChanged();
				break;
			
			case LOADER_VIEW_ANNOUNCEMENT_FRAGMENT:
				cursor.moveToFirst();
				result.putString("ivleId", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.IVLE_ID)));
				result.putString("title", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.TITLE)));
				result.putString("userName", cursor.getString(cursor.getColumnIndex(UsersContract.NAME)));
				result.putString("description", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.DESCRIPTION)));
				result.putString("createdDate", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.CREATED_DATE)));
				result.putString("expiryDate", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.EXPIRY_DATE)));
				result.putString("url", cursor.getString(cursor.getColumnIndex(AnnouncementsContract.URL)));
				result.putBoolean("isRead", cursor.getInt(cursor.getColumnIndex(AnnouncementsContract.IS_READ)) == 0 ? false : true);
				break;
			
			case LOADER_VIEW_WEBCAST_ACTIVITY:
				cursor.moveToFirst();
				result.putString("title", cursor.getString(cursor.getColumnIndex(WebcastsContract.TITLE)));
				break;
				
			case LOADER_VIEW_WORKBIN_FRAGMENT:
			case LOADER_VIEW_WEBCAST_FRAGMENT:
			case LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT:
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
			
			case LOADER_VIEW_WEBCAST_FILE_ACTIVITY:
				cursor.moveToFirst();
				result.putString("createDate", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.CREATE_DATE)));
				result.putString("fileTitle", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_TITLE)));
				result.putString("fileName", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_NAME)));
				result.putString("fileDescription", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_DESCRIPTION)));
				result.putString("mediaFormat", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MEDIA_FORMAT)));
				result.putString("MP3", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MP3)));
				result.putString("MP4", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MP4)));
				break;
				
			case LOADER_VIEW_WORKBIN_ACTIVITY:
				cursor.moveToFirst();
				result.putString("title", cursor.getString(cursor.getColumnIndex(WorkbinsContract.TITLE)));
				break;
				
			case LOADER_VIEW_WORKBIN_FILES_FRAGMENT:
				result.putInt("cursorCount", cursor.getCount());
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;
			
			case LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT:
				result.putInt("cursorCount", cursor.getCount());
				((SimpleCursorAdapter) mAdapter).swapCursor(cursor);
				break;

			default:
				throw new IllegalArgumentException("No such loader");
		}
		
		if (mListener != null) {
			((DataLoaderListener) mListener).onLoaderFinished(result);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Select the correct action based on ID.
		switch (loader.getId()) {
			case LOADER_MODULES_FRAGMENT:
				((ModulesCursorAdapter) mAdapter).swapCursor(null);
				break;
				
			case LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT:
			case LOADER_NEW_ANNOUNCEMENTS_FRAGMENT:
				((CursorAdapter) mAdapter).swapCursor(null);
				break;
				
			case LOADER_MODULE_WEBCASTS_FRAGMENT:
			case LOADER_MODULE_WORKBINS_FRAGMENT:
			case LOADER_VIEW_WEBCAST_FRAGMENT:
			case LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT:
			case LOADER_VIEW_WORKBIN_FILES_FRAGMENT:
			case LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT:
				((SimpleCursorAdapter) mAdapter).swapCursor(null);
				break;
				
			case LOADER_MODULE_INFO_FRAGMENT:
			case LOADER_VIEW_ANNOUNCEMENT_FRAGMENT:
			case LOADER_VIEW_WEBCAST_ACTIVITY:
			case LOADER_VIEW_WEBCAST_FILE_ACTIVITY:
			case LOADER_VIEW_WORKBIN_ACTIVITY:
			case LOADER_VIEW_WORKBIN_FRAGMENT:
				// Do nothing.
				break;
				
			default:
				throw new IllegalArgumentException("No such loader");
		}
	}
	
	// }}}
}
