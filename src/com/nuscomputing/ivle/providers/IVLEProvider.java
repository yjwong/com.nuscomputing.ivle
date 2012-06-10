package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.Constants;
import com.nuscomputing.ivle.DatabaseHelper;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
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
	private static final int MODULES_GRADEBOOKS = 5;
	private static final int MODULES_GRADEBOOKS_ID = 6;
	private static final int MODULES_GRADEBOOK_ITEMS = 7;
	private static final int MODULES_GRADEBOOK_ITEMS_ID = 8;
	private static final int MODULES_WEBCASTS = 9;
	private static final int MODULES_WEBCASTS_ID = 10;
	private static final int MODULES_WEBLINKS  = 11;
	private static final int MODULES_WEBLINKS_ID = 12;
	private static final int MODULES_WORKBINS = 13;
	private static final int MODULES_WORKBINS_ID = 14;
	private static final int ANNOUNCEMENTS = 15;
	private static final int ANNOUNCEMENTS_ID = 16;
	private static final int GRADEBOOKS = 17;
	private static final int GRADEBOOKS_ID = 18;
	private static final int GRADEBOOK_ITEMS = 19;
	private static final int GRADEBOOK_ITEMS_ID = 20;
	private static final int USERS = 21;
	private static final int USERS_ID = 22;
	private static final int WEBCASTS = 23;
	private static final int WEBCASTS_ID = 24;
	private static final int WEBLINKS = 25;
	private static final int WEBLINKS_ID = 26;
	private static final int WORKBINS = 27;
	private static final int WORKBINS_ID = 28;
	
	// }}}
	// {{{ methods
	
	static {
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules", MODULES);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#", MODULES_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/announcements", MODULES_ANNOUNCEMENTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/announcements/#", MODULES_ANNOUNCEMENTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebooks", MODULES_GRADEBOOKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebooks/#", MODULES_GRADEBOOKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebook_items", MODULES_GRADEBOOK_ITEMS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/gradebook_items/#", MODULES_GRADEBOOK_ITEMS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcasts", MODULES_WEBCASTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/webcasts/#", MODULES_WEBCASTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/weblinks", MODULES_WEBLINKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/weblinks/#", MODULES_WEBLINKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbins", MODULES_WORKBINS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "modules/#/workbins/#", MODULES_WORKBINS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "announcements", ANNOUNCEMENTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "announcements/#", ANNOUNCEMENTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebooks", GRADEBOOKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebooks/#", GRADEBOOKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebook_items", GRADEBOOK_ITEMS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "gradebook_items/#", GRADEBOOK_ITEMS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "users", USERS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "users/#", USERS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcasts", WEBCASTS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "webcasts/#", WEBCASTS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "weblinks", WEBLINKS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "weblinks/#", WEBLINKS_ID);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbins", WORKBINS);
		sUriMatcher.addURI(Constants.PROVIDER_AUTHORITY, "workbins/#", WORKBINS_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
		// Build query based on URI.
		int ret = -1;
		String moduleId = null;
		String webcastId = null;
		String weblinkId = null;
		String workbinId = null;
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
				moduleId = uri.getPathSegments().get(2);
				whereClause = AnnouncementsContract.MODULE_ID + "=" + moduleId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection + 
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
			
			case MODULES_ANNOUNCEMENTS_ID:
				moduleId = uri.getPathSegments().get(2);
				whereClause = AnnouncementsContract.MODULE_ID + "=" + moduleId +
						" AND " + AnnouncementsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case MODULES_GRADEBOOKS:
				moduleId = uri.getPathSegments().get(2);
				whereClause = GradebooksContract.MODULE_ID + "=" + moduleId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection + 
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOKS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
			
			case MODULES_GRADEBOOKS_ID:
				moduleId = uri.getPathSegments().get(2);
				whereClause = GradebooksContract.MODULE_ID + "=" + moduleId +
						" AND " + GradebooksContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOKS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case MODULES_GRADEBOOK_ITEMS:
				moduleId = uri.getPathSegments().get(2);
				whereClause = GradebookItemsContract.MODULE_ID + "=" + moduleId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case MODULES_GRADEBOOK_ITEMS_ID:
				moduleId = uri.getPathSegments().get(2);
				whereClause = GradebookItemsContract.MODULE_ID + "=" + moduleId +
						" AND " + GradebookItemsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case MODULES_WEBCASTS:
				webcastId = uri.getPathSegments().get(2);
				whereClause = WebcastsContract.MODULE_ID + "=" + webcastId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBCASTS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
			
			case MODULES_WEBCASTS_ID:
				webcastId = uri.getPathSegments().get(2);
				whereClause = WebcastsContract.MODULE_ID + "=" + moduleId +
						" AND " + WebcastsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBCASTS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
				
			case MODULES_WEBLINKS:
				weblinkId = uri.getPathSegments().get(2);
				whereClause = WeblinksContract.MODULE_ID + "=" + weblinkId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBLINKS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
			
			case MODULES_WEBLINKS_ID:
				weblinkId = uri.getPathSegments().get(2);
				whereClause = WeblinksContract.MODULE_ID + "=" + moduleId +
						" AND " + WeblinksContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBLINKS_TABLE_NAME, 
						whereClause, selectionArgs);
				break;
				
			case MODULES_WORKBINS:
				workbinId = uri.getPathSegments().get(2);
				whereClause = WorkbinsContract.MODULE_ID + "=" + workbinId +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WORKBINS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case MODULES_WORKBINS_ID:
				workbinId = uri.getPathSegments().get(2);
				whereClause = WorkbinsContract.MODULE_ID + "=" + workbinId +
						" AND " + WorkbinsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WORKBINS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case ANNOUNCEMENTS_ID:
				whereClause = AnnouncementsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case ANNOUNCEMENTS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all announcements");
				}
				ret = mDatabase.delete(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME, 
						selection, selectionArgs);
				break;
				
			case GRADEBOOKS_ID:
				whereClause = GradebooksContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOKS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
				
			case GRADEBOOKS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all gradebooks");
				}
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOKS_TABLE_NAME, 
						selection, selectionArgs);
				break;
			
			case GRADEBOOK_ITEMS_ID:
				whereClause = GradebookItemsContract.ID + "=" + 
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case GRADEBOOK_ITEMS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all gradebook items");
				}
				ret = mDatabase.delete(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME,
						selection, selectionArgs);
				break;
			
			case USERS_ID:
				whereClause = UsersContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.USERS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case USERS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all users");
				}
				ret = mDatabase.delete(DatabaseHelper.USERS_TABLE_NAME,
						selection, selectionArgs);
				break;
				
			case WEBCASTS_ID:
				whereClause = WebcastsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBCASTS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case WEBCASTS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all webcasts");
				}
				ret = mDatabase.delete(DatabaseHelper.WEBCASTS_TABLE_NAME,
						selection, selectionArgs);
				break;
				
			case WEBLINKS_ID:
				whereClause = WeblinksContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WEBLINKS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case WEBLINKS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all weblinks");
				}
				ret = mDatabase.delete(DatabaseHelper.WEBLINKS_TABLE_NAME,
						selection, selectionArgs);
				break;
			
			case WORKBINS_ID:
				whereClause = WorkbinsContract.ID + "=" +
						uri.getLastPathSegment() +
						(!TextUtils.isEmpty(selection) ? " AND (" + selection +
						")" : "");
				ret = mDatabase.delete(DatabaseHelper.WORKBINS_TABLE_NAME,
						whereClause, selectionArgs);
				break;
			
			case WORKBINS:
				if (selection == null && selectionArgs == null) {
					Log.d(TAG, "Removing all workbins");
				}
				ret = mDatabase.delete(DatabaseHelper.WORKBINS_TABLE_NAME,
						selection, selectionArgs);
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
		
		// Build query based on URI.
		String selection = null;
		String announcementId = null;
		String gradebookId = null;
		String gradebookItemId = null;
		String moduleId = null;
		String userId = null;
		String webcastId = null;
		String weblinkId = null;
		String workbinId = null;
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
				moduleId = uri.getPathSegments().get(2);
				announcementId = uri.getLastPathSegment();
				selection = AnnouncementsContract.MODULE_ID + " = " + moduleId +
						" AND " + AnnouncementsContract.ID + " = " + announcementId;
				this.update(uri, values, selection, null);
				break;
				
			case MODULES_GRADEBOOKS_ID:
				moduleId = uri.getPathSegments().get(2);
				gradebookId = uri.getLastPathSegment();
				selection = GradebooksContract.MODULE_ID + " = " + moduleId +
						" AND " + GradebooksContract.ID + " = " + gradebookId;
				this.update(uri, values, selection, null);
				break;
				
			case MODULES_GRADEBOOK_ITEMS_ID:
				moduleId = uri.getPathSegments().get(2);
				gradebookItemId = uri.getLastPathSegment();
				selection = GradebookItemsContract.MODULE_ID + "=" + moduleId +
						" AND " + GradebookItemsContract.ID + " = " + gradebookItemId;
				this.update(uri, values, selection, null);
				
			case MODULES_WEBCASTS_ID:
				moduleId = uri.getPathSegments().get(2);
				webcastId = uri.getLastPathSegment();
				selection = WebcastsContract.MODULE_ID + " = " + moduleId +
						" AND " + WebcastsContract.ID + " = " + webcastId;
				this.update(uri, values, selection, null);
				break;
				
			case MODULES_WEBLINKS_ID:
				moduleId = uri.getPathSegments().get(2);
				weblinkId = uri.getLastPathSegment();
				selection = WeblinksContract.MODULE_ID + " = " + moduleId +
						" AND " + WeblinksContract.ID + " = " + weblinkId;
				this.update(uri, values, selection, null);
				break;
			
			case MODULES_WORKBINS_ID:
				moduleId = uri.getPathSegments().get(2);
				workbinId = uri.getLastPathSegment();
				selection = WorkbinsContract.MODULE_ID + " = " + moduleId +
						" AND " + WorkbinsContract.ID + " = " + workbinId;
				this.update(uri, values, selection, null);
				break;
				
			case ANNOUNCEMENTS:
				rowId = mDatabase.insert(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
				
			case ANNOUNCEMENTS_ID:
				announcementId = uri.getLastPathSegment();
				selection = AnnouncementsContract.ID + " = " + announcementId;
				this.update(uri, values, selection, null);
				break;
				
			case GRADEBOOKS:
				rowId = mDatabase.insert(DatabaseHelper.GRADEBOOKS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
				
			case GRADEBOOKS_ID:
				gradebookId = uri.getLastPathSegment();
				selection = GradebooksContract.ID + " = " + gradebookId;
				this.update(uri, values, selection, null);
				break;
			
			case GRADEBOOK_ITEMS:
				rowId = mDatabase.insert(DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case GRADEBOOK_ITEMS_ID:
				gradebookItemId = uri.getLastPathSegment();
				selection = GradebookItemsContract.ID + "=" + gradebookItemId;
				this.update(uri, values, selection, null);
				break;
			
			case USERS:
				rowId = mDatabase.insert(DatabaseHelper.USERS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case USERS_ID:
				userId = uri.getLastPathSegment();
				selection = UsersContract.ID + " = " + userId;
				this.update(uri, values, selection, null);
				break;
				
			case WEBCASTS:
				rowId = mDatabase.insert(DatabaseHelper.WEBCASTS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case WEBCASTS_ID:
				webcastId = uri.getLastPathSegment();
				selection = WebcastsContract.ID + " = " + webcastId;
				this.update(uri, values, selection, null);
				break;
				
			case WEBLINKS:
				rowId = mDatabase.insert(DatabaseHelper.WEBLINKS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case WEBLINKS_ID:
				weblinkId = uri.getLastPathSegment();
				selection = WeblinksContract.ID + " = " + weblinkId;
				this.update(uri, values, selection, null);
				break;
				
			case WORKBINS:
				rowId = mDatabase.insert(DatabaseHelper.WORKBINS_TABLE_NAME, null, values);
				uri = Uri.withAppendedPath(uri, Long.toString(rowId));
				break;
			
			case WORKBINS_ID:
				workbinId = uri.getLastPathSegment();
				selection = WorkbinsContract.ID + " = " + workbinId;
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

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		// Obtain a writable database if it doesn't exist yet.
		if (mDatabase == null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		
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
				queryBuilder.appendWhere(
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + 
						AnnouncementsContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_ANNOUNCEMENTS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + 
						AnnouncementsContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case MODULES_GRADEBOOKS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOKS_TABLE_NAME + "." +
						GradebooksContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_GRADEBOOKS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOKS_TABLE_NAME + "." + 
						GradebooksContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case MODULES_GRADEBOOK_ITEMS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME + "." +
						GradebookItemsContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_GRADEBOOK_ITEMS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME + "." + 
						GradebookItemsContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case MODULES_WEBCASTS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WEBCASTS_TABLE_NAME + "." +
						WebcastsContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_WEBCASTS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.WEBCASTS_TABLE_NAME + "." + 
						WebcastsContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case MODULES_WEBLINKS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WEBLINKS_TABLE_NAME + "." +
						WeblinksContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_WEBLINKS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.WEBLINKS_TABLE_NAME + "." + 
						WeblinksContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case MODULES_WORKBINS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WORKBINS_TABLE_NAME + "." +
						WorkbinsContract.ID + "=" + uri.getLastPathSegment()
				);
				
			case MODULES_WORKBINS: // Fall through
				queryBuilder.appendWhere(
						DatabaseHelper.WORKBINS_TABLE_NAME + "." + 
						WorkbinsContract.MODULE_ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case ANNOUNCEMENTS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." +
						AnnouncementsContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case GRADEBOOKS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOKS_TABLE_NAME + "." +
						GradebooksContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case GRADEBOOK_ITEMS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME + "." +
						GradebookItemsContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case USERS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.USERS_TABLE_NAME + "." +
						UsersContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
				
			case WEBCASTS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WEBCASTS_TABLE_NAME + "." +
						WebcastsContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case WEBLINKS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WEBLINKS_TABLE_NAME + "." +
						WeblinksContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case WORKBINS_ID:
				queryBuilder.appendWhere(
						DatabaseHelper.WORKBINS_TABLE_NAME + "." +
						WorkbinsContract.ID + "=" + uri.getPathSegments().get(1)
				);
				break;
			
			case MODULES:
			case ANNOUNCEMENTS:
			case GRADEBOOKS:
			case GRADEBOOK_ITEMS:
			case USERS:
			case WEBLINKS:
			case WORKBINS:
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
							DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.CREATOR + "=" +
							DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID +
						")"
				);
				
				// Set projection maps.
				projectionMap.put(ModulesContract.ID, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ID);
				projectionMap.put(ModulesContract.IVLE_ID, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.IVLE_ID);
				projectionMap.put(ModulesContract.ACCOUNT, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT);
				projectionMap.put(ModulesContract.BADGE, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.BADGE);
				projectionMap.put(ModulesContract.BADGE_ANNOUNCEMENT, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.BADGE_ANNOUNCEMENT);
				projectionMap.put(ModulesContract.COURSE_ACAD_YEAR, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_ACAD_YEAR);
				projectionMap.put(ModulesContract.COURSE_CLOSE_DATE, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_CLOSE_DATE);
				projectionMap.put(ModulesContract.COURSE_CODE, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_CODE);
				projectionMap.put(ModulesContract.COURSE_DEPARTMENT, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_DEPARTMENT);
				projectionMap.put(ModulesContract.COURSE_LEVEL, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_LEVEL);
				projectionMap.put(ModulesContract.COURSE_MC, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_MC);
				projectionMap.put(ModulesContract.COURSE_NAME, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_NAME);
				projectionMap.put(ModulesContract.COURSE_OPEN_DATE, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_OPEN_DATE);
				projectionMap.put(ModulesContract.COURSE_SEMESTER, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.COURSE_SEMESTER);
				projectionMap.put(ModulesContract.HAS_ANNOUNCEMENT_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_ANNOUNCEMENT_ITEMS);
				projectionMap.put(ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP);
				projectionMap.put(ModulesContract.HAS_CLASS_ROSTER_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_CLASS_ROSTER_ITEMS);
				projectionMap.put(ModulesContract.HAS_CONSULTATION_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_CONSULTATION_ITEMS);
				projectionMap.put(ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP);
				projectionMap.put(ModulesContract.HAS_DESCRIPTION_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_DESCRIPTION_ITEMS);
				projectionMap.put(ModulesContract.HAS_GRADEBOOK_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_GRADEBOOK_ITEMS);
				projectionMap.put(ModulesContract.HAS_GROUPS_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_GROUPS_ITEMS);
				projectionMap.put(ModulesContract.HAS_GUEST_ROSTER_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_GUEST_ROSTER_ITEMS);
				projectionMap.put(ModulesContract.HAS_LECTURER_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_LECTURER_ITEMS);
				projectionMap.put(ModulesContract.HAS_PROJECT_GROUP_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_PROJECT_GROUP_ITEMS);
				projectionMap.put(ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP);
				projectionMap.put(ModulesContract.HAS_READING_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_READING_ITEMS);
				projectionMap.put(ModulesContract.HAS_TIMETABLE_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_TIMETABLE_ITEMS);
				projectionMap.put(ModulesContract.HAS_WEBLINK_ITEMS, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.HAS_WEBLINK_ITEMS);
				projectionMap.put(ModulesContract.IS_ACTIVE, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.IS_ACTIVE);
				projectionMap.put(ModulesContract.PERMISSION, DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.PERMISSION);
				projectionMap.put("creator" + UsersContract.ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID);
				projectionMap.put("creator_" + UsersContract.IVLE_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.IVLE_ID);
				projectionMap.put("creator_" + UsersContract.ACCOUNT, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT);
				projectionMap.put("creator_" + UsersContract.ACCOUNT_TYPE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT_TYPE);
				projectionMap.put("creator_" + UsersContract.EMAIL, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.EMAIL);
				projectionMap.put("creator_" + UsersContract.NAME, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.NAME);
				projectionMap.put("creator_" + UsersContract.TITLE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.TITLE);
				projectionMap.put("creator_" + UsersContract.USER_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.USER_ID);
				queryBuilder.setProjectionMap(projectionMap);
				break;
				
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_ANNOUNCEMENTS:
			case ANNOUNCEMENTS_ID:
			case ANNOUNCEMENTS:
				// Set table names and joins.
				queryBuilder.setTables(
						DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + " JOIN " + 
						DatabaseHelper.USERS_TABLE_NAME + " ON " + "(" +
							DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.CREATOR + " = " +
							DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID +
						")"
				);
				
				// Set projection maps.
				projectionMap.put(AnnouncementsContract.ID, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ID);
				projectionMap.put(AnnouncementsContract.IVLE_ID, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.IVLE_ID);
				projectionMap.put(AnnouncementsContract.MODULE_ID, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.MODULE_ID);
				projectionMap.put(AnnouncementsContract.ACCOUNT, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT);
				projectionMap.put(AnnouncementsContract.TITLE, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.TITLE);
				projectionMap.put(AnnouncementsContract.CREATOR, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.CREATOR);
				projectionMap.put(AnnouncementsContract.DESCRIPTION, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.DESCRIPTION);
				projectionMap.put(AnnouncementsContract.CREATED_DATE, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.CREATED_DATE);
				projectionMap.put(AnnouncementsContract.EXPIRY_DATE, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.EXPIRY_DATE);
				projectionMap.put(AnnouncementsContract.URL, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.URL);
				projectionMap.put(AnnouncementsContract.IS_READ, DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.IS_READ);
				projectionMap.put("creator" + UsersContract.ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ID);
				projectionMap.put("creator_" + UsersContract.IVLE_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.IVLE_ID);
				projectionMap.put("creator_" + UsersContract.ACCOUNT, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT);
				projectionMap.put("creator_" + UsersContract.ACCOUNT_TYPE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.ACCOUNT_TYPE);
				projectionMap.put("creator_" + UsersContract.EMAIL, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.EMAIL);
				projectionMap.put("creator_" + UsersContract.NAME, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.NAME);
				projectionMap.put("creator_" + UsersContract.TITLE, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.TITLE);
				projectionMap.put("creator_" + UsersContract.USER_ID, DatabaseHelper.USERS_TABLE_NAME + "." + UsersContract.USER_ID);
				queryBuilder.setProjectionMap(projectionMap);
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
				
			default:
				throw new IllegalArgumentException();
		}
		
		// Projection map to include user information.
		Log.v(TAG, "query: " + queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null));
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
		
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
			case MODULES_ID:
			case MODULES:
				tableName = DatabaseHelper.MODULES_TABLE_NAME;
				break;
			case MODULES_ANNOUNCEMENTS_ID:
			case MODULES_ANNOUNCEMENTS:
			case ANNOUNCEMENTS_ID:
			case ANNOUNCEMENTS:
				tableName = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME;
				break;
			case MODULES_GRADEBOOKS_ID:
			case MODULES_GRADEBOOKS:
			case GRADEBOOKS_ID:
			case GRADEBOOKS:
				tableName = DatabaseHelper.GRADEBOOKS_TABLE_NAME;
				break;
			case MODULES_GRADEBOOK_ITEMS_ID:
			case MODULES_GRADEBOOK_ITEMS:
			case GRADEBOOK_ITEMS_ID:
			case GRADEBOOK_ITEMS:
				tableName = DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME;
				break;
			case USERS_ID:
			case USERS:
				tableName = DatabaseHelper.USERS_TABLE_NAME;
				break;
			case MODULES_WEBCASTS_ID:
			case MODULES_WEBCASTS:
			case WEBCASTS_ID:
			case WEBCASTS:
				tableName = DatabaseHelper.WEBCASTS_TABLE_NAME;
				break;
			case MODULES_WEBLINKS_ID:
			case MODULES_WEBLINKS:
			case WEBLINKS_ID:
			case WEBLINKS:
				tableName = DatabaseHelper.WEBLINKS_TABLE_NAME;
				break;
			case MODULES_WORKBINS_ID:
			case MODULES_WORKBINS:
			case WORKBINS_ID:
			case WORKBINS:
				tableName = DatabaseHelper.WORKBINS_TABLE_NAME;
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		int ret = mDatabase.update(tableName, values, selection, selectionArgs);
		return ret;
	}
	
	// }}}
}
