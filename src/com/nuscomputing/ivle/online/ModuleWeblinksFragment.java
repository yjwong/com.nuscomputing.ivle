package com.nuscomputing.ivle.online;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.Weblink;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleWeblinksFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleWeblinksFragment";
	
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
		return inflater.inflate(R.layout.module_weblinks_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the module ID.
		Bundle args = getArguments();
		mModuleIvleId = args.getString("moduleIvleId");
        if (mModuleIvleId == null) {
        	throw new IllegalStateException("No module IVLE ID was passed to ModuleWeblinksFragment");
        }
        
        // Get the layout inflater.
        mLayoutInflater = getActivity().getLayoutInflater();
		
		// Load the lecturers.
		getLoaderManager().initLoader(DataLoader.LOADER_MODULE_WEBLINKS_FRAGMENT, args, new WeblinksLoaderCallbacks());
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Get the weblink object.
		Weblink weblink = (Weblink) getListView().getItemAtPosition(position);
		
		// Create the intent.
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(weblink.url));
		startActivity(intent);
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The list adapter for lecturers.
	 * @author yjwong
	 */
	@TargetApi(11)
	class WeblinkAdapter extends ArrayAdapter<Weblink> {
		// {{{ methods
		
		public WeblinkAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		public WeblinkAdapter(Context context, int textViewResourceId,
				List<Weblink> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the item.
			final Weblink weblink = getItem(position);
			
			// Inflate list item from layout XML.
			convertView = mLayoutInflater.inflate(R.layout.module_weblinks_fragment_list_item, null);
			
			// Set the description.
			TextView tvDescription = (TextView) convertView.findViewById(R.id.module_weblinks_fragment_list_description);
			tvDescription.setText(weblink.description);
			
			// Set the site type.
			TextView tvURL = (TextView) convertView.findViewById(R.id.module_weblinks_fragment_list_url);
			tvURL.setText(weblink.url);

			// Return the view.
			return convertView;
		}
		
		// }}}
	}
	
	// }}}
	// {{{ classes

	/**
	 * The loader callbacks for module information.
	 * @author yjwong
	 */
	class WeblinksLoaderCallbacks implements
		LoaderManager.LoaderCallbacks<List<Weblink>> {
		// {{{ methods
		
		@Override
		public Loader<List<Weblink>> onCreateLoader(int id, Bundle args) {
			// We are loading!
			ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
			setListAdapter(loadingAdapter);
			
			return new WeblinksLoader(getActivity(), args);
		}
		
		@Override
		public void onLoadFinished(Loader<List<Weblink>> loader, List<Weblink> result) {
			// Set the view data.
			if (result != null) {
				if (result.size() == 0) {
					TextView tvNoLecturers = (TextView) getActivity().findViewById(R.id.module_weblinks_fragment_no_weblinks);
					tvNoLecturers.setVisibility(View.VISIBLE);
					
					// Hide the list.
					ListView listview = getListView();
					listview.setVisibility(View.GONE);
					
				} else {
					// Create new list adapter.
					WeblinkAdapter adapter = new WeblinkAdapter(getActivity(), R.id.module_weblinks_fragment_no_weblinks, result);
					setListAdapter(adapter);
				}
				
			} else {
				Toast.makeText(getActivity(), R.string.module_lecturers_fragment_unable_to_load, Toast.LENGTH_SHORT)
				.show();
			}
		}
		
		@Override
		public void onLoaderReset(Loader<List<Weblink>> loader) {
			// Do nothing.
		}
		
		// }}}
	}
	
	/**
	 * The weblinks loader.
	 * @author yjwong
	 */
	static class WeblinksLoader extends AsyncTaskLoader<List<Weblink>> {
		// {{{ properties
		
		/** Arguments to this loader */
		private Bundle mArgs;
		
		/** The context */
		private Context mContext;
		
		/** The list of weblinks */
		private List<Weblink> mWeblinks;
		
		// }}}
		// {{{ methods
		
		WeblinksLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
		}
		
		@Override
		public void onStartLoading() {
			if (mWeblinks != null	) {
				deliverResult(mWeblinks);
			}
			if (takeContentChanged() || mWeblinks == null) {
				forceLoad();
			}
		}
		
		@Override
		public List<Weblink> loadInBackground() {
			// Obtain the IVLE ID.
			String moduleIvleId = mArgs.getString("moduleIvleId");
			
			// Acquire a new IVLE object.
			IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
			try {
				Weblink[] weblinks = Module.getWeblinks(ivle, moduleIvleId);
				mWeblinks = Arrays.asList(weblinks);
				return mWeblinks;
				
			} catch (NetworkErrorException e) {
				Log.e(TAG, "NetworkErrorException encountered while loading lecturers");
			} catch (FailedLoginException e) {
				Log.e(TAG, "FailedLoginException encountered while loading lecturers");
			} catch (JSONParserException e) {
				Log.e(TAG, "JSONParserException encountered while loading lecturers");
			}
			
			return null;
		}
		
		// }}}
	}
	
	// }}}
}
