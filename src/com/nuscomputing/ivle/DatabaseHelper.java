package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.GradebookItemsContract;
import com.nuscomputing.ivle.providers.GradebooksContract;
import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivle.providers.UsersContract;
import com.nuscomputing.ivle.providers.WeblinksContract;
import com.nuscomputing.ivle.providers.WorkbinsContract;

import android.content.Context;
import android.database.sqlite.*;

/**
 * Provides a storage for the IVLE app.
 * Used to store accounts and various miscellaneous IVLE-related data.
 * @author yjwong
 */

public class DatabaseHelper extends SQLiteOpenHelper {
	// {{{ properties
	
	/** Version of the database schema */
	private static final int DATABASE_VERSION = 2;
	
	/** Name of this database */
	private static final String DATABASE_NAME = "ivle";
	
	/** Data for announcements data table */
	public static final String ANNOUNCEMENTS_TABLE_NAME = "announcements";
	private static final String ANNOUNCEMENTS_TABLE_CREATE =
			"CREATE TABLE " + ANNOUNCEMENTS_TABLE_NAME + "(" +
			AnnouncementsContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			AnnouncementsContract.IVLE_ID + " TEXT, " +
			AnnouncementsContract.MODULE_ID + " TEXT, " +
			AnnouncementsContract.ACCOUNT + " TEXT, " +
			AnnouncementsContract.TITLE + " TEXT, " +
			AnnouncementsContract.CREATOR + " TEXT, " +
			AnnouncementsContract.DESCRIPTION + " TEXT, " +
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
			ModulesContract.CREATOR + " TEXT, " +
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
	}
	
	/**
	 * Method: drop
	 * Drops the entire table.
	 */
	public static void drop(SQLiteDatabase db, String table) {
		db.execSQL("DROP TABLE " + table);
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
		db.execSQL(MODULES_TABLE_CREATE);
		db.execSQL(USERS_TABLE_CREATE);
		db.execSQL(WEBLINKS_TABLE_CREATE);
		db.execSQL(WORKBINS_TABLE_CREATE);
	}
	
	/**
	 * Method: onUpgrade
	 * Called when the database needs to be upgraded.
	 * This should drop tables, add tables, or do anything else it needs to
	 * upgrade to the new schema version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Currently we don't need an implementation of this yet...
		return;
	}
	
	// }}}
}
