package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the webcasts provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WebcastsContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/webcasts");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
	/** The creator ID */
	public static final String CREATOR_ID = "creator_id";
	
	/** Other columns */
	public static final String BADGE_TOOL = "badgeTool";
	public static final String PUBLISHED = "published";
	public static final String TITLE = "title";
	
	// }}}
}
