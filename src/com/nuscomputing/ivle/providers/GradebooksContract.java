package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the gradebooks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class GradebooksContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/gradebooks");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.GRADEBOOKS_TABLE_NAME;
	
	/** Other columns */
	public static final String CATEGORY_TITLE = "categoryTitle";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return GradebooksContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return GradebooksContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + GradebooksContract.IVLE_ID, TABLE + "." + GradebooksContract.IVLE_ID);
		map.put(prefix + GradebooksContract.ACCOUNT, TABLE + "." + GradebooksContract.ACCOUNT);
		map.put(prefix + GradebooksContract.CATEGORY_TITLE, TABLE + "." + GradebooksContract.CATEGORY_TITLE);
		return map;
	}
	
	// }}}
}
