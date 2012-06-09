package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class ModulesFragment extends ListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModulesFragment";
	
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
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(Constants.LOADER_MODULES, null, this);
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.modules_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		
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
		mLoaderManager.restartLoader(Constants.LOADER_MODULES, null, this);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Obtain the current account.
		Log.v(TAG, "onCreateLoader");
		Account activeAccount = AccountUtils.getActiveAccount(getActivity(), true);
		if (activeAccount == null) {
			// Launch activity to add account.
			Log.e(TAG, "Error loading accounts");
			return null;
		}
		
		// Obtain the account name.
		String accountName = activeAccount.name;
		Log.d(TAG, "Account found, using " + accountName);

		// Set up our query parameters.
		String[] projection = { ModulesContract.ID, ModulesContract.COURSE_CODE, ModulesContract.COURSE_NAME };
		String selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
		String[] selectionArgs = { accountName };
		
		// Set up the cursor loader.
		CursorLoader loader = new CursorLoader(getActivity());
		Log.d(TAG, "Setting up cursorLoader");
		loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules"));
		loader.setProjection(projection);
		loader.setSelection(selection);
		loader.setSelectionArgs(selectionArgs);
		return loader;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	// }}}
}
