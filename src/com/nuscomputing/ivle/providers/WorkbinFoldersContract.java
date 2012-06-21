package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the weblinks provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WorkbinFoldersContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/workbin_folders");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
	/** The workbin ID */
	public static final String WORKBIN_ID = "workbin_id";
	
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
}
