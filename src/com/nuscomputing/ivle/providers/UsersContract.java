package com.nuscomputing.ivle.providers;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the users provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class UsersContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/users");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.USERS_TABLE_NAME;

	/** Other columns */
	public static final String ACCOUNT_TYPE = "accountType";
	public static final String EMAIL = "email";
	public static final String NAME = "name";
	public static final String TITLE = "title";
	public static final String USER_ID = "userID";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return UsersContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return UsersContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return null;
	}
	
	// }}}
}
