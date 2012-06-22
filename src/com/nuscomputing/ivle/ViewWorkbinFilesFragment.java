package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinFilesContract;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to view a workbin's files.
 * @author yjwong
 */
public class ViewWorkbinFilesFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWorkbinFilesFragment";
	
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
		return inflater.inflate(R.layout.view_workbin_files_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the workbin folder ID.
		Bundle args = getArguments();
		
		// Load the workbin file data.
		String[] uiBindFrom = {
				WorkbinFilesContract.FILE_NAME
		};
		int[] uiBindTo = {
				R.id.view_workbin_files_fragment_list_file_name
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_workbin_files_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mLoader = new DataLoader(getActivity(), mAdapter);
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.VIEW_WORKBIN_FILES_FRAGMENT_LOADER, args, mLoader);
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	// }}}
}
