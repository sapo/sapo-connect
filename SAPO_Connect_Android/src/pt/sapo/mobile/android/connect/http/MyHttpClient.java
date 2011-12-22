package pt.sapo.mobile.android.connect.http;

import java.lang.ref.SoftReference;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import pt.sapo.mobile.android.connect.system.Log;

/**
 * Singleton class for creating HTTPClient instances properly configured by an implementation of the IHttpClientConfiguration interface.
 * 
 * Simple Usages:
 *   MyHttpClient.getInstance(HttpClientConfigurationV2.getInstance(getApplicationContext())).getHttpClient(true);
 *   MyHttpClient.getInstance(HttpClientConfigurationV2.getInstance(getApplicationContext())).getHttpClient(false);
 *   MyHttpClient.getInstance(HttpClientConfigurationV2.getInstance(getApplicationContext())).getHttpClient();
 * 
 * The last two invocations will create an HTTPClient without the HTTPS schema registered.
 * 
 * @author Rui Roque
 */
public class MyHttpClient {
	
	/**
	 * Log tag for this class.
	 */
	private static final String TAG = "MyHttpClient";
	
	/**
	 * The connection timeout for the HTTPClient.
	 */
	private int httpClientConnectionTimeout;
	
	/**
	 * The socket timeout for the HTTPClient.
	 */
	private int httpClientSocketTimeout;
	

	/**
	 * The instance reference for this Singleton.
	 */
	private static SoftReference<MyHttpClient> instanceRef;
	
	/**
	 * Constructor. It will fetch the available configuration in the IHttpClientConfiguration implementation that was passed
	 * as parameter to the constructor.
	 * 
	 * @param httpConfiguration The Class that implements the interface for the client configuration. 
	 */
	private MyHttpClient(IHttpClientConfiguration httpConfiguration) {
		this.httpClientConnectionTimeout = httpConfiguration.getConnectionTimeout();
		this.httpClientSocketTimeout = httpConfiguration.getSocketTimeout();
		Log.d(TAG, "MyHttpClient() - HTTPClientConnectionTimeout=" + httpClientConnectionTimeout + "; HTTPClientSocketTimeout=" + httpClientSocketTimeout);
	}
	
	/**
	 * Get an instance of the HTTP Client.
	 * 
	 * @param httpConfiguration The implementing class of the interface for the client configuration.
	 * @return An instance of this Singleton.
	 */
	public synchronized static MyHttpClient getInstance(IHttpClientConfiguration httpConfiguration) {
		if (instanceRef != null && instanceRef.get() != null) {
			return instanceRef.get();
		}
		instanceRef = new SoftReference<MyHttpClient>(new MyHttpClient(httpConfiguration));
		return instanceRef.get();
	}
	
	/**
	 * Gets an new instance of a configured HTTP Client.
	 * 
	 * @return The configured HTTP Client.
	 */
	public synchronized HttpClient getHttpClient() {
		return createHttpClient(false);
	}

	/**
	 * Gets an new instance of a configured HTTP Client.
	 * 
	 * @return The configured HTTP Client.
	 */
	public synchronized HttpClient getHttpClient(boolean enableHTTPS) {
		return createHttpClient(true);
	}
	
	/**
	 * @param enableHTTPS If true, the HTTPS scheme will be registered and HTTPS connections will be allowed with the created HTTPClient instance.
	 * @return A HTTPClient instance properly configured with the given configuration.
	 */
	private HttpClient createHttpClient(boolean enableHTTPS) {
		// Get the parameters from the DefaultHttpClient.
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
				
		// Registers the HTTP and HTTPS schemes.
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		if (enableHTTPS) {
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		}
		
		// Creates a new ThreadSafeClientConnManager with the params and scheme registry for the HTPPS connection.
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
		
		// Creates the HttpClient.
		httpClient = new DefaultHttpClient(manager, params);
		HttpConnectionParams.setConnectionTimeout(params, httpClientConnectionTimeout);
	    HttpConnectionParams.setSoTimeout(params, httpClientSocketTimeout);
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
	    ConnManagerParams.setTimeout(params, httpClientConnectionTimeout);
		
		return httpClient;
	}
	
}