package com.nuscomputing.ivle.online;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nuscomputing.ivle.DataLoader;
import com.nuscomputing.ivle.IVLEUtils;
import com.nuscomputing.ivle.R;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.Module.Lecturer;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.NoSuchModuleException;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleLecturersFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleLecturersFragment";
	
	/** The layout inflater */
	private LayoutInflater mLayoutInflater;
	
	/** The module IVLE ID */
	private String mModuleIvleId;
	
	// }}}
	// {{{ methods

	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.module_lecturers_fragment, container, false);
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
        
        // Get the layout inflater.
        mLayoutInflater = getActivity().getLayoutInflater();
		
		// We are loading!
		ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
		setListAdapter(loadingAdapter);
		
		// Load the lecturers.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_LECTURERS_FRAGMENT, args, new LecturersLoaderCallbacks());
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The list adapter for lecturers.
	 * @author yjwong
	 */
	class LecturerAdapter extends ArrayAdapter<Lecturer> {
		// {{{ methods
		
		public LecturerAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		public LecturerAdapter(Context context, int textViewResourceId,
				List<Lecturer> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the item.
			Lecturer lecturer = getItem(position);
			
			// Inflate list item from layout XML.
			convertView = mLayoutInflater.inflate(R.layout.module_lecturers_fragment_list_item, null);
			
			// Set the name.
			TextView tvName = (TextView) convertView.findViewById(R.id.module_lecturers_fragment_list_name);
			tvName.setText(lecturer.user.name);
			return convertView;
		}
		
		// }}}
	}
	
	/**
	 * The loader callbacks for module information.
	 * @author yjwong
	 */
	class LecturersLoaderCallbacks implements
		LoaderManager.LoaderCallbacks<List<Lecturer>> {
		// {{{ methods
		
		@Override
		public Loader<List<Lecturer>> onCreateLoader(int id, Bundle args) {
			return new LecturersLoader(getActivity(), args);
		}
		
		@Override
		public void onLoadFinished(Loader<List<Lecturer>> loader, List<Lecturer> result) {
			// Set the view data.
			if (result != null) {
				if (result.size() == 0) {
					TextView tvNoLecturers = (TextView) getActivity().findViewById(R.id.module_lecturers_fragment_no_lecturers);
					tvNoLecturers.setVisibility(View.VISIBLE);
				} else {
					// Create new list adapter.
					LecturerAdapter adapter = new LecturerAdapter(getActivity(), R.id.module_lecturers_fragment_no_lecturers, result);
					setListAdapter(adapter);
				}
				
			} else {
				Toast.makeText(getActivity(), R.string.module_lecturers_fragment_unable_to_load, Toast.LENGTH_SHORT)
				.show();
			}
		}
		
		@Override
		public void onLoaderReset(Loader<List<Lecturer>> loader) {
			// Do nothing.
		}
		
		// }}}
	}
	
	/**
	 * The module information loader.
	 * @author yjwong
	 */
	static class LecturersLoader extends AsyncTaskLoader<List<Lecturer>> {
		// {{{ properties
		
		/** Arguments to this loader */
		private Bundle mArgs;
		
		/** The context */
		private Context mContext;
		
		/** The list of lecturers */
		private List<Lecturer> mLecturers;
		
		// }}}
		// {{{ methods
		
		LecturersLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
		}
		
		@Override
		public void onStartLoading() {
			if (mLecturers != null	) {
				deliverResult(mLecturers);
			}
			if (takeContentChanged() || mLecturers == null) {
				forceLoad();
			}
		}
		
		@Override
		public List<Lecturer> loadInBackground() {
			// Obtain the IVLE ID.
			String moduleIvleId = mArgs.getString("moduleIvleId");
			
			// Acquire a new IVLE object.
			IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
			try {
				Module module = ivle.getModule(moduleIvleId);
				Lecturer[] lecturers = module.getLecturers();
				mLecturers = Arrays.asList(lecturers);
				return mLecturers;
				
			} catch (NetworkErrorException e) {
				Log.e(TAG, "NetworkErrorException encountered while loading lecturers");
			} catch (FailedLoginException e) {
				Log.e(TAG, "FailedLoginException encountered while loading lecturers");
			} catch (JSONParserException e) {
				Log.e(TAG, "JSONParserException encountered while loading lecturers");
			} catch (NoSuchModuleException e) {
				Log.e(TAG, "NoSuchModuleException encountered while loading lecturers");
			}
			
			return null;
		}
		
		// }}}
	}
	
	// }}}
}
