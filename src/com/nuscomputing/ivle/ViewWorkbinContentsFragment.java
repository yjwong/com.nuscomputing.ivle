package com.nuscomputing.ivle;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nuscomputing.ivle.providers.WorkbinFoldersContract;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to view a workbin.
 * @author yjwong
 */
public class ViewWorkbinContentsFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ViewWorkbinContentsFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The workbin folder ID */
	private long mWorkbinFolderId = -1;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.view_workbin_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the workbin folder ID.
		Bundle args = getArguments();
		mWorkbinFolderId = args.getLong("workbinFolderId");
        if (mWorkbinFolderId == -1) {
        	throw new IllegalStateException("No workbin folder ID was passed to ViewWorkbinContentsFragment");
        }
        
		// Load the workbin data.
		String[] uiBindFrom = {
				WorkbinFoldersContract.FOLDER_NAME
		};
		int[] uiBindTo = {
				R.id.view_workbin_fragment_list_title
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_workbin_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
        mLoader = new DataLoader(getActivity(), mAdapter);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(DataLoader.LOADER_VIEW_WORKBIN_FRAGMENT, args, mLoader);
        
        // Set the list adapter.
        setListAdapter(mAdapter);
	}
	
	// }}}
}
