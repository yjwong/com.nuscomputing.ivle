package com.nuscomputing.ivle.online;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.nuscomputing.ivle.DataLoader;
import com.nuscomputing.ivle.IVLEUtils;
import com.nuscomputing.ivle.R;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Lecturer;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.NoSuchModuleException;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleLecturersFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleLecturersFragment";
	
	/** The layout inflater */
	private LayoutInflater mLayoutInflater;
	
	/** The module IVLE ID */
	private String mModuleIvleId;
	
	/** The cached list of photo URLs */
	private URL[] mCachedURLs;
	
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
        	throw new IllegalStateException("No module IVLE ID was passed to ModuleLecturersFragment");
        }
        
        // Get the layout inflater.
        mLayoutInflater = getActivity().getLayoutInflater();
		
		// Load the lecturers.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_LECTURERS_FRAGMENT, args, new LecturersLoaderCallbacks());
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The list adapter for lecturers.
	 * @author yjwong
	 */
	@TargetApi(11)
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
			final Lecturer lecturer = getItem(position);
			
			// Inflate list item from layout XML.
			convertView = mLayoutInflater.inflate(R.layout.module_lecturers_fragment_list_item, null);
			
			// Set the name.
			TextView tvName = (TextView) convertView.findViewById(R.id.module_lecturers_fragment_list_name);
			tvName.setText(lecturer.user.name);
			
			// Set the role.
			TextView tvRole = (TextView) convertView.findViewById(R.id.module_lecturers_fragment_list_role);
			tvRole.setText(lecturer.role);
			
			// Set the photo.
			ImageView ivPhoto = (ImageView) convertView.findViewById(R.id.module_lecturers_fragment_list_photo);
			PhotoURLTask ivPhotoTask = new PhotoURLTask();
			ivPhotoTask.execute(ivPhoto, lecturer, position);
			
			// Set the spinner.
			Spinner spinner = (Spinner) convertView.findViewById(R.id.module_lecturers_fragment_list_photo_spinner);
			LecturerOptionsSpinnerAdapter adapter = new LecturerOptionsSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, Arrays.asList("Select an item...", "Send email", "Add to contacts"));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				/** Flag to workaround quirky spinner behaviour */
				boolean mInitialLayout = true;
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Work around item being selected on layout.
					Log.v(TAG, "position = " + position);
					if (mInitialLayout) {
						mInitialLayout = false;
						return;
					}
					
					// Check the position.
					switch (position) {
						case 1:
					    	// Set up the email intent.
					    	Intent intent = new Intent(Intent.ACTION_SEND);
					    	intent.setType("message/rfc822");
					    	intent.putExtra(Intent.EXTRA_EMAIL, new String[] { lecturer.user.email });
					    	startActivity(Intent.createChooser(intent, getString(R.string.module_lecturers_fragment_email_via)));
					    	break;
					    
						case 2:
							// Handle add to contacts.
							intent = new Intent(Intent.ACTION_INSERT);
							intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
							intent.putExtra(ContactsContract.Intents.Insert.NAME, lecturer.user.name);
							intent.putExtra(ContactsContract.Intents.Insert.EMAIL, lecturer.user.email);
							startActivity(intent);
							break;
					}
					
					parent.setSelection(0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> view) {
					// Do nothing.
				}
			});

			// Return the view.
			return convertView;
		}
		
		// }}}
	}
	
	// }}}
	// {{{ classes
	
    /**
     * Helper class for the activity spinner.
     * @author yjwong
     */
    public class LecturerOptionsSpinnerAdapter extends ArrayAdapter<String>
    		implements SpinnerAdapter {
    	// {{{ properties
    	
    	/** The list of items */
    	private List<String> mItems;
    	
    	/** The context */
    	private Context mContext;
    	
    	// }}}
    	// {{{ methods
    	
    	LecturerOptionsSpinnerAdapter(Context context, int textViewResourceId, List<String> items) {
    		super(context, textViewResourceId, items);
    		mContext = context;
    		mItems = items;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// Inflate the layout for the item.
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.sherlock_spinner_item, null);
    		}
    		
			TextView tvItem = (TextView) convertView.findViewById(android.R.id.text1);
			tvItem.setVisibility(View.GONE);
    		return convertView;
    	}
    	
    	public View getDropDownView(int position, View convertView, ViewGroup parent) {
    		// Return a empty view for position 0.
    		if (position == 0) {
    			LinearLayout layout = new LinearLayout(mContext);
    			layout.setVisibility(View.GONE);
    			return layout;
    		}
    		
    		// Inflate the layout.
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
    		
    		String item = mItems.get(position);
    		if (item != null) {
    			TextView tvItem = (TextView) convertView.findViewById(android.R.id.text1);
    			if (tvItem != null) {
    				tvItem.setText(item);
    			}
    		}
    		
    		return convertView;
    	}
    	
    	// }}}
    }
	
	/**
	 * AsyncTask to get the display photo REAL url.
	 * @author yjwong
	 */
	class PhotoURLTask extends AsyncTask<Object, Object, URL> {
		// {{{ properties
		
		/** The ImageView */
		private ImageView mImageView;
		
		/** The lecturer */
		private Lecturer mLecturer;
		
		/** The position */
		private Integer mPosition;
		
		// }}}
		// {{{ methods
		
		@Override
		protected URL doInBackground(Object... params) {
			// Check arguments.
			if (params.length != 3 || !(params[0] instanceof ImageView)
					|| !(params[1] instanceof Lecturer
					|| !(params[2] instanceof Integer))) {
				throw new IllegalArgumentException();
			}
			
			// Set arguments.
			mImageView = (ImageView) params[0];
			mLecturer = (Lecturer) params[1];
			mPosition = (Integer) params[2];
			
			// Obtain the HTML content.
			if (mCachedURLs[mPosition] == null) { 
				Log.v(TAG, "URL not found in cache, requesting");
				try {
					URL url = mLecturer.user.getDisplayPhotoURL(mModuleIvleId);
					return url;
					
				} catch (MalformedURLException e) {
					// Ignore.
				} catch (IOException e) {
					// Ignore.
				}
				
			} else {
				Log.v(TAG, "URL found in cache, returning");
				return mCachedURLs[mPosition];
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(URL url) {
			// Cache the URL.
			mCachedURLs[mPosition] = url;
			UrlImageViewHelper.setUrlDrawable(mImageView, url.toString());
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
			// We are loading!
			ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
			setListAdapter(loadingAdapter);
			
			return new LecturersLoader(getActivity(), args);
		}
		
		@Override
		public void onLoadFinished(Loader<List<Lecturer>> loader, List<Lecturer> result) {
			// Set the view data.
			if (result != null) {
				if (result.size() == 0) {
					TextView tvNoLecturers = (TextView) getActivity().findViewById(R.id.module_lecturers_fragment_no_lecturers);
					tvNoLecturers.setVisibility(View.VISIBLE);
					
					// Hide the list.
					getListView().setVisibility(View.GONE);
					
				} else {
					// Create list of cached URLs.
					mCachedURLs = new URL[result.size()];
					
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
