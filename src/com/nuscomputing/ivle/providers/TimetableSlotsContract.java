package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the webcasts provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class TimetableSlotsContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/timetable_slots");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
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
}
