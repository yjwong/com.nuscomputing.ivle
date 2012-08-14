package com.nuscomputing.ivle.providers;

import java.util.HashMap;
import java.util.Map;

import com.nuscomputing.ivle.DatabaseHelper;

import android.net.Uri;

/**
 * The contract between the workbins provider and applications.
 * Contains definitions for supported URIs and data columns.
 * @author yjwong
 */
public class WorkbinFilesContract extends IVLEContract {
	// {{{ properties
	
	/** The content:// style URL for the top level authority */
	public static final Uri CONTENT_URI = Uri.parse("content://com.nuscomputing.ivle.provider/workbin_files");
	
	/** The database table backing this type */
	public static final String TABLE = DatabaseHelper.WORKBIN_FILES_TABLE_NAME;
	
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
	// {{{ methods
	
	@Override
	public Uri getContentUri() {
		return WorkbinFilesContract.CONTENT_URI;
	}

	@Override
	public String getTableName() {
		return WorkbinFilesContract.TABLE;
	}

	@Override
	public String getColumnNameModuleId() {
		return IVLEContract.MODULE_ID;
	}

	@Override
	public Map<String, String> getJoinProjectionMap(String prefix) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(prefix + WorkbinFilesContract.IVLE_ID, TABLE + "." + WorkbinFilesContract.IVLE_ID);
		map.put(prefix + WorkbinFilesContract.ACCOUNT, TABLE + "." + WorkbinFilesContract.ACCOUNT);
		map.put(prefix + WorkbinFilesContract.FILE_DESCRIPTION, TABLE + "." + WorkbinFilesContract.FILE_DESCRIPTION);
		map.put(prefix + WorkbinFilesContract.FILE_NAME, TABLE + "." + WorkbinFilesContract.FILE_NAME);
		map.put(prefix + WorkbinFilesContract.FILE_REMARKS, TABLE + "." + WorkbinFilesContract.FILE_REMARKS);
		map.put(prefix + WorkbinFilesContract.FILE_REMARKS_ATTACHMENT, TABLE + "." + WorkbinFilesContract.FILE_REMARKS_ATTACHMENT);
		map.put(prefix + WorkbinFilesContract.FILE_SIZE, TABLE + "." + WorkbinFilesContract.FILE_SIZE);
		map.put(prefix + WorkbinFilesContract.FILE_TYPE, TABLE + "." + WorkbinFilesContract.FILE_TYPE);
		map.put(prefix + WorkbinFilesContract.IS_DOWNLOADED, TABLE + "." + WorkbinFilesContract.IS_DOWNLOADED);
		map.put(prefix + WorkbinFilesContract.DOWNLOAD_URL, TABLE + "." + WorkbinFilesContract.DOWNLOAD_URL);
		return map;
	}
	
	// }}}
}
