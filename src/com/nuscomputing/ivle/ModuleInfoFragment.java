package com.nuscomputing.ivle;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nuscomputing.ivle.DataLoader;
import com.nuscomputing.ivle.providers.DescriptionsContract;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleInfoFragment extends ListFragment implements
		DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleInfoFragment";
	
	/** Data loader instance */
	private DataLoader mInfoLoader;
	
	/** Data loader instance */
	private DataLoader mDescriptionLoader;
	
	/** The module ID */
	private long mModuleId = -1;
	
	/** The header view */
	private View mHeaderView;
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	// }}}
	// {{{ methods

	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		mHeaderView = inflater.inflate(R.layout.module_info_fragment_list_header, null);
		mHeaderView.setClickable(true);
		return inflater.inflate(R.layout.module_info_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the module ID.
		Bundle args = getArguments();
		mModuleId = args.getLong("moduleId", -1);
        if (mModuleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleInfoFragment");
        }
		
		// Find and insert the linear layout.
        setListAdapter(null);
		getListView().addHeaderView(mHeaderView);
		
		// Load the module info.
		mInfoLoader = new DataLoader(getActivity(), this);
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT, args, mInfoLoader);
        
		// Load the module descriptions.
		String[] uiBindFrom = { DescriptionsContract.TITLE };
		int[] uiBindTo = { R.id.module_info_fragment_list_description_title };
		mAdapter = new SimpleCursorAdapter(
			getActivity(),
			R.layout.module_info_fragment_list_item,
			null, uiBindFrom, uiBindTo,
			CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mDescriptionLoader = new DataLoader(getActivity(), mAdapter, this);
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS, args, mDescriptionLoader);
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	public void onLoaderFinished(int id, Bundle result) {
		// Set the view data.
		switch (id) {
			case DataLoader.LOADER_MODULE_INFO_FRAGMENT:
				TextView tvCourseName = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_name);
				tvCourseName.setText(result.getString("courseName"));
				TextView tvCourseCode = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_code);
				tvCourseCode.setText(result.getString("courseCode"));
				TextView tvCourseAcadYear = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_acad_year);
				tvCourseAcadYear.setText(result.getString("courseAcadYear"));
				TextView tvCourseSemester = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_semester);
				tvCourseSemester.setText(result.getString("courseSemester"));
				break;
			
			case DataLoader.LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS:
				int cursorCount = result.getInt("cursorCount");
				if (cursorCount == 0) {
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1,
						new String[] { getString(R.string.module_info_fragment_no_additional_info ) }
					);
					setListAdapter(adapter);
				}
				break;
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// If the position is zero then we ignore.
		if (position == 0) {
			return;
		}

		// Obtain the description.
		Object item = l.getItemAtPosition(position);
		if (item != null) {
			Cursor c = (Cursor) item;
			
			// Add the description and title as arguments.
			Bundle args = new Bundle();
			args.putString("description", c.getString(c.getColumnIndex(DescriptionsContract.DESCRIPTION)));
			args.putString("title", c.getString(c.getColumnIndex(DescriptionsContract.TITLE)));
			c.close();
			
			// Instantiate a new dialog fragment.
			FragmentManager manager = getActivity().getSupportFragmentManager();
			DialogFragment fragment = new ModuleInfoDescriptionDialogFragment();
			fragment.setArguments(args);
			fragment.show(manager, null);
		}
	}
	
	// }}}
}
