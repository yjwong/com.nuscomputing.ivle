package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the modules provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class ModulesContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/modules");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.MODULES_TABLE_NAME;
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** Other columns */
	public static final String BADGE = "badge";
	public static final String BADGE_ANNOUNCEMENT ="badgeAnnouncement";
	public static final String COURSE_ACAD_YEAR = "courseAcadYear";
	public static final String COURSE_CLOSE_DATE = "courseCloseDate";
	public static final String COURSE_CODE = "courseCode";
	public static final String COURSE_DEPARTMENT = "courseDepartment";
	public static final String COURSE_LEVEL = "courseLevel";
	public static final String COURSE_MC = "courseMC";
	public static final String COURSE_NAME = "courseName";
	public static final String COURSE_OPEN_DATE = "courseOpenDate";
	public static final String COURSE_SEMESTER = "courseSemester";
	public static final String HAS_ANNOUNCEMENT_ITEMS = "hasAnnouncementItems";
	public static final String HAS_CLASS_GROUPS_FOR_SIGN_UP = "hasClassGroupsForSignUp";
	public static final String HAS_CLASS_ROSTER_ITEMS = "hasClassRosterItems";
	public static final String HAS_CONSULTATION_ITEMS = "hasConsultationItems";
	public static final String HAS_CONSULTATION_SLOTS_FOR_SIGN_UP = "hasConsultationSlotsForSignUp";
	public static final String HAS_DESCRIPTION_ITEMS = "hasDescriptionItems";
	public static final String HAS_GRADEBOOK_ITEMS = "hasGradebookItems";
	public static final String HAS_GROUPS_ITEMS = "hasGroupsItems";
	public static final String HAS_GUEST_ROSTER_ITEMS = "hasGuestRosterItems";
	public static final String HAS_LECTURER_ITEMS = "hasLecturerItems";
	public static final String HAS_PROJECT_GROUP_ITEMS = "hasProjectGroupItems";
	public static final String HAS_PROJECT_GROUPS_FOR_SIGN_UP = "hasProjectGroupsForSignUp";
	public static final String HAS_READING_ITEMS = "hasReadingItems";
	public static final String HAS_TIMETABLE_ITEMS = "hasTimetableItems";
	public static final String HAS_WEBLINK_ITEMS = "hasWeblinkItems";
	public static final String IS_ACTIVE = "isActive";
	public static final String PERMISSION = "permission";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return ModulesContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return ModulesContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return null;
	}
	
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + ModulesContract.IVLE_ID, TABLE + "." + ModulesContract.IVLE_ID);
		map.put(prefix + ModulesContract.ACCOUNT, TABLE + "." + ModulesContract.ACCOUNT);
		map.put(prefix + ModulesContract.BADGE, TABLE + "." + ModulesContract.BADGE);
		map.put(prefix + ModulesContract.BADGE_ANNOUNCEMENT, TABLE + "." + ModulesContract.BADGE_ANNOUNCEMENT);
		map.put(prefix + ModulesContract.COURSE_ACAD_YEAR, TABLE + "." + ModulesContract.COURSE_ACAD_YEAR);
		map.put(prefix + ModulesContract.COURSE_CLOSE_DATE, TABLE + "." + ModulesContract.COURSE_CLOSE_DATE);
		map.put(prefix + ModulesContract.COURSE_CODE, TABLE + "." + ModulesContract.COURSE_CODE);
		map.put(prefix + ModulesContract.COURSE_DEPARTMENT, TABLE + "." + ModulesContract.COURSE_DEPARTMENT);
		map.put(prefix + ModulesContract.COURSE_LEVEL, TABLE + "." + ModulesContract.COURSE_LEVEL);
		map.put(prefix + ModulesContract.COURSE_MC, TABLE + "." + ModulesContract.COURSE_MC);
		map.put(prefix + ModulesContract.COURSE_NAME, TABLE + "." + ModulesContract.COURSE_NAME);
		map.put(prefix + ModulesContract.COURSE_OPEN_DATE, TABLE + "." + ModulesContract.COURSE_OPEN_DATE);
		map.put(prefix + ModulesContract.COURSE_SEMESTER, TABLE + "." + ModulesContract.COURSE_SEMESTER);
		map.put(prefix + ModulesContract.HAS_ANNOUNCEMENT_ITEMS, TABLE + "." + ModulesContract.HAS_ANNOUNCEMENT_ITEMS);
		map.put(prefix + ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP, TABLE + "." + ModulesContract.HAS_CLASS_GROUPS_FOR_SIGN_UP);
		map.put(prefix + ModulesContract.HAS_CLASS_ROSTER_ITEMS, TABLE + "." + ModulesContract.HAS_CLASS_ROSTER_ITEMS);
		map.put(prefix + ModulesContract.HAS_CONSULTATION_ITEMS, TABLE + "." + ModulesContract.HAS_CONSULTATION_ITEMS);
		map.put(prefix + ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP, TABLE + "." + ModulesContract.HAS_CONSULTATION_SLOTS_FOR_SIGN_UP);
		map.put(prefix + ModulesContract.HAS_DESCRIPTION_ITEMS, TABLE + "." + ModulesContract.HAS_DESCRIPTION_ITEMS);
		map.put(prefix + ModulesContract.HAS_GRADEBOOK_ITEMS, TABLE + "." + ModulesContract.HAS_GRADEBOOK_ITEMS);
		map.put(prefix + ModulesContract.HAS_GROUPS_ITEMS, TABLE + "." + ModulesContract.HAS_GROUPS_ITEMS);
		map.put(prefix + ModulesContract.HAS_GUEST_ROSTER_ITEMS, TABLE + "." + ModulesContract.HAS_GUEST_ROSTER_ITEMS);
		map.put(prefix + ModulesContract.HAS_LECTURER_ITEMS, TABLE + "." + ModulesContract.HAS_LECTURER_ITEMS);
		map.put(prefix + ModulesContract.HAS_PROJECT_GROUP_ITEMS, TABLE + "." + ModulesContract.HAS_PROJECT_GROUP_ITEMS);
		map.put(prefix + ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP, TABLE + "." + ModulesContract.HAS_PROJECT_GROUPS_FOR_SIGN_UP);
		map.put(prefix + ModulesContract.HAS_READING_ITEMS, TABLE + "." + ModulesContract.HAS_READING_ITEMS);
		map.put(prefix + ModulesContract.HAS_TIMETABLE_ITEMS, TABLE + "." + ModulesContract.HAS_TIMETABLE_ITEMS);
		map.put(prefix + ModulesContract.HAS_WEBLINK_ITEMS, TABLE + "." + ModulesContract.HAS_WEBLINK_ITEMS);
		map.put(prefix + ModulesContract.IS_ACTIVE, TABLE + "." + ModulesContract.IS_ACTIVE);
		map.put(prefix + ModulesContract.PERMISSION, TABLE + "." + ModulesContract.PERMISSION);
		return map;
	}
	
	// }}}
}
