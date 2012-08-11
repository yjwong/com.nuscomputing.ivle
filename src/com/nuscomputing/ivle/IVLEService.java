package com.nuscomputing.ivle;

import java.io.IOException;
import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivlelapi.Announcement;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.NetworkErrorException;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

/**
 * The main service providing access to IVLE.
 * @author yjwong
 */
public class IVLEService extends IntentService {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLEService";
	
	/** Task types */
	public final static int TASK_ANNOUNCEMENT_MARK_AS_READ = 0;
	public final static int TASK_MAX = 1;
	
	// }}}
	// {{{ methods

	public IVLEService() {
		super("IVLEService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Get the task type.
		int taskType = intent.getIntExtra("taskType", -1);
		if (taskType == -1 || taskType >= TASK_MAX) {
			Log.e(TAG, "Invalid task type specified");
		}
		
		// Do work.
		switch (taskType) {
			case TASK_ANNOUNCEMENT_MARK_AS_READ:
				// Obtain the announcement ID.
				String ivleId = intent.getStringExtra("announcementIvleId");
				if (ivleId == null) {
					Log.e(TAG, "Missing announcement IVLE id");
					break;
				}
				
				long id = intent.getLongExtra("announcementId", -1);
				if (id == -1) {
					Log.e(TAG, "Missing announcement id");
					break;
				}
				
				new TaskAnnouncementMarkAsRead(this).execute(intent.getExtras());
				break;
				
			default:
				Log.e(TAG, "Unable to handle task of type " + taskType);
		}
	}

	// }}}
	// {{{ classes
	
	/**
	 * AsyncTask to mark an announcement as read.
	 * @author yjwong
	 */
	class TaskAnnouncementMarkAsRead extends AsyncTask<Bundle,Void,Void> {
		// {{{ properties
		
		/** The context */
		private Context mContext;
		
		// }}}
		// {{{ methods
		
		TaskAnnouncementMarkAsRead(Context context) {
			mContext = context;
		}

		@Override
		protected Void doInBackground(Bundle... params) {
			// Sanity checks.
			if (params.length != 1) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Wrong number of parameters specified");
			}
			
			// Get the announcement ID.
			String ivleId = params[0].getString("announcementIvleId");
			long id = params[0].getLong("announcementId");
			
			// Obtain an IVLE instance and mark announcement as read.
			try {
				IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
				Announcement.addLog(ivle, ivleId);
				
			} catch (NetworkErrorException e) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Unexpected NetworkErrorException");
			} catch (IOException e) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Unexpected IOException");
			} catch (JSONParserException e) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Unexpected JSONParserException");
			} catch (FailedLoginException e) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Unexpected FailedLoginException");
			}
			
			// Mark the local announcement as read.
			try {
				ContentResolver resolver = mContext.getContentResolver();
				ContentProviderClient provider = resolver.acquireContentProviderClient(AnnouncementsContract.CONTENT_URI);
				ContentValues values = new ContentValues();
				values.put(AnnouncementsContract.IS_READ, 1);
				
				// XXX: We need to be a little more defensive here.
				provider.update(
						AnnouncementsContract.CONTENT_URI,
						values,
						AnnouncementsContract.ID.concat("= ?"),
						new String[] { Long.toString(id) }
				);
				
			} catch (RemoteException e) {
				Log.e(TAG, "TaskAnnouncementMarkAsRead: Unexpected RemoteException");
			}

			return null;
		}
		
		// }}}
	}
	
	// }}}
}
