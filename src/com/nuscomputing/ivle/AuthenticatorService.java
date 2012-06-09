package com.nuscomputing.ivle;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authenticator service to perform authentication in the background.
 * Used by the AccountManager as well.
 * @author yjwong
 */
public class AuthenticatorService extends Service {
	// {{{ properties
	
	/** An instance of the authenticator */
	private Authenticator mAuthenticator;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate() {
		mAuthenticator = new Authenticator(this);
		return;
	}
	
	@Override
	public void onDestroy() {
		return;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// XXX: Provide full implementation
		return mAuthenticator.getIBinder();
	}

	// }}}
}
