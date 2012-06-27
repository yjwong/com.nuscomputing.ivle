package com.nuscomputing.ivle;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Fragment to view an announcement.
 * @author yjwong
 */
public class ViewAnnouncementFragment extends Fragment
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewAnnouncementFragment";
	
	/** The announcement ID */
	private long mAnnouncementId = -1;
	
	/** Results from the loader */
	private Bundle mLoaderResult;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.view_announcement_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// We have a menu to populate.
		this.setHasOptionsMenu(true);
		
		// Obtain the announcement ID.
		ViewAnnouncementActivity activity = (ViewAnnouncementActivity) getActivity();
		mAnnouncementId = activity.announcementId;
        if (mAnnouncementId == -1) {
        	throw new IllegalStateException("No announcement ID was passed to ViewAnnouncementFragment");
        }
        
		// Load the module data.
        Bundle args = new Bundle();
        args.putLong("announcementId", mAnnouncementId);
        DataLoader loader = new DataLoader(getActivity(), this);
		getLoaderManager().initLoader(DataLoader.VIEW_ANNOUNCEMENT_FRAGMENT_LOADER, args, loader);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.view_announcement_fragment_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.view_announcement_fragment_menu_share:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, mLoaderResult.getString("title"));
				intent.putExtra(Intent.EXTRA_TEXT, mLoaderResult.getString("description"));
				startActivity(Intent.createChooser(intent, "Share via"));
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void onLoaderFinished(Bundle result) {
		// Set the title and subtitle.
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar bar = getActivity().getActionBar();
			bar.setTitle(result.getString("title"));
			bar.setSubtitle(result.getString("userName"));
		}
		
		// Set the content for the webview.
		WebView wvDescription = (WebView) getActivity().findViewById(R.id.view_announcement_fragment_webview);
		wvDescription.loadData(result.getString("description"), "text/html", null);
		
		// Save the loader result.
		mLoaderResult = result;
	}
	
	// }}}
}
