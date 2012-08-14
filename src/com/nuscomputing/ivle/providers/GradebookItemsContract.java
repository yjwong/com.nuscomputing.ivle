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

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + GradebookItemsContract.IVLE_ID, TABLE + "." + GradebookItemsContract.IVLE_ID);
		map.put(prefix + GradebookItemsContract.ACCOUNT, TABLE + "." + GradebookItemsContract.ACCOUNT);
		map.put(prefix + GradebookItemsContract.AVERAGE_MEDIAN_MARKS, TABLE + "." + GradebookItemsContract.AVERAGE_MEDIAN_MARKS);
		map.put(prefix + GradebookItemsContract.DATE_ENTERED, TABLE + "." + GradebookItemsContract.DATE_ENTERED);
		map.put(prefix + GradebookItemsContract.HIGHEST_LOWEST_MARKS, TABLE + "." + GradebookItemsContract.HIGHEST_LOWEST_MARKS);
		map.put(prefix + GradebookItemsContract.ITEM_DESCRIPTION, TABLE + "." + GradebookItemsContract.ITEM_DESCRIPTION);
		map.put(prefix + GradebookItemsContract.ITEM_NAME, TABLE + "." + GradebookItemsContract.ITEM_NAME);
		map.put(prefix + GradebookItemsContract.MARKS_OBTAINED, TABLE + "." + GradebookItemsContract.MARKS_OBTAINED);
		map.put(prefix + GradebookItemsContract.MAX_MARKS, TABLE + "." + GradebookItemsContract.MAX_MARKS);
		map.put(prefix + GradebookItemsContract.PERCENTILE, TABLE + "." + GradebookItemsContract.PERCENTILE);
		map.put(prefix + GradebookItemsContract.REMARK, TABLE + "." + GradebookItemsContract.REMARK);
		return map;
	}
	
	// }}}
}
