package pt.sapo.mobile.android.connect.http;

import android.text.format.DateUtils;

/**
 * Specific implementation of the HttpClientConfiguration for the SAPO Connect network operations.
 * This class allows to specify an connection and socket timeout for the HTTPClient.
 * 
 * Simple usage:
 *   ConnectHttpClientConfiguration.getInstance();
 *   ConnectHttpClientConfiguration.getInstance(1*60*1000);
 * 
 * It is also valid to configure the timeout for the
 * connection through an initialization method.
 * 
 * @author Rui Roque
 */
public class ConnectHttpClientConfiguration implements IHttpClientConfiguration {
	
	/**
	 * Specifies the time-out for the socket and connection in the HTTPClient.
	 */
	private static int timeOut;
	
	/**
	 * The default time-out for the socket and connection in the HTTPClient.
	 */
	private static final int DEFAULT_TIMEOUT = (int) (DateUtils.SECOND_IN_MILLIS * 10);
	
	/**
	 * Instance for this Singleton.
	 */
	private static ConnectHttpClientConfiguration instance;
	
	
	public static synchronized ConnectHttpClientConfiguration getInstance() {
		if (instance == null) {
			instance = new ConnectHttpClientConfiguration();
		}
		timeOut = DEFAULT_TIMEOUT;
		return instance;
	}
	
	public static synchronized ConnectHttpClientConfiguration getInstance(int initTimeOut) {
		if (instance == null) {
			instance = new ConnectHttpClientConfiguration();
		}
		timeOut = initTimeOut;
		return instance;
	}
	
	public void setConnectionTimeout(int initTimeOut) {
		timeOut = initTimeOut;
	}
	
	public int getConnectionTimeout() {
		return timeOut;
	}
	
	public void setSocketTimeout(int initTimeOut) {
		timeOut = initTimeOut;
	}

	public int getSocketTimeout() {
		return timeOut;
	}
	
}
