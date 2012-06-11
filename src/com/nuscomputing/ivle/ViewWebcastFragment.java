package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WebcastItemGroupsContract;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment to view an announcement.
 * @author yjwong
 */
public class ViewWebcastFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastFragment";
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The webcast ID */
	private long mWebcastId = -1;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.view_webcast_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the announcement ID.
		ViewWebcastActivity activity = (ViewWebcastActivity) getActivity();
		mWebcastId = activity.webcastId;
        if (mWebcastId == -1) {
        	throw new IllegalStateException("No webcast ID was passed to ViewWebcastFragment");
        }
        
		// Load the module data.
		String[] uiBindFrom = {
				WebcastItemGroupsContract.ITEM_GROUP_TITLE
		};
		int[] uiBindTo = {
				R.id.view_webcast_fragment_list_item_group_title
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_webcast_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        Bundle args = new Bundle();
        args.putLong("webcastId", mWebcastId);
        DataLoader loader = new DataLoader(getActivity(), mAdapter);
		getLoaderManager().initLoader(DataLoader.VIEW_WEBCAST_FRAGMENT_LOADER, args, loader);
		
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.view_webcast_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Fragment arguments.
				Bundle fragmentArgs = new Bundle();
				fragmentArgs.putLong("webcastItemGroupId", id);
				
				// Invoke fragment to view files in the item group.
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				Fragment fragment = new ViewWebcastItemGroupsFragment();
				fragment.setArguments(fragmentArgs);
				transaction.addToBackStack(null);
				transaction.replace(R.id.view_webcast_activity_fragment_container, fragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				transaction.commit();
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	// }}}
}
