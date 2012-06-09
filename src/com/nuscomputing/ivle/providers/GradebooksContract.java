package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the gradebooks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class GradebooksContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/gradebooks");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
	/** Other columns */
	public static final String CATEGORY_TITLE = "categoryTitle";
	
	// }}}
}
