package com.nuscomputing.ivle;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.app.ActionBar;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleInfoFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleInfoFragment";
	
	/** The module ID */
	private long mModuleId = -1;
	
	/** Loader ID */
	private static final int MODULE_LOADER = 1;
	
	// }}}
	// {{{ methods

	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.module_info_fragment, container, false);
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
        
		// Load the module data.
        Bundle args = new Bundle();
        args.putLong("moduleId", mModuleId);
        DataLoader loader = new DataLoader(getActivity());
		getLoaderManager().initLoader(ModuleInfoFragment.MODULE_LOADER, args, loader);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Reset the cursor.
		cursor.moveToFirst();
		
		// Set the title.
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActivity().getActionBar();
			
			// Set the title appropriately.
			actionBar.setTitle(cursor.getString(1));
		}
		
		// Set the view data.
		TextView tvCourseName = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_name);
		tvCourseName.setText(cursor.getString(2));
		TextView tvCourseCode = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_code);
		tvCourseCode.setText(cursor.getString(1));
		TextView tvCourseAcadYear = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_acad_year);
		tvCourseAcadYear.setText(cursor.getString(3));
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
				ModulesContract.ID,
				ModulesContract.COURSE_CODE,
				ModulesContract.COURSE_NAME,
				ModulesContract.COURSE_ACAD_YEAR
		};
		String selection = DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?";
		String[] selectionArgs = { accountName };
		
		// Set up the cursor loader.
		CursorLoader loader = new CursorLoader(getActivity());
		Log.d(TAG, "Setting up cursorLoader");
		loader.setUri(Uri.parse("content://com.nuscomputing.ivle.provider/modules/" + mModuleId));
		loader.setProjection(projection);
		loader.setSelection(selection);
		loader.setSelectionArgs(selectionArgs);
		return loader;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Do nothing.
	}
	
	// }}}
}
