package com.nuscomputing.ivle;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.GradebookItemsContract;
import com.nuscomputing.ivle.providers.GradebooksContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.UsersContract;
import com.nuscomputing.ivle.providers.WeblinksContract;
import com.nuscomputing.ivle.providers.WorkbinsContract;
import com.nuscomputing.ivlelapi.Announcement;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.Gradebook;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.User;
import com.nuscomputing.ivlelapi.Workbin;
import com.nuscomputing.ivlelapi.Module.Weblink;

/**
 * The actual sync adapter implementation for announcements.
 * @author yjwong
 */
public class IVLESyncAdapter extends AbstractThreadedSyncAdapter {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLESyncAdapter";
	
	/** The account manager */
	private static AccountManager mAccountManager = null;
	
	/** The context of the sync service */
	private Context mContext;
	
	/** Account to be synced */
	private Account mAccount;
	
	/** Content provider client */
	private ContentProviderClient mProvider;
	
	/** Sync result */
	private SyncResult mSyncResult;
	
	// }}}
	// {{{ methods
	
	public IVLESyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		Log.v(TAG, "IVLESyncAdapter started");
		mContext = context;
		mAccountManager = AccountManager.get(mContext);
	}
	
	/**
	 * Method: onPerformSync
	 * <p>
	 * Performs the actual synchronisation operation.
	 * <p>
	 * This is a giant method, and each time we sync, we remove all existing
	 * content. Not the best of choice on a mobile platform, but this is to
	 * maintain data consistency at its best possible state.
	 */ 
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		// Set class members so that helper functions can use them.
		this.mAccount = account;
		this.mProvider = provider;
		this.mSyncResult = syncResult;
		
		// Obtain an IVLE object.
		Log.d(TAG, "Performing sync of IVLE data");
		String authToken = null;

		try {
			// Obtain the authentication, and get the list of modules.
			authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
			IVLE ivle = new IVLE(Constants.API_KEY, authToken);
			Module[] modules = ivle.getModules();
			Log.v(TAG, modules.length + " modules found: ");
			
			// Purge old data associated with this account.
			this.purgeAccountData();
			
			// Put those modules into the provider.
			for (Module module : modules) {
				// Insert the creator into the user's table.
				Integer moduleCreatorId = null;
				if (module.creator.ID != null) {
					Uri moduleCreatorUri = this.insertUserIfNotExists(module.creator);
					moduleCreatorId = Integer.parseInt(moduleCreatorUri.getLastPathSegment());
				}
				
				// Insert modules.
				int moduleId = this.insertModule(module, moduleCreatorId);
				
				// Fetch announcements.
				Log.v(TAG, "Fetching announcements");
				Announcement[] announcements = module.getAnnouncements();
				for (Announcement announcement : announcements) {
					// Insert the creator into the user's table.
					Integer announcementCreatorId = null;
					if (announcement.creator.ID != null) {
						Uri announcementCreatorUri = this.insertUserIfNotExists(announcement.creator);
						announcementCreatorId = Integer.parseInt(announcementCreatorUri.getLastPathSegment());
					}

					// Insert announcements.
					this.insertAnnouncement(announcement, moduleId, announcementCreatorId);
				}
				
				// Fetch gradebooks.
				Log.v(TAG, "Fetching gradebooks");
				Gradebook[] gradebooks = module.getGradebooks();
				for (Gradebook gradebook : gradebooks) {
					int gradebookId = this.insertGradebook(gradebook, moduleId);
					
					// Fetch gradebook items.
					Log.v(TAG, "Fetching gradebook items");
					Gradebook.Item[] gradebookItems = gradebook.getItems();
					for (Gradebook.Item gradebookItem : gradebookItems) {
						this.insertGradebookItem(gradebookItem, moduleId, gradebookId);
					}
				}
				
				// Fetch workbins.
				Log.v(TAG, "Fetching workbins");
				Workbin[] workbins = module.getWorkbins();
				for (Workbin workbin : workbins) {
					this.insertWorkbin(workbin, moduleId);
				}

				// Fetch weblinks.
				Log.v(TAG, "Fetching weblinks");
				Weblink[] weblinks = module.getWeblinks();
				for (Weblink weblink : weblinks) {
					this.insertWeblink(weblink, moduleId);
				}
			}
			
			Log.d(TAG, "Sync complete");
			IVLESyncService.broadcastSyncSuccess(mContext);
			
		} catch (Exception e) {
			// Handle any sync exceptions.
			this.handleSyncExceptions(authToken, e);
		}
	}
	
	@Override
	public void onSyncCanceled(Thread thread) {
		IVLESyncService.broadcastSyncCanceled(mContext);
	}
	
	/**
	 * Method: handleSyncExceptions
	 * <p>
	 * Handles synchronization exceptions.
	 */
	private void handleSyncExceptions(String authToken, Exception e) throws
		IllegalStateException {
		if (e instanceof OperationCanceledException) {
			Log.d(TAG, "Sync canceled");
			IVLESyncService.broadcastSyncCanceled(mContext);
		} else if (e instanceof AuthenticatorException || e instanceof FailedLoginException) {
			Log.d(TAG, "AuthenticatorException or FailedLoginException, refreshing authToken");
			mSyncResult.stats.numAuthExceptions++;
			mAccountManager.invalidateAuthToken(Constants.AUTHTOKEN_TYPE, authToken);
			return;
		} else if (e instanceof RemoteException) {
			Log.d(TAG, "RemoteException");
			mSyncResult.databaseError = true;
			e.printStackTrace();
		} else if (e instanceof SQLiteException) {
			Log.d(TAG, "SQLiteException");
			mSyncResult.databaseError = true;
			e.printStackTrace();
		} else if (e instanceof IOException || e instanceof NetworkErrorException) {
			Log.d(TAG, "IOException or NetworkErrorException");
			mSyncResult.stats.numIoExceptions++;
			e.printStackTrace();
		} else if (e instanceof JSONParserException) {
			Log.d(TAG, "JSONParserException");
			mSyncResult.stats.numParseExceptions++;
			e.printStackTrace();
		} else {
			// Tell the system that we have a sync failure.
			Log.d(TAG, "Unknown exception encountered");
			mSyncResult.stats.numParseExceptions++;
			e.printStackTrace();
		}
		
		// The sync failed, so broadcast our failure.
		IVLESyncService.broadcastSyncFailed(mContext);
	}
	
	/**
	 * Method: insertAnnouncement
	 * <p>
	 * Inserts an announcement into the announcement table.
	 */
	private int insertAnnouncement(Announcement announcement, int moduleId,
			int creatorId) throws RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertAnnouncement: " + announcement.title);
		ContentValues values = new ContentValues();
		values.put(AnnouncementsContract.IVLE_ID, announcement.ID);
		values.put(AnnouncementsContract.MODULE_ID, moduleId);
		values.put(AnnouncementsContract.ACCOUNT, mAccount.name);
		values.put(AnnouncementsContract.TITLE, announcement.title);
		values.put(AnnouncementsContract.CREATOR, creatorId);
		values.put(AnnouncementsContract.DESCRIPTION, announcement.description);
		values.put(AnnouncementsContract.CREATED_DATE, announcement.createdDate.toString());
		values.put(AnnouncementsContract.EXPIRY_DATE, announcement.expiryDate.toString());
		values.put(AnnouncementsContract.URL, announcement.url);
		values.put(AnnouncementsContract.IS_READ, announcement.isRead ? 1 : 0);
		
		// Insert announcements.
		Uri uri = mProvider.insert(AnnouncementsContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertGradebook
	 * <p>
	 * Inserts a gradebook into the gradebook table.
	 */
	private int insertGradebook(Gradebook gradebook, int moduleId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertGradebook: " + gradebook.categoryTitle);
		ContentValues values = new ContentValues();
		values.put(GradebooksContract.IVLE_ID, gradebook.ID);
		values.put(GradebooksContract.MODULE_ID, moduleId);
		values.put(GradebooksContract.ACCOUNT, mAccount.name);
		values.put(GradebooksContract.CATEGORY_TITLE, gradebook.categoryTitle);
		
		// Insert gradebooks.
		Uri gradebookUri = mProvider.insert(GradebooksContract.CONTENT_URI, values);
		return Integer.parseInt(gradebookUri.getLastPathSegment());
	}
	
	/**
	 * Method: insertGradebookItem
	 * <p>
	 * Inserts a gradebook item into the gradebook item table.
	 */
	private int insertGradebookItem(Gradebook.Item item, int moduleId, 
			int gradebookId) throws RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertGradebookItem: " + item.itemName);
		ContentValues values = new ContentValues();
		values.put(GradebookItemsContract.IVLE_ID, item.ID);
		values.put(GradebookItemsContract.MODULE_ID, moduleId);
		values.put(GradebookItemsContract.GRADEBOOK_ID, gradebookId);
		values.put(GradebookItemsContract.ACCOUNT, mAccount.name);
		values.put(GradebookItemsContract.AVERAGE_MEDIAN_MARKS, item.averageMedianMarks);
		values.put(GradebookItemsContract.DATE_ENTERED, item.dateEntered);
		values.put(GradebookItemsContract.HIGHEST_LOWEST_MARKS, item.highestLowestMarks);
		values.put(GradebookItemsContract.ITEM_DESCRIPTION, item.itemDescription);
		values.put(GradebookItemsContract.ITEM_NAME, item.itemName);
		values.put(GradebookItemsContract.MARKS_OBTAINED, item.marksObtained);
		values.put(GradebookItemsContract.MAX_MARKS, item.maxMarks);
		values.put(GradebookItemsContract.PERCENTILE, item.percentile);
		values.put(GradebookItemsContract.REMARK, item.remark);
		
		// Insert gradebook item.
		Uri uri = mProvider.insert(GradebookItemsContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertModule
	 * <p>
	 * Inserts a module into the module table.
	 */
	private int insertModule(Module module, Integer creatorId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, module.courseCode);
		ContentValues values = new ContentValues();
		values.put(ModulesContract.IVLE_ID, module.ID);
		values.put(ModulesContract.ACCOUNT, mAccount.name);
		values.put(ModulesContract.BADGE, module.badge);
		values.put(ModulesContract.BADGE_ANNOUNCEMENT, module.badgeAnnouncement);
		values.put(ModulesContract.COURSE_ACAD_YEAR, module.courseAcadYear);
		values.put(ModulesContract.COURSE_CLOSE_DATE, module.courseCloseDate.toString());
		values.put(ModulesContract.COURSE_CODE, module.courseCode);
		values.put(ModulesContract.COURSE_DEPARTMENT, module.courseDepartment);
		values.put(ModulesContract.COURSE_LEVEL, module.courseLevel);
		values.put(ModulesContract.COURSE_MC, module.courseMC);
		values.put(ModulesContract.COURSE_NAME, module.courseName);
		values.put(ModulesContract.COURSE_OPEN_DATE, module.courseOpenDate.toString());
		values.put(ModulesContract.COURSE_SEMESTER, module.courseSemester);
		values.put(ModulesContract.CREATOR, creatorId);
		values.put(ModulesContract.HAS_ANNOUNCEMENT_ITEMS, module.hasAnnouncementItems ? 1 : 0);
		values.put(ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP, module.hasClassGroupsForSignUp ? 1 : 0);
		values.put(ModulesContract.HAS_CLASS_ROSTER_ITEMS, module.hasClassRosterItems ? 1 : 0);
		values.put(ModulesContract.HAS_CONSULTATION_ITEMS, module.hasConsultationItems ? 1 : 0);
		values.put(ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP, module.hasConsultationSlotsForSignUp ? 1 : 0);
		values.put(ModulesContract.HAS_DESCRIPTION_ITEMS, module.hasDescriptionItems ? 1 : 0);
		values.put(ModulesContract.HAS_GRADEBOOK_ITEMS, module.hasGradebookItems ? 1 : 0);
		values.put(ModulesContract.HAS_GROUPS_ITEMS, module.hasGroupsItems ? 1 : 0);
		values.put(ModulesContract.HAS_GUEST_ROSTER_ITEMS, module.hasGuestRosterItems ? 1 : 0);
		values.put(ModulesContract.HAS_LECTURER_ITEMS, module.hasLecturerItems ? 1 : 0);
		values.put(ModulesContract.HAS_PROJECT_GROUP_ITEMS, module.hasProjectGroupItems ? 1 : 0);
		values.put(ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP, module.hasProjectGroupsForSignUp ? 1 : 0);
		values.put(ModulesContract.HAS_READING_ITEMS, module.hasReadingItems ? 1 : 0);
		values.put(ModulesContract.HAS_TIMETABLE_ITEMS, module.hasTimetableItems ? 1 : 0);
		values.put(ModulesContract.HAS_WEBLINK_ITEMS, module.hasWeblinkItems ? 1 : 0);
		values.put(ModulesContract.PERMISSION, module.permission);
		
		// Obtain the ID after insertion.
		Uri uri = mProvider.insert(ModulesContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWorkbin
	 * <p>
	 * Inserts a workbin into the workbin table.
	 */
	private int insertWorkbin(Workbin workbin, int moduleId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWorkbin: " + workbin.title);
		ContentValues values = new ContentValues();
		values.put(WorkbinsContract.IVLE_ID, workbin.ID);
		values.put(WorkbinsContract.MODULE_ID, moduleId);
		values.put(WorkbinsContract.ACCOUNT, mAccount.name);
		values.put(WorkbinsContract.CREATOR_ID, workbin.creator.ID);
		values.put(WorkbinsContract.BADGE_TOOL, workbin.badgeTool);
		values.put(WorkbinsContract.PUBLISHED, workbin.published);
		values.put(WorkbinsContract.TITLE, workbin.title);
		
		// Insert workbins.
		Uri uri = mProvider.insert(WorkbinsContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWeblink
	 * <p>
	 * Inserts a weblink into the user table.
	 */
	private int insertWeblink(Weblink weblink, int moduleId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWeblink: " + weblink.description);
		ContentValues values = new ContentValues();
		values.put(WeblinksContract.IVLE_ID, weblink.ID);
		values.put(WeblinksContract.MODULE_ID, moduleId);
		values.put(WeblinksContract.ACCOUNT, mAccount.name);
		values.put(WeblinksContract.DESCRIPTION, weblink.description);
		values.put(WeblinksContract.ORDER, weblink.order);
		values.put(WeblinksContract.RATING, weblink.rating);
		values.put(WeblinksContract.SITE_TYPE, weblink.siteType);
		values.put(WeblinksContract.URL, weblink.url.toString());
		
		Uri uri = mProvider.insert(WeblinksContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertUserIfNotExists
	 * <p>
	 * Inserts a user into the user table if the user doesn't already
	 * exist. If the user object is null, then this method returns null.
	 */
	private Uri insertUserIfNotExists(User user) throws
			RemoteException {
		// If user ID is null, then the user is probably null, and we should
		// ignore it.
		if (user.ID == null) {
			Log.d(TAG, "User was null in insertUserIfNotExists!");
			return null;
		}
		
		// Query for the user first.
		String[] projection = { UsersContract.ID };
		String selection = UsersContract.IVLE_ID + " = ?";
		String[] selectionArgs = { user.ID };
		Cursor cursor = mProvider.query(UsersContract.CONTENT_URI, projection, 
				selection, selectionArgs, null);
		
		// Prepare values to be inserted.
		ContentValues values = new ContentValues();
		values.put(UsersContract.IVLE_ID, user.ID);
		values.put(UsersContract.ACCOUNT, mAccount.name);
		values.put(UsersContract.ACCOUNT_TYPE, user.accountType);
		values.put(UsersContract.EMAIL, user.email);
		values.put(UsersContract.NAME, user.name);
		values.put(UsersContract.TITLE, user.title);
		values.put(UsersContract.USER_ID, user.userID);
		
		// Check number of users that matched.
		if (cursor.getCount() < 1) {
			return mProvider.insert(UsersContract.CONTENT_URI, values);
		} else {
			// Obtain the ID for the user we want to update.
			cursor.moveToFirst();
			int userId = cursor.getInt(0);
			return mProvider.insert(
					Uri.withAppendedPath(UsersContract.CONTENT_URI, "/" + userId),
					values
			);
		}
	}
	
	/**
	 * Method: purgeAccountData
	 * <p>
	 * Purges all data associated with the currently synced account from 
	 * the SQLite database backing the content provider.
	 */
	private void purgeAccountData() throws RemoteException {
		// We need to define the account selection.
		String selection = "account = ?";
		String selectionArgs[] = { mAccount.name };
		
		// Delete all data.
		mProvider.delete(AnnouncementsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(GradebooksContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(GradebookItemsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(ModulesContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(UsersContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WeblinksContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WorkbinsContract.CONTENT_URI, selection, selectionArgs);
	}
	
	// }}}
}