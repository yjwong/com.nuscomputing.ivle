package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the workbins provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WorkbinsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/workbins");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WORKBINS_TABLE_NAME;
	
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
		return WorkbinsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WorkbinsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WorkbinsContract.IVLE_ID, TABLE + "." + WorkbinsContract.IVLE_ID);
		map.put(prefix + WorkbinsContract.ACCOUNT, TABLE + "." + WorkbinsContract.ACCOUNT);
		map.put(prefix + WorkbinsContract.BADGE_TOOL, TABLE + "." + WorkbinsContract.BADGE_TOOL);
		map.put(prefix + WorkbinsContract.PUBLISHED, TABLE + "." + WorkbinsContract.PUBLISHED);
		map.put(prefix + WorkbinsContract.TITLE, TABLE + "." + WorkbinsContract.TITLE);
		return map;
	}
	
	// }}}
}
