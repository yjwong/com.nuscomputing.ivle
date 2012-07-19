package com.nuscomputing.ivle;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModulesFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModulesFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	public ModulesCursorAdapter mAdapter = null;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup view,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.modules_fragment, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Fetch data for the list adapter.
		mAdapter = new ModulesCursorAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.LOADER_MODULES_FRAGMENT, null, mLoader);
		
		// Get the listview.
		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@TargetApi(16)
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
					intent.putExtra("moduleIvleId", view.getTag().toString());
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						// Someday, somehow...
						// Bundle options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
						// MainApplication.getContext().startActivity(intent, options);
						startActivity(intent);
					} else {
						startActivity(intent);
					}
					
				} else {
					// Prepare the fragment.
					Bundle args = new Bundle();
					args.putLong("moduleId", id);
					args.putString("moduleCourseName", courseName);
					args.putString("moduleIvleId", view.getTag().toString());
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
	// {{{ classes
	
	/**
	 * Cursor adapter for modules.
	 * @author yjwong
	 */
	public class ModulesCursorAdapter extends CursorAdapter {
		// {{{ methods
		
		public ModulesCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Bind the course code.
			TextView tvCourseCode = (TextView) view.findViewById(R.id.modules_fragment_list_course_code);
			tvCourseCode.setText(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_CODE)));
			
			// Bind the course name.
			TextView tvCourseName = (TextView) view.findViewById(R.id.modules_fragment_list_course_name);
			tvCourseName.setText(cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
			
			// Set the IVLE ID.
			view.setTag(cursor.getString(cursor.getColumnIndex(ModulesContract.IVLE_ID)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Obtain a layout inflater.
			LayoutInflater inflater = LayoutInflater.from(context);
			
			// Return the view.
			View v = inflater.inflate(R.layout.modules_fragment_list_item, parent, false);
			bindView(v, context, cursor);
			return v;
		}
		
		// }}}
	}
	
	// }}}
}

