package com.nuscomputing.ivle.providers;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the webcasts provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WebcastFilesContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/webcast_files");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WEBCAST_FILES_TABLE_NAME;
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** The webcast item group ID */
	public static final String WEBCAST_ITEM_GROUP_ID = "webcast_item_group_id";
	
	/** Other columns */
	public static final String BANK_ITEM_ID = "bankItemId";
	public static final String CREATE_DATE = "createDate";
	public static final String FILE_DESCRIPTION = "fileDescription";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_TITLE = "fileTitle";
	public static final String MP3 = "MP3";
	public static final String MP4 = "MP4";
	public static final String MEDIA_FORMAT = "mediaFormat";
	public static final String IS_READ = "isRead";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WebcastFilesContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WebcastFilesContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}
	
	// }}}
}
