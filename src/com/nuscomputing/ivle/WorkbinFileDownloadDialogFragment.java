package com.nuscomputing.ivle;

import java.io.File;

import com.nuscomputing.support.android.app.DownloadManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment used to display the current progress of downloading a file from
 * the workbin.
 * <p>
 * @author yjwong
 */
public class WorkbinFileDownloadDialogFragment extends DialogFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "WorkbinFileDownloadDialogFragment";
	
	/** Instance of the alert dialog */
	private AlertDialog mAlertDialog;
	
	/** The file name */
	private String mFileName;
	
	/** The file size */
	private Double mFileSize;
	
	/** The download URL */
	private Uri mDownloadUrl;
	
	/** Is a download in progress? */
	private boolean mDownloadInProgress;
	
	/** The download manager */
	private DownloadManager mDownloadManager;
	
	/** This dialog fragment */
	private DialogFragment mDialogFragment = this;
	
	/** The download ID, returned by DownloadManager */
	private long mDownloadId;
	
	/** Should we monitor download status? */
	private boolean mShouldMonitorStatus = true;
	
	/** Check download status */
	private Handler mHandler = new Handler();
	private Runnable mDownloadStatusUpdater = new Runnable() {
		@Override
		public void run() {
			// Query the download manager.
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(mDownloadId);
			Cursor cursor = mDownloadManager.query(query);
			cursor.moveToFirst();
			
			// Get the download status.
			int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			Toast toast;
			
			// After configuration change: wait a little while for the activity to return.
			if (mDialogFragment.getActivity() == null) {
				mHandler.postDelayed(mDownloadStatusUpdater, 250);
				cursor.close();
				return;
			}

			Context toastContext = mDialogFragment.getActivity().getApplicationContext();
			switch (status) {
				case DownloadManager.STATUS_FAILED:
					mShouldMonitorStatus = false;
					mDialogFragment.dismiss();
					
					// Notify the user.
					toast = Toast.makeText(toastContext, getString(R.string.workbin_file_download_dialog_fragment_unable_to_download), Toast.LENGTH_SHORT);
					toast.show();
					break;
				
				case DownloadManager.STATUS_PAUSED:
					mShouldMonitorStatus = false;
					mDownloadManager.remove(mDownloadId);
					mDialogFragment.dismiss();
					
					// Notify the user.
					toast = Toast.makeText(toastContext, getString(R.string.workbin_file_download_dialog_fragment_unable_to_download), Toast.LENGTH_SHORT);
					toast.show();
					break;
				
				case DownloadManager.STATUS_PENDING:
					// Hide the status.
					TextView tvProgress = (TextView) mAlertDialog.findViewById(R.id.workbin_file_download_dialog_fragment_progress);
					tvProgress.setText("");
					tvProgress.setVisibility(View.GONE);
					
					// Update the progress bar.
					ProgressBar progressBar = (ProgressBar) mAlertDialog.findViewById(R.id.workbin_file_download_dialog_fragment_progress_bar);
					progressBar.setIndeterminate(true);
					
					// Update the file name.
					TextView tvFileName = (TextView) mAlertDialog.findViewById(R.id.workbin_file_download_dialog_fragment_file_name);
					tvFileName.setText(mFileName);
					break;
					
				case DownloadManager.STATUS_RUNNING:
					// Obtain some download data.
					Double bytesDownloaded = cursor.getDouble(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					Double totalBytes = mFileSize;
					Double downloadPct = bytesDownloaded / totalBytes * 100;
					
					// Format the arguments.
					String bytesDownloadedStr = Formatter.formatFileSize(getActivity(), bytesDownloaded.intValue());
					String totalBytesStr = Formatter.formatFileSize(getActivity(), totalBytes.intValue());
					
					// Update the progress bar.
					progressBar = (ProgressBar) mAlertDialog.findViewById(R.id.workbin_file_download_dialog_fragment_progress_bar);
					progressBar.setMax(totalBytes.intValue());
					progressBar.setProgress(bytesDownloaded.intValue());
					progressBar.setIndeterminate(false);
					
					// Update the bytes downloaded.
					tvProgress = (TextView) mAlertDialog.findViewById(R.id.workbin_file_download_dialog_fragment_progress);
					tvProgress.setText(getString(R.string.workbin_file_download_dialog_fragment_progress, bytesDownloadedStr, totalBytesStr, downloadPct.intValue()));
					tvProgress.setVisibility(View.VISIBLE);
					break;
				
				case DownloadManager.STATUS_SUCCESSFUL:
					mShouldMonitorStatus = false;
					
					// Obtain the downloaded file location.
					String localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					Log.v(TAG, localUri);
					if (localUri == null) {
						break;
					}
					
					// Obtain the MIME type.
					File downloadFile = new File(localUri);
					MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
					String downloadFileExt = downloadFile.getName().substring(downloadFile.getName().indexOf(".") + 1).toLowerCase();
					String mimeType = mimeMap.getMimeTypeFromExtension(downloadFileExt);
					
					// Create the file open intent.
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(localUri), mimeType);
					
					// Try to open the file.
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						toast = Toast.makeText(toastContext,getString(R.string.workbin_file_download_dialog_fragment_no_app_found), Toast.LENGTH_SHORT);
						toast.show();
					}
					
					mDialogFragment.dismiss();
					break;
			}
			
			// Continue monitoring.
			if (mShouldMonitorStatus) {
				mHandler.postDelayed(mDownloadStatusUpdater, 250);
			}
			
			// Clean up.
			cursor.close();
		}
	};
	
	// }}}
	// {{{ methods
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Additional fragment parameters.
		this.setCancelable(false);
		
		// Check if we're just returning.
		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}
		mDownloadInProgress = savedInstanceState.getBoolean("downloadInProgress", false);
		if (mDownloadInProgress) {
			mDownloadId = savedInstanceState.getLong("downloadId");
			mDownloadUrl = Uri.parse(savedInstanceState.getString("downloadUrl"));
			mFileName = savedInstanceState.getString("fileName");
			mFileSize = savedInstanceState.getDouble("fileSize");
		}
		
		// Set up the dialog.
		LayoutInflater inflater = (LayoutInflater) new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog))
			.setNeutralButton("Hide", new onClickHideListener())
			.setNegativeButton("Cancel", new onClickCancelListener())
			.setView(inflater.inflate(R.layout.workbin_file_download_dialog_fragment, null))
			.create();
		return mAlertDialog;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save the state.
		outState.putBoolean("downloadInProgress", mDownloadInProgress);
		outState.putLong("downloadId", mDownloadId);
		outState.putString("downloadUrl", mDownloadUrl.toString());
		outState.putString("fileName", mFileName);
		outState.putDouble("fileSize", mFileSize);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// (Re)connect to download manager service.
		mDownloadManager = new DownloadManager(getActivity().getContentResolver(), getClass().getPackage().getName());
		if (mDownloadInProgress) {
			// Resume the status updater.
			mHandler.post(mDownloadStatusUpdater);
			
		} else {
			// Obtain the download URL.
			Bundle args = getArguments();
			mDownloadUrl = Uri.parse(args.getString("downloadUrl"));
			mFileName = args.getString("fileName");
			mFileSize = args.getDouble("fileSize");
			startDownload();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacksAndMessages(null);
	}
	
	private void startDownload() {
		Log.v(TAG, "starting download of " + mDownloadUrl);

		// Create a new download request.
		DownloadManager.Request request = new DownloadManager.Request(mDownloadUrl);
		// request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
		request.setVisibleInDownloadsUi(false);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileName);
		mDownloadId = mDownloadManager.enqueue(request);
		mDownloadInProgress = true;
		
		// Start monitoring the download status.
		mHandler.post(mDownloadStatusUpdater);
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * What happens when "Hide" is clicked?
	 * @author yjwong
	 */
	class onClickHideListener implements OnClickListener {
		// {{{ methods
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			Log.v(TAG, "hide button clicked");
		}
		
		// }}}
	}
	
	/**
	 * What happens when "Cancel" is clicked?
	 * @author yjwong
	 */
	class onClickCancelListener implements OnClickListener {
		// {{{ methods
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Log.v(TAG, "Download job " + mDownloadId + " cancelled by user");
			mDownloadManager.remove(mDownloadId);
			dialog.dismiss();
		}
		
		// }}}
	}
	
	// }}}
}
