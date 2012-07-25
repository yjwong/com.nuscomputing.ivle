package com.nuscomputing.ivle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for "My Agenda" screen.
 * @author yjwong
 */
public class MyAgendaFragment extends Fragment {
	// {{{ properties
	
	/** TAG for logging */
	public static final String TAG = "MyAgendaFragment";
	
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
    	return inflater.inflate(R.layout.my_agenda_fragment, container, false);
    }
    
    // }}}
}
