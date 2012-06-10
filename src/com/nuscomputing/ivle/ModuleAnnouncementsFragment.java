package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.UsersContract;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleAnnouncementsFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleAnnouncementsFragment";
	
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
		// Inflate the module info view.
		return inflater.inflate(R.layout.module_announcements_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the module ID.
		ModuleActivity activity = (ModuleActivity) getActivity();
		mModuleId = activity.moduleId;
        if (mModuleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleFragment");
        }
        
		// Load the announcement data.
		String[] uiBindFrom = { AnnouncementsContract.TITLE, UsersContract.NAME };
		int[] uiBindTo = { R.id.module_announcements_fragment_list_title, R.id.module_announcements_fragment_list_creator };
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.module_announcements_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        Bundle args = new Bundle();
        args.putLong("moduleId", mModuleId);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(DataLoader.MODULE_ANNOUNCEMENT_FRAGMENT_LOADER, args, mLoader).forceLoad();
		
		// Get the listview.
		// ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
		/*
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Start the module info activity.
				Intent intent = new Intent();
				intent.setClass(getActivity(), ModuleActivity.class);
				intent.putExtra("moduleId", id);
				startActivity(intent);
			}
		});
		*/
		
		setListAdapter(mAdapter);
	}
	
	// }}}
}
