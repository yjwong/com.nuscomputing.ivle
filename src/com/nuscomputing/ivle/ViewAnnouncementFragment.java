package com.nuscomputing.ivle;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Fragment to view an announcement.
 * @author yjwong
 */
public class ViewAnnouncementFragment extends SherlockFragment
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewAnnouncementFragment";
	
	/** The announcement ID */
	private long mAnnouncementId = -1;
	
	/** Results from the loader */
	private Bundle mLoaderResult;
	
	/** The sharing description */
	private String mShareDescription;
	
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
		
		// Get the Action Bar.
		ActionBar bar = getSherlockActivity().getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		// Obtain the announcement ID.
		Bundle args = getArguments();
		mAnnouncementId = args.getLong("announcementId");
        if (mAnnouncementId == -1) {
        	throw new IllegalStateException("No announcement ID was passed to ViewAnnouncementFragment");
        }
        
		// Load the module data.
        DataLoader loader = new DataLoader(getActivity(), this);
		getLoaderManager().initLoader(DataLoader.LOADER_VIEW_ANNOUNCEMENT_FRAGMENT, args, loader);
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
				intent.putExtra(Intent.EXTRA_TEXT, mShareDescription);
				startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
				return true;
				
			case R.id.view_announcement_fragment_menu_details:
				// Add the details.
				Map<String, String> detailsMap = new LinkedHashMap<String, String>();
				detailsMap.put("Title", mLoaderResult.getString("title"));
				detailsMap.put("Creator", mLoaderResult.getString("userName"));
				detailsMap.put("Created", mLoaderResult.getString("createdDate"));
				detailsMap.put("Expiry", mLoaderResult.getString("expiryDate"));
				detailsMap.put("URL", mLoaderResult.getString("url"));
				
				// Define dialog fragment arguments.
				Bundle fragmentArgs = new Bundle();
				fragmentArgs.putSerializable("items", (Serializable) detailsMap);
				fragmentArgs.putString("title", getString(R.string.details));
				
				// Create the fragment.
				DialogFragment fragment = new DetailsDialogFragment();
				fragment.setArguments(fragmentArgs);
				
				// Add the fragment.
				FragmentManager manager = getFragmentManager();
				fragment.show(manager, null);
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void onLoaderFinished(Bundle result) {
		// Set the title and subtitle.
		ActionBar bar = getSherlockActivity().getSupportActionBar();
		bar.setTitle(result.getString("title"));
		bar.setSubtitle(result.getString("userName"));
		
		// Set the content for the webview.
		WebView wvDescription = (WebView) getActivity().findViewById(R.id.view_announcement_fragment_webview);
		wvDescription.getSettings().setBuiltInZoomControls(true);
		wvDescription.loadData(result.getString("description"), "text/html", null);
		
		// Save the loader result.
		mLoaderResult = result;
		
		// Process the sharing description.
		mShareDescription = Html.fromHtml(result.getString("description")).toString();
		
		// Mark the announcement as read.
		if (!result.getBoolean("isRead", false)) {
			Log.v(TAG, "Announcement is not read, marking it as read. ");
			Intent svcIntent = new Intent(getActivity(), IVLEService.class);
			svcIntent.putExtra("taskType", IVLEService.TASK_ANNOUNCEMENT_MARK_AS_READ);
			svcIntent.putExtra("announcementIvleId", result.getString("ivleId"));
			svcIntent.putExtra("announcementId", mAnnouncementId);
			getActivity().startService(svcIntent);
		}
	}
	
	// }}}
}
