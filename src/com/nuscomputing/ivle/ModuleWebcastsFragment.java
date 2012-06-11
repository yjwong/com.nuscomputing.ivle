package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WebcastsContract;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleWebcastsFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleWebcastsFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The module ID */
	private long mModuleId = -1;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module webcast view.
		return inflater.inflate(R.layout.module_webcasts_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the module ID.
		ModuleActivity activity = (ModuleActivity) getActivity();
		mModuleId = activity.moduleId;
        if (mModuleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleWebcastsFragment");
        }
        
		// Load the announcement data.
		String[] uiBindFrom = {
				WebcastsContract.TITLE
		};
		int[] uiBindTo = {
				R.id.module_webcasts_fragment_list_title
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.module_webcasts_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        Bundle args = new Bundle();
        args.putLong("moduleId", mModuleId);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(DataLoader.MODULE_WEBCAST_FRAGMENT_LOADER, args, mLoader).forceLoad();
        
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.module_webcasts_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
		
		// Get the listview.
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Start the webcast activity.
				Log.v(TAG, "webcast ID = " + id);
				Intent intent = new Intent();
				intent.setClass(getActivity(), ViewWebcastActivity.class);
				intent.putExtra("webcastId", id);
				startActivity(intent);
			}
		});
		
		setListAdapter(mAdapter);
	}
	
	// }}}
}
