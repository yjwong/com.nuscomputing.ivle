package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the weblinks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WorkbinFoldersContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/workbin_folders");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WORKBIN_FOLDERS_TABLE_NAME;
	
	/** The workbin ID */
	public static final String WORKBIN_ID = "workbin_id";
	
	/** The parent workbin folder ID */
	public static final String WORKBIN_FOLDER_ID = "workbin_folder_id";
	
	/** Other columns */
	public static final String ALLOW_UPLOAD = "allowUpload";
	public static final String ALLOW_VIEW = "allowView";
	public static final String CLOSE_DATE = "closeDate";
	public static final String COMMENT_OPTION = "commentOption";
	public static final String FILE_COUNT = "fileCount";
	public static final String FOLDER_NAME = "folderName";
	public static final String ORDER = "itemOrder";
	public static final String OPEN_DATE = "openDate";
	public static final String SORT_FILES_BY = "sortFilesBy";
	public static final String UPLOAD_DISPLAY_OPTION = "uploadDisplayOption";
	
	// }}}
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WorkbinFoldersContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WorkbinFoldersContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WorkbinFoldersContract.IVLE_ID, TABLE + "." + WorkbinFoldersContract.IVLE_ID);
		map.put(prefix + WorkbinFoldersContract.ACCOUNT, TABLE + "." + WorkbinFoldersContract.ACCOUNT);
		map.put(prefix + WorkbinFoldersContract.ALLOW_UPLOAD, TABLE + "." + WorkbinFoldersContract.ALLOW_UPLOAD);
		map.put(prefix + WorkbinFoldersContract.ALLOW_VIEW, TABLE + "." + WorkbinFoldersContract.ALLOW_VIEW);
		map.put(prefix + WorkbinFoldersContract.CLOSE_DATE, TABLE + "." + WorkbinFoldersContract.CLOSE_DATE);
		map.put(prefix + WorkbinFoldersContract.COMMENT_OPTION, TABLE + "." + WorkbinFoldersContract.COMMENT_OPTION);
		map.put(prefix + WorkbinFoldersContract.FILE_COUNT, TABLE + "." + WorkbinFoldersContract.FILE_COUNT);
		map.put(prefix + WorkbinFoldersContract.FOLDER_NAME, TABLE + "." + WorkbinFoldersContract.FOLDER_NAME);
		map.put(prefix + WorkbinFoldersContract.ORDER, TABLE + "." + WorkbinFoldersContract.ORDER);
		map.put(prefix + WorkbinFoldersContract.OPEN_DATE, TABLE + "." + WorkbinFoldersContract.OPEN_DATE);
		map.put(prefix + WorkbinFoldersContract.SORT_FILES_BY, TABLE + "." + WorkbinFoldersContract.SORT_FILES_BY);
		map.put(prefix + WorkbinFoldersContract.UPLOAD_DISPLAY_OPTION, TABLE + "." + WorkbinFoldersContract.UPLOAD_DISPLAY_OPTION);
		return map;
	}
	
	// }}}
}
