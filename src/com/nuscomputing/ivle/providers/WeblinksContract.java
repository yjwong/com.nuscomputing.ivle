package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the weblinks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WeblinksContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/weblinks");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WEBLINKS_TABLE_NAME;
	
	/** Other columns */
	public static final String DESCRIPTION = "description";
	public static final String ORDER = "itemOrder";
	public static final String RATING = "rating";
	public static final String SITE_TYPE = "siteType";
	public static final String URL = "url";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WeblinksContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WeblinksContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WeblinksContract.IVLE_ID, TABLE + "." + WeblinksContract.IVLE_ID);
		map.put(prefix + WeblinksContract.ACCOUNT, TABLE + "." + WeblinksContract.ACCOUNT);
		map.put(prefix + WeblinksContract.DESCRIPTION, TABLE + "." + WeblinksContract.DESCRIPTION);
		map.put(prefix + WeblinksContract.ORDER, TABLE + "." + WeblinksContract.ORDER);
		map.put(prefix + WeblinksContract.RATING, TABLE + "." + WeblinksContract.RATING);
		map.put(prefix + WeblinksContract.SITE_TYPE, TABLE + "." + WeblinksContract.SITE_TYPE);
		map.put(prefix + WeblinksContract.URL, TABLE + "." + WeblinksContract.URL);
		return map;
	}
	
	// }}}
}
