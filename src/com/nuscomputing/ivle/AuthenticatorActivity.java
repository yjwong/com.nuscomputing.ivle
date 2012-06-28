package com.nuscomputing.ivle;

import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * UI to authenticate.
 * @author yjwong
 */

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "AuthenticatorActivity";
	
	/** Intent extra: Stores username */
	public static final String PARAM_USERNAME = "username";
	
	/** Intent extra: Stores authentication token type */
	public static final String PARAM_AUTHTOKEN_TYPE = "authTokenType";
	
	/** Intent flag: Confirm credentials */
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
	
	/** Account Manager */
	private AccountManager mAccountManager;
	
	/** Keep track of the login task to we can cancel it when requested */
	private AuthenticationTask mAuthTask = null;
	
	/** Progress dialog to notify user of login progress */
	private ProgressDialog mProgressDialog = null;
	
	/** Alert dialog for various purposes */
	private AlertDialog mAlertDialog = null;
	
	/** Was the original caller asking for an entirely new account? */
	protected boolean mRequestNewAccount = false;
	
	/** Username */
	private String mUsername;
	
	/** Password */
	private String mPassword;
	
	/** Username edit field */
	private EditText mUsernameEdit;
	
	/** Password edit field */
	private EditText mPasswordEdit;
	
	/** Connection state */
	protected boolean mHasConnection = true;
	
	/**
	 * If set, we are just checking that the user knows their credentials; this
	 * doesn't cause the user's password or authToken to be changed on the
	 * device.
	 */
	private Boolean mConfirmCredentials = false;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Call the superclass.
        super.onCreate(savedInstanceState);
        
        // Obtain an instance of the account manager.
        mAccountManager = AccountManager.get(this);
        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mRequestNewAccount = (mUsername == null);
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
        
        // Display the UI.
        setContentView(R.layout.authenticator_activity);
        mUsernameEdit = (EditText) findViewById(R.id.username);
        mPasswordEdit = (EditText) findViewById(R.id.password);
        if (!TextUtils.isEmpty(mUsername)) {
        	mUsernameEdit.setText(mUsername);
        }
    }
    
    /**
     * Method: onPause
     * Part of the Android activity lifecycle.
     */
    @Override
    public void onPause() {
    	// Call the superclass.
    	super.onPause();
    	
    	// If we have any currently opened alert dialogs, dismiss.
    	Log.v(TAG, "onPause: mAlertDialog = " + mAlertDialog);
    	if (mAlertDialog != null) {
    		mAlertDialog.dismiss();
    	}
    }
    
    /**
     * Method: onResume
     * Part of the Android activity lifecycle.
     */
    @Override
    public void onResume() {
    	// Call the superclass.
    	super.onResume();
    	
    	// If we have any previously opened alert dialogs, show.
    	Log.v(TAG, "onResume: mAlertDialog = " + mAlertDialog);
    	if (mAlertDialog != null) {
    		mAlertDialog.show();
    	}
    }
    
    /**
     * Method: handleSignIn
     * Handles onClick event on the "Sign In" button. Sends username/password
     * to the server for authentication. The button is confirmed to call
     * handleSignIn() in the XML.
     * 
     * @param view
     */
    public void handleSignIn(View view) {
    	// Obtain user credentials from form fields.
    	Log.d(TAG, "handleSignIn: handling sign in");
    	
    	// Validate form fields.
    	if (mRequestNewAccount) {
    		mUsername = mUsernameEdit.getText().toString();
    	}
    	
    	if (TextUtils.isEmpty(mUsername)) {
    		final AlertDialog dialog = new AlertDialog.Builder(this).create();
    		dialog.setMessage(getText(R.string.authenticator_activity_enter_nusnet_id));
    		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    		dialog.show();
    		return;
    	}
    	
    	mPassword = mPasswordEdit.getText().toString();
    	if (TextUtils.isEmpty(mPassword)) {
    		final AlertDialog dialog = new AlertDialog.Builder(this).create();
    		dialog.setMessage(getText(R.string.authenticator_activity_enter_password));
    		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    		dialog.show();
    		return;
    	}
    	
    	// Show progress dialog, and kick off a background task to perform
    	// the user login attempt.
    	showProgress();
    	mAuthTask = new AuthenticationTask();
    	mAuthTask.execute();
    }
    
    /**
     * Method: handleCancel
     * Handles onClick event on the "Cancel" button.
     * 
     * @param view
     */
    public void handleCancel(View view) {
    	setResult(RESULT_CANCELED);
    	finish();
    }
    
    /**
     * Method: showProgress
     * Shows a progress dialog to indicate login activity.
     */
    public void showProgress() {
    	final ProgressDialog dialog = new ProgressDialog(this);
    	dialog.setMessage(getText(R.string.signing_in));
    	dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	dialog.setIndeterminate(true);
    	dialog.setCancelable(true);
    	dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mAuthTask != null) {
					mAuthTask.cancel(true);
				}
			}
		});
    	
    	mProgressDialog = dialog;
    	dialog.show();
    }
    
    /**
     * Method: hideProgress
     * Hides the progress dialog.
     */
    public void hideProgress() {
    	if (mProgressDialog != null) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
    	}
    }
    
    /**
     * Method: onAuthenticationCancel
     * Called when the authentication process is cancelled.
     */
    public void onAuthenticationCancel() {
    	// Our task is complete, so clear it out.
    	mAuthTask = null;
    	
    	// Hide the progress dialog.
    	hideProgress();
    }
    
    /**
     * Method: onAuthenticationResult
     * Called when the authentication process completes.
     * 
     * @param authToken
     */
    public void onAuthenticationResult(String authToken) {
    	// Check result of authentication.
    	Log.v(TAG, "onAuthenticationResult: authToken = " + authToken);
    	boolean success = ((authToken != null) && (authToken.length() > 0));
    	
    	// Our task is complete, so clear it out.
    	mAuthTask = null;
    	
    	// Hide the progress dialog.
    	hideProgress();
    	
    	// Inform the invoker about the result.
    	if (success) {
    		if (!mConfirmCredentials) {
    			finishLogin(authToken);
    		} else {
    			finishConfirmCredentials(success);
    		}
    		
    	} else {
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
    		if (mHasConnection) {
        		if (mRequestNewAccount) {
            		dialog.setMessage(getText(R.string.authenticator_activity_nusnet_id_or_password_invalid));
        		} else {
        			// Used when the account is already in the database but the
        			// password doesn't work.
            		dialog.setMessage(getText(R.string.authenticator_activity_password_invalid));
        		}

    		} else {
    			dialog.setMessage(getText(R.string.authenticator_activity_problem_communicating_with_server));
    		}
    		
    		// Set button parameters and show the dialog.
    		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mAlertDialog = null;
				}
			});
    		dialog.show();
    		mAlertDialog = dialog;
    	}
    }
    
    /**
     * Method: finishLogin
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store
     * the authToken that's returned from the server as the "password" for
     * this account - so we're never storing the user's actual password
     * locally.
     * 
     * @param result
     */
    private void finishLogin(String authToken) {
    	final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
    	if (mRequestNewAccount) {
    		mAccountManager.addAccountExplicitly(account, mPassword, null);
    	} else {
    		mAccountManager.setPassword(account, mPassword);
    	}
    	
    	// Send a toast notification.
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, getText(R.string.authenticator_activity_account_successfully_added), duration);
    	toast.show();
    	
    	// Activate sync automatically.
    	Resources res = getResources();
    	ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITY, new Bundle());
    	ContentResolver.setSyncAutomatically(account, Constants.PROVIDER_AUTHORITY, true);
    	ContentResolver.addPeriodicSync(account, Constants.PROVIDER_AUTHORITY, new Bundle(), res.getInteger(R.integer.default_sync_interval) * 60 * 60);
    	
    	// Say we have a pending sync.
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor prefsEditor = prefs.edit();
    	prefsEditor.putBoolean(IVLESyncService.KEY_SYNC_IN_PROGRESS + "_" + mUsername, true);
    	prefsEditor.commit();
    	
    	// Return to the caller.
    	final Intent intent = new Intent();
    	intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
    	intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
    	setAccountAuthenticatorResult(intent.getExtras());
    	
    	// Okay, done.
    	setResult(RESULT_OK, intent);
    	finish();
    }
    
    /**
     * Method: finishConfirmCredentials
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     * 
     * @param result
     */
    private void finishConfirmCredentials(boolean result) {
    	// Set the password.
    	final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
    	mAccountManager.setPassword(account, mPassword);
    	
    	// Return to the caller.
    	final Intent intent = new Intent();
    	intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
    	setAccountAuthenticatorResult(intent.getExtras());
    	setResult(RESULT_OK, intent);
    	finish();
    }
    
    // }}}
    // {{{ classes
    
    /**
     * An asynchronous task to authenticate the user.
     * @author yjwong
     */
    public class AuthenticationTask extends AsyncTask<Void, Void, String> {
    	// {{{ methods
    	
		@Override
		protected String doInBackground(Void... params) {
			Log.v(TAG, "AuthenticationTask: started");
			mHasConnection = true;
			
			try {				
				IVLE ivle;
				ivle = new IVLE(Constants.API_KEY, mUsername, mPassword);
				return ivle.authToken;
			} catch (FailedLoginException e) {
				Log.v(TAG, "AuthenticationTask: got FailedLoginException");
				return null;
			} catch (NetworkErrorException e) {
				Log.v(TAG, "AuthenticationTask: got NetworkErrorException");
				mHasConnection = false;
				return null;
			} catch (JSONParserException e) {
				Log.v(TAG, "AuthenticationTask: got XMLParserException");
				throw new IllegalStateException();
			}
		}
		
		@Override
		protected void onPostExecute(final String authToken) {
			// On a successful authentication, call back into the Activity to
			// communicate the authToken (or null for an error)
			onAuthenticationResult(authToken);
		}
		
		@Override
		protected void onCancelled() {
			// If the action was cancelled (by the user touching the cancel
			// button in the progress dialog), then call back into the activity
			// to let it know.
			onAuthenticationCancel();
		}
    	
		// }}}
    }
    
    // }}}
}
