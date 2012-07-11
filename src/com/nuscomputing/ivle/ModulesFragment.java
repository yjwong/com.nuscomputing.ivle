package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

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
	
	/** The refresh receiver */
	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Account account = intent.getParcelableExtra("com.nuscomputing.ivle.Account");
			if (account.name.equals(AccountUtils.getActiveAccount(getActivity(), false).name)) {
				Log.v(TAG, "Received sync completion broadcast, reloading data");
				mLoaderManager.restartLoader(DataLoader.LOADER_MODULES_FRAGMENT, null, mLoader);
			}
		}
	};
	
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
		mLoaderManager.initLoader(DataLoader.LOADER_MODULES_FRAGMENT, null, mLoader);
		
		// Get the listview.
		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Obtain the module name.
				TextView tvCourseName = (TextView) view.findViewById(R.id.modules_fragment_list_course_name);
				String courseName = tvCourseName.getText().toString();
				
				// Start the module info activity, or replace with fragment.
				View multipane = getActivity().findViewById(R.id.main_multipane);
				if (multipane == null) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), ModuleActivity.class);
					intent.putExtra("moduleId", id);
					intent.putExtra("moduleCourseName", courseName);
					startActivity(intent);
				} else {
					// Prepare the fragment.
					Bundle args = new Bundle();
					args.putLong("moduleId", id);
					args.putString("moduleCourseName", courseName);
					Fragment fragment = new ModuleInfoFragment();
					fragment.setArguments(args);
					
					// Add the fragment.
					FragmentManager manager = getActivity().getSupportFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					transaction.replace(R.id.main_right_fragment_container, fragment);
					transaction.commit();
				}
			}
		});
		
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mRefreshReceiver);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(mRefreshReceiver, new IntentFilter(IVLESyncService.ACTION_SYNC_SUCCESS));
	}
	
	// }}}
}
