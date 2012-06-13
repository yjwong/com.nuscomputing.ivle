package com.nuscomputing.ivle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;
import android.app.ActionBar;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

/**
 * Activity to view webcast videos.
 * @author yjwong
 */
public class ViewWebcastFileActivity extends FragmentActivity implements 
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
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
	
	/** Checks if the video is already playing */
	Handler mHandler = new Handler();
	Runnable mPlayingChecker = new Runnable() {
		public void run() {
			if (mVideoView.isPlaying()) {
				mProgressView.setVisibility(View.GONE);
			} else {
				mHandler.postDelayed(mPlayingChecker, 250);
			}
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
			getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.view_webcast_file_activity_action_bar_background));
			
			ActionBar bar = getActionBar();
			bar.setHomeButtonEnabled(true);
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
        DataLoader loader = new DataLoader(this);
		getSupportLoaderManager().initLoader(DataLoader.VIEW_WEBCAST_FILE_ACTIVITY_LOADER, args, loader);
		
		// Restore video position.
		if (savedInstanceState != null) {
			Log.v(TAG, "Restoring video position");
			int videoCurrentPosition = savedInstanceState.getInt("videoCurrentPosition", -1);
			if (videoCurrentPosition != -1) {
				mVideoView.seekTo(videoCurrentPosition);
			}
		}
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
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection.
    	switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
    	}
    }
    
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
    	mHandler.removeCallbacksAndMessages(null);
    	mProgressView.setVisibility(View.GONE);
    	return false;
    }
    
    public void onCompletion(MediaPlayer player) {
    	// Nothing to here.
    }
    
    public void setVideoUri(Uri uri) {
    	mVideoUri = uri;
    }
	
	// }}}
}
