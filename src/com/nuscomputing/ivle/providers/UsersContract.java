package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

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

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + UsersContract.IVLE_ID, TABLE + "." + UsersContract.IVLE_ID);
		map.put(prefix + UsersContract.ACCOUNT, TABLE + "." + UsersContract.ACCOUNT);
		map.put(prefix + UsersContract.ACCOUNT_TYPE, TABLE + "." + UsersContract.ACCOUNT_TYPE);
		map.put(prefix + UsersContract.EMAIL, TABLE + "." + UsersContract.EMAIL);
		map.put(prefix + UsersContract.NAME, TABLE + "." + UsersContract.NAME);
		map.put(prefix + UsersContract.TITLE, TABLE + "." + UsersContract.TITLE);
		map.put(prefix + UsersContract.USER_ID, TABLE + "." + UsersContract.USER_ID);
		return map;
	}
	
	// }}}
}
