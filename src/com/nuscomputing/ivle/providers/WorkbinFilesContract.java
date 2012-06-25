package com.nuscomputing.ivle.providers;

import android.net.Uri;

/**
 * The contract between the workbins provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WorkbinFilesContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/workbin_files");
	
	/** The row ID */
	public static final String ID = "_id";
	
	/** The ivle ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The module ID */
	public static final String MODULE_ID = "module_id";
	
	/** The account associated with the weblink */
	public static final String ACCOUNT = "account";
	
	/** The creator of the folder */
	public static final String CREATOR_ID = "creator_id";
	
	/** Commenter of the folder */
	public static final String COMMENTER_ID = "commenter_id";
	
	/** The workbin folder ID */
	public static final String WORKBIN_FOLDER_ID = "workbin_folder_id";
	
	/** Other columns */
	public static final String FILE_DESCRIPTION = "fileDescription";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_REMARKS = "fileRemarks";
	public static final String FILE_REMARKS_ATTACHMENT = "fileRemarksAttachment";
	public static final String FILE_SIZE = "fileSize";
	public static final String FILE_TYPE = "fileType";
	public static final String IS_DOWNLOADED = "isDownloaded";
	
	/** Additional column for download URL */
	public static final String DOWNLOAD_URL = "downloadUrl";
	
	// }}}
}
