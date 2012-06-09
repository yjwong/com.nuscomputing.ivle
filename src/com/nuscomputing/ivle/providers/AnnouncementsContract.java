package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the announcements provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class AnnouncementsContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/announcements");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the announcement */
	public static final String ACCOUNT = "account";
	
	/** Other columns */
	public static final String TITLE = "title";
	public static final String CREATOR = "creator";
	public static final String DESCRIPTION = "description";
	public static final String CREATED_DATE = "createdDate";
	public static final String EXPIRY_DATE = "expiryDate";
	public static final String URL = "url";
	public static final String IS_READ = "isRead";
	
	// }}}
}
