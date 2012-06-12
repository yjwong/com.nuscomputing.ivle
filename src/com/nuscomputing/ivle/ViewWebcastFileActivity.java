package com.nuscomputing.ivle;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.os.Build;
import android.os.Bundle;

/**
 * Activity to view webcast videos.
 * @author yjwong
 */
public class ViewWebcastFileActivity extends FragmentActivity {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastFileActivity";
	
	// }}}
	// {{{ methods
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the webcast file ID.
		Bundle bundle = getIntent().getExtras();
		long webcastFileId = bundle.getLong("webcastFileId", -1);
		if (webcastFileId == -1) {
			throw new IllegalStateException("No webcast file ID was passed to ViewWebcastFileActivity");
		}
		
		// Load the activity and start video playback.
        Bundle args = new Bundle();
        args.putLong("webcastFileId", webcastFileId);
        DataLoader loader = new DataLoader(this);
		getSupportLoaderManager().initLoader(DataLoader.VIEW_WEBCAST_FILE_ACTIVITY_LOADER, args, loader);
		
		// Set the content view.
		setContentView(R.layout.view_webcast_file_activity);
		
		// Set to full screen.
		if (Build.VERSION.SDK_INT >= 14) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			// getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}
	
	// }}}
}
