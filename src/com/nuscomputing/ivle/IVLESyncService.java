package com.nuscomputing.ivle;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * The primary sync adapter for all announcements.
 * @author yjwong
 */
public class IVLESyncService extends Service {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLESyncService";
	
	/** The sync adapter */
	private static IVLESyncAdapter sSyncAdapter = null;
	
	/** Sync completion flag */
	public static final String ACTION_SYNC_COMPLETE = "com.nuscomputing.ivle.intent.action.SYNC_COMPLETE";
	
	/** Sync canceled flag */
	public static final String ACTION_SYNC_CANCELED = "com.nuscomputing.ivle.intent.action.SYNC_CANCELED";
	
	/** Sync failure flag */
	public static final String ACTION_SYNC_FAILED = "com.nuscomputing.ivle.intent.action.SYNC_FAILED";
	
	// }}}
	// {{{ methods
	
	@Override
	public IBinder onBind(Intent intent) {
		// For some reason, need to call setIsSyncable for Gingerbread.
		if (Build.VERSION.SDK_INT < 11) {
	        AccountManager am = AccountManager.get(this);
	        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
	        for (Account account : accounts) {
	        	ContentResolver.setIsSyncable(account, Constants.PROVIDER_AUTHORITY, 1);
	        }
		}
        
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}
	
	/**
	 * Method: getSyncAdapter
	 * <p>
	 * Gets the sync adapter for this service. If the sync adapter has not been
	 * initialized, initialize it and return.
	 * 
	 * @return AnnouncementsSyncAdapter
	 */
	private IVLESyncAdapter getSyncAdapter() {
		if (sSyncAdapter == null) {
			sSyncAdapter = new IVLESyncAdapter(getApplicationContext(), false);
		}
		
		return sSyncAdapter;
	}
	
	/**
	 * Method: broadcastSyncCanceled
	 * <p>
	 * Sends a system broadcast that our sync has been canceled.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncCanceled(Context context) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_CANCELED);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: broadcastSyncComplete
	 * <p>
	 * Sends a system broadcast that our sync has succeeded.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncSuccess(Context context) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_COMPLETE);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: broadcastSyncFailed
	 * <p>
	 * Sends a system broadcast that our sync has failed.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncFailed(Context context) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_FAILED);
		context.sendBroadcast(intent);
	}
	
	// }}}
}
