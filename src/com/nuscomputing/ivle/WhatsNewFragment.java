package com.nuscomputing.ivle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for "What's New" screen.
 * @author yjwong
 */
public class WhatsNewFragment extends Fragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "WhatsNewFragment";
	
	// }}}
	// {{{ methods
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.whats_new_fragment, container, false);
    }
    
    // }}}
}
