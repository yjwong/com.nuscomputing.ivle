package com.nuscomputing.ivle;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nuscomputing.ivle.DataLoader;
import com.nuscomputing.ivle.IVLEUtils;
import com.nuscomputing.ivle.R;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.Module.Description;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.NoSuchModuleException;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleInfoFragment extends SherlockListFragment implements
		DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleInfoFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** The module ID */
	private long mModuleId = -1;
	
	/** The main listview */
	private ListView mListView;
	
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
		Bundle args = getArguments();
		mModuleId = args.getLong("moduleId", -1);
        if (mModuleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleInfoFragment");
        }
        
		// Obtain the listview.
        mListView = getListView();
		
		// Find and insert the linear layout.
		LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.module_info_fragment_list_header, null);
		layout.setClickable(true);
		ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
		mListView.addHeaderView(layout);
		mListView.setAdapter(loadingAdapter);
		
		// Load the module info.
		mLoader = new DataLoader(getActivity(), this);
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT, args, mLoader);
        
		// Load the module descriptions.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS, args, new DescriptionsLoaderCallbacks());
	}
	
	public void onLoaderFinished(Bundle result) {
		// Set the view data.
		TextView tvCourseName = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_name);
		tvCourseName.setText(result.getString("courseName"));
		TextView tvCourseCode = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_code);
		tvCourseCode.setText(result.getString("courseCode"));
		TextView tvCourseAcadYear = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_acad_year);
		tvCourseAcadYear.setText(result.getString("courseAcadYear"));
		TextView tvCourseSemester = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_semester);
		tvCourseSemester.setText(result.getString("courseSemester"));
	}
	
	// }}}
	// {{{ classes

	/**
	 * The array adapter for module descriptions.
	 * @author yjwong
	 */
	class DescriptionArrayAdapter extends ArrayAdapter<Description> {
		// {{{ properties
		
		/** The descriptions */
		private List<Description> mObjects;
		
		// }}}
		// {{{ methods
		
		public DescriptionArrayAdapter(Context context,
				int textViewResourceId, List<Description> objects) {
			super(context, textViewResourceId, objects);
			mObjects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.module_info_fragment_list_item, null);
			
			// Set the text.
			convertView = view.findViewById(R.id.module_info_fragment_list_description_title);
			((TextView) convertView).setText(mObjects.get(position).title);
			return view;
		}
		
		// }}}
	}
	
	/**
	 * The loader callbacks for module descriptions.
	 * @author yjwong
	 */
	class DescriptionsLoaderCallbacks implements
			LoaderManager.LoaderCallbacks<List<Description>> {
		// {{{ methods

		@Override
		public Loader<List<Description>> onCreateLoader(int id, Bundle args) {
			return new DescriptionsLoader(getActivity(), args);
		}

		@Override
		public void onLoadFinished(Loader<List<Description>> loader,
				List<Description> descriptions) {
			// Check for errors.
			if (descriptions != null) {
				if (descriptions.size() > 0) {
					DescriptionArrayAdapter adapter = new DescriptionArrayAdapter(getActivity(), R.layout.module_info_fragment_list_item, descriptions);
					mListView.setAdapter(adapter);
					mListView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							// If the position is zero then we ignore.
							if (position == 0) {
								return;
							}
	
							// Obtain the description.
							Description description = (Description) parent.getItemAtPosition(position);
							
							// Add the description and title as arguments.
							Bundle args = new Bundle();
							args.putString("description", description.description);
							args.putString("title", description.title);
							
							// Instantiate a new dialog fragment.
							FragmentManager manager = getActivity().getSupportFragmentManager();
							DialogFragment fragment = new ModuleInfoDescriptionDialogFragment();
							fragment.setArguments(args);
							fragment.show(manager, null);
						}
					});
					
				} else {
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							getActivity(),
							R.layout.module_info_fragment_list_item,
							R.id.module_info_fragment_list_description_title
					);
					adapter.add(getString(R.string.module_info_fragment_no_additional_info));
					mListView.setAdapter(adapter);
				}
				
			} else {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(),
						R.layout.module_info_fragment_list_item,
						R.id.module_info_fragment_list_description_title
				);
				adapter.add(getString(R.string.module_info_fragment_failed_to_load));
				mListView.setAdapter(adapter);
				Toast.makeText(
						getActivity(),
						getString(R.string.module_info_fragment_unable_to_load_additional),
						Toast.LENGTH_SHORT)
				.show();
			}
		}

		@Override
		public void onLoaderReset(Loader<List<Description>> loader) { }
		
		// }}}
	}
	
	/**
	 * The loader for module descriptions.
	 * @author yjwong
	 */
	static class DescriptionsLoader extends AsyncTaskLoader<List<Description>> {
		// {{{ properties
		
		/** Arguments to this loader */
		private Bundle mArgs; 
		
		/** The context */
		private Context mContext;
		
		/** The descriptions */
		private List<Description> mDescriptions;
		
		// }}}
		// {{{ methods
		
		DescriptionsLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
		}
		
		@Override
		public void onStartLoading() {
			if (mDescriptions != null) {
				deliverResult(mDescriptions);
			}
			if (takeContentChanged() || mDescriptions == null) {
				forceLoad();
			}
		}
		
		@Override
		public List<Description> loadInBackground() {
			// Check if the descriptions are already loaded.
			if (mDescriptions != null) {
				return mDescriptions;
			}
			
			// Get the module IVLE ID.
			String moduleIvleId = mArgs.getString("moduleIvleId");
			try {
				// Acquire a new IVLE object.
				IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
				Module module = ivle.getModule(moduleIvleId);
				
				// Get the description.
				Description[] descriptions = module.getDescriptions();
				mDescriptions = Arrays.asList(descriptions);
				return mDescriptions;
				
			} catch (NetworkErrorException e) {
				Log.w(TAG, "Unable to load module descriptions: network error");
			} catch (FailedLoginException e) {
				Log.e(TAG, "Unable to load module descriptions: API raised FailedLoginException");
			} catch (JSONParserException e) {
				Log.e(TAG, "Unable to load module descriptions: API raised JSONParserException");
			} catch (NoSuchModuleException e) {
				Log.e(TAG, "Unable to load module descriptions: API raised NoSuchModuleException");
			}
			
			return null;
		}
		
		// }}}
	}
	
	// }}}
}
