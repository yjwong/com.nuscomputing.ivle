package com.nuscomputing.ivle;

import java.util.Locale;

import org.joda.time.DateTime;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nuscomputing.ivle.providers.AnnouncementsContract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class NewAnnouncementsFragment extends SherlockListFragment
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "NewAnnouncementsFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	private AnnouncementsCursorAdapter mAdapter = null;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.new_announcements_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        
		// Set up the announcement adapter.
		mAdapter = new AnnouncementsCursorAdapter(
				getActivity(),
				null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		
		// Load announcement data.
        mLoader = new DataLoader(getActivity(), mAdapter, this);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(DataLoader.LOADER_NEW_ANNOUNCEMENTS_FRAGMENT, new Bundle(), mLoader);
        
        // Get the listview.
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Should start a webview here.
				Intent intent = new Intent();
				intent.setClass(getActivity(), ViewAnnouncementActivity.class);
				intent.putExtra("announcementId", id);
				startActivity(intent);
			}
		});
		
		// Set the list adapter.
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Our announcements list might have changed, requery.
		getLoaderManager().restartLoader(DataLoader.LOADER_NEW_ANNOUNCEMENTS_FRAGMENT, new Bundle(), mLoader);
	}
	
	public void onLoaderFinished(Bundle result) {
		TextView tvNoAnnouncements = (TextView) getActivity().findViewById(R.id.new_announcements_fragment_no_announcements);
		tvNoAnnouncements.setVisibility(result.getInt("cursorCount") == 0 ? TextView.VISIBLE : TextView.GONE);
	}
	
	// }}}
	// {{{ classes
	
	/**
	 * Extended SimpleCursorAdapter to also show unread items.
	 * @author yjwong
	 */
	class AnnouncementsCursorAdapter extends CursorAdapter {
		// {{{ methods
		
		public AnnouncementsCursorAdapter(Context context, Cursor c,
				int flags) {
			super(context, c, flags);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// To reference the child views in subsequent actions.
			AnnouncementsViewHolder holder;
			
			// Inflate the list item.
			if (convertView == null) {
				convertView = newView(mContext, null, parent);
				
				// Cache view fields into the holder.
				holder = new AnnouncementsViewHolder();
				holder.tvTitle = (TextView) convertView.findViewById(R.id.new_announcements_fragment_list_title);
				holder.tvDescription = (TextView) convertView.findViewById(R.id.new_announcements_fragment_list_description);
				holder.tvCreatedDate = (TextView) convertView.findViewById(R.id.new_announcements_fragment_list_created_date);
				
				// Associate the holder with the view for later lookup.
				convertView.setTag(holder);
				
			} else {
				// View already exists, get the holder instance.
				holder = (AnnouncementsViewHolder) convertView.getTag();
			}

			// Return the view.
			getCursor().moveToPosition(position);
			this.bindView(convertView, mContext, getCursor());
			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			// Get the view holder.
			AnnouncementsViewHolder holder = (AnnouncementsViewHolder) view.getTag();
			
			// Set read status.
			boolean isRead = c.getInt(c.getColumnIndex(AnnouncementsContract.IS_READ)) == 1 ? true : false;
			if (!isRead) {
				view.setBackgroundResource(R.drawable.new_announcements_fragment_list_item_unread);
				holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			} else {
				view.setBackgroundResource(0);
				holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			}
			
			// Assign the title.
			String title = c.getString(c.getColumnIndex(AnnouncementsContract.TITLE));
			holder.tvTitle.setText(title);
			
			// Use the filtered HTML description.
			String description = c.getString(c.getColumnIndex(AnnouncementsContract._DESCRIPTION_NOHTML));
			holder.tvDescription.setText(description);
			
			// Format the date and assign it.
			DateTime createdDate = new DateTime(c.getString(c.getColumnIndex(AnnouncementsContract.CREATED_DATE)));
			String month = createdDate.monthOfYear().getAsShortText(Locale.ENGLISH);
			String day = Integer.toString(createdDate.getDayOfMonth());
			String createdDateStr = month.concat(" ").concat(day);
			holder.tvCreatedDate.setText(createdDateStr);
		}

		@Override
		public View newView(Context context, Cursor c, ViewGroup parent) {
			// Inflate the layout.
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.new_announcements_fragment_list_item, null);

			// Return the view.
			return v;
		}
		
		// }}}
	}
	
	/**
	 * A view holder to cache views for scrolling performance.
	 * @author yjwong
	 */
	static class AnnouncementsViewHolder {
		TextView tvTitle;
		TextView tvDescription;
		TextView tvCreatedDate;
	}
	
	// }}}
}
