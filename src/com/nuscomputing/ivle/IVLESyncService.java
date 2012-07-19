package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.AnnouncementsContract;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

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
	
	/** Sync started flag */
	public static final String ACTION_SYNC_STARTED = "com.nuscomputing.ivle.intent.action.SYNC_STARTED";
	
	/** Sync success flag */
	public static final String ACTION_SYNC_SUCCESS = "com.nuscomputing.ivle.intent.action.SYNC_SUCCESS";
	
	/** Sync completion flag */
	public static final String ACTION_SYNC_COMPLETE = "com.nuscomputing.ivle.intent.action.SYNC_COMPLETE";
	
	/** Sync canceled flag */
	public static final String ACTION_SYNC_CANCELED = "com.nuscomputing.ivle.intent.action.SYNC_CANCELED";
	
	/** Sync failure flag */
	public static final String ACTION_SYNC_FAILED = "com.nuscomputing.ivle.intent.action.SYNC_FAILED";
	
	/** SharedPreferences key for sync_in_progress */
	public static final String KEY_SYNC_IN_PROGRESS = "sync_in_progress";
	
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
	 * Method: getSyncInProgressKey
	 * <p>
	 * Gets the shared preference key for sync status.
	 */
	public static String getSyncInProgressKey(Account account) {
		return KEY_SYNC_IN_PROGRESS + "_" + account.name;
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
	 * Method: isSyncInProgress
	 * <p>
	 * Returns true if a sync is in progress for the specified account, false
	 * otherwise.
	 */
	public static boolean isSyncInProgress(Context context, Account account) {
		return IVLESyncAdapter.isSyncInProgress(context, account);
	}
	
	/**
	 * Method: broadcastSyncStarted
	 * <p>
	 * Sends a system broadcast that our sync is started.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncStarted(Context context, Account account) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_STARTED);
		intent.putExtra("com.nuscomputing.ivle.Account", account);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: broadcastSyncCanceled
	 * <p>
	 * Sends a system broadcast that our sync has been canceled.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncCanceled(Context context, Account account) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_CANCELED);
		intent.putExtra("com.nuscomputing.ivle.Account", account);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: broadcastSyncSuccess
	 * <p>
	 * Sends a system broadcast that our sync has succeeded.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncSuccess(Context context, Account account) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_SUCCESS);
		intent.putExtra("com.nuscomputing.ivle.Account", account);
		context.sendBroadcast(intent);
		IVLESyncService.onSyncSuccess(context, account);
	}
	
	/**
	 * Method: broadcastSyncComplete
	 * <p>
	 * Sends a system broadcast that our sync has completed.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncComplete(Context context, Account account) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_COMPLETE);
		intent.putExtra("com.nuscomputing.ivle.Account", account);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: broadcastSyncFailed
	 * <p>
	 * Sends a system broadcast that our sync has failed.
	 * This is made because there is not many ways of knowing the status of a
	 * sync operation in Android's current sync framework.
	 */
	public static void broadcastSyncFailed(Context context, Account account) {
		Intent intent = new Intent(IVLESyncService.ACTION_SYNC_FAILED);
		intent.putExtra("com.nuscomputing.ivle.Account", account);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Method: onSyncSuccess
	 * <p>
	 * Called when the sync was successful. Currently used to display
	 * notifications.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(16)
	private static void onSyncSuccess(Context context, Account account) {
		// Check if we enable notifications.
		SharedPreferences prefs = context.getSharedPreferences("account_" + account.name, Context.MODE_PRIVATE);
		boolean notifsEnabled = prefs.getBoolean("notifications", true);
		boolean notifsAnnouncementsEnabled = prefs.getBoolean("notifications_announcements", true);
		
		if (notifsEnabled) {
			if (notifsAnnouncementsEnabled) {
				// Get unread announcements.
				ContentResolver resolver = context.getContentResolver();
				ContentProviderClient provider = resolver.acquireContentProviderClient(AnnouncementsContract.CONTENT_URI);
				try {
					Cursor cursor = provider.query(
							AnnouncementsContract.CONTENT_URI,
							new String[] { AnnouncementsContract.ID, AnnouncementsContract.TITLE },
							AnnouncementsContract.IS_READ.concat(" = ?").concat(" AND ")
								.concat(DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME).concat(".").concat(AnnouncementsContract.ACCOUNT).concat(" = ?"),
							new String[] { "0", account.name },
							null
					);
					
					// Get the notification manager.
					NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					int announcementCount = cursor.getCount();
					
					// Determine how we show the notification based on the
					// number of announcements.
					if (announcementCount > 0) {
						Notification notif = null;
						cursor.moveToFirst();
						
						if (announcementCount == 1) {
							// Obtain the details for the only announcement.
							String notifTitle = cursor.getString(cursor.getColumnIndex(AnnouncementsContract.TITLE));
							long announcementId = cursor.getLong(cursor.getColumnIndex(AnnouncementsContract.ID));
							
							// Create a pending intent.
							Intent intent = new Intent(context, ViewAnnouncementActivity.class);
							intent.putExtra("announcementId", announcementId);
							PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
							
							// Create notification based on platform version.
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								Notification.Builder builder = new Notification.Builder(context)
									.setContentTitle(notifTitle)
									.setContentText(account.name)
									.setContentIntent(pendingIntent)
									.setSmallIcon(R.drawable.ic_launcher);
								
								notif = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ? builder.build() : builder.getNotification();
							} else {
								notif = new Notification(R.drawable.ic_launcher, notifTitle, System.currentTimeMillis());
								notif.setLatestEventInfo(context, notifTitle, account.name, pendingIntent);
							}
	
						} else {
							String notifTitle = Integer.toString(announcementCount).concat(" new announcements");
							
							// Create a pending intent.
							Intent intent = new Intent(context, MainActivity.class);
							PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
							
							// Create notification based on platform version.
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								Notification.Builder builder = new Notification.Builder(context)
									.setContentTitle(notifTitle)
									.setContentText(account.name)
									.setContentIntent(pendingIntent)
									.setSmallIcon(R.drawable.ic_launcher);
								
								// Use the inbox style on Jellybean.
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
									Notification.InboxStyle builderInboxStyle = new Notification.InboxStyle(builder);
									
									// We also want to limit the number of lines to 5.
									int a = 0;
									while (!cursor.isAfterLast() && a < 5) {
										builderInboxStyle.addLine(cursor.getString(cursor.getColumnIndex(AnnouncementsContract.TITLE)));
										cursor.moveToNext();
										a++;
									}
									
									// Tap to show the rest.
									if (announcementCount > 5) {
										builderInboxStyle.setSummaryText("+" + (announcementCount - 5) + " more");
									}
									
									notif = builderInboxStyle.build();
									
								} else {
									notif = builder.getNotification();
								}
								
							} else {
								notif = new Notification(R.drawable.ic_launcher, notifTitle, System.currentTimeMillis());
								notif.setLatestEventInfo(context, notifTitle, account.name, pendingIntent);
							}
						}
						
						// Send the notification.
						manager.notify(0, notif);
					}
					
				} catch (RemoteException e) {
					Log.e(TAG, "Error trying to check for unread announcements");
				}
			}
		}
	}
	
	// }}}
}
