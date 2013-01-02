package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.GradebookItemsContract;
import com.nuscomputing.ivle.providers.GradebooksContract;
import com.nuscomputing.ivle.providers.DescriptionsContract;
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

import android.accounts.Account;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Provides a storage for the IVLE app.
 * Used to store accounts and various miscellaneous IVLE-related data.
 * @author yjwong
 */

public class DatabaseHelper extends SQLiteOpenHelper {
	// {{{ properties
	
	/** Version of the database schema */
	private static final int DATABASE_VERSION = 19;
	
	/** Name of this database */
	private static final String DATABASE_NAME = "ivle";
	
	/** The context */
	private Context mContext;
	
	/** Data for announcements data table */
	public static final String ANNOUNCEMENTS_TABLE_NAME = "announcements";
	private static final String ANNOUNCEMENTS_TABLE_CREATE =
			"CREATE TABLE " + ANNOUNCEMENTS_TABLE_NAME + "(" +
			AnnouncementsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			AnnouncementsContract.IVLE_ID + " TEXT, " +
			AnnouncementsContract.MODULE_ID + " TEXT, " +
			AnnouncementsContract.CREATOR_ID + " TEXT, " +
			AnnouncementsContract.ACCOUNT + " TEXT, " +
			AnnouncementsContract.TITLE + " TEXT, " +
			AnnouncementsContract.DESCRIPTION + " TEXT, " +
			AnnouncementsContract._DESCRIPTION_NOHTML + " TEXT, " +
			AnnouncementsContract.CREATED_DATE + " DATETIME, " +
			AnnouncementsContract.EXPIRY_DATE + " DATETIME, " +
			AnnouncementsContract.URL + " TEXT, " +
			AnnouncementsContract.IS_READ + " BOOLEAN" +
			");";
	
	/** Data for modules data table */
	public static final String MODULES_TABLE_NAME = "modules";
	private static final String MODULES_TABLE_CREATE =
			"CREATE TABLE " + MODULES_TABLE_NAME + "(" +
			ModulesContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			ModulesContract.IVLE_ID + " TEXT, " +
			ModulesContract.ACCOUNT + " TEXT, " +
			ModulesContract.CREATOR_ID + " TEXT, " +
			ModulesContract.BADGE + " INTEGER, " +
			ModulesContract.BADGE_ANNOUNCEMENT + " INTEGER, " +
			ModulesContract.COURSE_ACAD_YEAR + " TEXT, " +
			ModulesContract.COURSE_CLOSE_DATE + " DATETIME," +
			ModulesContract.COURSE_CODE + " TEXT, " +
			ModulesContract.COURSE_DEPARTMENT + " TEXT, " +
			ModulesContract.COURSE_LEVEL + " INTEGER, " +
			ModulesContract.COURSE_MC + " INTEGER, " +
			ModulesContract.COURSE_NAME + " TEXT, " +
			ModulesContract.COURSE_OPEN_DATE + " DATETIME, " +
			ModulesContract.COURSE_SEMESTER + " TEXT, " +
			ModulesContract.HAS_ANNOUNCEMENT_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP + " BOOLEAN, " +
			ModulesContract.HAS_CLASS_ROSTER_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_CONSULTATION_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP + " BOOLEAN, " + 
			ModulesContract.HAS_DESCRIPTION_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_GRADEBOOK_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_GROUPS_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_GUEST_ROSTER_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_LECTURER_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_PROJECT_GROUP_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP + " BOOLEAN, " +
			ModulesContract.HAS_READING_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_TIMETABLE_ITEMS + " BOOLEAN, " +
			ModulesContract.HAS_WEBLINK_ITEMS + " BOOLEAN, " +
			ModulesContract.IS_ACTIVE + " BOOLEAN, " + 
			ModulesContract.PERMISSION + " TEXT" +
			");";
	
	/** Data for module descriptions data table */
	public static final String DESCRIPTIONS_TABLE_NAME = "descriptions";
	private static final String DESCRIPTIONS_TABLE_CREATE =
			"CREATE TABLE " + DESCRIPTIONS_TABLE_NAME + "(" +
			DescriptionsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			DescriptionsContract.MODULE_ID + " TEXT, " +
			DescriptionsContract.ACCOUNT + " TEXT, " +
			DescriptionsContract.TITLE + " TEXT, " +
			DescriptionsContract.DESCRIPTION + " TEXT, " +
			DescriptionsContract.ORDER + " INTEGER" +
			");";
	
	/** Data for gradebooks data table */
	public static final String GRADEBOOKS_TABLE_NAME = "gradebook";
	private static final String GRADEBOOKS_TABLE_CREATE =
			"CREATE TABLE " + GRADEBOOKS_TABLE_NAME + "(" +
			GradebooksContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			GradebooksContract.IVLE_ID + " TEXT, " +
			GradebooksContract.MODULE_ID + " TEXT, " +
			GradebooksContract.ACCOUNT + " TEXT, " +
			GradebooksContract.CATEGORY_TITLE + " TEXT" +
			");";
	
	/** Data for gradebook items data table */
	public static final String GRADEBOOK_ITEMS_TABLE_NAME = "gradebook_items";
	private static final String GRADEBOOK_ITEMS_TABLE_CREATE =
			"CREATE TABLE " + GRADEBOOK_ITEMS_TABLE_NAME + "(" +
			GradebookItemsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			GradebookItemsContract.IVLE_ID + " TEXT, " +
			GradebookItemsContract.MODULE_ID + " TEXT, " +
			GradebookItemsContract.GRADEBOOK_ID + " TEXT, " +
			GradebookItemsContract.ACCOUNT + " TEXT, " +
			GradebookItemsContract.AVERAGE_MEDIAN_MARKS + " TEXT, " +
			GradebookItemsContract.DATE_ENTERED + " TEXT, " +
			GradebookItemsContract.HIGHEST_LOWEST_MARKS + " TEXT, " +
			GradebookItemsContract.ITEM_DESCRIPTION + " TEXT, " +
			GradebookItemsContract.ITEM_NAME + " TEXT, " +
			GradebookItemsContract.MARKS_OBTAINED + " TEXT, " +
			GradebookItemsContract.MAX_MARKS + " INTEGER, " +
			GradebookItemsContract.PERCENTILE + " TEXT, " +
			GradebookItemsContract.REMARK + " TEXT" +
			");";
	
	/** Data for timetable slots table */
	public static final String TIMETABLE_SLOTS_TABLE_NAME = "timetable_slots";
	private static final String TIMETABLE_SLOTS_TABLE_CREATE =
			"CREATE TABLE " + TIMETABLE_SLOTS_TABLE_NAME + "(" +
			TimetableSlotsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			TimetableSlotsContract.MODULE_ID + " TEXT, " +
			TimetableSlotsContract.ACCOUNT + " TEXT, " +
			TimetableSlotsContract.ACAD_YEAR + " TEXT, " +
			TimetableSlotsContract.SEMESTER + " TEXT, " +
			TimetableSlotsContract.START_TIME + " TEXT, " +
			TimetableSlotsContract.END_TIME + " TEXT, " +
			TimetableSlotsContract.MODULE_CODE + " TEXT, " +
			TimetableSlotsContract.CLASS_NO + " TEXT, " +
			TimetableSlotsContract.LESSON_TYPE + " TEXT, " +
			TimetableSlotsContract.VENUE + " TEXT, " +
			TimetableSlotsContract.DAY_CODE + " TEXT, " +
			TimetableSlotsContract.DAY_TEXT + " TEXT, " +
			TimetableSlotsContract.WEEK_CODE + " TEXT, " +
			TimetableSlotsContract.WEEK_TEXT + " TEXT" +
			");";
	
	/** Data for users data table */
	public static final String USERS_TABLE_NAME = "users";
	private static final String USERS_TABLE_CREATE =
			"CREATE TABLE " + USERS_TABLE_NAME + "(" +
			UsersContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			UsersContract.IVLE_ID + " TEXT, " +
			UsersContract.ACCOUNT + " TEXT, " +
			UsersContract.ACCOUNT_TYPE + " TEXT, " +
			UsersContract.EMAIL + " TEXT, " +
			UsersContract.NAME + " TEXT, " +
			UsersContract.TITLE + " TEXT, " +
			UsersContract.USER_ID + " TEXT" +
			");";
	
	/** Data for webcasts data table */
	public static final String WEBCASTS_TABLE_NAME = "webcasts";
	private static final String WEBCASTS_TABLE_CREATE =
			"CREATE TABLE " + WEBCASTS_TABLE_NAME + "(" +
			WebcastsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WebcastsContract.IVLE_ID + " TEXT, " +
			WebcastsContract.MODULE_ID + " TEXT, " +
			WebcastsContract.ACCOUNT + " TEXT, " +
			WebcastsContract.CREATOR_ID + " TEXT, " +
			WebcastsContract.BADGE_TOOL + " INTEGER, " +
			WebcastsContract.PUBLISHED + " INTEGER, " +
			WebcastsContract.TITLE + " TEXT" +
			");";
	
	/** Data for webcast files table */
	public static final String WEBCAST_FILES_TABLE_NAME = "webcast_files";
	private static final String WEBCAST_FILES_TABLE_CREATE =
			"CREATE TABLE " + WEBCAST_FILES_TABLE_NAME + "(" +
			WebcastFilesContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WebcastFilesContract.IVLE_ID + " TEXT, " +
			WebcastFilesContract.MODULE_ID + " TEXT, " +
			WebcastFilesContract.WEBCAST_ITEM_GROUP_ID + " TEXT, " +
			WebcastFilesContract.ACCOUNT + " TEXT, " +
			WebcastFilesContract.CREATOR_ID + " TEXT, " +
			WebcastFilesContract.BANK_ITEM_ID + " TEXT, " +
			WebcastFilesContract.CREATE_DATE + " TEXT, " +
			WebcastFilesContract.FILE_DESCRIPTION + " TEXT, " +
			WebcastFilesContract.FILE_NAME + " TEXT, " +
			WebcastFilesContract.FILE_TITLE + " TEXT, " +
			WebcastFilesContract.MP3 + " TEXT, " +
			WebcastFilesContract.MP4 + " TEXT, " +
			WebcastFilesContract.MEDIA_FORMAT + " TEXT, " +
			WebcastFilesContract.IS_READ + " BOOLEAN" +
			");";
	
	/** Data for webcast item groups table */
	public static final String WEBCAST_ITEM_GROUPS_TABLE_NAME = "webcast_item_groups";
	private static final String WEBCAST_ITEM_GROUPS_TABLE_CREATE =
			"CREATE TABLE " + WEBCAST_ITEM_GROUPS_TABLE_NAME + "(" +
			WebcastItemGroupsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WebcastItemGroupsContract.IVLE_ID + " TEXT, " +
			WebcastItemGroupsContract.MODULE_ID + " TEXT, " +
			WebcastItemGroupsContract.WEBCAST_ID + " TEXT, " +
			WebcastItemGroupsContract.ACCOUNT + " TEXT, " +
			WebcastItemGroupsContract.ITEM_GROUP_TITLE + " TEXT" +
			");";
	
	/** Data for weblinks data table */
	public static final String WEBLINKS_TABLE_NAME = "weblinks";
	private static final String WEBLINKS_TABLE_CREATE =
			"CREATE TABLE " + WEBLINKS_TABLE_NAME + "(" +
			WeblinksContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WeblinksContract.IVLE_ID + " TEXT, " +
			WeblinksContract.MODULE_ID + " TEXT, " +
			WeblinksContract.ACCOUNT + " TEXT, " +
			WeblinksContract.DESCRIPTION + " TEXT, " +
			WeblinksContract.ORDER + " INTEGER, " +
			WeblinksContract.RATING + " INTEGER, " +
			WeblinksContract.SITE_TYPE + " INTEGER, " +
			WeblinksContract.URL + " TEXT" +
			");";
	
	/** Data for workbins data table */
	public static final String WORKBINS_TABLE_NAME = "workbins";
	private static final String WORKBINS_TABLE_CREATE =
			"CREATE TABLE " + WORKBINS_TABLE_NAME + "(" +
			WorkbinsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WorkbinsContract.IVLE_ID + " TEXT, " +
			WorkbinsContract.MODULE_ID + " TEXT, " +
			WorkbinsContract.ACCOUNT + " TEXT, " +
			WorkbinsContract.CREATOR_ID + " TEXT, " +
			WorkbinsContract.BADGE_TOOL + " INTEGER, " +
			WorkbinsContract.PUBLISHED + " BOOLEAN, " +
			WorkbinsContract.TITLE + " TEXT" +
			");";
	
	/** Data for workbin folders data table */
	public static final String WORKBIN_FOLDERS_TABLE_NAME = "workbin_folders";
	private static final String WORKBIN_FOLDERS_TABLE_CREATE =
			"CREATE TABLE " + WORKBIN_FOLDERS_TABLE_NAME + "(" +
			WorkbinFoldersContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WorkbinFoldersContract.IVLE_ID + " TEXT, " +
			WorkbinFoldersContract.MODULE_ID + " TEXT, " +
			WorkbinFoldersContract.ACCOUNT + " TEXT, " +
			WorkbinFoldersContract.WORKBIN_ID + " TEXT, " +
			WorkbinFoldersContract.WORKBIN_FOLDER_ID + " TEXT, " +
			WorkbinFoldersContract.ALLOW_UPLOAD + " BOOLEAN, " + 
			WorkbinFoldersContract.ALLOW_VIEW + " BOOLEAN, " +
			WorkbinFoldersContract.CLOSE_DATE + " TEXT, " +
			WorkbinFoldersContract.COMMENT_OPTION + " TEXT, " +
			WorkbinFoldersContract.FILE_COUNT + " INTEGER, " +
			WorkbinFoldersContract.FOLDER_NAME + " TEXT, " +
			WorkbinFoldersContract.ORDER + " INTEGER, " +
			WorkbinFoldersContract.OPEN_DATE + " TEXT, " +
			WorkbinFoldersContract.SORT_FILES_BY + " TEXT, " +
			WorkbinFoldersContract.UPLOAD_DISPLAY_OPTION + " TEXT" +
			");";
	
	/** Data for workbin files data table */
	public static final String WORKBIN_FILES_TABLE_NAME = "workbin_files";
	private static final String WORKBIN_FILES_TABLE_CREATE =
			"CREATE TABLE " + WORKBIN_FILES_TABLE_NAME + "(" +
			WorkbinFilesContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			WorkbinFilesContract.IVLE_ID + " TEXT, " +
			WorkbinFilesContract.MODULE_ID + " TEXT, " +
			WorkbinFilesContract.ACCOUNT + " TEXT, " +
			WorkbinFilesContract.WORKBIN_FOLDER_ID + " TEXT, " +
			WorkbinFilesContract.CREATOR_ID + " TEXT, " +
			WorkbinFilesContract.COMMENTER_ID + " TEXT, " +
			WorkbinFilesContract.FILE_DESCRIPTION + " TEXT, " +
			WorkbinFilesContract.FILE_NAME + " TEXT, " +
			WorkbinFilesContract.FILE_REMARKS + " TEXT, " +
			WorkbinFilesContract.FILE_REMARKS_ATTACHMENT + " TEXT, " +
			WorkbinFilesContract.FILE_SIZE + " DOUBLE, " +
			WorkbinFilesContract.FILE_TYPE + " TEXT, " +
			WorkbinFilesContract.IS_DOWNLOADED + " BOOLEAN," +
			WorkbinFilesContract.DOWNLOAD_URL + " TEXT" +
			");";
	
	// }}}
	// {{{ methods
	
	/**
	 * Class constructor.
	 * Calls the super constructor to initialize this class.
	 * 
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION);
		mContext = context;
	}
	
	/**
	 * Method: drop
	 * Drops the entire table.
	 */
	public static void drop(SQLiteDatabase db, String table) {
		db.execSQL("DROP TABLE IF EXISTS " + table);
	}
	
	/**
	 * Method: onCreate
	 * Called when the database is created for the first time.
	 * This is where the creation of tables and the initial population of the
	 * tables should happen.
	 * 
	 * @param db 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ANNOUNCEMENTS_TABLE_CREATE);
		db.execSQL(GRADEBOOKS_TABLE_CREATE);
		db.execSQL(GRADEBOOK_ITEMS_TABLE_CREATE);
		db.execSQL(TIMETABLE_SLOTS_TABLE_CREATE);
		db.execSQL(MODULES_TABLE_CREATE);
		db.execSQL(DESCRIPTIONS_TABLE_CREATE);
		db.execSQL(USERS_TABLE_CREATE);
		db.execSQL(WEBCASTS_TABLE_CREATE);
		db.execSQL(WEBCAST_FILES_TABLE_CREATE);
		db.execSQL(WEBCAST_ITEM_GROUPS_TABLE_CREATE);
		db.execSQL(WEBLINKS_TABLE_CREATE);
		db.execSQL(WORKBINS_TABLE_CREATE);
		db.execSQL(WORKBIN_FOLDERS_TABLE_CREATE);
		db.execSQL(WORKBIN_FILES_TABLE_CREATE);
	}
	
	/**
	 * Method: onUpgrade
	 * Called when the database needs to be upgraded.
	 * This should drop tables, add tables, or do anything else it needs to
	 * upgrade to the new schema version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This is a very rudimentary implementation.
		DatabaseHelper.drop(db, ANNOUNCEMENTS_TABLE_NAME);
		DatabaseHelper.drop(db, GRADEBOOKS_TABLE_NAME);
		DatabaseHelper.drop(db, GRADEBOOK_ITEMS_TABLE_NAME);
		DatabaseHelper.drop(db, TIMETABLE_SLOTS_TABLE_NAME);
		DatabaseHelper.drop(db, MODULES_TABLE_NAME);
		DatabaseHelper.drop(db, DESCRIPTIONS_TABLE_NAME);
		DatabaseHelper.drop(db, USERS_TABLE_NAME);
		DatabaseHelper.drop(db, WEBCASTS_TABLE_NAME);
		DatabaseHelper.drop(db, WEBCAST_FILES_TABLE_NAME);
		DatabaseHelper.drop(db, WEBCAST_ITEM_GROUPS_TABLE_NAME);
		DatabaseHelper.drop(db, WEBLINKS_TABLE_NAME);
		DatabaseHelper.drop(db, WORKBINS_TABLE_NAME);
		DatabaseHelper.drop(db, WORKBIN_FOLDERS_TABLE_NAME);
		DatabaseHelper.drop(db, WORKBIN_FILES_TABLE_NAME);
		
		// Recreate databases.
		this.onCreate(db);
		
		// Re-sync the databases.
		Account[] accounts = AccountUtils.getAllAccounts(mContext);
		for (Account account : accounts) {
			IVLEUtils.requestSyncNow(account);
		}
		
		return;
	}
	
	// }}}
}
