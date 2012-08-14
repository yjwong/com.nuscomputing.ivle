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

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WebcastFilesContract.IVLE_ID, TABLE + "." + WebcastFilesContract.IVLE_ID);
		map.put(prefix + WebcastFilesContract.ACCOUNT, TABLE + "." + WebcastFilesContract.ACCOUNT);
		map.put(prefix + WebcastFilesContract.BANK_ITEM_ID, TABLE + "." + WebcastFilesContract.BANK_ITEM_ID);
		map.put(prefix + WebcastFilesContract.CREATE_DATE, TABLE + "." + WebcastFilesContract.CREATE_DATE);
		map.put(prefix + WebcastFilesContract.FILE_DESCRIPTION, TABLE + "." + WebcastFilesContract.FILE_DESCRIPTION);
		map.put(prefix + WebcastFilesContract.FILE_NAME, TABLE + "." + WebcastFilesContract.FILE_NAME);
		map.put(prefix + WebcastFilesContract.FILE_TITLE, TABLE + "." + WebcastFilesContract.FILE_TITLE);
		map.put(prefix + WebcastFilesContract.MP3, TABLE + "." + WebcastFilesContract.MP3);
		map.put(prefix + WebcastFilesContract.MP4, TABLE + "." + WebcastFilesContract.MP4);
		map.put(prefix + WebcastFilesContract.MEDIA_FORMAT, TABLE + "." + WebcastFilesContract.MEDIA_FORMAT);
		map.put(prefix + WebcastFilesContract.IS_READ, TABLE + "." + WebcastFilesContract.IS_READ);
		return map;
	}
	
	// }}}
}
