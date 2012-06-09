package com.nuscomputing.ivle;

import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Pluggable authentication module for IVLE.
 * @author yjwong
 */

public class Authenticator extends AbstractAccountAuthenticator {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "Authenticator";
	
	/** Stores the current context */
	private Context mContext;
	
	// }}}
	// {{{ methods
	
	public Authenticator(Context context) {
		super(context);
		mContext = context;
	}
	
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType, String[] requiredFeatures,
			Bundle options) throws NetworkErrorException {
		// Bundle to notify AccountManager that we need the user to perform
		// the authentication. The intent points towards our own IVLE auth UI.
		Log.v(TAG, "addAccount: request to add new account");
		final Bundle result;
		final Intent intent;
		
		intent = new Intent(mContext, AuthenticatorActivity.class);
		intent.putExtra(Constants.AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
				response);
		
		result = new Bundle();
		result.putParcelable(AccountManager.KEY_INTENT, intent);
		
		return result;
	}
	
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		return null;
	}
	
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// If the caller requested an authToken type we don't support, then
		// return an error.
		Log.v(TAG, "getAuthToken: requesting an authentication token");
		if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE)) {
			final Bundle result = new Bundle();
			Log.e(TAG, "getAuthToken: invalid authToken type (this should never happen)");
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authToken type");
			return result;
		}
		
		// Extract the username and password from the AccountManager, and ask
		// the server for an appropriate authToken.
		Log.v(TAG, "getAuthToken: invoking AccountManager");
		final AccountManager am = AccountManager.get(mContext);
		final String password = am.getPassword(account);
		if (password != null) {
			try {
				IVLE ivle = new IVLE(Constants.API_KEY, account.name, password);
				final Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
				result.putString(AccountManager.KEY_AUTHTOKEN, ivle.authToken);
				return result;
				
			} catch (JSONParserException ie) {
				throw new IllegalStateException();
			} catch (com.nuscomputing.ivlelapi.NetworkErrorException nee) {
				throw new NetworkErrorException();
			} catch (com.nuscomputing.ivlelapi.FailedLoginException fle) {
				// Ignore and continue.
				Log.v(TAG, "Login failed, requesting new credentials");
			}
		}
		
		// If we get here, then we couldn't access the user's password - so we
		// need to re-prompt them for their credentials. We do that by creating
		// an intent to display our AuthenticatorActivity panel.
		final Intent intent = new Intent(mContext, AuthenticatorActivity.class	);
		intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
		intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}
	
	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// We don't support multiple authToken types.
		return null;
	}
	
	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		// We don't expect to get called, so we always return a "no" for any
		// queries.
		final Bundle result = new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
		return result;
	}
	
	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}
	
	// }}}
}
