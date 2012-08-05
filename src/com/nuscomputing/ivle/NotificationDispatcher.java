package com.nuscomputing.ivle;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Activity to open other activities from a notification.
 * @author yjwong
 *
 */
public class NotificationDispatcher extends SherlockActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "NotificationDispatcherActivity";
	
	/** Constants for notification IDs */
	public static final int NOTIFICATION_ANNOUNCEMENT_SINGLE = 0;
	public static final int NOTIFICATION_ANNOUNCEMENT_MANY = 1;
	public static final int NOTIFICATION_MAX = 2;
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get intent.
        Intent intent = getIntent();
        int type = intent.getIntExtra("notificationType", -1);
        if (type == -1) {
        	Log.w(TAG, "invalid notification type " + type + " received");
        }
        Log.v(TAG, "notificationType = " + type);
        
        // Switch the account.
        String withAccount = intent.getStringExtra("withAccount");
        if (withAccount != null) {
        	AccountUtils.setActiveAccount(this, withAccount);
        }
        
        // Create the outgoing intent.
        Intent outIntent = new Intent();
        outIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        outIntent.putExtras(intent);
        
        // Open the correct activity.
        switch (type) {
        	case NOTIFICATION_ANNOUNCEMENT_SINGLE:
        		outIntent.setClass(getApplicationContext(), ViewAnnouncementActivity.class);
        		startActivity(outIntent);
        		break;
        	
        	case NOTIFICATION_ANNOUNCEMENT_MANY:
        		startMainActivity();
        		break;
        	
        	default:
        		// Unrecognized notification found, open the main activity.
        		startMainActivity();
        }
        
        finish();
    }
    
    /**
     * Method: createIntent
     * <p>
     * Creates an intent for use with this notification dispatcher.
     */
    public static Intent createIntent(Context context, int type, String withAccount) {
    	// Validate the type.
    	if (type >= NOTIFICATION_MAX || type < 0) {
    		throw new IllegalArgumentException("Invalid notification type " + type);
    	}
    	
		Intent intent = new Intent(context, NotificationDispatcher.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("notificationType", type);
		intent.putExtra("withAccount", withAccount);
		return intent;
    }
    
    public static Intent createIntent(Context context, int type) {
    	return createIntent(context, type, null);
    }
    
    /**
     * Method: startMainActivity
     * <p>
     * Starts the main activity. Used when the received notification intent is
     * either unrecognized or incomplete.
     */
    private void startMainActivity() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), MainActivity.class);
		startActivity(intent);
    }
	
	// }}}
}
