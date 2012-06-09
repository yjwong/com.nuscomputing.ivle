package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the modules provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class ModulesContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/modules");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The account associated with the module */
	public static final String ACCOUNT = "account";
	
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
	public static final String CREATOR = "creator";
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
}
