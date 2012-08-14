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
public class WebcastsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/webcasts");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WEBCASTS_TABLE_NAME;
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** Other columns */
	public static final String BADGE_TOOL = "badgeTool";
	public static final String PUBLISHED = "published";
	public static final String TITLE = "title";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WebcastsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WebcastsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WebcastsContract.IVLE_ID, TABLE + "." + WebcastsContract.IVLE_ID);
		map.put(prefix + WebcastsContract.ACCOUNT, TABLE + "." + WebcastsContract.ACCOUNT);
		map.put(prefix + WebcastsContract.BADGE_TOOL, TABLE + "." + WebcastsContract.BADGE_TOOL);
		map.put(prefix + WebcastsContract.PUBLISHED, TABLE + "." + WebcastsContract.PUBLISHED);
		map.put(prefix + WebcastsContract.TITLE, TABLE + "." + WebcastsContract.TITLE);
		return map;
	}
	
	// }}}
}
