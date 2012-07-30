package com.nuscomputing.ivle;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nuscomputing.ivle.providers.WebcastFilesContract;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment to view an announcement.
 * @author yjwong
 */
public class ViewWebcastItemGroupsFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWebcastItemGroupsFragment";
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The webcast item group ID */
	private long mWebcastItemGroupId = -1;
	
	/** The bundle containing details used in the AlertDialog */
	private LinkedHashMap<String, String> mDetailsDialogMap;
	
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

		// Restore the state of this fragment.
		mDetailsDialogMap = new LinkedHashMap<String, String>();
		if (savedInstanceState != null) {
			// Add the details that were previously there.
			LinkedHashMap<?, ?> detailsDialogMapTemp = (LinkedHashMap<?, ?>) savedInstanceState.getSerializable("detailsDialogMap");
			for (Map.Entry<?, ?> item : detailsDialogMapTemp.entrySet()) {
				mDetailsDialogMap.put(item.getKey().toString(), item.getValue().toString());
			}
		}
		
		// Obtain the webcast item group ID.
		mWebcastItemGroupId = this.getArguments().getLong("webcastItemGroupId", -1);
		Log.v(TAG, "requesting files from item group " + mWebcastItemGroupId);
        if (mWebcastItemGroupId == -1) {
        	throw new IllegalStateException("No webcast ID was passed to ViewWebcastItemGroupsFragment");
        }
        
		// Define the bindings for the webcast data.
		String[] uiBindFrom = {
				WebcastFilesContract.FILE_TITLE,
				WebcastFilesContract.FILE_DESCRIPTION
		};
		int[] uiBindTo = {
				R.id.view_webcast_item_group_fragment_list_file_title,
				R.id.view_webcast_item_group_fragment_list_file_description,
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_webcast_item_group_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// The description field.
				if (columnIndex == cursor.getColumnIndex(WebcastFilesContract.FILE_DESCRIPTION)) {
					// Filter HTML in description.
					String description = cursor.getString(columnIndex);
					description = Html.fromHtml(description).toString();
					description = description.replace('\r', ' ').replace('\n', ' ').trim();
					TextView tvDescription = (TextView) view;
					tvDescription.setText(description);
					return true;
				}

				return false;
			}
		});
		
		// Load webcast file data.
        Bundle args = new Bundle();
        args.putLong("webcastItemGroupId", mWebcastItemGroupId);
        DataLoader loader = new DataLoader(getActivity(), mAdapter);
		getLoaderManager().initLoader(DataLoader.LOADER_VIEW_WEBCAST_ITEM_GROUP_FRAGMENT, args, loader);
		
        // Set up the list view.
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// Show the CAB when long clicked.
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Use contextual action bar to show items.
				getSherlockActivity().startActionMode(new WebcastFilesActionModeCallback(position));
				getListView().setItemChecked(position, true);
				return true;
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Prevent item from being checked.
		getListView().setItemChecked(position, false);
		
		// Invoke another item.
		Intent intent = new Intent();
		intent.setClass(getActivity(), ViewWebcastFileActivity.class);
		intent.putExtra("webcastFileId", id);
		startActivity(intent);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Put the details bundle into our state.
		outState.putSerializable("detailsDialogMap", mDetailsDialogMap);
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * ActionMode callbacks for webcast files.
	 * @author yjwong
	 */
	class WebcastFilesActionModeCallback implements ActionMode.Callback {
		// {{{ properties
		
		/** The item position */
		private int mPosition;
		
		// }}}
		// {{{ methods
		
		WebcastFilesActionModeCallback(int position) {
			mPosition = position; 
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.view_webcast_item_group_fragment_contextual, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode,
				Menu menu) {
			mode.setTitle("Webcast selected");
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode,
				MenuItem item) {
			switch (item.getItemId()) {
				case R.id.view_webcast_item_group_fragment_contextual_details:
					// Get details.
					Cursor cursor = (Cursor) getListView().getItemAtPosition(mPosition);
					cursor.moveToPosition(mPosition);
					
					// Add the details.
					Map<String, String> detailsMap = new LinkedHashMap<String, String>();
					detailsMap.put("File Title", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_TITLE)));
					detailsMap.put("File Name", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.FILE_NAME)));
					detailsMap.put("Created", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.CREATE_DATE)));
					detailsMap.put("Media Format", cursor.getString(cursor.getColumnIndex(WebcastFilesContract.MEDIA_FORMAT)));
					
					// Define dialog fragment arguments.
					Bundle fragmentArgs = new Bundle();
					fragmentArgs.putSerializable("items", (Serializable) detailsMap);
					fragmentArgs.putString("title", "Webcast Details");
					
					// Create the fragment.
					DialogFragment fragment = new DetailsDialogFragment();
					fragment.setArguments(fragmentArgs);
					
					// Add the fragment.
					FragmentManager manager = getFragmentManager();
					fragment.show(manager, null);
					
					// End contextual action mode.
					mode.finish();
					return true;
					
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Do nothing.
			getListView().setItemChecked(mPosition, false);
		}
		
		// }}}
	}
	
	// }}}
}
