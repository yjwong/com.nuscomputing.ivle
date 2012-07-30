package com.nuscomputing.ivle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * A dialog fragment for item details.
 * @author yjwong
 */
public class DetailsDialogFragment extends SherlockDialogFragment {
	// {{{ methods
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get the fragment arguments.
		Bundle args = getArguments();
		LinkedHashMap<?, ?> detailsMapFromBundle = (LinkedHashMap<?, ?>) args.getSerializable("items");
		LinkedHashMap<String, String> detailsMap = new LinkedHashMap<String, String>();
		for (Map.Entry<?, ?> detail : detailsMapFromBundle.entrySet()) {
			detailsMap.put(detail.getKey().toString(), detail.getValue().toString());
		}
		
		// Create the details list.
		List<HashMap<String, String>> detailsList = createDetailsList(detailsMap); 
		String dialogTitle = args.getString("title") != null ? args.getString("title") : "";
		
		// Return a dialog.
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ic_menu_info_details)
			.setTitle(dialogTitle)
			.setPositiveButton("Close", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setAdapter(
				new SimpleAdapter(
					getActivity(),
					detailsList,
					R.layout.details_dialog_fragment_list_item,
					new String[] { "title", "data" },
					new int[] { android.R.id.text1, android.R.id.text2 }
				), 
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing.
					}
				}
			)
			.create();
		
		// Obtain the list view for the adapter.
		dialog.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Do nothing.
			}
		});
		
		return dialog;
	}
	
	/**
	 * Method: createAdapterDetailsMap
	 * <p>
	 * Creates a details bundle for use with this DialogFragment.
	 * 
	 * @param items
	 * @return
	 */
	private List<HashMap<String, String>> createDetailsList(Map<String, String> items) {
		// Create the master bundle.
		List<HashMap<String, String>> detailsList = new ArrayList<HashMap<String, String>>();
		
		// Iterate through each item.
		for (Map.Entry<String, String> item : items.entrySet()) {
			HashMap<String, String> detail = new HashMap<String, String>();
			detail.put("title", item.getKey());
			detail.put("data", item.getValue());
			detailsList.add(detail);
		}
		
		return detailsList;
	}
	
	// }}}
}
