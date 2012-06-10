package com.nuscomputing.ivle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to view an announcement.
 * @author yjwong
 */
public class ViewAnnouncementFragment extends Fragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewAnnouncementFragment";
	
	/** The announcement ID */
	private long mAnnouncementId = -1;
	
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
		
		// Obtain the announcement ID.
		ViewAnnouncementActivity activity = (ViewAnnouncementActivity) getActivity();
		mAnnouncementId = activity.announcementId;
        if (mAnnouncementId == -1) {
        	throw new IllegalStateException("No announcement ID was passed to ViewAnnouncementFragment");
        }
        
		// Load the module data.
        Bundle args = new Bundle();
        args.putLong("announcementId", mAnnouncementId);
        DataLoader loader = new DataLoader(getActivity());
		getLoaderManager().initLoader(DataLoader.VIEW_ANNOUNCEMENT_FRAGMENT_LOADER, args, loader);
	}
	
	// }}}
}
