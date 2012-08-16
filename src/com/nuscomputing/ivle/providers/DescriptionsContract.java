package com.nuscomputing.ivle.providers;

import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the module descriptions provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class DescriptionsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/module_descriptions");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.DESCRIPTIONS_TABLE_NAME;
	
	/** Other columns */
	public static final String DESCRIPTION = "description";
	public static final String ORDER = "itemOrder";
	public static final String TITLE = "title";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return DescriptionsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return DescriptionsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		return null;
	}

	// }}}
}
