package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * Base contract for all contracts.
 * @author yjwong
 */
public class IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = null;
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	// }}}
}
