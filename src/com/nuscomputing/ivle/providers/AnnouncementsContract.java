package com.nuscomputing.ivle.providers;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the announcements provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class AnnouncementsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/announcements");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME;
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** Other columns */
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String CREATED_DATE = "createdDate";
	public static final String EXPIRY_DATE = "expiryDate";
	public static final String URL = "url";
	public static final String IS_READ = "isRead";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return AnnouncementsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return AnnouncementsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}
	
	// }}}
}
