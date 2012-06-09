package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the gradebooks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class GradebookItemsContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/gradebook_items");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The gradebook ID for this item */
	public static final String GRADEBOOK_ID = "gradebook_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
	/** Other columns */
	public static final String AVERAGE_MEDIAN_MARKS = "averageMedianMarks";
	public static final String DATE_ENTERED = "dateEntered";
	public static final String HIGHEST_LOWEST_MARKS = "highestLowestMarks";
	public static final String ITEM_DESCRIPTION = "itemDescription";
	public static final String ITEM_NAME = "itemName";
	public static final String MARKS_OBTAINED = "marksObtained";
	public static final String MAX_MARKS = "maxMarks";
	public static final String PERCENTILE = "percentile";
	public static final String REMARK = "remark";
	
	// }}}
}
