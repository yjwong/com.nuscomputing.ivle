package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModulesFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModulesFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	public SimpleCursorAdapter mAdapter = null;
	
	// }}}
	// {{{ methods
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Define the bindings to the listview.
		Log.v(TAG, "onActivityCreated");
		String[] uiBindFrom = { ModulesContract.COURSE_CODE, ModulesContract.COURSE_NAME };
		int[] uiBindTo = { R.id.modules_fragment_list_course_code, R.id.modules_fragment_list_course_name };
		
		// Fetch data for the list adapter.
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.modules_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.MODULES_FRAGMENT_LOADER, null, mLoader);
		
		// Get the listview.
		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
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
		
		setListAdapter(mAdapter);
	}
	
	public void restartLoader() {
		mLoaderManager.restartLoader(Constants.LOADER_MODULES, null, mLoader);
	}
	
	// }}}
}
