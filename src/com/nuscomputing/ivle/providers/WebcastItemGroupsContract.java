package com.nuscomputing.ivle.providers;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the webcasts provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WebcastItemGroupsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/webcast_item_groups");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WEBCAST_ITEM_GROUPS_TABLE_NAME;
	
	/** The webcast ID for this item */
	public static final String WEBCAST_ID = "webcast_id";
	
	/** Other columns */
	public static final String ITEM_GROUP_TITLE = "itemGroupTitle";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WebcastItemGroupsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WebcastItemGroupsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}
	
	// }}}
}
