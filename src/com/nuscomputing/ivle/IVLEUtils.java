package com.nuscomputing.ivle;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.nuscomputing.ivlelapi.IVLE;

/**
 * IVLE-related utility methods.
 * @author yjwong
 */
public class IVLEUtils {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "IVLEUtils";
	
	/** Number of tries to obtain an IVLE instance */
	private static int sGetIVLEInstanceTries = 0;
	
	// }}}
	// {{{ methods
	
	/**
	 * Method: getIVLEInstance
	 * <p>
	 * Gets an instance of a IVLE object, based on the currently active
	 * account.
	 */
	public static IVLE getIVLEInstance(Context context) {
		// Obtain the currently active account.
		Account account = AccountUtils.getActiveAccount(context, false);
		AccountManager manager = AccountManager.get(context);
		
		// Get the authentication token.
		String authToken = null;
		try {
			authToken = manager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
			IVLE ivle = new IVLE(Constants.API_KEY, authToken);
			sGetIVLEInstanceTries = 0;
			return ivle;
			
		} catch (AuthenticatorException e) {
			if (sGetIVLEInstanceTries > 5) {
				Log.w(TAG, "Tried to obtain IVLE instance for more than 5 times, giving up");
				sGetIVLEInstanceTries = 0;
				return null;
			}
			
			Log.w(TAG, "AuthenticatorException while acquiring an IVLE instance");
			manager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authToken);
			try {
				Thread.sleep(250);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			
			// Retry the request.
			sGetIVLEInstanceTries++;
			return IVLEUtils.getIVLEInstance(context);
			
		} catch (IOException e) {
			Log.w(TAG, "IOException while acquiring an IVLE instance");
		} catch (OperationCanceledException e) {
			Log.w(TAG, "OperationCanceledException while acquiring an IVLE instance");
		}
		
		return null;
	}
	
	/**
	 * Method: requestSyncNow
	 * <p>
	 * Requests a sync of all IVLE data now.
	 */
	public static void requestSyncNow(Account account) {
		if (!ContentResolver.isSyncActive(account, Constants.PROVIDER_AUTHORITY)) {
			Bundle args = new Bundle();
			args.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			args.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, args);
		}
	}
	
	/**
	 * Method: filterHTML
	 * <p>
	 * Filters HTML for use in ListViews.
	 */
	public static void filterHTML() {
		
	}
	
	// }}}
}
