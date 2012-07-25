package com.nuscomputing.ivle;

import java.util.Locale;

import org.joda.time.DateTime;

import com.nuscomputing.ivle.providers.AnnouncementsContract;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment to list modules.
 * @author yjwong
 */
public class ModuleAnnouncementsFragment extends ListFragment
		implements DataLoaderListener {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "ModuleAnnouncementsFragment";
	
	/** Data loader instance */
	private DataLoader mLoader;
	
	/** LoaderManager instance */
	private LoaderManager mLoaderManager;
	
	/** The list adapter */
	private SimpleCursorAdapter mAdapter = null;
	
	/** The module ID */
	private long mModuleId = -1;
	
	// }}}
	// {{{ methods
	
	@Override																			
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the module info view.
		return inflater.inflate(R.layout.module_announcements_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Obtain the module ID.
		Bundle args = getArguments();
		mModuleId = args.getLong("moduleId");
        if (mModuleId == -1) {
        	throw new IllegalStateException("No module ID was passed to ModuleAnnouncementsFragment");
        }
        
		// Load the announcement data.
		String[] uiBindFrom = {
				AnnouncementsContract.TITLE,
				AnnouncementsContract.DESCRIPTION,
				AnnouncementsContract.CREATED_DATE
		};
		int[] uiBindTo = {
				R.id.module_announcements_fragment_list_title,
				R.id.module_announcements_fragment_list_description,
				R.id.module_announcements_fragment_list_created_date
		};
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.module_announcements_fragment_list_item,
				null, uiBindFrom, uiBindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		mAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// The description field.
				if (columnIndex == cursor.getColumnIndex(AnnouncementsContract.DESCRIPTION)) {
					// Filter HTML in description.
					String description = cursor.getString(columnIndex);
					description = Html.fromHtml(description).toString();
					description = description.replace('\r', ' ').replace('\n', ' ').trim();
					TextView tvDescription = (TextView) view;
					tvDescription.setText(description);
					return true;
				}
				
				if (columnIndex == cursor.getColumnIndex(AnnouncementsContract.CREATED_DATE)) {
					// Format the date.
					DateTime createdDate = new DateTime(cursor.getString(columnIndex));
					String month = createdDate.monthOfYear().getAsShortText(Locale.ENGLISH);
					String day = Integer.toString(createdDate.getDayOfMonth());
					String createdDateStr = month.concat(" ").concat(day);
					TextView tvCreatedDate = (TextView) view;
					tvCreatedDate.setText(createdDateStr);
					return true;
				}

				return false;
			}
		});
        mLoader = new DataLoader(getActivity(), mAdapter, this);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(DataLoader.LOADER_MODULE_ANNOUNCEMENTS_FRAGMENT, args, mLoader);
        
        // Get the listview.
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.module_announcements_fragment_linear_layout);
        ListView listView = (ListView) layout.findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
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
	
	public void onLoaderFinished(Bundle result) {
		TextView tvNoAnnouncements = (TextView) getActivity().findViewById(R.id.module_announcements_fragment_no_announcements);
		tvNoAnnouncements.setVisibility(result.getInt("cursorCount") == 0 ? TextView.VISIBLE : TextView.GONE);
	}
	
	// }}}
}
