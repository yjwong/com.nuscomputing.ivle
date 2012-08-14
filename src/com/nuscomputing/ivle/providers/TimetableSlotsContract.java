package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the webcasts provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class TimetableSlotsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/timetable_slots");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.TIMETABLE_SLOTS_TABLE_NAME;
	
	/** Other columns */
	public static final String ACAD_YEAR = "acadYear";
	public static final String SEMESTER = "semester";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String MODULE_CODE = "moduleCode";
	public static final String CLASS_NO = "classNo";
	public static final String LESSON_TYPE = "lessonType";
	public static final String VENUE = "venue";
	public static final String DAY_CODE = "dayCode";
	public static final String DAY_TEXT = "dayText";
	public static final String WEEK_CODE = "weekCode";
	public static final String WEEK_TEXT = "weekText";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return TimetableSlotsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return TimetableSlotsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return null;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + TimetableSlotsContract.ACCOUNT, TABLE + "." + TimetableSlotsContract.ACCOUNT);
		map.put(prefix + TimetableSlotsContract.ACAD_YEAR, TABLE + "." + TimetableSlotsContract.ACAD_YEAR);
		map.put(prefix + TimetableSlotsContract.SEMESTER, TABLE + "." + TimetableSlotsContract.SEMESTER);
		map.put(prefix + TimetableSlotsContract.START_TIME, TABLE + "." + TimetableSlotsContract.START_TIME);
		map.put(prefix + TimetableSlotsContract.END_TIME, TABLE + "." + TimetableSlotsContract.END_TIME);
		map.put(prefix + TimetableSlotsContract.MODULE_CODE, TABLE + "." + TimetableSlotsContract.MODULE_CODE);
		map.put(prefix + TimetableSlotsContract.CLASS_NO, TABLE + "." + TimetableSlotsContract.CLASS_NO);
		map.put(prefix + TimetableSlotsContract.LESSON_TYPE, TABLE + "." + TimetableSlotsContract.LESSON_TYPE);
		map.put(prefix + TimetableSlotsContract.VENUE, TABLE + "." + TimetableSlotsContract.VENUE);
		map.put(prefix + TimetableSlotsContract.DAY_CODE, TABLE + "." + TimetableSlotsContract.DAY_CODE);
		map.put(prefix + TimetableSlotsContract.DAY_TEXT, TABLE + "." + TimetableSlotsContract.DAY_TEXT);
		map.put(prefix + TimetableSlotsContract.WEEK_CODE, TABLE + "." + TimetableSlotsContract.WEEK_CODE);
		map.put(prefix + TimetableSlotsContract.WEEK_TEXT, TABLE + "." + TimetableSlotsContract.WEEK_TEXT);
		return map;
	}
	
	// }}}
}
