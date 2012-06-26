package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinsContract;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleWorkbinsFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "WorkbinsFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	public SimpleCursorAdapter mAdapter = null;
	
	/** The module ID */
	private long mModuleId = -1;
	
	/** The refresh receiver */
	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "Received sync completion broadcast, reloading data");
			mLoaderManager.restartLoader(DataLoader.MODULE_WORKBINS_FRAGMENT_LOADER, null, mLoader);
		}
	};
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module workbins view.
		return inflater.inflate(R.layout.module_workbins_fragment, container, false);
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
        
		// Define the bindings to the listview.
		String[] uiBindFrom = { WorkbinsContract.TITLE };
		int[] uiBindTo = { R.id.module_workbins_fragment_list_title };
		
		// Fetch data for the list adapter.
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.module_workbins_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        Bundle args = new Bundle();
        args.putLong("moduleId", mModuleId);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.MODULE_WORKBINS_FRAGMENT_LOADER, args, mLoader);
		
		// Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.module_workbins_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Start the module workbin activity.
				Intent intent = new Intent();
				intent.setClass(getActivity(), ViewWorkbinActivity.class);
				intent.putExtra("workbinId", id);
				startActivity(intent);
			}
		});
		
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().getApplicationContext().unregisterReceiver(mRefreshReceiver);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().getApplicationContext().registerReceiver(mRefreshReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_COMPLETE));
	}
	
	// }}}
}