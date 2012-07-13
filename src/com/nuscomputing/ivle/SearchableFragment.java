package com.nuscomputing.ivle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nuscomputing.ivle.providers.ModulesContract;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.Module;
import com.nuscomputing.ivlelapi.NetworkErrorException;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Searchable fragment to display search results.
 * @author yjwong
 */
public class SearchableFragment extends ListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "SearchableFragment";
	
	/** A reference to the layout inflater */
	private LayoutInflater mLayoutInflater;
	
	/** The search results adapter */
	private ArrayAdapter<SearchResult> mAdapter;
	
	// }}}
	// {{{ methods
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.searchable_fragment, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the search query.
		Bundle args = getArguments();

		// Get the layout inflater.
		mLayoutInflater = getActivity().getLayoutInflater();
		
		// Create the adapter with no results first, we will populate it as
		// we search.
		List<SearchResult> results = new ArrayList<SearchResult>();
		mAdapter = new SearchResultAdapter(getActivity(), results);
		setListAdapter(mAdapter);
		
		// Perform the search query.
		getLoaderManager().initLoader(DataLoader.LOADER_SEARCHABLE_FRAGMENT, args, new SearchResultLoaderCallbacks());
		
		// Get the list view.
		ListView listview = getListView();
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Get the search result.
				SearchResult result = (SearchResult) parent.getItemAtPosition(position);
				Bundle resultDetails = result.getResultDetails();
				int resultType = result.getResultType();
				
				// Act based on the result type.
				switch (resultType) {
					case SearchResult.RESULT_TYPE_HEADING:
						return;
					
					case SearchResult.RESULT_TYPE_MODULE:
						// Get the module ID.
						long moduleId = resultDetails.getLong(SearchResult.KEY_MODULE_ID);
						String moduleCourseName = resultDetails.getString(SearchResult.KEY_MODULE_NAME);
						
						// Start the module activity.
						Intent intent = new Intent();
						intent.putExtra("moduleId", moduleId);
						intent.putExtra("moduleCourseName", moduleCourseName);
						intent.setClass(getActivity(), ModuleActivity.class);
						startActivity(intent);
						return;
					
					case SearchResult.RESULT_TYPE_MODULE_ONLINE:
						// Get the IVLE ID.
						String moduleIvleId = resultDetails.getString(SearchResult.KEY_MODULE_IVLE_ID);
						moduleCourseName = resultDetails.getString(SearchResult.KEY_MODULE_NAME);
						
						// Start the online module activity.
						intent = new Intent();
						intent.putExtra("moduleIvleId", moduleIvleId);
						intent.putExtra("moduleCourseName", moduleCourseName);
						intent.setClass(getActivity(), com.nuscomputing.ivle.online.ModuleActivity.class);
						startActivity(intent);
						return;
					
					default:
						// Ignore.
				}
			}
		});
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The loader callbacks for search results.
	 * @author yjwong
	 */
	class SearchResultLoaderCallbacks implements
			LoaderManager.LoaderCallbacks<List<SearchResult>> {
		// {{{ methods
		
		@Override
		public Loader<List<SearchResult>> onCreateLoader(int id, Bundle args) {
			// Create a new loader.
			SearchResultLoader loader = new SearchResultLoader(getActivity(), args);
			loader.setAdapter(mAdapter);
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<List<SearchResult>> loader,
				List<SearchResult> results) {
			// Check if there are no results.
			if (results.size() == 0) {
				TextView tvNoSearchResults = (TextView) getActivity().findViewById(R.id.searchable_fragment_no_search_results);
				tvNoSearchResults.setVisibility(View.VISIBLE);
			} else {
				// We finished searching, notify that our data set is changed.
				mAdapter.clear();
				mAdapter.addAll(results);
				mAdapter.notifyDataSetChanged();
			}
			
			// Hide the loading progress.
			TextView tvLoading = (TextView) getActivity().findViewById(R.id.searchable_fragment_loading);
			tvLoading.setVisibility(View.GONE);
		}

		@Override
		public void onLoaderReset(Loader<List<SearchResult>> loader) {
			// Do nothing.
		}
		
		// }}}
	}
	
	/**
	 * The loader for search results.
	 * @author yjwong
	 */
	static class SearchResultLoader extends AsyncTaskLoader<List<SearchResult>> {
		// {{{ properties
		
		/** Arguments to this loader */
		private Bundle mArgs; 
		
		/** The context */
		private Context mContext;
		
		/** The search results */
		private List<SearchResult> mSearchResults;
		
		/** The adapter */
		private ArrayAdapter<SearchResult> mAdapter;
		
		/** Handler to run stuff on UI thread */
		private Handler mHandler = new Handler();
		
		/** Runnable to update results */
		private Runnable mResultUpdater = new Runnable() {
			@TargetApi(11)
			@Override
			public void run() {
				mAdapter.clear();
				mAdapter.addAll(mSearchResults);
				mAdapter.notifyDataSetChanged();
			}
		};
		
		// }}}
		// {{{ methods
		
		SearchResultLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
		}
		
		void setAdapter(ArrayAdapter<SearchResult> adapter) {
			mAdapter = adapter;
		}
		
		@Override
		public void onStartLoading() {
			if (mSearchResults != null) {
				deliverResult(mSearchResults);
			}
			if (takeContentChanged() || mSearchResults == null) {
				forceLoad();
			}
		}
		
		@Override
		public List<SearchResult> loadInBackground() {
			// Get the search query.
			Bundle resultDetails;
			SearchResult result;
			String query = mArgs.getString(SearchManager.QUERY);
			
			// List of results.
			mSearchResults = new ArrayList<SearchResult>();
			
			// Get the active account.
			Account account = AccountUtils.getActiveAccount(mContext, false);
			
			// Get an IVLE instance.
			IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
			
			// Get the content provider.
			ContentResolver resolver = mContext.getContentResolver();
			ContentProviderClient provider = resolver.acquireContentProviderClient(Constants.PROVIDER_AUTHORITY);
			
			// Acquire modules first.
			try {
				Cursor cursor = provider.query(ModulesContract.CONTENT_URI, 
						new String[] {
							ModulesContract.ID,
							ModulesContract.COURSE_NAME
						},
						"(" +
							ModulesContract.COURSE_CODE + " LIKE ? OR " +
							ModulesContract.COURSE_NAME + " LIKE ? " +
						") AND " +
						DatabaseHelper.MODULES_TABLE_NAME + "." + ModulesContract.ACCOUNT + " = ?",
						new String[] {
							query,
							"%" + query + "%",
							account.name
						}, null);
				
				// Have we found any results in modules?
				if (cursor.getCount() > 0) {
					// Add the heading.
					resultDetails = new Bundle();
					resultDetails.putString(SearchResult.KEY_HEADING_TITLE, "Modules");
					result = new SearchResult(SearchResult.RESULT_TYPE_HEADING, resultDetails);
					mSearchResults.add(result);
					
					// Add the results.
					while (cursor.moveToNext()) {
						resultDetails = new Bundle();
						resultDetails.putLong(SearchResult.KEY_MODULE_ID, cursor.getLong(cursor.getColumnIndex(ModulesContract.ID)));
						resultDetails.putString(SearchResult.KEY_MODULE_NAME, cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
						result = new SearchResult(SearchResult.RESULT_TYPE_MODULE, resultDetails);
						mSearchResults.add(result);
					}
				}
				
				// Update results.
				mHandler.post(mResultUpdater);
				
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException encountered while searching");
			}
			
			// Acquire online modules.
			try {
				// Obtain the modules via the API.
				Map<String, String> criterion = new HashMap<String, String>();
				//criterion.put("ModuleCode", query);
				criterion.put("ModuleTitle", URLEncoder.encode(query, "UTF-8"));
				Module[] modules = ivle.searchModules(criterion);
				
				// Have we found any results?
				if (modules.length > 0) {
					// Add the heading.
					resultDetails = new Bundle();
					resultDetails.putString(SearchResult.KEY_HEADING_TITLE, "Modules (Online)");
					result = new SearchResult(SearchResult.RESULT_TYPE_HEADING, resultDetails);
					mSearchResults.add(result);
					
					// Add the results.
					for (Module module : modules) {
						resultDetails = new Bundle();
						resultDetails.putString(SearchResult.KEY_MODULE_IVLE_ID, module.ID);
						resultDetails.putString(SearchResult.KEY_MODULE_NAME, module.courseName);
						result = new SearchResult(SearchResult.RESULT_TYPE_MODULE_ONLINE, resultDetails);
						mSearchResults.add(result);
					}
				}
				
				// Update results.
				mHandler.post(mResultUpdater);
				
			} catch (NetworkErrorException e) {
				Log.e(TAG, "NetworkErrorException encountered in searchModules");
			} catch (FailedLoginException e) {
				Log.e(TAG, "FailedLoginException encountered in searchModules");
			} catch (JSONParserException e) {
				Log.e(TAG, "JSONParserException encountered in searchModules");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException encountered in searchModules");
			}
			
			return mSearchResults;
		}
		
		// }}}
	}
	
	/**
	 * Search result adapter.
	 * @author yjwong
	 */
	class SearchResultAdapter extends ArrayAdapter<SearchResult> {
		// {{{ properties
		
		/** The view types */
		public static final int VIEW_TYPE_HEADING = 1;
		public static final int VIEW_TYPE_MODULE = 2;
		public static final int VIEW_TYPE_MODULE_ONLINE = 3;
		
		// }}}
		// {{{ methods
		
		public SearchResultAdapter(Context context, List<SearchResult> results) {
			super(context, android.R.id.text1, results);
		}

		@Override
		public int getItemViewType(int position) {
			// Get the item.
			SearchResult item = getItem(position);
			return item.getResultType();
		}
		
		@Override
		public int getViewTypeCount() {
			return SearchResult.RESULT_TYPE_MAX;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the view type.
			int viewType = getItemViewType(position);
			
			// Get the item.
			SearchResult item = getItem(position);
			Bundle resultDetails = item.getResultDetails();
			
			// Setup the appropriate view based on the view type.
			switch (viewType) {
				case VIEW_TYPE_HEADING:
					convertView = mLayoutInflater.inflate(R.layout.searchable_fragment_list_heading, null);
					TextView tvHeadingTitle = (TextView) convertView.findViewById(R.id.searchable_fragment_list_heading_title);
					tvHeadingTitle.setText(resultDetails.getString(SearchResult.KEY_HEADING_TITLE));
					return convertView;
					
				case VIEW_TYPE_MODULE:
				case VIEW_TYPE_MODULE_ONLINE:
					convertView = mLayoutInflater.inflate(R.layout.searchable_fragment_list_item_module, null);
					convertView.setTag(item);
					TextView tvModuleTitle = (TextView) convertView.findViewById(R.id.searchable_fragment_list_module_name);
					tvModuleTitle.setText(resultDetails.getString(SearchResult.KEY_MODULE_NAME));
					return convertView;
					
				default:
					throw new IllegalArgumentException("invalid view type");
			}
		}
		
		// }}}
	}
	
	/**
	 * Represents a search result.
	 * @author yjwong
	 */
	static class SearchResult {
		// {{{ properties
		
		/** The types of search result */
		public static final int RESULT_TYPE_HEADING = 1;
		public static final int RESULT_TYPE_MODULE = 2;
		public static final int RESULT_TYPE_MODULE_ONLINE = 3;
		public static final int RESULT_TYPE_MAX = 4;
		
		/** Keys for various result items */
		public static final String KEY_HEADING_TITLE = "heading_title";
		public static final String KEY_MODULE_ID = "module_id";
		public static final String KEY_MODULE_IVLE_ID = "module_ivle_id";
		public static final String KEY_MODULE_NAME = "module_name";
		
		/** Bundle containing the result details */
		private final Bundle mResultDetails; 
		
		/** The type of this search result */
		private int mResultType;
		
		// }}}
		// {{{ methods
		
		SearchResult(int type, Bundle details) {
			// Check the type.
			if (type >= RESULT_TYPE_MAX) {
				throw new IllegalArgumentException("type is invalid");
			}
			
			mResultType = type;
			mResultDetails = details;
		}
		
		/**
		 * Method: getResultType
		 * <p>
		 * Gets the result type for the search query.
		 */
		public int getResultType() {
			return mResultType;
		}
		
		/**
		 * Method: getResultDetails
		 * <p>
		 * Gets the result details.
		 */
		public Bundle getResultDetails() {
			return mResultDetails;
		}
		
		// }}}
	}
	
	// }}}
}
