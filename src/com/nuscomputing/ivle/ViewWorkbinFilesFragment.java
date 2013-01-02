package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.WorkbinFilesContract;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
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
 * Fragment to view a workbin's files.
 * @author yjwong
 */
public class ViewWorkbinFilesFragment extends ListFragment 
		implements DataLoaderListener {
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
		final Bundle args = getArguments();
		
		// Load the workbin file data.
		String[] uiBindFrom = {
				WorkbinFilesContract.FILE_NAME,
				WorkbinFilesContract.DOWNLOAD_URL,
				WorkbinFilesContract.FILE_SIZE
		};
		int[] uiBindTo = {
				R.id.view_workbin_files_fragment_list_file_name,
				R.id.view_workbin_files_fragment_list_download_url,
				R.id.view_workbin_files_fragment_list_file_size
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.view_workbin_files_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == cursor.getColumnIndex(WorkbinFilesContract.FILE_NAME)) {
					view.setTag(cursor.getString(columnIndex));
					return false;
				}
				
				if (columnIndex == cursor.getColumnIndex(WorkbinFilesContract.DOWNLOAD_URL)) {
					view.setTag(cursor.getString(columnIndex));
					return true;
				}
				
				if (columnIndex == cursor.getColumnIndex(WorkbinFilesContract.FILE_SIZE)) {
					view.setTag(cursor.getDouble(columnIndex));
					return true;
				}
				
				return false;
			}
		});
		mLoader = new DataLoader(getActivity(), mAdapter, this);
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(DataLoader.LOADER_VIEW_WORKBIN_FILES_FRAGMENT, args, mLoader);
		
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.view_workbin_files_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Obtain download URL.
				View tvDownloadUrl = view.findViewById(R.id.view_workbin_files_fragment_list_download_url);
				String downloadUrl = (String) tvDownloadUrl.getTag();
				
				// Obtain file name.
				View tvFileName = view.findViewById(R.id.view_workbin_files_fragment_list_file_name);
				String fileName = (String) tvFileName.getTag();
				
				// Obtain file size.
				View tvFileSize = view.findViewById(R.id.view_workbin_files_fragment_list_file_size);
				Double fileSize = (Double) tvFileSize.getTag();
				
				// Define download arguments.
				Bundle args = new Bundle();
				args.putString("downloadUrl", downloadUrl);
				args.putString("fileName", fileName);
				args.putDouble("fileSize", fileSize);

				// Launch progress dialog to download this file.
				showDownloadDialog(args);
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	private void showDownloadDialog(Bundle args) {
		// Show the dialog.
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		DialogFragment fragment = new WorkbinFileDownloadDialogFragment();
		fragment.setArguments(args);
		fragment.show(transaction, "DOWNLOAD_DIALOG");
	}
	
	public void onLoaderFinished(int id, Bundle result) {
		TextView tvNoFiles = (TextView) getActivity().findViewById(R.id.view_workbin_files_fragment_no_files);
		tvNoFiles.setVisibility(result.getInt("cursorCount") == 0 ? TextView.VISIBLE : TextView.GONE);
	}
	
	// }}}
}
