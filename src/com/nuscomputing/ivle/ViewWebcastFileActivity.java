package com.nuscomputing.ivle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Activity to view webcast videos.
 * @author yjwong
 */
@TargetApi(11)
public class ViewWebcastFileActivity extends FragmentActivity implements 
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
		DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastFileActivity";
	
	/** The video view */
	private VideoView mVideoView;
	
	/** The progress view */
	private View mProgressView;
	
	/** The media controller */
	private MediaController mMediaController;
	
	/** The position of the video when paused */
	private int mPositionWhenPaused;
	
	/** Was the video playing when paused? */
	private boolean mWasPlayingWhenPaused;
	
	/** The URL of the video */
	private Uri mVideoUri;
	
	/** The file name of the video */
	private String mVideoFileName;
	
	/** Checks if the video is already playing */
	Handler mHandler = new Handler();
	Runnable mPlayingChecker = new Runnable() {
		public void run() {
			if (mVideoView.isPlaying()) {
				mProgressView.setVisibility(View.GONE);
				
				// Once the video is playing, hide the action bar.
				mHandler.postDelayed(mActionBarHider, 3000);
				
			} else {
				mHandler.postDelayed(mPlayingChecker, 250);
			}
		}
	};
	
	/** Hides the action bar */
	Runnable mActionBarHider = new Runnable() {
		public void run() {
			ActionBar bar = getActionBar();
			bar.hide();
			
			// Set to low profile mode again.
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	
	/** Shows the action bar */
	Runnable mActionBarShower = new Runnable() {
		public void run() {
			ActionBar bar = getActionBar();
			bar.show();
		}
	};

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
		
		// Use overlaid action bar.
		if (Build.VERSION.SDK_INT >= 11) {
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
			getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
			getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.view_webcast_file_activity_action_bar_background));
			
			ActionBar bar = getActionBar();
			bar.setDisplayHomeAsUpEnabled(true);
		}
		
		// Set to full screen.
		if (Build.VERSION.SDK_INT >= 14) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		
		// Set the content view.
		setContentView(R.layout.view_webcast_file_activity);
		
		// Obtain the views.
		mVideoView = (VideoView) findViewById(R.id.view_webcast_file_video_view);
		mProgressView = (View) findViewById(R.id.view_webcast_file_progress_view);
		
		// Check if the video is playing, then hide the loading indicator
		// once buffering is complete.
		mHandler.postDelayed(mPlayingChecker, 250);
		
		// Set the media controller UI.
		mMediaController = new MediaController(this);
		mVideoView.setMediaController(mMediaController);
		mVideoView.requestFocus();
		
		// Load the activity and start video playback.
        Bundle args = new Bundle();
        args.putLong("webcastFileId", webcastFileId);
        DataLoader loader = new DataLoader(this, this);
		getSupportLoaderManager().initLoader(DataLoader.VIEW_WEBCAST_FILE_ACTIVITY_LOADER, args, loader);
		
		// Restore video position.
		if (savedInstanceState != null) {
			Log.v(TAG, "Restoring video position");
			int videoCurrentPosition = savedInstanceState.getInt("videoCurrentPosition", -1);
			if (videoCurrentPosition != -1) {
				mVideoView.seekTo(videoCurrentPosition);
			}
		}
		
		// Hide/show the action bar on touches.
		getWindow().getDecorView().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHandler.removeCallbacks(mActionBarHider);
					mHandler.post(mActionBarShower);
					mHandler.postDelayed(mActionBarHider, 3000);
				}
				return false;
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Get the current position.
		int videoCurrentPosition = mVideoView.getCurrentPosition();
		outState.putInt("videoCurrentPosition", videoCurrentPosition);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Pause the video.
		mPositionWhenPaused = mVideoView.getCurrentPosition();
		mWasPlayingWhenPaused = mVideoView.isPlaying();
		mVideoView.stopPlayback();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Resume video playback.
		if (mPositionWhenPaused >= 0) {
			mVideoView.setVideoURI(mVideoUri);
			mVideoView.seekTo(mPositionWhenPaused);
			mPositionWhenPaused = -1;
			if (mWasPlayingWhenPaused) {
				mMediaController.show(0);
			}
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.view_webcast_file_activity_menu, menu);
    	inflater.inflate(R.menu.global, menu);
    	return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	if (!MainApplication.onOptionsItemSelected(this, item)) {
	    	// Handle item selection.
	    	switch (item.getItemId()) {
	    		case R.id.view_webcast_file_activity_menu_open_in_browser:
	    			Intent intent = new Intent(Intent.ACTION_VIEW);
	    			intent.setData(Uri.parse(mVideoFileName));
	    			startActivity(intent);
	    			return true;
	    			
	    		case R.id.view_webcast_file_activity_menu_save:
	    			// Obtain the preference manager.
	    			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    			
	    			// Check if we allow downloads over mobile network.
	    			boolean downloadOverMobile = prefs.getBoolean("download_over_mobile_networks", true)	;
	    			int allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI;
	    			allowedNetworkTypes |= downloadOverMobile ? DownloadManager.Request.NETWORK_MOBILE : 0; 
	    			
	    			// Get connectivity state.
	    			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    			NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
	    			int networkType = networkInfo.getType();
	    			
	    			// Construct video file name.
	    			String fileName = mVideoUri.getLastPathSegment();
	    			//String filePath = "IVLE Webcasts/" + fileName;
	    			DownloadManager.Request request = new DownloadManager.Request(mVideoUri);
	    			
	    			// Request a save of the video file.
	    			request.allowScanningByMediaScanner();
	    			request.setDescription("IVLE Webcast");
	    			request.setAllowedNetworkTypes(allowedNetworkTypes);
	    			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName);
	    			DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	    			manager.enqueue(request);
	    			
	    			// Show a toast.
	    			if (networkType == ConnectivityManager.TYPE_MOBILE && !downloadOverMobile) {
	    				Toast.makeText(this, getString(R.string.view_webcast_file_activity_webcast_queued_wifi), Toast.LENGTH_SHORT)
	    					.show();
	    			} else {
	    				Toast.makeText(this, getString(R.string.view_webcast_file_activity_webcast_queued), Toast.LENGTH_SHORT)
	    					.show();
	    			}
	    			
	    			return true;
	
				case android.R.id.home:
					finish();
					return true;
					
				default:
					return super.onOptionsItemSelected(item);
	    	}
	    	
    	} else {
    		return true;
    	}
    }
    
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
    	mHandler.removeCallbacksAndMessages(null);
    	mProgressView.setVisibility(View.GONE);
    	return false;
    }
    
    public void onCompletion(MediaPlayer player) {
    	finish();
    }
    
    public void setVideoUri(Uri uri) {
    	mVideoUri = uri;
    }
    
    public void setVideoFileName(String fileName) {
    	mVideoFileName = fileName;
    }
    
    public void onLoaderFinished(Bundle result) {
		// Set the title.
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setTitle(result.getString("fileTitle"));
		}
		
		// Start the video playback.
		Log.v(TAG, "video = " + result.getString("MP4"));
		Uri videoUri = Uri.parse(result.getString("MP4"));
		setVideoUri(videoUri);
		VideoView videoView = (VideoView) findViewById(R.id.view_webcast_file_video_view);
		videoView.setVideoURI(videoUri);
		videoView.start();
		
		// Set the file name.
		String fileName = result.getString("fileName");
		setVideoFileName(fileName);
    }
	
	// }}}
}
