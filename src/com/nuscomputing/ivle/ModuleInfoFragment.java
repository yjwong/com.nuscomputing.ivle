package com.nuscomputing.ivle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleInfoFragment extends Fragment implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleInfoFragment";
	
	/** The module ID */
	private long mModuleId = -1;
	
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
        DataLoader loader = new DataLoader(getActivity(), this);
		getLoaderManager().initLoader(DataLoader.MODULE_INFO_FRAGMENT_LOADER, args, loader);
	}
	
	public void onLoaderFinished(Bundle result) {
		// Set the view data.
		TextView tvCourseName = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_name);
		tvCourseName.setText(result.getString("courseName"));
		TextView tvCourseCode = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_code);
		tvCourseCode.setText(result.getString("courseCode"));
		TextView tvCourseAcadYear = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_acad_year);
		tvCourseAcadYear.setText(result.getString("courseAcadYear"));
	}
	
	// }}}
}
