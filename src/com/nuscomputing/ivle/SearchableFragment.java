package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.List;

import com.nuscomputing.ivle.providers.ModulesContract;

import android.accounts.Account;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
	
	// }}}
	// {{{ methods
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the search query.
		Bundle args = getArguments();

		// Get the layout inflater.
		mLayoutInflater = getActivity().getLayoutInflater();
		
		// Set the loading adapter.
		ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[] { getString(R.string.loading) });
		getListView().setAdapter(loadingAdapter);
		
		// Perform the search query.
		getLoaderManager().initLoader(0, args, new SearchResultLoaderCallbacks());
		
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
			return new SearchResultLoader(getActivity(), args);
		}

		@Override
		public void onLoadFinished(Loader<List<SearchResult>> loader,
				List<SearchResult> results) {
			// Create an adapter.
			SearchResultAdapter adapter = new SearchResultAdapter(getActivity(), android.R.id.text1, results);
			setListAdapter(adapter);
		}

		@Override
		public void onLoaderReset(Loader<List<SearchResult>> loader) {
			// TODO Auto-generated method stub
			
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
		
		// }}}
		// {{{ methods
		
		SearchResultLoader(Context context, Bundle args) {
			super(context);
			mContext = context;
			mArgs = args;
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
			String query = mArgs.getString(SearchManager.QUERY);
			
			// List of results.
			List<SearchResult> resultList = new ArrayList<SearchResult>();
			
			// Get the active account.
			Account account = AccountUtils.getActiveAccount(mContext, false);
			
			// Get the content provider.
			ContentResolver resolver = mContext.getContentResolver();
			ContentProviderClient provider = resolver.acquireContentProviderClient(Constants.PROVIDER_AUTHORITY);
			
			// Acquire modules first.
			Bundle resultDetails = new Bundle();
			resultDetails.putString(SearchResult.KEY_HEADING_TITLE, "Modules");
			SearchResult result = new SearchResult(SearchResult.RESULT_TYPE_HEADING, resultDetails);
			resultList.add(result);
			
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
					while (cursor.moveToNext()) {
						resultDetails = new Bundle();
						resultDetails.putLong(SearchResult.KEY_MODULE_ID, cursor.getLong(cursor.getColumnIndex(ModulesContract.ID)));
						resultDetails.putString(SearchResult.KEY_MODULE_NAME, cursor.getString(cursor.getColumnIndex(ModulesContract.COURSE_NAME)));
						result = new SearchResult(SearchResult.RESULT_TYPE_MODULE, resultDetails);
						resultList.add(result);
					}
				}
				
			} catch (RemoteException e) {
				Log.e(TAG, "Exception encountered while searching");
			}
			
			return resultList;
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
		
		// }}}
		// {{{ methods
		
		public SearchResultAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		public SearchResultAdapter(Context context, int textViewResourceId,
				List<SearchResult> results) {
			super(context, textViewResourceId, results);
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
		public static final int RESULT_TYPE_MAX = 3;
		
		/** Keys for various result items */
		public static final String KEY_HEADING_TITLE = "heading_title";
		public static final String KEY_MODULE_ID = "module_id";
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
