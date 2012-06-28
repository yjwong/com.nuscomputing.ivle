package com.nuscomputing.ivle;

import java.io.IOException;
import java.net.MalformedURLException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.GradebookItemsContract;
import com.nuscomputing.ivle.providers.GradebooksContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.TimetableSlotsContract;
import com.nuscomputing.ivle.providers.UsersContract;
import com.nuscomputing.ivle.providers.WebcastFilesContract;
import com.nuscomputing.ivle.providers.WebcastItemGroupsContract;
import com.nuscomputing.ivle.providers.WebcastsContract;
import com.nuscomputing.ivle.providers.WeblinksContract;
import com.nuscomputing.ivle.providers.WorkbinFilesContract;
import com.nuscomputing.ivle.providers.WorkbinFoldersContract;
import com.nuscomputing.ivle.providers.WorkbinsContract;
import com.nuscomputing.ivlelapi.Announcement;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.Gradebook;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.Timetable;
import com.nuscomputing.ivlelapi.User;
import com.nuscomputing.ivlelapi.Webcast;
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
		
		// Tell interested listeners that sync has started.
		this.setSyncInProgress(account, true);
		IVLESyncService.broadcastSyncStarted(mContext, account);
		
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
				
				// Fetch webcasts.
				Log.v(TAG, "Fetching webcasts");
				Webcast[] webcasts = module.getWebcasts();
				for (Webcast webcast : webcasts) {
					// Insert the creator into the user's table.
					Integer webcastCreatorId = null;
					if (webcast.creator.ID != null) {
						Uri webcastCreatorUri = this.insertUserIfNotExists(webcast.creator);
						webcastCreatorId = Integer.parseInt(webcastCreatorUri.getLastPathSegment());
					}
					
					// Insert webcasts.
					int webcastId = this.insertWebcast(webcast, moduleId, webcastCreatorId);
					
					// Fetch webcast item groups.
					Log.v(TAG, "Fetching webcast item groups");
					Webcast.ItemGroup[] webcastItemGroups = webcast.getItemGroups();
					for (Webcast.ItemGroup webcastItemGroup : webcastItemGroups) {
						int webcastItemGroupId = this.insertWebcastItemGroup(webcastItemGroup, moduleId, webcastId);
						
						// Fetch webcast files.
						Log.v(TAG, "Fetching webcast files");
						Webcast.File[] webcastFiles = webcastItemGroup.getFiles();
						for (Webcast.File webcastFile : webcastFiles) {
							// Insert the creator of the file into the user's table.
							Integer webcastFileCreatorId = null;
							if (webcastFile.creator.ID != null) {
								Uri webcastFileCreatorUri = this.insertUserIfNotExists(webcastFile.creator);
								webcastFileCreatorId = Integer.parseInt(webcastFileCreatorUri.getLastPathSegment());
							}
							
							// Insert webcast files.
							this.insertWebcastFile(webcastFile, moduleId, webcastItemGroupId, webcastFileCreatorId);
						}
					}
				}
				
				// Fetch weblinks.
				Log.v(TAG, "Fetching weblinks");
				Weblink[] weblinks = module.getWeblinks();
				for (Weblink weblink : weblinks) {
					// Insert weblinks.
					this.insertWeblink(weblink, moduleId);
				}
				
				// Fetch workbins.
				Log.v(TAG, "Fetching workbins");
				Workbin[] workbins = module.getWorkbins();
				for (Workbin workbin : workbins) {
					int workbinId = this.insertWorkbin(workbin, moduleId);
					
					// Fetch workbin folders.
					Log.v(TAG, "Fetching workbin folders");
					Workbin.Folder[] workbinFolders = workbin.getFolders();
					for (Workbin.Folder workbinFolder : workbinFolders) {
						// Insert workbin folders.
						this.insertWorkbinFolder(workbinFolder, moduleId, workbinId, null);
					}
				}
			}
			
			// Fetch the user's timetable.
			Log.v(TAG, "Fetching timetable slots");
			Timetable timetable = ivle.getTimetableStudent();
			for (Timetable.Slot timetableSlot : timetable.slots) {
				Cursor cursor = mProvider.query(
					ModulesContract.CONTENT_URI,
					new String[]{ ModulesContract.ID },
					ModulesContract.IVLE_ID + " = ?",
					new String[]{ timetableSlot.courseId },
					null
				);
				
				cursor.moveToFirst();
				int moduleId = cursor.getInt(cursor.getColumnIndex(ModulesContract.ID));
				this.insertTimetableSlot(timetableSlot, moduleId);
			}
			
			Log.d(TAG, "Sync complete");
			IVLESyncService.broadcastSyncSuccess(mContext, account);
			
		} catch (Exception e) {
			// Handle any sync exceptions.
			this.handleSyncExceptions(authToken, e);
			
		} finally {
			this.setSyncInProgress(account, false);
		}
	}
	
	@Override
	public void onSyncCanceled(Thread thread) {
		IVLESyncService.broadcastSyncCanceled(mContext);
	}
	
	/**
	 * Method: isSyncInProgress
	 * <p>
	 * Returns true if a sync is in progress, false otherwise.
	 */
	public static boolean isSyncInProgress(Context context, Account account) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(IVLESyncService.KEY_SYNC_IN_PROGRESS + "_" + account.name, false);
	}
	
	/**
	 * Method: setSyncInProgress
	 * <p>
	 * Sets whether the sync is in progress or not.
	 */
	private void setSyncInProgress(Account account, boolean inProgress) {
		// Abuse of shared preferences to set sync status ):
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putBoolean(IVLESyncService.KEY_SYNC_IN_PROGRESS + "_" + account.name, inProgress);
		prefsEditor.commit();
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
	 * Method: insertTimetableSlot
	 * <p>
	 * Inserts a timetable slot into the timetable slot table.
	 */
	private int insertTimetableSlot(Timetable.Slot slot, int moduleId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues values = new ContentValues();
		values.put(TimetableSlotsContract.MODULE_ID, moduleId);
		values.put(TimetableSlotsContract.ACCOUNT, mAccount.name);
		values.put(TimetableSlotsContract.ACAD_YEAR, slot.acadYear);
		values.put(TimetableSlotsContract.SEMESTER, slot.semester);
		values.put(TimetableSlotsContract.START_TIME, slot.startTime);
		values.put(TimetableSlotsContract.END_TIME, slot.endTime);
		values.put(TimetableSlotsContract.MODULE_CODE, slot.moduleCode);
		values.put(TimetableSlotsContract.CLASS_NO, slot.classNo);
		values.put(TimetableSlotsContract.LESSON_TYPE, slot.lessonType);
		values.put(TimetableSlotsContract.VENUE, slot.venue);
		values.put(TimetableSlotsContract.DAY_CODE, slot.dayCode);
		values.put(TimetableSlotsContract.DAY_TEXT, slot.dayText);
		values.put(TimetableSlotsContract.WEEK_CODE, slot.weekCode);
		values.put(TimetableSlotsContract.WEEK_TEXT, slot.weekText);
		
		// Insert timetable slot.
		Uri uri = mProvider.insert(TimetableSlotsContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWebcast
	 * <p>
	 * Inserts a webcast into the webcast table.
	 */
	private int insertWebcast(Webcast webcast, int moduleId, int creatorId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWebcast: " + webcast.title);
		ContentValues values = new ContentValues();
		values.put(WorkbinsContract.IVLE_ID, webcast.ID);
		values.put(WorkbinsContract.MODULE_ID, moduleId);
		values.put(WorkbinsContract.ACCOUNT, mAccount.name);
		values.put(WorkbinsContract.CREATOR_ID, creatorId);
		values.put(WorkbinsContract.BADGE_TOOL, webcast.badgeTool);
		values.put(WorkbinsContract.PUBLISHED, webcast.published);
		values.put(WorkbinsContract.TITLE, webcast.title);
		
		// Insert workbins.
		Uri uri = mProvider.insert(WebcastsContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWebcastFile
	 * <p>
	 * Inserts a webcast file into the webcast file table.
	 */
	private int insertWebcastFile(Webcast.File file, int moduleId,
			int webcastItemGroupId, Integer creatorId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWebcastFile: " + file.fileTitle);
		ContentValues values = new ContentValues();
		values.put(WebcastFilesContract.IVLE_ID, file.ID);
		values.put(WebcastFilesContract.MODULE_ID, moduleId);
		values.put(WebcastFilesContract.WEBCAST_ITEM_GROUP_ID, webcastItemGroupId);
		values.put(WebcastFilesContract.ACCOUNT, mAccount.name);
		values.put(WebcastFilesContract.CREATOR_ID, creatorId);
		values.put(WebcastFilesContract.BANK_ITEM_ID, file.bankItemID);
		values.put(WebcastFilesContract.CREATE_DATE, file.createDate.toString());
		values.put(WebcastFilesContract.FILE_DESCRIPTION, file.fileDescription);
		values.put(WebcastFilesContract.FILE_NAME, file.fileName);
		values.put(WebcastFilesContract.FILE_TITLE, file.fileTitle);
		values.put(WebcastFilesContract.MP3, file.MP3);
		values.put(WebcastFilesContract.MP4, file.MP4);
		values.put(WebcastFilesContract.MEDIA_FORMAT, file.mediaFormat);
		values.put(WebcastFilesContract.IS_READ, file.isRead);
		
		// Insert webcast files.
		Uri uri = mProvider.insert(WebcastFilesContract.CONTENT_URI, values);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWebcastItemGroup
	 * <p>
	 * Inserts a webcast item group into the webcast item group table.
	 */
	private int insertWebcastItemGroup(Webcast.ItemGroup item, int moduleId, 
			int webcastId) throws RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWebcastItemGroup: " + item.itemGroupTitle);
		ContentValues values = new ContentValues();
		values.put(WebcastItemGroupsContract.IVLE_ID, item.ID);
		values.put(WebcastItemGroupsContract.MODULE_ID, moduleId);
		values.put(WebcastItemGroupsContract.WEBCAST_ID, webcastId);
		values.put(WebcastItemGroupsContract.ACCOUNT, mAccount.name);
		values.put(WebcastItemGroupsContract.ITEM_GROUP_TITLE, item.itemGroupTitle);
		
		// Insert webcast item group.
		Uri uri = mProvider.insert(WebcastItemGroupsContract.CONTENT_URI, values);
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
	 * Method: insertWorkbinFolder
	 * <p>
	 * Inserts a workbin folder into the workbin folders table.
	 */
	private int insertWorkbinFolder(Workbin.Folder folder, int moduleId,
			int workbinId, Integer workbinFolderId) throws RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWorkbinFolder: " + folder.folderName);
		ContentValues values = new ContentValues();
		values.put(WorkbinFoldersContract.IVLE_ID, folder.ID);
		values.put(WorkbinFoldersContract.MODULE_ID, moduleId);
		values.put(WorkbinFoldersContract.ACCOUNT, mAccount.name);
		values.put(WorkbinFoldersContract.WORKBIN_ID, workbinId);
		values.put(WorkbinFoldersContract.WORKBIN_FOLDER_ID, workbinFolderId);
		values.put(WorkbinFoldersContract.ALLOW_UPLOAD, folder.allowUpload);
		values.put(WorkbinFoldersContract.ALLOW_VIEW, folder.allowView);
		values.put(WorkbinFoldersContract.CLOSE_DATE, folder.closeDate.toString());
		values.put(WorkbinFoldersContract.COMMENT_OPTION, folder.commentOption.toString());
		values.put(WorkbinFoldersContract.FILE_COUNT, folder.fileCount);
		values.put(WorkbinFoldersContract.FOLDER_NAME, folder.folderName);
		values.put(WorkbinFoldersContract.ORDER, folder.order);
		values.put(WorkbinFoldersContract.OPEN_DATE, folder.openDate.toString());
		values.put(WorkbinFoldersContract.SORT_FILES_BY, folder.sortFilesBy);
		values.put(WorkbinFoldersContract.UPLOAD_DISPLAY_OPTION, folder.uploadDisplayOption);
	
		// Insert workbin folders.
		Uri uri = mProvider.insert(WorkbinFoldersContract.CONTENT_URI, values);
		int insertedWorkbinFolderId = Integer.parseInt(uri.getLastPathSegment());
		
		// Insert the files inside this folder.
		Workbin.File[] files = folder.getFiles();
		for (Workbin.File file : files) {
			Integer creatorId = null;
			if (file.creator.ID != null) {
				Uri creatorUri = this.insertUserIfNotExists(file.creator);
				creatorId = Integer.parseInt(creatorUri.getLastPathSegment());
			}
			
			Integer commenterId = null;
			if (file.commenter.ID != null) {
				Uri commenterUri = this.insertUserIfNotExists(file.commenter);
				commenterId = Integer.parseInt(commenterUri.getLastPathSegment());
			}
			
			this.insertWorkbinFile(file, moduleId, insertedWorkbinFolderId, creatorId, commenterId);
		}
		
		// Insert the subfolders.
		Workbin.Folder[] subfolders = folder.getFolders();
		for (Workbin.Folder subfolder : subfolders) {
			this.insertWorkbinFolder(subfolder, moduleId, workbinId, insertedWorkbinFolderId);
		}
		
		return insertedWorkbinFolderId;
	}
	
	/**
	 * Method: insertWorkbinFile
	 * <p>
	 * Inserts a workbin file into the workbin files table.
	 */
	private int insertWorkbinFile(Workbin.File file, int moduleId,
			int workbinFolderId, int creatorId, int commenterId) throws
			RemoteException {
		// Prepare the content values.
		Log.v(TAG, "insertWorkbinFile: " + file.fileName);
		ContentValues values = new ContentValues();
		values.put(WorkbinFilesContract.IVLE_ID, file.ID);
		values.put(WorkbinFilesContract.MODULE_ID, moduleId);
		values.put(WorkbinFilesContract.ACCOUNT, mAccount.name);
		values.put(WorkbinFilesContract.WORKBIN_FOLDER_ID, workbinFolderId);
		values.put(WorkbinFilesContract.CREATOR_ID, creatorId);
		values.put(WorkbinFilesContract.COMMENTER_ID, commenterId);
		values.put(WorkbinFilesContract.FILE_DESCRIPTION, file.fileDescription);
		values.put(WorkbinFilesContract.FILE_NAME, file.fileName);
		values.put(WorkbinFilesContract.FILE_REMARKS, file.fileRemarks);
		values.put(WorkbinFilesContract.FILE_REMARKS_ATTACHMENT, file.fileRemarksAttachment);
		values.put(WorkbinFilesContract.FILE_SIZE, file.fileSize);
		values.put(WorkbinFilesContract.FILE_TYPE, file.fileType);
		values.put(WorkbinFilesContract.IS_DOWNLOADED, file.isDownloaded);
		
		try {
			values.put(WorkbinFilesContract.DOWNLOAD_URL, file.getDownloadURL().toString());
		} catch (MalformedURLException e) {
			// Ignore the exception.
		}
		
		// Insert workbin files.
		Uri uri = mProvider.insert(WorkbinFilesContract.CONTENT_URI, values);
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
		mProvider.delete(TimetableSlotsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(UsersContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WebcastsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WebcastItemGroupsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WebcastFilesContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WeblinksContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WorkbinsContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WorkbinFoldersContract.CONTENT_URI, selection, selectionArgs);
		mProvider.delete(WorkbinFilesContract.CONTENT_URI, selection, selectionArgs);
	}
	
	// }}}
}