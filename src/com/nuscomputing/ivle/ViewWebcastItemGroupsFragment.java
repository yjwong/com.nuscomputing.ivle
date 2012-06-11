package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WebcastFilesContract;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
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
public class ViewWebcastItemGroupsFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastItemGroupsFragment";
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The webcast item group ID */
	private long mWebcastItemGroupId = -1;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.view_webcast_item_group_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the webcast item group ID.
		mWebcastItemGroupId = this.getArguments().getLong("webcastItemGroupId", -1);
		Log.v(TAG, "requesting files from item group " + mWebcastItemGroupId);
        if (mWebcastItemGroupId == -1) {
        	throw new IllegalStateException("No webcast ID was passed to ViewWebcastItemGroupsFragment");
        }
        
		// Load the module data.
		String[] uiBindFrom = {
				WebcastFilesContract.FILE_TITLE
		};
		int[] uiBindTo = {
				R.id.view_webcast_item_group_fragment_list_file_title
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_webcast_item_group_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        Bundle args = new Bundle();
        args.putLong("webcastItemGroupId", mWebcastItemGroupId);
        DataLoader loader = new DataLoader(getActivity(), mAdapter);
		getLoaderManager().initLoader(DataLoader.VIEW_WEBCAST_ITEM_GROUP_FRAGMENT_LOADER, args, loader);
		
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.view_webcast_item_group_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Invoke another item.
				Log.v(TAG, "webcast file " + id);
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	// }}}
}
