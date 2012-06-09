package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.AnnouncementsContract;
import com.nuscomputing.ivle.providers.UsersContract;

import android.accounts.Account;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleAnnouncementsFragment extends ListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleAnnouncementsFragment";
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The module ID */
	private long mModuleId = -1;
	
	/** Loader IDs */
	private static final int LOADER_ANNOUNCEMENTS = 1;
	
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
		getLoaderManager().initLoader(ModuleAnnouncementsFragment.LOADER_ANNOUNCEMENTS, null, this).forceLoad();
		String[] uiBindFrom = { AnnouncementsContract.TITLE, UsersContract.NAME };
		int[] uiBindTo = { R.id.module_announcements_fragment_list_title, R.id.module_announcements_fragment_list_creator };
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.module_announcements_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		
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
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Change the visibility message.
		TextView tvNoAnnouncements = (TextView) getActivity().findViewById(R.id.module_announcements_fragment_no_announcements);
		tvNoAnnouncements.setVisibility((cursor.getCount() == 0) ? TextView.VISIBLE : TextView.GONE);
		mAdapter.swapCursor(cursor);
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
		String[] projection = {
				AnnouncementsContract.ID,
				AnnouncementsContract.TITLE,
				"creator_" + UsersContract.NAME,
				AnnouncementsContract.URL
		};
		String selection = DatabaseHelper.ANNOUNCEMENTS_TABLE_NAME + "." + AnnouncementsContract.ACCOUNT + " = ?";
		String[] selectionArgs = { accountName };
		
		// Set up the cursor loader.
		CursorLoader loader = new CursorLoader(getActivity());
		Log.d(TAG, "Setting up cursorLoader");
		loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + mModuleId + "/announcements"));
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
