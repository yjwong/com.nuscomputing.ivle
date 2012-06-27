package com.nuscomputing.ivle;

import android.os.Bundle;

/**
 * Interface that all activities within this application must implement.
 * @author yjwong
 */
public interface DataLoaderListener {
	// {{{ methods
	
	/**
	 * The main method to run after the loader is complete.
	 * @param result
	 */
	public void onLoaderFinished(Bundle result);
	
	// }}}
}
