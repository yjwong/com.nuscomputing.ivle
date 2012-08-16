package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.Constants;
import com.nuscomputing.ivle.DatabaseHelper;
import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class IVLEProvider extends ContentProvider {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLEProvider";
	
	/** Defines a handle to the database helper object */
	private DatabaseHelper mDatabaseHelper;
	
	/** Holds the database object */
	private SQLiteDatabase mDatabase;
	
	/** A UriMatcher object to match ContentProvider URIs */
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	/** Possible content provider paths */
	private static final int MODULES = 1;
	private static final int MODULES_ID = 2;
	private static final int MODULES_ANNOUNCEMENTS = 3;
	private static final int MODULES_ANNOUNCEMENTS_ID = 4;
	private static final int MODULES_DESCRIPTIONS = 5;
	private static final int MODULES_DESCRIPTIONS_ID = 6;
	private static final int MODULES_GRADEBOOKS = 7;
	private static final int MODULES_GRADEBOOKS_ID = 8;
	private static final int MODULES_GRADEBOOK_ITEMS = 9;
	private static final int MODULES_GRADEBOOK_ITEMS_ID = 10;
	private static final int MODULES_WEBCASTS = 11;
	private static final int MODULES_WEBCASTS_ID = 12;
	private static final int MODULES_WEBCAST_FILES = 13;
	private static final int MODULES_WEBCAST_FILES_ID = 14;
	private static final int MODULES_WEBCAST_ITEM_GROUPS = 15;
	private static final int MODULES_WEBCAST_ITEM_GROUPS_ID = 16;
	private static final int MODULES_WEBLINKS  = 17;
	private static final int MODULES_WEBLINKS_ID = 18;
	private static final int MODULES_WORKBINS = 19;
	private static final int MODULES_WORKBINS_ID = 20;
	private static final int MODULES_WORKBIN_FOLDERS = 21;
	private static final int MODULES_WORKBIN_FOLDERS_ID = 22;
	private static final int MODULES_WORKBIN_FILES = 23;
	private static final int MODULES_WORKBIN_FILES_ID = 24;
	private static final int ANNOUNCEMENTS = 25;
	private static final int ANNOUNCEMENTS_ID = 26;
	private static final int DESCRIPTIONS = 27;
	private static final int DESCRIPTIONS_ID = 28;
	private static final int GRADEBOOKS = 29;
	private static final int GRADEBOOKS_ID = 30;
	private static final int GRADEBOOK_ITEMS = 31;
	private static final int GRADEBOOK_ITEMS_ID = 32;
	private static final int TIMETABLE_SLOTS = 33;
	private static final int TIMETABLE_SLOTS_ID = 34;
	private static final int USERS = 35;
	private static final int USERS_ID = 36;
	private static final int WEBCASTS = 37;
	private static final int WEBCASTS_ID = 38;
	private static final int WEBCAST_FILES = 39;
	private static final int WEBCAST_FILES_ID = 40;
	private static final int WEBCAST_ITEM_GROUPS = 41;
	private static final int WEBCAST_ITEM_GROUPS_ID = 42;
	private static final int WEBLINKS = 43;
	private static final int WEBLINKS_ID = 44;
	private static final int WORKBINS = 45;
	private static final int WORKBINS_ID = 46;
	private static final int WORKBIN_FOLDERS = 47;
	private static final int WORKBIN_FOLDERS_ID = 48;
	private static final int WORKBIN_FILES = 49;
	private static final int WORKBIN_FILES_ID = 50;
	
	// }}}
	// {{{ methods
	
	static {
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules", MODULES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#", MODULES_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/announcements", MODULES_ANNOUNCEMENTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/announcements/#", MODULES_ANNOUNCEMENTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/descriptions", MODULES_DESCRIPTIONS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/descriptions/#", MODULES_DESCRIPTIONS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebooks", MODULES_GRADEBOOKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebooks/#", MODULES_GRADEBOOKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebook_items", MODULES_GRADEBOOK_ITEMS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebook_items/#", MODULES_GRADEBOOK_ITEMS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcasts", MODULES_WEBCASTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcasts/#", MODULES_WEBCASTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcast_files", MODULES_WEBCAST_FILES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcast_files/#", MODULES_WEBCAST_FILES_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcast_item_groups", MODULES_WEBCAST_ITEM_GROUPS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcast_item_groups/#", MODULES_WEBCAST_ITEM_GROUPS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/weblinks", MODULES_WEBLINKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/weblinks/#", MODULES_WEBLINKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbins", MODULES_WORKBINS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbins/#", MODULES_WORKBINS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbin_folders", MODULES_WORKBIN_FOLDERS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbin_folders/#", MODULES_WORKBIN_FOLDERS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbin_files", MODULES_WORKBIN_FILES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbin_files/#", MODULES_WORKBIN_FILES_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "announcements", ANNOUNCEMENTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "announcements/#", ANNOUNCEMENTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "descriptions", DESCRIPTIONS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "descriptions/#", DESCRIPTIONS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebooks", GRADEBOOKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebooks/#", GRADEBOOKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebook_items", GRADEBOOK_ITEMS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebook_items/#", GRADEBOOK_ITEMS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "timetable_slots", TIMETABLE_SLOTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "timetable_slots/#", TIMETABLE_SLOTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "users", USERS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "users/#", USERS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcasts", WEBCASTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcasts/#", WEBCASTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcast_files", WEBCAST_FILES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcast_files/#", WEBCAST_FILES_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcast_item_groups", WEBCAST_ITEM_GROUPS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcast_item_groups/#", WEBCAST_ITEM_GROUPS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "weblinks", WEBLINKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "weblinks/#", WEBLINKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbins", WORKBINS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbins/#", WORKBINS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbin_folders", WORKBIN_FOLDERS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbin_folders/#", WORKBIN_FOLDERS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbin_files", WORKBIN_FILES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbin_files/#", WORKBIN_FILES_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
		// Get the contract.
		IVLEContract contract = getContractFromUri(uri);
		
		// Build query based on URI.
		int ret = -1;
		String moduleId = null;
		String whereClause = null;
		switch (sUriMatcher.match(uri)) {
			case MODULES:
				ret = mDatabase.delete(DatabaseHelper.MODULES_TABLE_NAME, 
						selection, selectionArgs);
				break;
			
			case MODULES_ID:
				moduleId = uri.getLastPathSegment();
				whereClause = ModulesContract.ID + "=" + moduleId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.MODULES_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
				
			case MODULES_ANNOUNCEMENTS:
			case MODULES_DESCRIPTIONS:
			case MODULES_GRADEBOOKS:
			case MODULES_GRADEBOOK_ITEMS:
			case MODULES_WEBCASTS:
			case MODULES_WEBCAST_FILES:
			case MODULES_WEBCAST_ITEM_GROUPS:
			case MODULES_WEBLINKS:
			case MODULES_WORKBINS:
			case MODULES_WORKBIN_FOLDERS:
			case MODULES_WORKBIN_FILES:
				moduleId = uri.getPathSegments().get(2);
				whereClause = contract.getColumnNameModuleId() + "=" + moduleId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(contract.getTableName(), whereClause,
						selectionArgs);
				break;
			
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_DESCRIPTIONS_ID:
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCAST_FILES_ID:
			case MODULES_WEBCAST_ITEM_GROUPS_ID:
			case MODULES_WEBLINKS_ID:
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBIN_FOLDERS_ID:
			case MODULES_WORKBIN_FILES_ID:
				moduleId = uri.getPathSegments().get(2);
				whereClause = contract.getColumnNameModuleId() + "=" + moduleId +
						" AND " + contract.getColumnNameId() + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(contract.getTableName(), whereClause,
						selectionArgs);
				break;
					
			case ANNOUNCEMENTS_ID:
			case DESCRIPTIONS_ID:
			case GRADEBOOKS_ID:
			case GRADEBOOK_ITEMS_ID:
			case TIMETABLE_SLOTS_ID:
			case USERS_ID:
			case WEBCASTS_ID:
			case WEBCAST_FILES_ID:
			case WEBCAST_ITEM_GROUPS_ID:
			case WEBLINKS_ID:
			case WORKBINS_ID:
			case WORKBIN_FOLDERS_ID:
			case WORKBIN_FILES_ID:
				whereClause = contract.getColumnNameId() + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(contract.getTableName(), whereClause,
						selectionArgs);
				break;
				
			case ANNOUNCEMENTS:
			case DESCRIPTIONS:
			case GRADEBOOKS:
			case GRADEBOOK_ITEMS:
			case TIMETABLE_SLOTS:
			case USERS:
			case WEBCASTS:
			case WEBCAST_FILES:
			case WEBCAST_ITEM_GROUPS:
			case WEBLINKS:
			case WORKBINS:
			case WORKBIN_FOLDERS:
			case WORKBIN_FILES:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all items of type " + contract.getContentUri());
				}
				ret = mDatabase.delete(contract.getTableName(), selection, selectionArgs);
				break;
			
			default:
				throw new IllegalArgumentException();
		}
			
		return ret;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Log.d(TAG, "getType not implemented yet");
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
		// Get the contract.
		IVLEContract contract = getContractFromUri(uri);
		
		// Build query based on URI.
		String selection = null;
		String id = null;
		String moduleId = null;
		long rowId = 0;
		switch (sUriMatcher.match(uri)) {
			case MODULES:
				rowId = mDatabase.insert(DatabaseHelper.MODULES_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case MODULES_ID:
				moduleId = uri.getLastPathSegment();
				selection = ModulesContract.ID + " = " + moduleId;
				this.update(uri, values, selection, null);
				break;
			
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_DESCRIPTIONS_ID:
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCAST_FILES_ID:
			case MODULES_WEBCAST_ITEM_GROUPS_ID:
			case MODULES_WEBLINKS_ID:
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBIN_FOLDERS_ID:
			case MODULES_WORKBIN_FILES_ID:
				moduleId = uri.getPathSegments().get(2);
				id = uri.getLastPathSegment();
				selection = contract.getColumnNameModuleId() + " = " + moduleId +
						" AND " + contract.getColumnNameId() + " = " + id;
				this.update(uri, values, selection, null);
				break;

			case ANNOUNCEMENTS:
			case DESCRIPTIONS:
			case GRADEBOOKS:
			case GRADEBOOK_ITEMS:
			case TIMETABLE_SLOTS:
			case USERS:
			case WEBCASTS:
			case WEBCAST_FILES:
			case WEBCAST_ITEM_GROUPS:
			case WEBLINKS:
			case WORKBINS:
			case WORKBIN_FOLDERS:
			case WORKBIN_FILES:
				rowId = mDatabase.insert(contract.getTableName(), null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
				
			case ANNOUNCEMENTS_ID:
			case DESCRIPTIONS_ID:
			case GRADEBOOKS_ID:
			case GRADEBOOK_ITEMS_ID:
			case TIMETABLE_SLOTS_ID:
			case USERS_ID:
			case WEBCASTS_ID:
			case WEBCAST_FILES_ID:
			case WEBCAST_ITEM_GROUPS_ID:
			case WEBLINKS_ID:
			case WORKBINS_ID:
			case WORKBIN_FOLDERS_ID:
			case WORKBIN_FILES_ID:
				id = uri.getLastPathSegment();
				selection = contract.getColumnNameId() + " = " + id;
				this.update(uri, values, selection, null);
				break;

			default:
				throw new IllegalArgumentException();
		}
		
		return uri;
	}

	@Override
	public boolean onCreate() {
		// Create a new helper object. This method always returns quickly.
		Log.v(TAG, "AnnouncementsProvider created");
		mDatabaseHelper = new DatabaseHelper(getContext());
		if (mDatabaseHelper == null) {
			return false;
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
		// Get the contract.
		IVLEContract contract = getContractFromUri(uri);
		
		// Use a query builder.
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// Build query based on URI.
		Log.v(TAG, "query received, uri = " + uri);
		switch (sUriMatcher.match(uri)) {
			case MODULES_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.MODULES_TABLE_NAME + "." + 
						ModulesContract.ID + "=" + uri.getLastPathSegment()
				);
				break;
				
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_DESCRIPTIONS_ID:
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCAST_FILES_ID:
			case MODULES_WEBCAST_ITEM_GROUPS_ID:
			case MODULES_WEBLINKS_ID:
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBIN_FOLDERS_ID:
			case MODULES_WORKBIN_FILES_ID:
				queryBuilder.appendWhere(
						contract.getTableName() + "." + 
						contract.getColumnNameId() + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_ANNOUNCEMENTS: // Fall through
			case MODULES_DESCRIPTIONS: // Fall through
			case MODULES_GRADEBOOKS: // Fall through
			case MODULES_GRADEBOOK_ITEMS: // Fall through
			case MODULES_WEBCASTS: // Fall through
			case MODULES_WEBCAST_FILES: // Fall through
			case MODULES_WEBCAST_ITEM_GROUPS: // Fall through
			case MODULES_WEBLINKS: // Fall through
			case MODULES_WORKBINS: // Fall through
			case MODULES_WORKBIN_FOLDERS: // Fall through
			case MODULES_WORKBIN_FILES: // Fall through
				queryBuilder.appendWhere(
						contract.getTableName() + "." + 
						contract.getColumnNameModuleId() + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case ANNOUNCEMENTS_ID:
			case DESCRIPTIONS_ID:
			case GRADEBOOKS_ID:
			case GRADEBOOK_ITEMS_ID:
			case TIMETABLE_SLOTS_ID:
			case USERS_ID:
			case WEBCASTS_ID:
			case WEBCAST_FILES_ID:
			case WEBCAST_ITEM_GROUPS_ID:
			case WEBLINKS_ID:
			case WORKBINS_ID:
			case WORKBIN_FOLDERS_ID:
			case WORKBIN_FILES_ID:
				queryBuilder.appendWhere(
						contract.getTableName() + "." +
						contract.getColumnNameId() + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case MODULES:
			case ANNOUNCEMENTS:
			case DESCRIPTIONS:
			case GRADEBOOKS:
			case GRADEBOOK_ITEMS:
			case TIMETABLE_SLOTS:
			case USERS:
			case WEBCASTS:
			case WEBCAST_FILES:
			case WEBCAST_ITEM_GROUPS:
			case WEBLINKS:
			case WORKBINS:
			case WORKBIN_FOLDERS:
			case WORKBIN_FILES:
				break;
				
			default:
				throw new IllegalArgumentException();
		}
		
		// Set table names and projection maps.
		Map<String, String> projectionMap = new HashMap<String, String>();
		switch (sUriMatcher.match(uri)) {
			case MODULES_ID:
			case MODULES:
				// Set table names and joins.
				queryBuilder.setTables(
						DatabaseHelper.MODULES_TABLE_NAME + " JOIN " +
						DatabaseHelper.USERS_TABLE_NAME + " ON " + "(" +
							DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.CREATOR_ID + "=" +
							DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID +
						")"
				);
				
				// Set projection maps.
				projectionMap.put(ModulesContract.ID, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ID);
				projectionMap.putAll(new ModulesContract().getJoinProjectionMap(""));
				projectionMap.putAll(new UsersContract().getJoinProjectionMap("creator_"));
				queryBuilder.setProjectionMap(projectionMap);
				break;
				
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_ANNOUNCEMENTS:
			case ANNOUNCEMENTS_ID:
			case ANNOUNCEMENTS:
				// Set table names and joins.
				queryBuilder.setTables(
					DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + " JOIN " +
					DatabaseHelper.MODULES_TABLE_NAME + " ON (" +
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.MODULE_ID + "=" +
						DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ID +
					") JOIN " + DatabaseHelper.USERS_TABLE_NAME + " ON (" +
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.CREATOR_ID + "=" +
						DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID +
					")"
				);
				
				// Set projection maps.
				projectionMap.put(AnnouncementsContract.ID, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ID);
				projectionMap.putAll(new AnnouncementsContract().getJoinProjectionMap(""));
				projectionMap.putAll(new ModulesContract().getJoinProjectionMap("module_"));
				projectionMap.putAll(new UsersContract().getJoinProjectionMap("creator_"));
				queryBuilder.setProjectionMap(projectionMap);
				break;
				
			case MODULES_DESCRIPTIONS_ID:
			case MODULES_DESCRIPTIONS:
			case DESCRIPTIONS_ID:
			case DESCRIPTIONS:
				queryBuilder.setTables(DatabaseHelper.DESCRIPTIONS_TABLE_NAME);
				break;
				
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOKS:
			case GRADEBOOKS_ID:
			case GRADEBOOKS:
				queryBuilder.setTables(DatabaseHelper.GRADEBOOKS_TABLE_NAME);
				break;
				
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_GRADEBOOK_ITEMS:
			case GRADEBOOK_ITEMS_ID:
			case GRADEBOOK_ITEMS:
				queryBuilder.setTables(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME);
				break;
			
			case TIMETABLE_SLOTS_ID:
			case TIMETABLE_SLOTS:
				queryBuilder.setTables(DatabaseHelper.TIMETABLE_SLOTS_TABLE_NAME);
				break;
				
			case USERS_ID:
			case USERS:
				queryBuilder.setTables(DatabaseHelper.USERS_TABLE_NAME);
				break;
				
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCASTS:
			case WEBCASTS_ID:
			case WEBCASTS:
				queryBuilder.setTables(DatabaseHelper.WEBCASTS_TABLE_NAME);
				break;
				
			case MODULES_WEBCAST_FILES_ID:
			case MODULES_WEBCAST_FILES:
			case WEBCAST_FILES_ID:
			case WEBCAST_FILES:
				// IVLE webcast files is broken! They all have no creator IDs.
				// Set table names and joins.
				queryBuilder.setTables(DatabaseHelper.WEBCAST_FILES_TABLE_NAME);
				/*
				queryBuilder.setTables(
						DatabaseHelper.WEBCAST_FILES_TABLE_NAME + " JOIN " + 
						DatabaseHelper.USERS_TABLE_NAME + " ON " + "(" +
							DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.CREATOR_ID + " = " +
							DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID +
						")"
				);
				*/
				
				// Set projection maps.
				/*
				projectionMap.put(WebcastFilesContract.ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ID);
				projectionMap.put(WebcastFilesContract.IVLE_ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.IVLE_ID);
				projectionMap.put(WebcastFilesContract.MODULE_ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.MODULE_ID);
				projectionMap.put(WebcastFilesContract.ACCOUNT, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.ACCOUNT);
				projectionMap.put(WebcastFilesContract.CREATOR_ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.CREATOR_ID);
				projectionMap.put(WebcastFilesContract.WEBCAST_ITEM_GROUP_ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.WEBCAST_ITEM_GROUP_ID);
				projectionMap.put(WebcastFilesContract.BANK_ITEM_ID, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.BANK_ITEM_ID);
				projectionMap.put(WebcastFilesContract.CREATE_DATE, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.CREATE_DATE);
				projectionMap.put(WebcastFilesContract.FILE_DESCRIPTION, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.FILE_DESCRIPTION);
				projectionMap.put(WebcastFilesContract.FILE_NAME, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.FILE_NAME);
				projectionMap.put(WebcastFilesContract.FILE_TITLE, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.FILE_TITLE);
				projectionMap.put(WebcastFilesContract.MP3, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.MP3);
				projectionMap.put(WebcastFilesContract.MP4, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.MP4);
				projectionMap.put(WebcastFilesContract.MEDIA_FORMAT, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.MEDIA_FORMAT);
				projectionMap.put(WebcastFilesContract.IS_READ, DatabaseHelper.WEBCAST_FILES_TABLE_NAME + "." + WebcastFilesContract.IS_READ);
				projectionMap.put("creator_" + UsersContract.IVLE_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.IVLE_ID);
				projectionMap.put("creator_" + UsersContract.ACCOUNT, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT);
				projectionMap.put("creator_" + UsersContract.ACCOUNT_TYPE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT_TYPE);
				projectionMap.put("creator_" + UsersContract.EMAIL, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.EMAIL);
				projectionMap.put("creator_" + UsersContract.NAME, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.NAME);
				projectionMap.put("creator_" + UsersContract.TITLE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.TITLE);
				projectionMap.put("creator_" + UsersContract.USER_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.USER_ID);
				queryBuilder.setProjectionMap(projectionMap);
				*/
				break;
				
			case MODULES_WEBCAST_ITEM_GROUPS_ID:
			case MODULES_WEBCAST_ITEM_GROUPS:
			case WEBCAST_ITEM_GROUPS_ID:
			case WEBCAST_ITEM_GROUPS:
				queryBuilder.setTables(DatabaseHelper.WEBCAST_ITEM_GROUPS_TABLE_NAME);
				break;
				
			case MODULES_WEBLINKS_ID:
			case MODULES_WEBLINKS:
			case WEBLINKS_ID:
			case WEBLINKS:
				queryBuilder.setTables(DatabaseHelper.WEBLINKS_TABLE_NAME);
				break;
				
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBINS:
			case WORKBINS_ID:
			case WORKBINS:
				queryBuilder.setTables(DatabaseHelper.WORKBINS_TABLE_NAME);
				break;
				
			case MODULES_WORKBIN_FOLDERS_ID:
			case MODULES_WORKBIN_FOLDERS:
			case WORKBIN_FOLDERS_ID:
			case WORKBIN_FOLDERS:
				queryBuilder.setTables(DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME);
				break;
				
			case MODULES_WORKBIN_FILES_ID:
			case MODULES_WORKBIN_FILES:
			case WORKBIN_FILES_ID:
			case WORKBIN_FILES:
				queryBuilder.setTables(DatabaseHelper.WORKBIN_FILES_TABLE_NAME);
				break;
				
			default:
				throw new IllegalArgumentException();
		}
		
		// Projection map to include user information.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Log.v(TAG, "query: " + queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null));
		} else {
			Log.v(TAG, "query: " + queryBuilder.buildQuery(projection, selection, null, null, null, sortOrder, null));
		}
		
		Cursor cursor = queryBuilder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, 
			String[] selectionArgs) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
		// Obtain the table name.
		IVLEContract contract = getContractFromUri(uri);
		String tableName = contract.getTableName();
		int ret = mDatabase.update(tableName, values, selection, selectionArgs);
		return ret;
	}
	
	private static IVLEContract getContractFromUri(Uri uri) {
		// Attempt to match the URI.
		Class<? extends IVLEContract> contractClass;
		switch (sUriMatcher.match(uri)) {
			case MODULES_ID:
			case MODULES:
				contractClass = ModulesContract.class;
				break;
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_ANNOUNCEMENTS:
			case ANNOUNCEMENTS_ID:
			case ANNOUNCEMENTS:
				contractClass = AnnouncementsContract.class;
				break;
			case MODULES_DESCRIPTIONS_ID:
			case MODULES_DESCRIPTIONS:
			case DESCRIPTIONS_ID:
			case DESCRIPTIONS:
				contractClass = DescriptionsContract.class;
				break;
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOKS:
			case GRADEBOOKS_ID:
			case GRADEBOOKS:
				contractClass = GradebooksContract.class;
				break;
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_GRADEBOOK_ITEMS:
			case GRADEBOOK_ITEMS_ID:
			case GRADEBOOK_ITEMS:
				contractClass = GradebookItemsContract.class;
				break;
			case TIMETABLE_SLOTS_ID:
			case TIMETABLE_SLOTS:
				contractClass = TimetableSlotsContract.class;
				break;
			case USERS_ID:
			case USERS:
				contractClass = UsersContract.class;
				break;
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCASTS:
			case WEBCASTS_ID:
			case WEBCASTS:
				contractClass = WebcastsContract.class;
				break;
			case MODULES_WEBCAST_FILES_ID:
			case MODULES_WEBCAST_FILES:
			case WEBCAST_FILES_ID:
			case WEBCAST_FILES:
				contractClass = WebcastFilesContract.class;
				break;
			case MODULES_WEBCAST_ITEM_GROUPS_ID:
			case MODULES_WEBCAST_ITEM_GROUPS:
			case WEBCAST_ITEM_GROUPS_ID:
			case WEBCAST_ITEM_GROUPS:
				contractClass = WebcastItemGroupsContract.class;
				break;
			case MODULES_WEBLINKS_ID:
			case MODULES_WEBLINKS:
			case WEBLINKS_ID:
			case WEBLINKS:
				contractClass = WeblinksContract.class;
				break;
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBINS:
			case WORKBINS_ID:
			case WORKBINS:
				contractClass = WorkbinsContract.class;
				break;
			case MODULES_WORKBIN_FOLDERS_ID:
			case MODULES_WORKBIN_FOLDERS:
			case WORKBIN_FOLDERS_ID:
			case WORKBIN_FOLDERS:
				contractClass = WorkbinFoldersContract.class;
				break;
			case MODULES_WORKBIN_FILES_ID:
			case MODULES_WORKBIN_FILES:
			case WORKBIN_FILES_ID:
			case WORKBIN_FILES:
				contractClass = WorkbinFilesContract.class;
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		// Instantiate the class.
		try {
			IVLEContract contract = contractClass.newInstance();
			return contract;
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException while trying to obtain contract instance");
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException while trying to obtain contract instance");
		}
		
		return null;
	}
	
	// }}}
}
