package pt.sapo.mobile.android.connect.network;

import android.database.Cursor;

public class NetworkObject {
	
	/**
	 * Constructor.
	 * 
	 * @param requestObject The Network Request object with all the service data.
	 * @param cursor The optional Cursor for holding the data.
	 */
	public NetworkObject(RequestObject requestObject, Cursor cursor) {
		this.requestCode = requestObject.getRequestCode();
		this.resultSet = requestObject.getResultSet();
		this.optionalParams = requestObject.getOptionalParameters();
		this.cursor = cursor;
	}
	
	/**
	 * The request code of initiating the service call. This value is returned so that the caller can identify several
	 * service calls. 
	 */
	public int requestCode;
	
	/**
	 * The result set of this instance.
	 */
	public Integer resultSet;
	
	/**
	 * Operation result indicator. Can be true in case of success or false in case of failure.
	 */
	public boolean result;
	
	/**
	 * A string containing a human-readable error in case of failure.
	 */
	public String resultFailReason;
	
	/**
	 * The String containing any result in case of success.
	 */
	public String successResult;
	
	/**
	 * The cursor for holding the data.
	 */
	public Cursor cursor;
	
	/**
	 * The number of inserted rows into the DB.
	 */
	public int insertedResults;
	
	/**
	 * The number of updated results into the DB.
	 */
	public int updatedResults;
	
	/**
	 * An array of objects with optional data.
	 */
	public Object[] optionalParams;

	
	public int getRequestCode() {
		return requestCode;
	}
	
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	public Integer getResultSet() {
		return resultSet;
	}

	public void setResultSet(Integer resultSet) {
		this.resultSet = resultSet;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getResultFailReason() {
		return resultFailReason;
	}

	public void setResultFailReason(String resultFailReason) {
		this.resultFailReason = resultFailReason;
	}

	public String getSuccessResult() {
		return successResult;
	}

	public void setSuccessResult(String successResult) {
		this.successResult = successResult;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public Object[] getOptionalParams() {
		return optionalParams;
	}

	public void setOptionalParams(Object[] optionalParams) {
		this.optionalParams = optionalParams;
	}

	public int getInsertedResults() {
		return insertedResults;
	}

	public void setInsertedResults(int insertedResults) {
		this.insertedResults = insertedResults;
	}

	public int getUpdatedResults() {
		return updatedResults;
	}

	public void setUpdatedResults(int updatedResults) {
		this.updatedResults = updatedResults;
	}
	
}
