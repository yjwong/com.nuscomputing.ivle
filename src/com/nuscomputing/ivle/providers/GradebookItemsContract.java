package com.nuscomputing.ivle.providers;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the gradebooks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class GradebookItemsContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/gradebook_items");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.GRADEBOOK_ITEMS_TABLE_NAME;
	
	/** The gradebook ID for this item */
	public static final String GRADEBOOK_ID = "gradebook_id";
	
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
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return GradebookItemsContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return GradebookItemsContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}
	
	// }}}
}
