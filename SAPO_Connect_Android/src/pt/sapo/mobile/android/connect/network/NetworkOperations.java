package pt.sapo.mobile.android.connect.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import net.oauth.OAuthException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import pt.sapo.mobile.android.connect.R;
import pt.sapo.mobile.android.connect.SAPOConnect;
import pt.sapo.mobile.android.connect.exception.SapoException;
import pt.sapo.mobile.android.connect.http.ConnectHttpClientConfiguration;
import pt.sapo.mobile.android.connect.http.MyHttpClient;
import pt.sapo.mobile.android.connect.system.Log;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

/**
 * @author Rui Roque
 */
public class NetworkOperations {

    /**
     * The Log tag for this class.
     */
    private static final String TAG = "NetworkOperations";
    
    /**
     * Creates and configures an HTTP Client every time this method is invoked.
     * 
     * @param context The caller Context.
     */
    public synchronized static HttpClient createHttpClient(Context context) {
    	Log.d(TAG, "createHttpClient() - Configuring HttpClient");
    	return MyHttpClient.getInstance(ConnectHttpClientConfiguration.getInstance()).getHttpClient();        
    }
    
    /**
     * Executes the network requests on a separate thread.
     *
     * @param runnable The runnable instance containing network runnable to be executed.
     */
    protected static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                }
            }
        };
        t.start();
        return t;
    }
	
    /**
     * Build an encoded URL from the supplied parameters. Could be something like:
     * 
     * http://host/wsName?pairedArgs[0]=pairedArgs[1]&pairedArgs[2]=pairedArgs[3]&...
     * 
     * or
     * 
     * https://host/wsName?client_id=4368fh73fbfv&pairedArgs[0]=pairedArgs[1]&pairedArgs[2]=pairedArgs[3]&...
     *
     * It will include the jsonArg parameter to get error responses in JSON from the server, instead of XML.
     *
     * @param host The hostname for the WebService.
     * @param wsName The WebService name.
     * @param requiresExplicitJsonResponse If true, the jsonArg will be used in the URL.
     * @param requiresClientId If true, the Client ID will be used in the URL.
     * @param paramsString A sequence of params URL Encoded in the form of paramName=paramValue.
     * @param clientId The Client ID for the URL.
     * @return The encoded URL.
     */
    protected static String buildUrlWithParamsString(String host, String wsName, boolean requiresExplicitJsonResponse, boolean requiresClientId, String paramsString, String clientId) {
    	// Build the URL string
    	StringBuilder sb = new StringBuilder();

    	// Build the URL
    	sb.append(host);
    	sb.append(wsName);
    	sb.append("?");
    	
    	// Add the Client ID parameter.
    	if (requiresClientId) {
    		sb.append(Services.PARAM_CLIENT_ID + "=" + clientId + "&");	
    	}
    	
    	// Add the jsonArg parameter.
    	if (requiresExplicitJsonResponse) {
    		sb.append(Services.PARAM_JSON_ARG + "=false" + "&");
    	}
    	
    	// Add the paired arguments.
    	if (paramsString != null) {
        	sb.append(paramsString);
        } else {
        	// Delete the last '&' char if it's there.
        	if (sb.charAt(sb.length() - 1) == '&') {
        		sb.deleteCharAt(sb.length() - 1);	
        	}
        }
    	    	
    	return sb.toString();
    }
    
    /**
     * Build an encoded URL without user defined parameters. The only optional param is the jsonArg value. E.g.:
     * 
     * http://host/wsName?jsonArg=false
     * 
     * if requiresExplicitJsonResponse is true, it will include the jsonArg parameter to get error responses in
     * JSON from the server, instead of XML.
     * 
     * @param host The hostname for the WebService.
     * @param wsName The WebService name.
     * @param requiresExplicitJsonResponse If true, the jsonArg will be used in the URL.
     * @return The encoded URL.
     */
    protected static String buildUrl(String host, String wsName, boolean requiresExplicitJsonResponse) {
    	// Build the URL string
    	StringBuilder sb = new StringBuilder();

    	// Build the URL
    	sb.append(host);
    	sb.append(wsName);
    	
    	// Add the jsonArg parameter.
    	if (requiresExplicitJsonResponse) {
    		sb.append("?" + Services.PARAM_JSON_ARG + "=false");
    	}
    	    	
    	return sb.toString();
    }
    
    /**
     * Dumps the contents of the requestObject into the 
     * @param requestObject
     */
    protected static void dumpRequestObject(RequestObject requestObject) {
    	if (Log.isDebug()) {
    		Log.d(TAG, requestObject.toString());	
    	}
    }
    
    /**
     * Connects to a WebService to retrieve information in the form of a JSON String.
     * 
     * @param handler The main UI thread's handler instance.
     * @param context The caller Context.
     * @param cursor The Cursor containing the query for the results.
     * @param callback The callback object in order to deliver the results.
     * @param unthreaded If true, the response is not to be delivered to another thread.
     * @param requestObject The RequestObject for the WS.
     */
    protected static NetworkObject callWebService(
    		final Handler handler,
    		final Context context,
    		final Cursor cursor,
    		final OnNetworkResultsListener callback,
    		final boolean unthreaded,
    		final RequestObject requestObject) {
    	Log.d(TAG, "callWebService() - Start");
    	    	
    	// Build the URL
    	String url = null;
    	
    	if (requestObject.getHttpMethod() == HttpMethod.GET) {
    		url = buildUrlWithParamsString(requestObject.getBaseUrl(), requestObject.getWebServiceName(), requestObject.requiresExplicitJsonResponse(), requestObject.requiresClientId(), requestObject.toUrlParamaters(), context.getString(R.string.sapo_network_client_id));	
    	} else if (requestObject.getHttpMethod() == HttpMethod.POST) {
    		url = buildUrl(requestObject.getBaseUrl(), requestObject.getWebServiceName(), requestObject.requiresExplicitJsonResponse());    		
    	} else {
    		throw new UnsupportedOperationException("Unknown HTTP Method:" + requestObject.getHttpMethod());
    	}
    	 
		Log.d(TAG, "callWebService() - URL=" + url);
		
		// Get the configured HTTL Client
		HttpClient httpClient = createHttpClient(context.getApplicationContext());
		
		// Add the URL to the GET request
		HttpRequestBase httpRequest = null;
		if (requestObject.getHttpMethod() == HttpMethod.GET) {
			httpRequest = new HttpGet(url);
		} else if (requestObject.getHttpMethod() == HttpMethod.POST) {
			httpRequest = new HttpPost(url);
			StringEntity stringEntity = null;
			try {
				stringEntity = new StringEntity(requestObject.getXmlPost(), HTTP.UTF_8);
				stringEntity.setContentType("text/xml");
				((HttpPost) httpRequest).setEntity(stringEntity);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "callWebService() - UnsupportedEncodingException", e);
				return sendResult(false, cursor, null, null, handler, context, callback, unthreaded, requestObject);
			}
		} else {
    		throw new UnsupportedOperationException("Unknown HTTP Method:" + requestObject.getHttpMethod());
    	}
		
		String responseString = null;
		
		try {
			// Send the request and catch the the response
			HttpResponse response = httpClient.execute(httpRequest);
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "callWebService() - Response status=" + response.getStatusLine().toString());
				return sendResult(false, cursor, null, null, handler, context, callback, unthreaded, requestObject);				
			}
			
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				responseString = EntityUtils.toString(entity, "UTF-8");
				Log.d(TAG, "callWebService() - Response string=" + responseString);
				entity.consumeContent();
			}
			
			if (responseString == null) {
				Log.d(TAG, "callWebService() - Result is NULL. Throwing IOException");
				throw new IOException("Unable to get any data from the response.");
			}
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, "callWebService() - ClientProtocolException", e);
			return sendResult(false, cursor, null, null, handler, context, callback, unthreaded, requestObject);
			
		} catch (IOException e) {
			Log.e(TAG, "callWebService() - IOException", e);
			int result = NetworkUtilities.checkConnectionStatus(context, httpClient, Services.SAPO_SERVICES_HOST, requestObject.getBaseUrl());
            String errorMessage = null;
            switch (result) {
	            case NetworkUtilities.STATUS_NO_NETWORK_CONNECTION:
	                // There is no Network connection
	  				Log.i(TAG, "callWebService() - No network connection available");
	  				errorMessage = context.getString(NetworkUtilities.MESSAGE_NO_NETWORK_CONNECTION);
	  				return sendResult(false, cursor, errorMessage, null, handler, context, callback, unthreaded, requestObject);
	
	            case NetworkUtilities.STATUS_NO_CONNECTION_TO_SERVER:
	  				// There is no server connection
	  				Log.i(TAG, "callWebService() - Network connection is available but server is not available");
	  				errorMessage = context.getString(NetworkUtilities.MESSAGE_NO_CONNECTION_TO_SERVER);
	  				return sendResult(false, cursor, errorMessage, null, handler, context, callback, unthreaded, requestObject);
	                
	            case NetworkUtilities.STATUS_CONNECTION_OK:
					// Some other IO error
					Log.i(TAG, "callWebService() - Network connection is available and server is reachable. Other IOException occured");
					return sendResult(false, cursor, null, null, handler, context, callback, unthreaded, requestObject);
            }
		}
		return sendResult(true, cursor, null, responseString, handler, context, callback, unthreaded, requestObject);
	}
    
    /**
     * Connects to a WebService to retrieve information in the form of a JSON String. Uses a secure OAuth connection.
     * The OAuth tokens must be initialized before any call to this method. 
     * 
     * @param handler The main UI thread's handler instance.
     * @param context The caller Context.
     * @param cursor The Cursor containing the query for the results.
     * @param callback The callback object in order to deliver the results.
     * @param unthreaded If true, the response is not to be delivered to another thread.
     * @param requestObject The RequestObject for the WS.
     */
    protected static NetworkObject callWebServiceWithOAuth(
    		final Handler handler,
    		final Context context,
    		final Cursor cursor,
    		final OnNetworkResultsListener callback,
    		final boolean unthreaded,
    		final RequestObject requestObject) {
    	    	
    	Log.d(TAG, "callWebServiceWithOauth() - Start");
    	
    	// Build the URL
    	String url = buildUrlWithParamsString(requestObject.getBaseUrl(), requestObject.getWebServiceName(), requestObject.requiresExplicitJsonResponse(), requestObject.requiresClientId(), requestObject.toUrlParamaters(), context.getString(R.string.sapo_network_client_id)); 
		Log.d(TAG, "callWebServiceWithOauth() - URL=" + url);
		
		String responseString = null;
		
		try {
			// Invoke the Service and get the response as a String
			responseString = SAPOConnect.invokeWebServiceGet(context.getApplicationContext(), url);
			Log.d(TAG, "callWebServiceWithOauth() - Response=" + responseString);
			
			if (responseString == null) {
				Log.d(TAG, "callWebServiceWithOauth() - Result is NULL. Throwing IOException");
				throw new IOException("Unable to get any data from the response.");
			}
			
		} catch (IOException e) {
			Log.e(TAG, "callWebServiceWithOauth() - IOException", e);
			
			// Get the configured HTTL Client
			HttpClient httpClient = createHttpClient(context.getApplicationContext());
			
			int result = NetworkUtilities.checkConnectionStatus(context, httpClient, Services.SAPO_SERVICES_HOST, requestObject.getBaseUrl());
            String errorMessage = null;
            switch (result) {
	            case NetworkUtilities.STATUS_NO_NETWORK_CONNECTION:
	                // There is no Network connection
	  				Log.i(TAG, "callWebServiceWithOauth() - No network connection available");
	  				errorMessage = context.getString(NetworkUtilities.MESSAGE_NO_NETWORK_CONNECTION);
	  				return sendResult(false, cursor, errorMessage, null, handler, context, callback, unthreaded, requestObject);
	
	            case NetworkUtilities.STATUS_NO_CONNECTION_TO_SERVER:
	  				// There is no server connection
	  				Log.i(TAG, "callWebServiceWithOauth() - Network connection is available but server is not available");
	  				errorMessage = context.getString(NetworkUtilities.MESSAGE_NO_CONNECTION_TO_SERVER);
	  				return sendResult(false, cursor, errorMessage, null, handler, context, callback, unthreaded, requestObject);
	                
	            case NetworkUtilities.STATUS_CONNECTION_OK:
					// Some other IO error
					Log.i(TAG, "callWebServiceWithOauth() - Network connection is available and server is reachable. Other IOException occured");
					errorMessage = context.getString(R.string.sapo_network_error_auth);
					return sendResult(false, cursor, errorMessage, null, handler, context, callback, unthreaded, requestObject);
            }
		} catch (OAuthException e) {
			Log.e(TAG, "callWebServiceWithOauth() - OAuthException", e);
			// Cannot request a new OAuth login to the user here because it may not be that kind of a problem
			return sendResult(false, cursor, context.getString(R.string.sapo_network_error_auth), null, handler, context, callback, unthreaded, requestObject);
			
		} catch (URISyntaxException e) {
			Log.e(TAG, "callWebServiceWithOauth() - URISyntaxException", e);
			return sendResult(false, cursor, context.getString(R.string.sapo_network_error_auth), null, handler, context, callback, unthreaded, requestObject);
			
		} catch (SapoException e) {
			Log.e(TAG, "callWebServiceWithOauth() - Impossible to retrieve OAuth credentials stored in SharedPreferences.");
			return sendResult(false, cursor, context.getString(R.string.sapo_network_error_oauth), null, handler, context, callback, unthreaded, requestObject);			
		}
		
		return sendResult(true, cursor, null, responseString, handler, context, callback, unthreaded, requestObject);
	}
    
    /**
     * Sends the WebService results back to the caller main UI thread through its callback object.
     * 
     * @param result If true, the WebService call was successful.
     * @param cursor The Cursor containing the query for the results.
     * @param failReason An optional reason in case of failure in the WebService call.
     * @param responseString A String containing the JSON response from the WebService.
     * @param handler The main UI thread's of the caller Activity handler instance.
     * @param context The application context.
     * @param callback The callback object in order to deliver the results to the caller Activity.
     * @param unthreaded If true, the response is not to be delivered to another thread.
     * @param requestObject The WS RequestObject,.
     */
    protected static NetworkObject sendResult(
    		final Boolean result,
    		final Cursor cursor,
    		final String failReason,
    		final String responseString,
    		final Handler handler,
    		final Context context,
    		final OnNetworkResultsListener callback,
    		final boolean unthreaded,
    		final RequestObject requestObject) {
    	Log.d(TAG, "sendResult() - Start");
    	
		final NetworkObject networkResponseObject;
		
    	if (result) {
    		// Result is OK. We have a valid responseString to process.
    		networkResponseObject = requestObject.executeOperations(context, handler, callback, unthreaded, responseString, cursor, requestObject);
    		
    	} else {
    		networkResponseObject = new NetworkObject(requestObject, cursor);
    		networkResponseObject.result = false;
    		networkResponseObject.resultFailReason = failReason;
    	}
    	
        if (unthreaded) {
        	// Send results back to the AsyncTask
        	return networkResponseObject;
            
        } else {
        	// Send results back to the UI thread if it's still there
        	if (handler == null || context == null) {
            	Log.d(TAG, "sendResult() - handler=null or context=null. Returning.");
                return null;
            }
            
        	handler.post(new Runnable() {
                public void run() {
                	if (callback != null) {
                    	callback.onNetworkResults(networkResponseObject);
                    } else {
            			Log.w(TAG, "sendResult() - Callback is NULL. Exiting without results.");
            			return;
            		}
                }
            });
        	
        	return networkResponseObject;
        }
    }
    
    /**
     * Initiates a thread with the WS and parsing operations for retrieving the elements according to the Request Object.
     * 
     * @param handler A Handler created in the UI thread of the caller Activity. 
     * @param context The application context.
     * @param cursor The Cursor containing the query for the results.
     * @param requestObject The request object describing the WS operations.
     * @param callback The callback object in order to deliver the results. 
     * @return
     */
    public static Thread invokeWebServiceFromRequestObject(final Handler handler, final Context context, final Cursor cursor, final RequestObject requestObject, final OnNetworkResultsListener callback) {
    	Log.d(TAG, "invokeWebServiceFromRequestObject() - Start");
    	dumpRequestObject(requestObject);
    	
    	if (requestObject.requiresOAuth()) {
    		final Runnable runnable = new Runnable() {
                public void run() {
                	callWebServiceWithOAuth(handler, context, cursor, callback, false, requestObject);
                }
            };
            // Run on background thread.
            return performOnBackgroundThread(runnable);
            
    	} else {
    		final Runnable runnable = new Runnable() {
                public void run() {
            		callWebService(handler, context, cursor, callback, false, requestObject);
                }
            };
            // Run on background thread.
            return performOnBackgroundThread(runnable);
    	}
    	
    }
    
    /**
     * Initiates in the current thread the WS and parsing operations for retrieving the elements according to the Request Object.
     * 
     * @param handler A Handler created in the UI thread of the caller Activity.
     * @param context The application context.
     * @param cursor The Cursor containing the query for the results.
     * @param requestObject The request object describing the WS operations.
     * @param callback The callback object in order to deliver the results.
     * @return
     */
    public static NetworkObject invokeWebServiceFromRequestObjectUnthreaded(final Handler handler, final Context context, final Cursor cursor, final RequestObject requestObject, final OnNetworkResultsListener callback) {
    	Log.d(TAG, "invokeWebServiceFromRequestObjectUnthreaded() - Start");
    	dumpRequestObject(requestObject);
    	
    	if (requestObject.requiresOAuth()) {
    		return callWebServiceWithOAuth(handler, context, cursor, callback, true, requestObject);
    	} else {
    		return callWebService(handler, context, cursor, callback, true, requestObject);
    	}
    }

    /**
     * Defines the supported HttpMethods for this utility class.
     * 
     * @author Rui Roque
     */
    public enum HttpMethod {
    	GET, POST
    }
    
}
