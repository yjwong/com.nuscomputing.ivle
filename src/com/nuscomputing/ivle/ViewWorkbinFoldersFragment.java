package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinFoldersContract;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment to view a workbin's folders.
 * @author yjwong
 */
public class ViewWorkbinFoldersFragment extends ListFragment
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWorkbinFoldersFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.view_workbin_folders_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the workbin folder ID.
		final Bundle args = getArguments();
		
		// Load the workbin file data.
		String[] uiBindFrom = {
				WorkbinFoldersContract.FOLDER_NAME,
				WorkbinFoldersContract.FILE_COUNT
		};
		int[] uiBindTo = {
				R.id.view_workbin_folders_fragment_list_folder_name,
				R.id.view_workbin_folders_fragment_list_file_count
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_workbin_folders_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// The description field.
				if (columnIndex == cursor.getColumnIndex(WorkbinFoldersContract.FILE_COUNT)) {
					// Filter HTML in description.
					String fileCount = getString(R.string.view_workbin_folders_fragment_file_count, cursor.getInt(columnIndex));
					TextView tvFileCount = (TextView) view;
					tvFileCount.setText(fileCount);
					return true;
				}

				return false;
			}
		});
		mLoader = new DataLoader(getActivity(), mAdapter, this);
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.LOADER_VIEW_WORKBIN_FOLDERS_FRAGMENT, args, mLoader);
		
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.view_workbin_folders_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Invoke fragment to view files in the item group.
				Intent intent = new Intent();
				intent.putExtra("workbinId", args.getLong("workbinId", -1));
				intent.putExtra("workbinFolderId", id);
				intent.setClass(getActivity(), ViewWorkbinActivity.class);
				startActivity(intent);
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	public void onLoaderFinished(Bundle result) {
		TextView tvNoFolders = (TextView) getActivity().findViewById(R.id.view_workbin_folders_fragment_no_folders);
		tvNoFolders.setVisibility(result.getInt("cursorCount") == 0 ? TextView.VISIBLE : TextView.GONE);
	}
	
	// }}}
}
