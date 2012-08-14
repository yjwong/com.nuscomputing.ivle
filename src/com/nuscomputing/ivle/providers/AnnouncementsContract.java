package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

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
	
	/** Prefixes for foreign keys */
	public static final String CREATOR_PREFIX = "creator_";
	public static final String MODULE_PREFIX = "module_";
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** Other columns */
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String CREATED_DATE = "createdDate";
	public static final String EXPIRY_DATE = "expiryDate";
	public static final String URL = "url";
	public static final String IS_READ = "isRead";
	
	/** Internal columns that are not in the API */
	public static final String _DESCRIPTION_NOHTML = "descriptionNoHtml";
	
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

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + AnnouncementsContract.IVLE_ID, TABLE + "." + AnnouncementsContract.IVLE_ID);
		map.put(prefix + AnnouncementsContract.MODULE_ID, TABLE + "." + AnnouncementsContract.MODULE_ID);
		map.put(prefix + AnnouncementsContract.ACCOUNT, TABLE + "." + AnnouncementsContract.ACCOUNT);
		map.put(prefix + AnnouncementsContract.CREATOR_ID, TABLE + "." + AnnouncementsContract.CREATOR_ID);
		map.put(prefix + AnnouncementsContract.TITLE, TABLE + "." + AnnouncementsContract.TITLE);
		map.put(prefix + AnnouncementsContract.DESCRIPTION, TABLE + "." + AnnouncementsContract.DESCRIPTION);
		map.put(prefix + AnnouncementsContract._DESCRIPTION_NOHTML, TABLE + "." + AnnouncementsContract._DESCRIPTION_NOHTML);
		map.put(prefix + AnnouncementsContract.CREATED_DATE, TABLE + "." + AnnouncementsContract.CREATED_DATE);
		map.put(prefix + AnnouncementsContract.EXPIRY_DATE, TABLE + "." + AnnouncementsContract.EXPIRY_DATE);
		map.put(prefix + AnnouncementsContract.URL, TABLE + "." + AnnouncementsContract.URL);
		map.put(prefix + AnnouncementsContract.IS_READ, TABLE + "." + AnnouncementsContract.IS_READ);
		return map;
	}
	
	// }}}
}
