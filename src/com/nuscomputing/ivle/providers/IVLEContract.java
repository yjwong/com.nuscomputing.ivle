package com.nuscomputing.ivle.providers;

import java.util.Map;

import android.net.Uri;

/**
 * Base contract for all contracts.
 * @author yjwong
 */
public abstract class IVLEContract {
	// {{{ properties
	
	/** The name of the column containing the SQLite ID */
	public static final String ID = "_id";
	
	/** The name of the column containing the IVLE ID */
	public static final String IVLE_ID = "ivle_id";
	
	/** The name of the column containing the account name */
	public static final String ACCOUNT = "account";
	
	/** The name of the column containing the module ID */
	public static final String MODULE_ID = "module_id";
	
	// }}}
	// {{{ methods
	
	/**
	 * Method: getContentUri
	 * <p>
	 * Returns the content URI used to access the data bounded to this
	 * IVLE contract.
	 *  
	 * @return
	 */
	public abstract Uri getContentUri();
	
	/**
	 * Method: getTableName
	 * <p>
	 * Returns the table name for the data bounded to this IVLE contract.
	 * 
	 * @return
	 */
	public abstract String getTableName();
	
	/**
	 * Method: getColumnNameId
	 * <p>
	 * Returns the name of the ID column for this IVLE contract.
	 * 
	 * @return
	 */
	public String getColumnNameId() {
		return IVLEContract.ID;
	}
	
	/**
	 * Method: getColumnNameIvleId
	 * <p>
	 * Returns the name of the IVLE ID column for this IVLE contract.
	 * 
	 * @return
	 */
	public String getColumnNameIvleId() {
		return IVLEContract.IVLE_ID;
	}
	
	/**
	 * Method: getColumnNameModuleId
	 * <p>
	 * Returns the name of the module ID column for this IVLE contract.
	 * Contracts should implement this to indicate that they have a module
	 * ID column.
	 * 
	 * @return
	 */
	public abstract String getColumnNameModuleId();
	
	/**
	 * Method: getColumnNameAccount
	 * <p>
	 * Returns the name of the column containing the account name.
	 * 
	 * @return
	 */
	public String getColumnNameAccount() {
		return IVLEContract.ACCOUNT;
	}
	
	/**
	 * Method: getJoinProjectionMap
	 * <p>
	 * Returns the projection map for this object type.
	 */
	public abstract Map<String, String> getJoinProjectionMap(String prefix);
	
	// }}}
}
