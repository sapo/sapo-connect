package pt.sapo.mobile.android.connect.network;

import pt.sapo.mobile.android.connect.network.NetworkOperations.HttpMethod;
import pt.sapo.mobile.android.connect.network.OnNetworkResultsListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

/**
 * All classes that implement a WebService request object must extend this class. The name of the implementing class
 * must be the name of the WebService in the URL.
 * 
 * @author Rui Roque
 */
public abstract class RequestObject {
	
    /**
     * Originally supplied to identify who this request came from. 
     */
    protected int requestCode;
    
    /**
     * Optional parameter to identify the result set in the calling thread for identical requests.
     */
    protected Integer resultSet;
    
    
    /**
     * Get the Request Code in order to identify the requester of the WS.
     * 
     * @return A unique request code given by the caller.
     */
    public int getRequestCode() {
		return requestCode;
	}

	/**
	 * Set the Request Code in order to tag who requested the WS call.
	 *  
	 * @param requestCode A unique request code given by the caller.
	 */
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	/**
	 * Get the result set code to identify which thread or task invoked the service.
	 * 
	 * @return The result set code for this instance.
	 */
	public Integer getResultSet() {
		return resultSet;
	}

	/**
	 * Set the result set code to identify which thread or task invoked the service.
	 * 
	 * @param resultSet An integer identifier for the result set.
	 */
	public void setResultSet(Integer resultSet) {
		this.resultSet = resultSet;
	}

	/**
     * Puts all field variables in URL parameter format:
     * paramName1=param1&paramName2=param2...
     * 
     * @return A String containing all the parameters.
     */
	public abstract String toUrlParamaters();
	
	/**
	 * Determines if the WS requires the OAuth signature in the URL parameters.
	 * 
	 * @return True if requires the OAuth signature in the URL parameters. False, otherwise.
	 */
	public abstract boolean requiresOAuth();
	
	/**
	 * Returns the WebService name for the URL.
	 * 
	 * @return The WebService name for the URL.
	 */
	public String getWebServiceName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Can return any number of optional Objects.
	 * 
	 * @return
	 */
	public abstract Object[] getOptionalParameters();
	
	/**
	 * Returns the service base URL to be used.
	 * 
	 * @return The WebService base URL to use.
	 */
	public abstract String getBaseUrl();
	
	/**
	 * Determines if we need to request an explicit JSON response to the BUS in the form of an URL parameter, like
	 * jsonVar=true or jsonArg=true.
	 * 
	 * @return True if it requires an explicit JSON response from the BUS.
	 */
	public abstract boolean requiresExplicitJsonResponse();
	
	/**
	 * Determines if we need to include the Client ID in the request URL.
	 * 
	 * @return True if it requires the Client ID in the URL.
	 */
	public abstract boolean requiresClientId();
	
	/**
	 * Determines the string ID of the TTL definition.
	 * 
	 * @return The id of the string with the TTL definition in minutes.
	 */
	public abstract Integer getTtlString();
	
	/**
	 * Determines which HTTP method should be used.
	 * 
	 * @return The HTTP method to use. Supported methods are described by the enum.
	 */
	public abstract HttpMethod getHttpMethod();
	
	/**
	 * Returns the XML string for the HTTP Post. Return null for HTTP Get requests.
	 * 
	 * @return The XML for the HTTP Post.
	 */
	public abstract String getXmlPost();
	
	/**
	 * This method can contain any type of operations to be executed after we receive a valid response from the WebService.
	 * The string from the service, which may contain XML, JSON, etc, is given in the 'responseString' parameter.
	 * 
	 * @param context The caller Context.
	 * @param handler The main UI thread's of the caller Activity handler instance.
	 * @param callback The callback object in order to deliver the results to the caller Activity.
	 * @param unthreaded If true, the response is not to be delivered to another thread.
	 * @param responseString The string retrieved from the WebService.
	 * @param cursor The Cursor containing the query for the results.
	 * @param requestObject The original RequestObject that originated the 'responseString'.
	 * @return A NetworkResponseObject containing the results.
	 */
	public abstract NetworkObject executeOperations(Context context, Handler handler, OnNetworkResultsListener callback, boolean unthreaded, String responseString, Cursor cursor, RequestObject requestObject);
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("RequestObject:");
		
		sb.append("\n    WS Name            = " + this.getWebServiceName());
		sb.append("\n    Base URL           = " + this.getBaseUrl());
		sb.append("\n    URL Paramaters     = " + this.toUrlParamaters());
		sb.append("\n    Request Code       = " + this.getRequestCode());
		sb.append("\n    Use OAuth          = " + this.requiresOAuth());
		sb.append("\n    Requires JSON      = " + this.requiresExplicitJsonResponse());
		sb.append("\n    Requires Client ID = " + this.requiresClientId());
		sb.append("\n    TTL String         = " + this.getTtlString());
		sb.append("\n    HTTP Method        = " + this.getHttpMethod());
		if (getHttpMethod() == HttpMethod.POST) {
			sb.append("\n    XML Content        = " + this.getXmlPost());	
		}
		return sb.toString();
	}
	
	protected String getEsbCredentialsXmlString(String esbToken) {
		return "<def:ESBCredentials xmlns:def=\"http://services.sapo.pt/definitions\"><def:ESBToken>" + esbToken + "</def:ESBToken></def:ESBCredentials>";
	}
	
	protected String getParamXmlString(String name, String value) {
		return "<tran:" + name + ">" + value + "</tran:" + name + ">";
	}
	
	protected String getComplexType(String name, String... values) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tran:" + name + ">");
		for (String value: values) {
			sb.append(value);
		}
		sb.append("</tran:" + name + ">");
		return sb.toString();
	}
	
	protected String getRootObject(String name, String schema, String... values) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tran:" + name + " xmlns:tran=\"" + schema + "\">");
		for (String value: values) {
			sb.append(value);
		}
		sb.append("</tran:" + name + ">");
		return sb.toString();
	}
}
