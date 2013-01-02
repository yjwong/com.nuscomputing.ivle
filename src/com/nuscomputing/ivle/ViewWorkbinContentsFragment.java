package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinFoldersContract;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Fragment to view a workbin.
 * @author yjwong
 */
public class ViewWorkbinContentsFragment extends ListFragment {
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
	// {{{ classes
	
	/**
	 * This adapter binds the results for the files and folders query into 
	 * a single adapter.
	 * @author yjwong
	 */
	class WorkbinContentsAdapter extends BaseAdapter {
		// {{{ properties
		
		/** The context */
		private Context mContext;
		
		/** A cursor holding references to file data */
		private Cursor mFilesCursor;
		
		/** A cursor holding references to folder data */
		private Cursor mFoldersCursor;
		
		// }}}
		// {{{ methods
		
		public WorkbinContentsAdapter(Context context) {
			super();
			mContext = context;
		}
		
		/**
		 * Change the underlying cursor to a new cursor.
		 * If there is an existing cursor it will be closed.
		 * @param cursor
		 */
		public void changeFilesCursor(Cursor cursor) {
			if (mFilesCursor != null) {
				mFilesCursor.close();
			}
			
			mFilesCursor = cursor;
		}
		
		public void changeFoldersCursor(Cursor cursor) {
			if (mFoldersCursor != null) {
				mFoldersCursor.close();
			}
			
			mFoldersCursor = cursor;
		}

		@Override
		public int getCount() {
			// We need to add one to a cursor of each type because the heading
			// should be included as well.
			int count = 0;
			if (mFoldersCursor != null) {
				count =+ mFoldersCursor.getCount() + 1;
			}
			
			if (mFilesCursor != null) {
				count =+ mFilesCursor.getCount() + 1;
			}
			
			return count;
		}

		@Override
		public Cursor getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
		// }}}
	}
	
	// }}}
}
