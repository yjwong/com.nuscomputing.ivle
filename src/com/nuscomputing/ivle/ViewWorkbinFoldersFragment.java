package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinFoldersContract;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment to view a workbin's folders.
 * @author yjwong
 */
public class ViewWorkbinFoldersFragment extends ListFragment {
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
				WorkbinFoldersContract.FOLDER_NAME
		};
		int[] uiBindTo = {
				R.id.view_workbin_folders_fragment_list_folder_name
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_workbin_folders_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mLoader = new DataLoader(getActivity(), mAdapter);
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.VIEW_WORKBIN_FOLDERS_FRAGMENT_LOADER, args, mLoader);
		
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
	
	// }}}
}
