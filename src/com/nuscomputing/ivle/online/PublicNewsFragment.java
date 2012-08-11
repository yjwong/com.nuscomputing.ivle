package com.nuscomputing.ivle.online;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import com.nuscomputing.ivle.DataLoader;
import com.nuscomputing.ivle.IVLEUtils;
import com.nuscomputing.ivle.R;
import com.nuscomputing.ivlelapi.FailedLoginException;
import com.nuscomputing.ivlelapi.IVLE;
import com.nuscomputing.ivlelapi.JSONParserException;
import com.nuscomputing.ivlelapi.NetworkErrorException;
import com.nuscomputing.ivlelapi.PublicNews;

/**
 * Fragment to display IVLE public news.
 * @author yjwong
 *
 */
public class PublicNewsFragment extends SherlockListFragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "PublicNewsFragment";
	
	/** The layout inflater */
	private LayoutInflater mLayoutInflater;
	
	// }}}
	// {{{ methods 
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        // Get the layout inflater.
        mLayoutInflater = getActivity().getLayoutInflater();
		
		// Load the public news.
		getLoaderManager().initLoader(DataLoader.LOADER_PUBLIC_NEWS_FRAGMENT, new Bundle(), new PublicNewsLoaderCallbacks());
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * The list adapter for public news.
	 * @author yjwong
	 */
	class PublicNewsAdapter extends ArrayAdapter<PublicNews> {
		// {{{ methods
		
		public PublicNewsAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		public PublicNewsAdapter(Context context, int textViewResourceId,
				List<PublicNews> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the item.
			final PublicNews news = getItem(position);
			
			// Inflate list item from layout XML.
			convertView = mLayoutInflater.inflate(R.layout.public_news_fragment_list_item, null);
			
			// Set the title.
			TextView tvTitle = (TextView) convertView.findViewById(R.id.public_news_fragment_list_title);
			tvTitle.setText(news.title);
			
			// Set the description.
			TextView tvDescription = (TextView) convertView.findViewById(R.id.public_news_fragment_list_description);
			tvDescription.setText(Html.fromHtml(news.description));
			tvDescription.setLinksClickable(true);
			tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
			
			// Set the date.
			TextView tvCreatedDate = (TextView) convertView.findViewById(R.id.public_news_fragment_list_created_date);
			DateTime createdDate = new DateTime(news.createdDate);
			String month = createdDate.monthOfYear().getAsShortText(Locale.ENGLISH);
			String day = Integer.toString(createdDate.getDayOfMonth());
			String createdDateStr = month.concat(" ").concat(day);
			tvCreatedDate.setText(createdDateStr);
			
			// Return the view.
			return convertView;
		}
		
		// }}}
	}
	
	/**
	 * The loader callbacks for public news.
	 * @author yjwong
	 */
	class PublicNewsLoaderCallbacks implements
			LoaderManager.LoaderCallbacks<List<PublicNews>> {
		// {{{ methods
				
		@Override
		public Loader<List<PublicNews>> onCreateLoader(int id, Bundle args) {
			return new PublicNewsLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<PublicNews>> loader, List<PublicNews> result) {
			// Set the view data.
			if (result != null) {
				if (result.size() == 0) {
					TextView tvNoNews = (TextView) getActivity().findViewById(R.id.public_news_fragment_no_news);
					tvNoNews.setVisibility(View.VISIBLE);
					
					// Hide the list.
					getListView().setVisibility(View.GONE);
					
				} else {
					// Create new list adapter.
					PublicNewsAdapter adapter = new PublicNewsAdapter(getActivity(), R.id.public_news_fragment_no_news, result);
					setListAdapter(adapter);
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<List<PublicNews>> loader) {
			// Do nothing.
		}
		
		// }}}
	}
	
	/**
	 * The public news loader.
	 * @author yjwong
	 */
	static class PublicNewsLoader extends AsyncTaskLoader<List<PublicNews>> {
		// {{{ properties
		
		/** The context */
		private Context mContext;
		
		/** The public news */
		private List<PublicNews> mPublicNews;
		
		// }}}
		// {{{ classes
		
		public PublicNewsLoader(Context context) {
			super(context);
			mContext = context;
		}
		
		@Override
		public void onStartLoading() {
			if (mPublicNews != null	) {
				deliverResult(mPublicNews);
			}
			if (takeContentChanged() || mPublicNews == null) {
				forceLoad();
			}
		}

		@Override
		public List<PublicNews> loadInBackground() {
			// Acquire a new IVLE object.
			IVLE ivle = IVLEUtils.getIVLEInstance(mContext);
			try {
				PublicNews[] news = ivle.getPublicNews();
				mPublicNews = Arrays.asList(news);
				return mPublicNews;
				
			} catch (FailedLoginException e) {
				Log.e(TAG, "FailedLoginException encountered while loading public news");
			} catch (NetworkErrorException e) {
				Log.e(TAG, "NetworkErrorException encountered while loading public news");
			} catch (JSONParserException e) {
				Log.e(TAG, "JSONParserException encountered while loading public news");
			}
			
			return null;
		}
		
		// }}}
	}
}
