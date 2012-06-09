package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the weblinks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class UsersContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/users");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The account associated with the user */
	public static final String ACCOUNT = "account";
	
	/** Other columns */
	public static final String ACCOUNT_TYPE = "accountType";
	public static final String EMAIL = "email";
	public static final String NAME = "name";
	public static final String TITLE = "title";
	public static final String USER_ID = "userID";
	
	// }}}
}
