package com.nuscomputing.ivle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.Instant;
import org.joda.time.Interval;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.nuscomputing.ivle.providers.IVLEContract;
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
import com.nuscomputing.ivlelapi.IVLEObject;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.Timetable;
import com.nuscomputing.ivlelapi.User;
import com.nuscomputing.ivlelapi.Webcast;
import com.nuscomputing.ivlelapi.Weblink;
import com.nuscomputing.ivlelapi.Workbin;

/**
 * The actual sync adapter implementation for announcements.
 * @author yjwong
 */
public class IVLESyncAdapter extends AbstractThreadedSyncAdapter {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLESyncAdapter";
	
	/** The key for last sync time in SharedPreferences */
	private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
	
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
	
	/** The last sync time */
	private Instant mLastSyncTime;
	
	// }}}
	// {{{ methods
	
	public IVLESyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		Log.v(TAG, "IVLESyncAdapter started");
		mContext = context;
		mAccountManager = AccountManager.get(mContext);
	}
	
	/**
	 * Method: getLastSyncTimeKey
	 * <p>
	 * Gets the SharedPreferences key that denotes the last time this account
	 * was synced.
	 */
	private String getLastSyncTimeKey() {
		return KEY_LAST_SYNC_TIME.concat("_").concat(mAccount.name);
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
		
		// Get the duration since the last sync.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		long lastSyncTimeLong = prefs.getLong(getLastSyncTimeKey(), -1);
		if (lastSyncTimeLong != -1) {
			mLastSyncTime = new Instant(lastSyncTimeLong);
		} else {
			mLastSyncTime = null;
		}
		
		// Tell interested listeners that sync has started.
		this.setSyncInProgress(account, true);
		IVLESyncService.broadcastSyncStarted(mContext, account);
		
		// Obtain an IVLE object.
		Log.d(TAG, "Performing sync of IVLE data, seconds since last sync = " + getSecondsSinceLastSync());
		String authToken = null;
		
		// Set the last sync time.
		Editor prefsEditor = prefs.edit();
		prefsEditor.putLong(getLastSyncTimeKey(), new Instant().getMillis());
		prefsEditor.commit();

		try {
			// Obtain the authentication, and get the list of modules.
			authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
			IVLE ivle = new IVLE(Constants.API_KEY, authToken);
			Module[] modules = ivle.getModules();
			Log.v(TAG, modules.length + " modules found: ");
			
			// Put those modules into the provider.
			for (Module module : modules) {
				// Insert the creator into the user's table.
				Integer moduleCreatorId = null;
				if (module.creator.ID != null) {
					Uri moduleCreatorUri = this.insertUserIfNotExists(module.creator);
					moduleCreatorId = Integer.parseInt(moduleCreatorUri.getLastPathSegment());
				}
				
				// Insert modules.
				long moduleId = this.insertModule(module, moduleCreatorId);
				
				// Fetch announcements.
				Log.v(TAG, "Fetching announcements");
				Announcement[] announcements = module.getAnnouncements();
				this.purgeDeletedItemsFromLocal(AnnouncementsContract.class, announcements, moduleId);
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
				this.purgeDeletedItemsFromLocal(GradebooksContract.class, gradebooks, moduleId);
				for (Gradebook gradebook : gradebooks) {
					// Insert gradebooks.
					long gradebookId = this.insertGradebook(gradebook, moduleId);
					
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
				this.purgeDeletedItemsFromLocal(WebcastsContract.class, webcasts, moduleId);
				for (Webcast webcast : webcasts) {
					// Insert the creator into the user's table.
					Integer webcastCreatorId = null;
					if (webcast.creator.ID != null) {
						Uri webcastCreatorUri = this.insertUserIfNotExists(webcast.creator);
						webcastCreatorId = Integer.parseInt(webcastCreatorUri.getLastPathSegment());
					}
					
					// Insert webcasts.
					long webcastId = this.insertWebcast(webcast, moduleId, webcastCreatorId);
					
					// Fetch webcast item groups.
					Log.v(TAG, "Fetching webcast item groups");
					Webcast.ItemGroup[] webcastItemGroups = webcast.getItemGroups();
					for (Webcast.ItemGroup webcastItemGroup : webcastItemGroups) {
						long webcastItemGroupId = this.insertWebcastItemGroup(webcastItemGroup, moduleId, webcastId);
						
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
				this.purgeDeletedItemsFromLocal(WeblinksContract.class, weblinks, moduleId);
				for (Weblink weblink : weblinks) {
					// Insert weblinks.
					this.insertWeblink(weblink, moduleId);
				}
				
				// Fetch workbins.
				Log.v(TAG, "Fetching workbins");
				Workbin[] workbins = module.getWorkbins();
				this.purgeDeletedItemsFromLocal(WorkbinsContract.class, workbins, moduleId);
				for (Workbin workbin : workbins) {
					long workbinId = this.insertWorkbin(workbin, moduleId);
					
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
			mProvider.delete(
				TimetableSlotsContract.CONTENT_URI,
				DatabaseHelper.TIMETABLE_SLOTS_TABLE_NAME.concat(".").concat(TimetableSlotsContract.ACCOUNT).concat(" = ?"),
				new String[] { mAccount.name }
			);
			Timetable timetable = ivle.getTimetableStudent("2012/2013", 1);
			for (Timetable.Slot timetableSlot : timetable.slots) {
				this.insertTimetableSlot(timetableSlot);
			}
			
			// Verbose: print sync statistics.
			Log.d(TAG, "Sync is complete");
			Log.v(TAG, "Sync statistics:");
			Log.v(TAG, "Deleted " + mSyncResult.stats.numDeletes + " record(s)");
			Log.v(TAG, "Inserted " + mSyncResult.stats.numInserts + " record(s)");
			Log.v(TAG, "Updated " + mSyncResult.stats.numUpdates + " record(s)");
			
			// Send a broadcast.
			IVLESyncService.broadcastSyncSuccess(mContext, account);
			
		} catch (Exception e) {
			// Handle any sync exceptions.
			this.handleSyncExceptions(authToken, e);
			
		} finally {
			this.setSyncInProgress(account, false);
			IVLESyncService.broadcastSyncComplete(mContext, account);
		}
	}
	
	@Override
	public void onSyncCanceled(Thread thread) {
		this.setSyncInProgress(mAccount, false);
		IVLESyncService.broadcastSyncCanceled(mContext, mAccount);
	}
	
	/**
	 * Method: getSecondsSinceLastSync
	 * <p>
	 * Returns the number of seconds since the last sync was completed.
	 * <p>
	 * This method should be called for every invocation of an IVLE API call
	 * since a call can possibly take tens of seconds to complete. If not, we
	 * might miss out some important data that has been changed.
	 * 
	 * @return The duration since the last sync.
	 */
	private int getSecondsSinceLastSync() {
		// The default is 0, i.e. fetch all data.
		long changeDuration = 0;
		Instant currentTime = new Instant();
		if (mLastSyncTime != null) {
			Interval changeInterval = new Interval(mLastSyncTime, currentTime);
			changeDuration = changeInterval.toDuration().getStandardSeconds();
		}
		
		return Long.valueOf(changeDuration).intValue();
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
			IVLESyncService.broadcastSyncCanceled(mContext, mAccount);
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
		this.setSyncInProgress(mAccount, false);
		IVLESyncService.broadcastSyncFailed(mContext, mAccount);
	}
	
	/**
	 * Method: findDeletedItemsByType
	 * <p>
	 * Generic method to find IVLE items deleted between the current sync
	 * and the time of the last sync.
	 */
	private <T extends IVLEObject> Set<String> findDeletedItemsByType(
			IVLEContract contract, T[] objects,
			long moduleId) throws RemoteException {
		// Get the column names.
		Uri fieldContentUri = contract.getContentUri();
		String fieldTable = contract.getTableName();
		String fieldIvleId = contract.getColumnNameIvleId();
		String fieldModuleId = contract.getColumnNameModuleId();
		String fieldAccount = contract.getColumnNameAccount();
		
		// Get the set of old items.
		Cursor c = mProvider.query(
				fieldContentUri,
				new String[] { fieldIvleId },
				fieldTable.concat(".").concat(fieldAccount).concat(" = ? AND ") +
				fieldTable.concat(".").concat(fieldModuleId).concat(" = ?"),
				new String[] { mAccount.name, Long.toString(moduleId) },
				null);
		Set<String> oldSet = new HashSet<String>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			oldSet.add(c.getString(c.getColumnIndex(fieldIvleId)));
			c.moveToNext();
		}
		
		// Get the new set of items.
		Set<String> newSet = new HashSet<String>();
		for (T object : objects) {
			newSet.add(object.ID);
		}
		
		oldSet.removeAll(newSet);
		return oldSet;
	}
	
	/**
	 * Method: purgeDeletedItemsFromLocal
	 * <p>
	 * Removes deleted items from the local cache.
	 */
	private <T extends IVLEObject> void purgeDeletedItemsFromLocal(
			Class<? extends IVLEContract> contractClass, T[] objects,
			long moduleId) throws RemoteException {
		// Create an instance of the contract.
		try {
			IVLEContract contract = contractClass.newInstance();
			
			// Find out what has been deleted.
			Set<String> removeSet = this.findDeletedItemsByType(contract, objects, moduleId);
			Uri fieldContentUri = contract.getContentUri();
			String fieldIvleId = contract.getColumnNameIvleId();
			String fieldModuleId = contract.getColumnNameModuleId();
			String fieldAccount = contract.getColumnNameAccount();
			for (String toRemove : removeSet) {
				mSyncResult.stats.numDeletes++;
				Log.v(TAG, "purging non-existent item of type " + contract.getClass().getName() + " with ID = " + toRemove);
				mProvider.delete(
						fieldContentUri,
						fieldIvleId.concat(" = ?").concat(" AND ")
							.concat(fieldAccount).concat(" = ?").concat(" AND ")
							.concat(fieldModuleId).concat(" = ?"),
						new String[] { toRemove, mAccount.name, Long.toString(moduleId) }
				);
			}
			
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException encountered purging deleted items of type " + contractClass.getName());
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException encountered purging deleted items of type " + contractClass.getName());
		} catch (IllegalArgumentException e) {
			// Do nothing.
		}

	}
	
	/**
	 * Method: itemExists
	 * <p>
	 * Determines if an IVLE item exists. If it exists, this method returns
	 * the item ID. Otherwise, it returns -1.
	 * 
	 * @return
	 * @throws RemoteException 
	 */
	private long itemExists(Class<? extends IVLEContract> contractClass,
			String ivleId) throws RemoteException {
		// Check if the item exists.
		try {
			IVLEContract contract = contractClass.newInstance();
			Cursor c = mProvider.query(
					contract.getContentUri(),
					new String[] { contract.getColumnNameId() },
					contract.getTableName().concat(".").concat(contract.getColumnNameIvleId()).concat(" = ? AND ") +
					contract.getTableName().concat(".").concat(contract.getColumnNameAccount()).concat(" = ?"),
					new String[] { ivleId, mAccount.name }, null);
			
			if (c.getCount() > 0) {
				c.moveToFirst();
				return c.getLong(c.getColumnIndex(contract.getColumnNameId()));
			} else {
				return -1;
			}
			
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException encountered checking if item of type " + contractClass.getName() + " exists");
			return -1;
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException encountered checking if item of type " + contractClass.getName() + " exists");
			return -1;
		}
	}
	
	/**
	 * Method: insertAnnouncement
	 * <p>
	 * Inserts an announcement into the announcement table.
	 */
	private long insertAnnouncement(Announcement announcement, long moduleId,
			int creatorId) throws RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(AnnouncementsContract.IVLE_ID, announcement.ID);
		v.put(AnnouncementsContract.MODULE_ID, moduleId);
		v.put(AnnouncementsContract.ACCOUNT, mAccount.name);
		v.put(AnnouncementsContract.CREATOR_ID, creatorId);
		v.put(AnnouncementsContract.TITLE, announcement.title);
		v.put(AnnouncementsContract.DESCRIPTION, announcement.description);
		v.put(AnnouncementsContract.CREATED_DATE, announcement.createdDate.toString());
		v.put(AnnouncementsContract.EXPIRY_DATE, announcement.expiryDate.toString());
		v.put(AnnouncementsContract.URL, announcement.url);
		v.put(AnnouncementsContract.IS_READ, announcement.isRead ? 1 : 0);
		
		// Insert or update announcements.
		long id = this.itemExists(AnnouncementsContract.class, announcement.ID);
		if (id > -1) {
			Log.v(TAG, "insertAnnouncement: title = " + announcement.title + ", ID = " + announcement.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					AnnouncementsContract.CONTENT_URI, v,
					AnnouncementsContract.IVLE_ID.concat(" = ? AND ") +
					AnnouncementsContract.ACCOUNT.concat(" = ?"),
					new String[] { announcement.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertAnnouncement: title = " + announcement.title + ", ID = " + announcement.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(AnnouncementsContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertGradebook
	 * <p>
	 * Inserts a gradebook into the gradebook table.
	 */
	private long insertGradebook(Gradebook gradebook, long moduleId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(GradebooksContract.IVLE_ID, gradebook.ID);
		v.put(GradebooksContract.MODULE_ID, moduleId);
		v.put(GradebooksContract.ACCOUNT, mAccount.name);
		v.put(GradebooksContract.CATEGORY_TITLE, gradebook.categoryTitle);
		
		// Insert or update gradebooks.
		long id = this.itemExists(GradebooksContract.class, gradebook.ID);
		if (id > -1) {
			Log.v(TAG, "insertGradebook: categoryTitle = " + gradebook.categoryTitle + ", ID = " + gradebook.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					GradebooksContract.CONTENT_URI, v,
					GradebooksContract.IVLE_ID.concat(" = ? AND ") +
					GradebooksContract.ACCOUNT.concat(" = ?"),
					new String[] { gradebook.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertGradebook: categoryTitle = " + gradebook.categoryTitle + ", ID = " + gradebook.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(GradebooksContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertGradebookItem
	 * <p>
	 * Inserts a gradebook item into the gradebook item table.
	 */
	private long insertGradebookItem(Gradebook.Item item, long moduleId, 
			long gradebookId) throws RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(GradebookItemsContract.IVLE_ID, item.ID);
		v.put(GradebookItemsContract.MODULE_ID, moduleId);
		v.put(GradebookItemsContract.GRADEBOOK_ID, gradebookId);
		v.put(GradebookItemsContract.ACCOUNT, mAccount.name);
		v.put(GradebookItemsContract.AVERAGE_MEDIAN_MARKS, item.averageMedianMarks);
		v.put(GradebookItemsContract.DATE_ENTERED, item.dateEntered);
		v.put(GradebookItemsContract.HIGHEST_LOWEST_MARKS, item.highestLowestMarks);
		v.put(GradebookItemsContract.ITEM_DESCRIPTION, item.itemDescription);
		v.put(GradebookItemsContract.ITEM_NAME, item.itemName);
		v.put(GradebookItemsContract.MARKS_OBTAINED, item.marksObtained);
		v.put(GradebookItemsContract.MAX_MARKS, item.maxMarks);
		v.put(GradebookItemsContract.PERCENTILE, item.percentile);
		v.put(GradebookItemsContract.REMARK, item.remark);
		
		// Insert or update gradebook items.
		long id = this.itemExists(GradebookItemsContract.class, item.ID);
		if (id > -1) {
			Log.v(TAG, "insertGradebookItem: itemName = " + item.itemName + ", ID = " + item.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					GradebookItemsContract.CONTENT_URI, v,
					GradebookItemsContract.IVLE_ID.concat(" = ? AND ") +
					GradebookItemsContract.ACCOUNT.concat(" = ? AND ") +
					GradebookItemsContract.GRADEBOOK_ID.concat(" = ?"),
					new String[] { item.ID, mAccount.name, Long.toString(gradebookId) }
			);
			return id;
		} else {
			Log.v(TAG, "insertGradebookItem: itemName = " + item.itemName + ", ID = " + item.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(GradebookItemsContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertModule
	 * <p>
	 * Inserts a module into the module table.
	 */
	private long insertModule(Module module, Integer creatorId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(ModulesContract.IVLE_ID, module.ID);
		v.put(ModulesContract.ACCOUNT, mAccount.name);
		v.put(ModulesContract.BADGE, module.badge);
		v.put(ModulesContract.BADGE_ANNOUNCEMENT, module.badgeAnnouncement);
		v.put(ModulesContract.COURSE_ACAD_YEAR, module.courseAcadYear);
		v.put(ModulesContract.COURSE_CLOSE_DATE, module.courseCloseDate.toString());
		v.put(ModulesContract.COURSE_CODE, module.courseCode);
		v.put(ModulesContract.COURSE_DEPARTMENT, module.courseDepartment);
		v.put(ModulesContract.COURSE_LEVEL, module.courseLevel);
		v.put(ModulesContract.COURSE_MC, module.courseMC);
		v.put(ModulesContract.COURSE_NAME, module.courseName);
		v.put(ModulesContract.COURSE_OPEN_DATE, module.courseOpenDate.toString());
		v.put(ModulesContract.COURSE_SEMESTER, module.courseSemester);
		v.put(ModulesContract.CREATOR_ID, creatorId);
		v.put(ModulesContract.HAS_ANNOUNCEMENT_ITEMS, module.hasAnnouncementItems ? 1 : 0);
		v.put(ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP, module.hasClassGroupsForSignUp ? 1 : 0);
		v.put(ModulesContract.HAS_CLASS_ROSTER_ITEMS, module.hasClassRosterItems ? 1 : 0);
		v.put(ModulesContract.HAS_CONSULTATION_ITEMS, module.hasConsultationItems ? 1 : 0);
		v.put(ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP, module.hasConsultationSlotsForSignUp ? 1 : 0);
		v.put(ModulesContract.HAS_DESCRIPTION_ITEMS, module.hasDescriptionItems ? 1 : 0);
		v.put(ModulesContract.HAS_GRADEBOOK_ITEMS, module.hasGradebookItems ? 1 : 0);
		v.put(ModulesContract.HAS_GROUPS_ITEMS, module.hasGroupsItems ? 1 : 0);
		v.put(ModulesContract.HAS_GUEST_ROSTER_ITEMS, module.hasGuestRosterItems ? 1 : 0);
		v.put(ModulesContract.HAS_LECTURER_ITEMS, module.hasLecturerItems ? 1 : 0);
		v.put(ModulesContract.HAS_PROJECT_GROUP_ITEMS, module.hasProjectGroupItems ? 1 : 0);
		v.put(ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP, module.hasProjectGroupsForSignUp ? 1 : 0);
		v.put(ModulesContract.HAS_READING_ITEMS, module.hasReadingItems ? 1 : 0);
		v.put(ModulesContract.HAS_TIMETABLE_ITEMS, module.hasTimetableItems ? 1 : 0);
		v.put(ModulesContract.HAS_WEBLINK_ITEMS, module.hasWeblinkItems ? 1 : 0);
		v.put(ModulesContract.PERMISSION, module.permission);
		
		// Insert or update gradebook items.
		long id = this.itemExists(ModulesContract.class, module.ID);
		if (id > -1) {
			Log.v(TAG, "insertModule: courseName = " + module.courseName + ", ID = " + module.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					ModulesContract.CONTENT_URI, v,
					ModulesContract.IVLE_ID.concat(" = ? AND ") +
					ModulesContract.ACCOUNT.concat(" = ?"),
					new String[] { module.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertModule: courseName = " + module.courseName + ", ID = " + module.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(ModulesContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertTimetableSlot
	 * <p>
	 * Inserts a timetable slot into the timetable slot table.
	 */
	private int insertTimetableSlot(Timetable.Slot slot) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(TimetableSlotsContract.ACCOUNT, mAccount.name);
		v.put(TimetableSlotsContract.ACAD_YEAR, slot.acadYear);
		v.put(TimetableSlotsContract.SEMESTER, slot.semester);
		v.put(TimetableSlotsContract.START_TIME, slot.startTime);
		v.put(TimetableSlotsContract.END_TIME, slot.endTime);
		v.put(TimetableSlotsContract.MODULE_CODE, slot.moduleCode);
		v.put(TimetableSlotsContract.CLASS_NO, slot.classNo);
		v.put(TimetableSlotsContract.LESSON_TYPE, slot.lessonType);
		v.put(TimetableSlotsContract.VENUE, slot.venue);
		v.put(TimetableSlotsContract.DAY_CODE, slot.dayCode);
		v.put(TimetableSlotsContract.DAY_TEXT, slot.dayText);
		v.put(TimetableSlotsContract.WEEK_CODE, slot.weekCode);
		v.put(TimetableSlotsContract.WEEK_TEXT, slot.weekText);
		
		// Insert timetable slot.
		Uri uri = mProvider.insert(TimetableSlotsContract.CONTENT_URI, v);
		return Integer.parseInt(uri.getLastPathSegment());
	}
	
	/**
	 * Method: insertWebcast
	 * <p>
	 * Inserts a webcast into the webcast table.
	 */
	private long insertWebcast(Webcast webcast, long moduleId, int creatorId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WebcastsContract.IVLE_ID, webcast.ID);
		v.put(WebcastsContract.MODULE_ID, moduleId);
		v.put(WebcastsContract.ACCOUNT, mAccount.name);
		v.put(WebcastsContract.CREATOR_ID, creatorId);
		v.put(WebcastsContract.BADGE_TOOL, webcast.badgeTool);
		v.put(WebcastsContract.PUBLISHED, webcast.published);
		v.put(WebcastsContract.TITLE, webcast.title);
		
		// Insert or update webcast items.
		long id = this.itemExists(WebcastsContract.class, webcast.ID);
		if (id > -1) {
			Log.v(TAG, "insertWebcast: title = " + webcast.title + ", ID = " + webcast.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					WebcastsContract.CONTENT_URI, v,
					WebcastsContract.IVLE_ID.concat(" = ? AND ") +
					WebcastsContract.ACCOUNT.concat(" = ?"),
					new String[] { webcast.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertWebcast: title = " + webcast.title + ", ID = " + webcast.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WebcastsContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertWebcastFile
	 * <p>
	 * Inserts a webcast file into the webcast file table.
	 */
	private long insertWebcastFile(Webcast.File file, long moduleId,
			long webcastItemGroupId, Integer creatorId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WebcastFilesContract.IVLE_ID, file.ID);
		v.put(WebcastFilesContract.MODULE_ID, moduleId);
		v.put(WebcastFilesContract.WEBCAST_ITEM_GROUP_ID, webcastItemGroupId);
		v.put(WebcastFilesContract.ACCOUNT, mAccount.name);
		v.put(WebcastFilesContract.CREATOR_ID, creatorId);
		v.put(WebcastFilesContract.BANK_ITEM_ID, file.bankItemID);
		v.put(WebcastFilesContract.CREATE_DATE, file.createDate.toString());
		v.put(WebcastFilesContract.FILE_DESCRIPTION, file.fileDescription);
		v.put(WebcastFilesContract.FILE_NAME, file.fileName);
		v.put(WebcastFilesContract.FILE_TITLE, file.fileTitle);
		v.put(WebcastFilesContract.MP3, file.MP3);
		v.put(WebcastFilesContract.MP4, file.MP4);
		v.put(WebcastFilesContract.MEDIA_FORMAT, file.mediaFormat);
		v.put(WebcastFilesContract.IS_READ, file.isRead);
		
		// Insert or update webcast file items.
		long id = this.itemExists(WebcastFilesContract.class, file.ID);
		if (id > -1) {
			Log.v(TAG, "insertWebcastFile: fileTitle = " + file.fileTitle + ", ID = " + file.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					WebcastFilesContract.CONTENT_URI, v,
					WebcastFilesContract.IVLE_ID.concat(" = ? AND ") +
					WebcastFilesContract.ACCOUNT.concat(" = ? AND ") +
					WebcastFilesContract.WEBCAST_ITEM_GROUP_ID.concat(" = ?"),
					new String[] { file.ID, mAccount.name, Long.toString(webcastItemGroupId) }
			);
			return id;
		} else {
			Log.v(TAG, "insertWebcastFile: fileTitle = " + file.fileTitle + ", ID = " + file.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WebcastFilesContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertWebcastItemGroup
	 * <p>
	 * Inserts a webcast item group into the webcast item group table.
	 */
	private long insertWebcastItemGroup(Webcast.ItemGroup group, long moduleId, 
			long webcastId) throws RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WebcastItemGroupsContract.IVLE_ID, group.ID);
		v.put(WebcastItemGroupsContract.MODULE_ID, moduleId);
		v.put(WebcastItemGroupsContract.WEBCAST_ID, webcastId);
		v.put(WebcastItemGroupsContract.ACCOUNT, mAccount.name);
		v.put(WebcastItemGroupsContract.ITEM_GROUP_TITLE, group.itemGroupTitle);
		
		// Webcast item groups don't have IDs, so we cannot identify whether
		// to update or insert.
		mProvider.delete(
				WebcastItemGroupsContract.CONTENT_URI,
				WebcastItemGroupsContract.ACCOUNT.concat(" = ? AND ") +
				WebcastItemGroupsContract.WEBCAST_ID.concat(" = ?"),
				new String[] { mAccount.name, Long.toString(webcastId) });
		Uri uri = mProvider.insert(WebcastItemGroupsContract.CONTENT_URI, v);
		return ContentUris.parseId(uri);
	}
	
	/**
	 * Method: insertWeblink
	 * <p>
	 * Inserts a weblink into the user table.
	 */
	private long insertWeblink(Weblink weblink, long moduleId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WeblinksContract.IVLE_ID, weblink.ID);
		v.put(WeblinksContract.MODULE_ID, moduleId);
		v.put(WeblinksContract.ACCOUNT, mAccount.name);
		v.put(WeblinksContract.DESCRIPTION, weblink.description);
		v.put(WeblinksContract.ORDER, weblink.order);
		v.put(WeblinksContract.RATING, weblink.rating);
		v.put(WeblinksContract.SITE_TYPE, weblink.siteType);
		v.put(WeblinksContract.URL, weblink.url.toString());
		
		// Insert or update weblink items.
		long id = this.itemExists(WeblinksContract.class, weblink.ID);
		if (id > -1) {
			Log.v(TAG, "insertWeblink: description = " + weblink.description + ", ID = " + weblink.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					WeblinksContract.CONTENT_URI, v,
					WeblinksContract.IVLE_ID.concat(" = ? AND ") +
					WeblinksContract.ACCOUNT.concat(" = ?"),
					new String[] { weblink.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertWeblink: description = " + weblink.description + ", ID = " + weblink.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WeblinksContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertWorkbin
	 * <p>
	 * Inserts a workbin into the workbin table.
	 */
	private long insertWorkbin(Workbin workbin, long moduleId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WorkbinsContract.IVLE_ID, workbin.ID);
		v.put(WorkbinsContract.MODULE_ID, moduleId);
		v.put(WorkbinsContract.ACCOUNT, mAccount.name);
		v.put(WorkbinsContract.CREATOR_ID, workbin.creator.ID);
		v.put(WorkbinsContract.BADGE_TOOL, workbin.badgeTool);
		v.put(WorkbinsContract.PUBLISHED, workbin.published);
		v.put(WorkbinsContract.TITLE, workbin.title);
		
		// Insert or update workbin items.
		long id = this.itemExists(WorkbinsContract.class, workbin.ID);
		if (id > -1) {
			Log.v(TAG, "insertWorkbin: title = " + workbin.title + ", ID = " + workbin.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					WorkbinsContract.CONTENT_URI, v,
					WorkbinsContract.IVLE_ID.concat(" = ? AND ") +
					WorkbinsContract.ACCOUNT.concat(" = ?"),
					new String[] { workbin.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertWorkbin: title = " + workbin.title + ", ID = " + workbin.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WorkbinsContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
	}
	
	/**
	 * Method: insertWorkbinFolder
	 * <p>
	 * Inserts a workbin folder into the workbin folders table.
	 */
	private long insertWorkbinFolder(Workbin.Folder folder, long moduleId,
			long workbinId, Long workbinFolderId) throws RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WorkbinFoldersContract.IVLE_ID, folder.ID);
		v.put(WorkbinFoldersContract.MODULE_ID, moduleId);
		v.put(WorkbinFoldersContract.ACCOUNT, mAccount.name);
		v.put(WorkbinFoldersContract.WORKBIN_ID, workbinId);
		v.put(WorkbinFoldersContract.WORKBIN_FOLDER_ID, workbinFolderId);
		v.put(WorkbinFoldersContract.ALLOW_UPLOAD, folder.allowUpload);
		v.put(WorkbinFoldersContract.ALLOW_VIEW, folder.allowView);
		v.put(WorkbinFoldersContract.CLOSE_DATE, folder.closeDate.toString());
		v.put(WorkbinFoldersContract.COMMENT_OPTION, folder.commentOption.toString());
		v.put(WorkbinFoldersContract.FILE_COUNT, folder.fileCount);
		v.put(WorkbinFoldersContract.FOLDER_NAME, folder.folderName);
		v.put(WorkbinFoldersContract.ORDER, folder.order);
		v.put(WorkbinFoldersContract.OPEN_DATE, folder.openDate.toString());
		v.put(WorkbinFoldersContract.SORT_FILES_BY, folder.sortFilesBy);
		v.put(WorkbinFoldersContract.UPLOAD_DISPLAY_OPTION, folder.uploadDisplayOption);
		
		// Insert or update workbin folders.
		long id = this.itemExists(WorkbinFoldersContract.class, folder.ID);
		long insertedId;
		if (id > -1) {
			Log.v(TAG, "insertWorkbinFolder: folderName = " + folder.folderName + ", ID = " + folder.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			
			// The workbin folder ID can be null.
			if (workbinFolderId == null) {
				mProvider.update(
						WorkbinFoldersContract.CONTENT_URI, v,
						WorkbinFoldersContract.IVLE_ID.concat(" = ? AND ") +
						WorkbinFoldersContract.ACCOUNT.concat(" = ? AND ") +
						WorkbinFoldersContract.WORKBIN_ID.concat(" = ?"),
						new String[] { folder.ID, mAccount.name, Long.toString(workbinId) }
				);
			} else {
				mProvider.update(
						WorkbinFoldersContract.CONTENT_URI, v,
						WorkbinFoldersContract.IVLE_ID.concat(" = ? AND ") +
						WorkbinFoldersContract.ACCOUNT.concat(" = ? AND ") +
						WorkbinFoldersContract.WORKBIN_ID.concat(" = ? AND ") +
						WorkbinFoldersContract.WORKBIN_FOLDER_ID.concat(" = ?"),
						new String[] { folder.ID, mAccount.name, Long.toString(workbinId), Long.toString(workbinFolderId) }
				);
			}
			
			insertedId = id;
		} else {
			Log.v(TAG, "insertWorkbinFolder: folderName = " + folder.folderName + ", ID = " + folder.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WorkbinFoldersContract.CONTENT_URI, v);
			insertedId = ContentUris.parseId(uri);
		}
		
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
			
			this.insertWorkbinFile(file, moduleId, insertedId, creatorId, commenterId);
		}
		
		// Insert the subfolders.
		Workbin.Folder[] subfolders = folder.getFolders();
		for (Workbin.Folder subfolder : subfolders) {
			this.insertWorkbinFolder(subfolder, moduleId, workbinId, insertedId);
		}
		
		return insertedId;
	}
	
	/**
	 * Method: insertWorkbinFile
	 * <p>
	 * Inserts a workbin file into the workbin files table.
	 */
	private long insertWorkbinFile(Workbin.File file, long moduleId,
			long workbinFolderId, int creatorId, int commenterId) throws
			RemoteException {
		// Prepare the content values.
		ContentValues v = new ContentValues();
		v.put(WorkbinFilesContract.IVLE_ID, file.ID);
		v.put(WorkbinFilesContract.MODULE_ID, moduleId);
		v.put(WorkbinFilesContract.ACCOUNT, mAccount.name);
		v.put(WorkbinFilesContract.WORKBIN_FOLDER_ID, workbinFolderId);
		v.put(WorkbinFilesContract.CREATOR_ID, creatorId);
		v.put(WorkbinFilesContract.COMMENTER_ID, commenterId);
		v.put(WorkbinFilesContract.FILE_DESCRIPTION, file.fileDescription);
		v.put(WorkbinFilesContract.FILE_NAME, file.fileName);
		v.put(WorkbinFilesContract.FILE_REMARKS, file.fileRemarks);
		v.put(WorkbinFilesContract.FILE_REMARKS_ATTACHMENT, file.fileRemarksAttachment);
		v.put(WorkbinFilesContract.FILE_SIZE, file.fileSize);
		v.put(WorkbinFilesContract.FILE_TYPE, file.fileType);
		v.put(WorkbinFilesContract.IS_DOWNLOADED, file.isDownloaded);
		
		try {
			v.put(WorkbinFilesContract.DOWNLOAD_URL, file.getDownloadURL().toString());
		} catch (MalformedURLException e) {
			// Ignore the exception.
			Log.w(TAG, "MalformedURLException inserting download URL for workbin file " + file.ID);
		}
		
		// Insert or update workbin file items.
		long id = this.itemExists(WorkbinFilesContract.class, file.ID);
		if (id > -1) {
			Log.v(TAG, "insertWorkbinFile: fileName = " + file.fileName + ", ID = " + file.ID + " (update)");
			mSyncResult.stats.numUpdates++;
			mProvider.update(
					WorkbinFilesContract.CONTENT_URI, v,
					WorkbinFilesContract.IVLE_ID.concat(" = ? AND ") +
					WorkbinFilesContract.ACCOUNT.concat(" = ?"),
					new String[] { file.ID, mAccount.name }
			);
			return id;
		} else {
			Log.v(TAG, "insertWorkbinFile: fileName = " + file.fileName + ", ID = " + file.ID);
			mSyncResult.stats.numInserts++;
			Uri uri = mProvider.insert(WorkbinFilesContract.CONTENT_URI, v);
			return ContentUris.parseId(uri);
		}
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
		Cursor c = mProvider.query(UsersContract.CONTENT_URI, projection, 
				selection, selectionArgs, null);
		
		// Prepare values to be inserted.
		ContentValues v = new ContentValues();
		v.put(UsersContract.IVLE_ID, user.ID);
		v.put(UsersContract.ACCOUNT, mAccount.name);
		v.put(UsersContract.ACCOUNT_TYPE, user.accountType);
		v.put(UsersContract.EMAIL, user.email);
		v.put(UsersContract.NAME, user.name);
		v.put(UsersContract.TITLE, user.title);
		v.put(UsersContract.USER_ID, user.userID);
		
		// Check number of users that matched.
		if (c.getCount() < 1) {
			mSyncResult.stats.numInserts++;
			return mProvider.insert(UsersContract.CONTENT_URI, v);
		} else {
			// Obtain the ID for the user we want to update.
			c.moveToFirst();
			int userId = c.getInt(0);
			mSyncResult.stats.numUpdates++;
			return mProvider.insert(
					Uri.withAppendedPath(UsersContract.CONTENT_URI, "/" + userId),
					v
			);
		}
	}
	
	// }}}
}