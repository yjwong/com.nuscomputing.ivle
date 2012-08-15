package com.nuscomputing.ivle.online;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
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
public class ModuleInfoFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleInfoFragment";
	
	/** The module IVLE ID */
	private String mModuleIvleId;
	
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
		mModuleIvleId = args.getString("moduleIvleId");
        if (mModuleIvleId == null) {
        	throw new IllegalStateException("No module IVLE ID was passed to ModuleInfoFragment");
        }
        
		// Obtain the listview.
        mListView = getListView();
		
		// Find and insert the linear layout.
		LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.module_info_fragment_list_header, null);
		ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
		mListView.addHeaderView(layout);
		mListView.setAdapter(loadingAdapter);
		
		// Load the module info.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_INFO, args, new InfoLoaderCallbacks());
        
		// Load the module descriptions.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS, args, new DescriptionsLoaderCallbacks());
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The loader callbacks for module information.
	 * @author yjwong
	 */
	class InfoLoaderCallbacks implements
		LoaderManager.LoaderCallbacks<Module> {
		// {{{ methods
		
		@Override
		public Loader<Module> onCreateLoader(int id, Bundle args) {
			return new InfoLoader(getActivity(), args);
		}
		
		@TargetApi(11)
		@Override
		public void onLoadFinished(Loader<Module> loader, Module result) {
			// Set the view data.
			if (result != null) {
				TextView tvCourseName = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_name);
				tvCourseName.setText(result.courseName);
				TextView tvCourseCode = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_code);
				tvCourseCode.setText(result.courseCode);
				TextView tvCourseAcadYear = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_acad_year);
				tvCourseAcadYear.setText(result.courseAcadYear);
				TextView tvCourseSemester = (TextView) getActivity().findViewById(R.id.module_info_fragment_course_semester);
				tvCourseSemester.setText(result.courseSemester);
				
			}  else {
				Toast.makeText(getActivity(), R.string.module_info_fragment_unable_to_load, Toast.LENGTH_SHORT).show();
				
				// Stop all other tasks.
				if (Build.VERSION.SDK_INT >= 11) {
					getActivity().getLoaderManager().destroyLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_INFO);
					getActivity().getLoaderManager().destroyLoader(DataLoader.LOADER_MODULE_INFO_FRAGMENT_DESCRIPTIONS);
					getActivity().getLoaderManager().destroyLoader(DataLoader.LOADER_MODULE_LECTURERS_FRAGMENT);
				}
				
				getActivity().finish();
			}
		}
		
		@Override
		public void onLoaderReset(Loader<Module> loader) {
			// Do nothing.
		}
		
		// }}}
	}
	
	/**
	 * The module information loader.
	 * @author yjwong
	 */
	static class InfoLoader extends AsyncTaskLoader<Module> {
		// {{{ properties
		
		/** Arguments to this loader */
		private Bundle mArgs;
		
		/** The context */
		private Context mContext;
		
		/** The module information */
		private Module mModule;
		
		// }}}
		// {{{ methods
		
		InfoLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
		}
		
		@Override
		public void onStartLoading() {
			if (mModule != null	) {
				deliverResult(mModule);
			}
			if (takeContentChanged() || mModule == null) {
				forceLoad();
			}
		}
		
		@Override
		public Module loadInBackground() {
			// Obtain the IVLE ID.
			String moduleIvleId = mArgs.getString("moduleIvleId");
			
			// Acquire a new IVLE object.
			IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
			try {
				Module module = ivle.getModule(moduleIvleId);
				return module;
				
			} catch (NetworkErrorException e) {
				Log.e(TAG, "NetworkErrorException encountered while loading module");
			} catch (FailedLoginException e) {
				Log.e(TAG, "FailedLoginException encountered while loading module");
			} catch (JSONParserException e) {
				Log.e(TAG, "JSONParserException encountered while loading module");
			} catch (NoSuchModuleException e) {
				Log.e(TAG, "NoSuchModuleException encountered while loading module");
			}
			
			return null;
		}
		
		// }}}
	}
	
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
