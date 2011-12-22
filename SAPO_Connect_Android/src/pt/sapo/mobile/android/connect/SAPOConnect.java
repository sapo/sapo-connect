package pt.sapo.mobile.android.connect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.client.httpclient4.HttpClientPool;
import net.oauth.http.HttpMessage;

import org.apache.http.client.HttpClient;

import pt.sapo.mobile.android.connect.exception.SapoException;
import pt.sapo.mobile.android.connect.http.ConnectHttpClientConfiguration;
import pt.sapo.mobile.android.connect.http.MyHttpClient;
import pt.sapo.mobile.android.connect.ntp.NTPClient;
import pt.sapo.mobile.android.connect.system.Log;
import pt.sapo.mobile.android.connect.system.VersionCodes;
import pt.sapo.mobile.android.connect.system.sharedpreference.SharedPreferencesOperations;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This abstract Activity is responsible for the entire OAuth process to login to SAPO Connect and obtain the SSO
 * User Token and Secret. The extending class can implement some optional interfaces to customize behavior. 
 * 
 * Abstract methods:
 * 
 *  public abstract LogInInterface getAditionalLogInOperations();
 *  public abstract LogOutInterface getAditionalLogOutOperations();
 *  public abstract WindowTitleBarControlInterface getWindowTitleBarControl();
 *  public abstract CustomNotificationsInterface getCustomNotificationsLayouts();
 * 
 * These four methods may return null or the optional customization interfaces.
 * 
 * This class relies in some mandatory values in the services.xml file that must be defined by the implementing application:
 *  sapo_connect_consumer_secret
 *  sapo_connect_consumer_key
 *  sapo_connect_callback_url
 * 
 * Simple usage:
 * 
 * Create a class in your application, e.g. 'MyAppSAPOConnect'.
 * 
 * public class MyAppSAPOConnect extends SAPOConnect {
 *     @Override public LogInInterface getAditionalLogInOperations() { return null; }
 *     @Override public LogOutInterface getAditionalLogOutOperations() { return null; }
 *     @Override public WindowTitleBarControlInterface getWindowTitleBarControl() { return null; }
 *     @Override public CustomNotificationsInterface getCustomNotificationsLayouts() { return null; }
 * }
 * 
 * For a customized configuration, one or more of the available interfaces may be used.
 * E.g. For adding additional LogIn functionality:
 * 
 * private LogInInterface logInInterface = new LogInInterface() {
 *     @Override
 *     public void logIn(Context context) {
 *         if (someOperation(context)) {
 *             setUserRegistered(context, true);
 *             goBackWithResults(true);
 *         } else {
 *             goBackWithResults(false);
 *         }
 *     }
 * };
 * 
 * @Override
 * public LogInInterface getAditionalLogInOperations() {
 *     return logInInterface;
 * }
 * 
 * Read each interface documentation to know how to use it.
 * 
 * @author Rui Roque
 */
public abstract class SAPOConnect extends Activity {
		
	/**
	 * Log tag for this Activity.
	 */
	private static final String TAG = "SAPOConnect"; 

	/**
	 * The Consumer Secret obtained from the SAPO Connect registration site.
	 */
	private static String sapoConsumerSecret;
	
	/**
	 * The Consumer Key obtained from the SAPO Connect registration site.
	 */
	private static String sapoConsumerKey;
		
	/**
	 * The callback URL that the SAPO Connect calls when the authentication process is completed.
	 */
	private static String callbackUrl;
	
	/**
	 * URI form for the CALLBACK_URL.
	 */
	private static Uri callbackUri;
	
	/**
	 * SharedPreferences file to store the SSO keys once they are obtained.
	 */
	private static final String SHARED_PREFS_FILE = "OAuth";
	
	/**
	 * SharedPreferences keys for storing values.
	 */
	private static final String REQUEST_TOKEN = "request_token";
	private static final String REQUEST_SECRET = "request_secret";
	private static final String USER_TOKEN = "user_token";
	private static final String USER_SECRET = "user_secret";
	private static final String USER_REGISTERED = "user_registered";
	
	/**
	 * The WebView to deal with all of the HTTPS calls and redirections. 
	 */
	private WebView webView;
	
	/**
	 * The main OAuthClient. 
	 */
	private OAuthClient oAuthClient;
	
	/**
	 * The OAuthAccessor for invoking the protected resource.
	 */
	private OAuthAccessor oAuthAccessor;
	
	/**
	 * The custom Alert Dialog for showing error messages with actions.
	 */
	protected AlertDialog alertDialog;
	
	/**
	 * Handler so that the WebView can post things back to the UI thread (in the error page).
	 */
	private Handler handler;
	
	// ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                      ACTIVITY INITIALIZATION                                                        //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
	/**
	 * Intent key to determine the Activity mode. It can be one of SAPO_CONNECT_LOGIN or SAPO_CONNECT_LOGOUT.
	 */
	public static final String SAPO_CONNECT_OPERATION = "SapoConnectOperation";
	
	/**
	 * Intent value for SAPO_CONNECT_OPERATION to start the Activity in the Login mode. 
	 */
	public static final int SAPO_CONNECT_LOGIN = 0;
	
	/**
	 * Intent value for SAPO_CONNECT_OPERATION to start the Activity in the Logout mode. 
	 */
	public static final int SAPO_CONNECT_LOGOUT = 1;
	
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                        SERVER CONFIGURATION                                                         //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
    /**
     * SAPO ID endpoints.
     */
	private static String sapoConnectUrl;
    
	/**
	 * The URL's to the SAPO ID endpoints.
	 */
	private static String sapoRequestTokenUrl;
	private static String sapoAccessTokenUrl;
	private static String sapoAuthorizeUrl;
	private static String sapoAuthorizationDenied;
	
	/**
	 * This identifier must be added to the UserAgent when requesting the Login Page, so that the correct native
	 * mobile version of the page can be requested correctly.
	 */
	private static final String SAPO_ID_LOGIN_USER_AGENT_PARAM = "; SapoAppWebView";
	
    
	// ********************************************************************************************************************************** //
    //                                              OPTIONAL IMPLEMENTING CLASS INTERFACES                                                //
    // ********************************************************************************************************************************** //
	
	/**
	 * If the implementing class wants to add functionality to the logOut method, it must pass this interface implementation
	 * in the aditionalLogOutOperations() method.
	 * 
	 * E.g.:
	 *
	 * private LogOutInterface logOutInterface = new LogOutInterface() {
	 *     @Override
	 *     public void logOut(Context context) {
	 *         removePublicProfile(context);
	 *     }
	 * };
	 *  
	 * @Override
	 * public LogOutInterface aditionalLogOutOperations() {
	 *     return logOutInterface;
	 * }
	 * 
	 * @author Rui Roque
	 */
	public interface LogOutInterface {
		void logOut(Context context);
	}
	
	/**
	 * If the implementing class wants to add functionality to be executed after the SSO process, it must pass this interface
	 * implementation in the aditionalLogInOperations() method. It is crucial if this interface is to be implemented, that at
	 * some point, the implementing class must invoke:
	 * 
	 * setUserRegistered(getApplicationContext(), true);
	 * 
	 * and:
	 * 
	 * goBackWithResults(true|false);
	 * 
	 * If this is not invoked, the user will never be effectively logged-in.
	 * 
	 * E.g.:
	 * 
	 * private LogInInterface logInInterface = new LogInInterface() {
	 *     @Override
	 *     public void logIn(Context context) {
	 *         if (someOperation(context)) {
	 *             setUserRegistered(context, true);
	 *             goBackWithResults(true);
	 *         } else {
	 *             goBackWithResults(false);
	 *         }
	 *     }
	 * };
	 * 
	 * @Override
	 * public LogInInterface getAditionalLogInOperations() {
	 *     return logInInterface;
	 * }
	 *  
	 * @author Rui Roque
	 */
	public interface LogInInterface {
		void logIn(Context context);
	}
	
	/**
	 * If the implementing class wants to add functionality to show any kind of progress when the WebView is busy, it must pass
	 * this interface implementation in the getWindowTitleBarControl() method.
	 * 
	 * @author Rui Roque
	 */
	public interface WindowTitleBarControlInterface {

		/**
		 * The implementing class can specify which Window Features to use so they may be requested in requestWindowFeature()
		 * before setting the content view for the SAPOConnect Activity.
		 * 
		 * @return True to use a Window Custom Title Bar. False to use the default Window Title Bar.
		 */
		int[] getWindowFeatures();
		
		/**
		 * Sets up the Window Title Bar with the loading (refresh) icon, enabling it to become animated to show some progress.
		 */
		void setUpWindowTitleBar();
		
	    /**
	     * Starts the indeterminate animation in the refresh button on the window title bar.
	     */
		void startRefreshAnimation();
		
	    /**
	     * Stops the indeterminate animation in the refresh button on the window title bar. The way to do this is to start
	     * a determinate animations and let it end it's cycle. The user will not notice the overhead time.
	     */
		void stopRefreshAnimation();
	}
    
	/**
	 * If the implementing class wants to add custom layouts for the Toast notifications and the Alert Dialogs, it must pass
	 * this interface implementation in the getCustomNotificationsLayouts() method.
	 * 
	 * @author Rui Roque
	 */
	public interface CustomNotificationsInterface {
		/**
		 * An optional custom layout for the Toast notifications. If this interface is not implemented, or if this method
		 * returns null, a standard Toast will be showed. The custom layout must have the following elements:
		 *   - A top container with android:id="@+id/toast_layout_root".
		 *   - A TextView with android:id="@+id/toast_text"
		 *   
		 * @return The layout ID of the custom layout. E.g.: return R.layout.my_custom_toast.
		 */
		Integer getCustomToastLayout();
		
		/**
		 * An optional custom layout for the AlertDialog with a positive and a negative button. If this interface is not
		 * implemented, or if this method returns null, a standard AlertDialog will be showed. The custom layout must have
		 * the following elements:
		 *   - A top container with android:id="@+id/customDialog_layoutRoot"
		 *   - A TextView with android:id="@+id/customDialog_text"
		 *   - A Button for the positive answer with android:id="@+id/customDialog_okBtn"
		 *   - A Button for the negative answer with android:id="@+id/customDialog_nokBtn"
		 *   
		 * @return The layout ID of the custom layout. E.g.: return R.layout.my_custom_alert_dialog.
		 */
		Integer getCustomAlertDialogLayout();
		
		/**
		 * An optional custom layout for the AlertDialog with only a positive button. If this interface is not implemented,
		 * or if this method returns null, a standard AlertDialog will be showed. The custom layout must have the following
		 * elements:
		 *   - A top container with android:id="@+id/customDialog_layoutRoot"
		 *   - A TextView with android:id="@+id/customDialog_text"
		 *   - A Button for the positive answer with android:id="@+id/customDialog_okBtn"
		 *   
		 * @return The layout ID of the custom layout. E.g.: return R.layout.my_custom_alert_dialog_one_button.
		 */
		Integer getCustomAlertDialogLayoutOneButton();
	}
	
	/**
	 * The optional additional logIn operations passed by the implementing class.
	 */
	private static LogInInterface logInInterface;
	
	/**
	 * The optional additional logOut operations passed by the implementing class.
	 */
	private static LogOutInterface logOutInterface;
	
	/**
	 * The optional Window Title Bar controls passed by the implementing class.
	 */
	private WindowTitleBarControlInterface windowTitleBarControlInterface;
	
	/**
	 * The optional interface that implements getter methods for the Toast and AlertDialog custom layouts.
	 */
	private CustomNotificationsInterface customNotificationsInterface;

	
	// ********************************************************************************************************************************** //
    //                                                         ABSTRACT METHODS                                                           //
    // ********************************************************************************************************************************** //
		
	/**
	 * The implementing class may add some additional functionality to the LogIn operation by the use of the LogInInterface.
	 * If the LogInInterface is implemented, it should be retrieved by this method.  
	 *   
	 * @return The optional LogInInterface implementation.
	 */
	public abstract LogInInterface getAditionalLogInOperations();
	
	/**
	 * The implementing class may add some additional functionality to the LogOut operation by the use of the LogOutInterface.
	 * If the LogOutInterface is implemented, it should be retrieved by this method.  
	 *   
	 * @return The optional LogOutInterface implementation.
	 */
	public abstract LogOutInterface getAditionalLogOutOperations();
	
	/**
	 * The implementing class may add some sort of control to determine the WebView activity progress by the use of the
	 * WindowTitleBarControlInterface. If the WindowTitleBarControlInterface is implemented, it should be retrieved by this method.  
	 *   
	 * @return The optional WindowTitleBarControlInterface implementation.
	 */
	public abstract WindowTitleBarControlInterface getWindowTitleBarControl();
	
	/**
	 * The implementing class may add custom layouts for the Toast notifications and the AlertDialogs.
	 * 
	 * @return The optional CustomNotificationsInterface implementation.
	 */
	public abstract CustomNotificationsInterface getCustomNotificationsLayouts();
	
	
	// ********************************************************************************************************************************** //
    //                                                       ACTIVITY LIFECYCLE                                                           //
    // ********************************************************************************************************************************** //
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Get the mandatory configuration from the system.
	    sapoConnectUrl = getString(R.string.sapo_connect_url);
	    
	    sapoRequestTokenUrl = getString(R.string.sapo_connect_request_token_url);
	    sapoAccessTokenUrl = getString(R.string.sapo_connect_access_token_url);
	    sapoAuthorizeUrl = getString(R.string.sapo_connect_authorize_url);
	    sapoAuthorizationDenied = getString(R.string.sapo_connect_authorization_denied);
	    
	    sapoConsumerSecret = getString(R.string.sapo_connect_consumer_secret);
	    sapoConsumerKey = getString(R.string.sapo_connect_consumer_key);
	    callbackUrl = getString(R.string.sapo_connect_callback_url);
	    
	    if (TextUtils.isEmpty(sapoConnectUrl) || TextUtils.isEmpty(sapoRequestTokenUrl) || TextUtils.isEmpty(sapoAccessTokenUrl) ||
	    		TextUtils.isEmpty(sapoAuthorizeUrl) || TextUtils.isEmpty(sapoAuthorizationDenied) || 
	    		TextUtils.isEmpty(sapoConsumerSecret) || TextUtils.isEmpty(sapoConsumerKey) || TextUtils.isEmpty(callbackUrl)) {
	    	throw new UnsupportedOperationException("Some mandatory values are missing. Check your services.xml file.");
	    }
	    
	    // Build the correct URLs
	    sapoRequestTokenUrl = "https://" + sapoConnectUrl + sapoRequestTokenUrl;
	    sapoAccessTokenUrl = "https://" + sapoConnectUrl + sapoAccessTokenUrl;
	    sapoAuthorizeUrl = "https://" + sapoConnectUrl + sapoAuthorizeUrl;
	    sapoAuthorizationDenied = "https://" + sapoConnectUrl + sapoAuthorizationDenied;
	    
	    callbackUri = Uri.parse(callbackUrl);
	    
	    // Get the optional configuration from the implementing class.
	    logInInterface = getAditionalLogInOperations();
	    logOutInterface = getAditionalLogOutOperations();
	    windowTitleBarControlInterface = getWindowTitleBarControl();
	    customNotificationsInterface = getCustomNotificationsLayouts();
	    
	    // Get the extras from the Intent to determine the Activity mode.
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	    	int sapoconnectOperation = extras.getInt(SAPO_CONNECT_OPERATION);
	    	switch (sapoconnectOperation) {
				case SAPO_CONNECT_LOGOUT:
					Log.d(TAG, "onCreate() - LogOut operation requested");
					logOut(getApplicationContext());
					goBackWithResults(true);
					finish();
					break;
					
				case SAPO_CONNECT_LOGIN:
					Log.d(TAG, "onCreate() - LogIn operation requested");
					
				default:
					if (windowTitleBarControlInterface != null && windowTitleBarControlInterface.getWindowFeatures() != null) {
						for (int windowFeature: windowTitleBarControlInterface.getWindowFeatures()) {
							requestWindowFeature(windowFeature);	
						}
					}
					
					setContentView(R.layout.connect);
					
					// Initializes the loading icon.
					if (windowTitleBarControlInterface != null) {
						windowTitleBarControlInterface.setUpWindowTitleBar();	
					}
					
					// Configures the WebView
					
					ViewGroup webviewContainer = (ViewGroup) findViewById(R.id.webviewContainer);
					
					// Do not pass 'this' to the WebView constructor, otherwise it will leak the Activity.
					// In 1.5, we cannot pass getApplicationContext() to WebViews with forms due to system dialogs. SUCKS!
					if (VersionCodes.BELOW_CUPCAKE_INCLUDED) {
						webView = new WebView(this);	
					} else {
						webView = new WebView(getApplicationContext());	
					}
					
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
					webviewContainer.addView(webView, layoutParams);
					
					webView.setBackgroundColor(Color.WHITE);
					webView.setVerticalFadingEdgeEnabled(true);
					webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
					webView.requestFocus(View.FOCUS_DOWN);
					
					handler = new Handler();
					
					webView.addJavascriptInterface(new Object(){
			            // This is not called on the UI thread. Post a runnable to invoke loadUrl on the UI thread.
						@SuppressWarnings("unused")
						public void tryAgain() {
			                handler.post(new Runnable() {
			                	@Override
			                    public void run() {
			                    	Log.d(TAG, "tryAgain() button clicked");
			                    	webView.setBackgroundColor(Color.WHITE);
			                    	webView.requestFocus(View.FOCUS_DOWN);
			                    	if (windowTitleBarControlInterface != null) {
										windowTitleBarControlInterface.startRefreshAnimation();	
									}
			                    	authenticate();
			                    }
			                });
			            }
			        }, "error");
					
					webView.setWebViewClient(new WebViewClient() {
						
						@Override
						public void onPageStarted(WebView view, String url, Bitmap favicon) {
							Log.d(TAG, "onPageStarted() - Start URL=" + url);
							
							if (url.startsWith(sapoAuthorizationDenied)) {
								// It's the callback URL from a user authorization denied.
								Log.d(TAG, "onPageStarted() - Detected control string in URL: " + sapoAuthorizationDenied);
								loadEmptyPage();
								showDialogOneButton(getString(R.string.sapo_connect_denied));
								
							} else if (url.startsWith(callbackUrl)) {
								
								if (VersionCodes.SDK_LEVEL < VersionCodes.GINGERBREAD) {
									// It's the callback URL from SAPO Connect.
									Log.e(TAG, "onPageStarted() - SDK_LEVEL < GINGERBREAD. Capture callback.");
									captureCallback(url);
									
								} else {
									// It's the callback URL from SAPO Connect. This should be handled in shouldOverrideUrlLoading() because the
									// the URL does not exist and the WebView will try to load it two times... even after exiting this Activity.
									// But, below Gingerbread, this doesn't happen, and the shouldOverrideUrlLoading() doesn't deal with redirects.
									Log.e(TAG, "onPageStarted() - Ups... we should't be here. Detected control string in URL: " + callbackUrl);	
								}
								
							} else if (url.startsWith("http://pesquisa.sapo.pt")) {
								// Do not load page.
								loadEmptyPage();
								
							} else {
								super.onPageStarted(view, url, favicon);
								if (windowTitleBarControlInterface != null) {
									windowTitleBarControlInterface.startRefreshAnimation();	
								}
							}
						}
						
						@Override
						public void onPageFinished(WebView view, String url) {
							super.onPageFinished(view, url);
							if (windowTitleBarControlInterface != null) {
								windowTitleBarControlInterface.stopRefreshAnimation();	
							}
						}
						
						@Override
						public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
							Log.d(TAG, "onReceivedError() - failingUrl=" + failingUrl + "; errorCode=" + errorCode + "; description=" + description);
							loadErrorPage(description);
							super.onReceivedError(view, errorCode, description, failingUrl);
						}		
						
						@Override
					    public boolean shouldOverrideUrlLoading(WebView view, String url){
							Log.d(TAG, "shouldOverrideUrlLoading() - URL=" + url);
							
							if (url.startsWith(callbackUrl)) {
								
								if (VersionCodes.SDK_LEVEL >= VersionCodes.GINGERBREAD) {
									Log.d(TAG, "shouldOverrideUrlLoading() - SDK_LEVEL >= GINGERBREAD. Capture callback.");
									// It's the callback URL from SAPO Connect. Below Gingerbread, we can never catch this redirect.
									captureCallback(url);
									
									view.stopLoading();
									return false;
								}
								
							} else {
								Log.d(TAG, "shouldOverrideUrlLoading() - SDK_LEVEL < GINGERBREAD. Do not capture callback.");
								if (!url.contains("pesquisa.sapo.pt")) {
									view.loadUrl(url);
								}
							}
							
							// Avoid redirects to the browser. We don't want to leave the WebView.
						    return true;
					   }
					});
					
					WebSettings webSettings = webView.getSettings();
					webSettings.setJavaScriptEnabled(true);
					webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
					webSettings.setSavePassword(false);
					
					// Set the UserAgent for the SAPO ID. Must be the default UserAgent with the parameter '; SapoAppWebView'
					webSettings.setUserAgentString(webSettings.getUserAgentString() + SAPO_ID_LOGIN_USER_AGENT_PARAM);
					
					// Start the authentication process.
					authenticate();
					
					break;
				}
	    }
	    
	}
	
	/**
	 * Retrieve the information from the Callback URL. This is invoked from the shouldOverrideUrlLoading() or from the
	 * onPageStarted() in the WebViewClient, depending on the SDK level we are in.
	 * 
	 * @param url The Callback URL.
	 */
	private void captureCallback(String url) {
		Log.d(TAG, "captureCallback() - Detected control string in URL: " + callbackUrl);
		
		if (windowTitleBarControlInterface != null) {
			windowTitleBarControlInterface.startRefreshAnimation();	
		}
		
		Uri uri = Uri.parse(url);
		
		String otoken = uri.getQueryParameter(OAuth.OAUTH_TOKEN);		// request_token
		String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);  // request_secret
		
		Log.d(TAG, "captureCallback() - Verifier=" + verifier + "; otoken=" + otoken);
		
		if (oAuthClient == null) {
			Log.d(TAG, "captureCallback() - OAuthClient is NULL. Creating a new one.");
			oAuthClient = new OAuthClient(new HttpClient4(new SsoHttpClient()));
		}
		
		if (oAuthAccessor == null) {
			Log.d(TAG, "captureCallback() - OAuthAccessor is NULL. Creating a new one.");
			oAuthAccessor = new OAuthAccessor(new OAuthConsumer(callbackUri.toString(), sapoConsumerKey, sapoConsumerSecret, new OAuthServiceProvider(sapoRequestTokenUrl, sapoAuthorizeUrl, sapoAccessTokenUrl)));
			oAuthAccessor.requestToken = SharedPreferencesOperations.getInstance(getApplicationContext(), SHARED_PREFS_FILE).retrieveStringValue(REQUEST_TOKEN, null);
			oAuthAccessor.tokenSecret = SharedPreferencesOperations.getInstance(getApplicationContext(), SHARED_PREFS_FILE).retrieveStringValue(REQUEST_SECRET, null); 
		}
		
		try {
			oAuthClient.getAccessToken(oAuthAccessor, null, OAuth.newList(OAuth.OAUTH_VERIFIER, verifier));
			
			Log.d(TAG, String.format("captureCallback() - oAuthAccessor.accessToken=%s, oAuthAccessor.tokenSecret=%s, oAuthAccessor.requestToken=%s", oAuthAccessor.accessToken, oAuthAccessor.tokenSecret, oAuthAccessor.requestToken));
			
			saveAuthInformation(getApplicationContext(), oAuthAccessor.accessToken, oAuthAccessor.tokenSecret);
			
			// We have successfully finished with success the OAuth process. From now on, we don't need a WebView anymore.
			loadEmptyPage();
			webView.setVisibility(View.GONE);
			
			// Execute the additional operations or end the process.
			if (logInInterface != null) {
				logInInterface.logIn(getApplicationContext());	
			} else {
				setUserRegistered(getApplicationContext(), true);
				goBackWithResults(true);
			}
			
		} catch (IOException e) {
			Log.e(TAG, "captureCallback() - IOException.", e);
			loadErrorPage(e.getMessage());
		} catch (OAuthException e) {
			Log.e(TAG, "captureCallback() - OAuthException.", e);
			loadErrorPage(e.getMessage());
		} catch (URISyntaxException e) {
			Log.e(TAG, "captureCallback() - URISyntaxException.", e);
			loadErrorPage(e.getMessage());
		}
		
		oAuthAccessor = null;
		oAuthClient = null;
	}
	
	/**
	 * Do the actual authentication stuff. This operation can be retried in this Activity whenever necessary, like for
	 * example, when there is an error and we need to retry the operation. 
	 */
	private void authenticate() {
		if (windowTitleBarControlInterface != null) {
			windowTitleBarControlInterface.startRefreshAnimation();	
		}
		
		final Runnable runnable = new Runnable() {
            public void run() {
            	try {
        			OAuthServiceProvider oAuthServiceProvider = new OAuthServiceProvider(sapoRequestTokenUrl, sapoAuthorizeUrl, sapoAccessTokenUrl);
        			OAuthConsumer oAuthConsumer = new OAuthConsumer(callbackUri.toString(), sapoConsumerKey, sapoConsumerSecret, oAuthServiceProvider);
        			oAuthAccessor = new OAuthAccessor(oAuthConsumer);
        			
        			HttpClient4 httpClient4 = new HttpClient4(new SsoHttpClient());
        			oAuthClient = new OAuthClient(httpClient4);
        			oAuthClient.getRequestToken(oAuthAccessor, null, OAuth.newList(OAuth.OAUTH_CALLBACK, callbackUri.toString()));

        			saveRequestInformation(getApplicationContext(), oAuthAccessor.requestToken, oAuthAccessor.tokenSecret);
        			
        			String aUrl = oAuthAccessor.consumer.serviceProvider.userAuthorizationURL +
                        "?oauth_token=" + oAuthAccessor.requestToken +
                        "&oauth_callback=" + URLEncoder.encode(oAuthAccessor.consumer.callbackURL);
        			
        			// Initializes the NTP Client to get the Delta and Server time from the NTP Public Server.
    				if (!checkNtpTime()) {
    					loadEmptyPage();
    					showDialogOneButton(getString(R.string.sapo_connect_time_offset));
    				} else {
    					Log.d(TAG, "authenticate() - aUrl=" + aUrl);
            			webView.loadUrl(aUrl);	
    				}
        			
        		} catch (UnknownHostException e) {
        			Log.e(TAG, "authenticate() - UnknownHostException.", e);
        			loadErrorPage(getString(R.string.sapo_connect_error_connection_mandatory));
        		} catch (IOException e) {
        			Log.e(TAG, "authenticate() - IOException.", e);
        			loadErrorPage(e.getMessage());
        		} catch (OAuthException e) {
        			Log.e(TAG, "authenticate() - OAuthException.", e);
        			loadErrorPage(e.getMessage());
        		} catch (URISyntaxException e) {
        			Log.e(TAG, "authenticate() - URISyntaxException.", e);
        			loadErrorPage(e.getMessage());	
        		} catch (Exception e) {
        			Log.e(TAG, "authenticate() - Exception.", e);
        			loadErrorPage(e.getMessage());
        		}
            }
        };
        
        performOnBackgroundThread(runnable);
	}
	
    
    /**
     * Executes the operations on a separate thread.
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
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume() - Start");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause() - Start");
    	// Stop any possible Refresh Animation.
		if (isFinishing() && windowTitleBarControlInterface != null) {
			windowTitleBarControlInterface.stopRefreshAnimation();	
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy() - Start");
		// Dumb, but crappy WebView OS implementation pre 2.2 requires it.
		if (webView != null) {
			webView.destroy();	
		}
		super.onDestroy();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (!VersionCodes.SUPPORTS_ECLAIR && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of the platform where it doesn't exist.
	    	clearWebView();
        }
        return super.onKeyDown(keyCode, event);
    }
	
	public void onBackPressed() {
		clearWebView();
		finish();
	}
	
	/**
	 * This will show an error HTML page to the user. The background is set to transparent in order to use the Activity theme
	 * background. Make sure to set to white after this (if it's required).
	 * 
	 * @param errorMessage The error message to display to the user.
	 */
	private void loadErrorPage(String errorMessage) {
		try {
			webView.stopLoading();
			String errorTemplate = Utils.getAssetAsString(getApplicationContext(), "error.html");
			errorTemplate = errorTemplate.replace("%%ERROR%%", errorMessage);
			errorTemplate = errorTemplate.replace("%%ERROR_BUTTON%%", getString(R.string.sapo_connect_error_page_button));
			errorTemplate = errorTemplate.replace("%%ERROR_TEXT%%", getString(R.string.sapo_connect_error_page_text));
			webView.setBackgroundColor(0);
			webView.loadDataWithBaseURL("", errorTemplate, "text/html", "utf-8", "");
		} catch (IOException e) {
			Log.e(TAG, "loadErrorPage() - Unable to show error HTML page.");
		}
	}
	
	/**
	 * This will show an empty HTML page.
	 */
	private void loadEmptyPage() {
		try {
			webView.stopLoading();
			String emptyTemplate = Utils.getAssetAsString(getApplicationContext(), "empty.html");
			webView.setBackgroundColor(0);
			webView.loadDataWithBaseURL("", emptyTemplate, "text/html", "utf-8", "");
		} catch (IOException e) {
			Log.e(TAG, "loadEmptyPage() - Unable to show empty HTML page.");
		}
	}
	
	
	// ********************************************************************************************************************************** //
    //                                                    GENERAL PUBLIC AUX METHODS                                                      //
    // ********************************************************************************************************************************** //

	/**
	 * Determines if the device is within the acceptable time frame for a valid OAuth connection.
	 * 
	 * @return True if the device is within the acceptable time frame. False otherwise.
	 */
	public static boolean checkNtpTime() {
		// Initializes the NTP Client to get the Delta and Server time from the NTP Public Server.
		NTPClient ntpClient = new NTPClient();
		
		// If the Delta time is null, we cannot proceed.
		if (ntpClient.isStatusOk()) {
			Log.d(TAG, "checkNtpTime() - NTPClient was successfully started.");
			if (ntpClient.isTimeWithinAcceptableOffset()) {
				Log.d(TAG, "checkNtpTime() - Device time is within acceptable window.");
				return true;
			} else {
				Log.w(TAG, "checkNtpTime() - Device time is not within the acceptable window.");
				return false;
			}
		} else {
			Log.w(TAG, "checkNtpTime() - Unable to start NTPClient. Let the authentication proceed anyway.");
			return true;
		}
	}
		
	/**
	 * Retrieves the stored SSO Token, If there is none available, return NULL.
	 * 
	 * @param context The caller Context.
	 * @return The SSO Token or NULL if there is none available.
	 */
	public static String getSsoToken(Context context) {
		final String EMPTY_VALUE = "Empty";
		String token = SharedPreferencesOperations.getInstance(context.getApplicationContext(), SHARED_PREFS_FILE).retrieveStringValue(USER_SECRET, EMPTY_VALUE);
		return token.equals(EMPTY_VALUE) ? null : token;
	}
	
	/**
	 * Determines if the user is logged in by determining the presence of the SSO Token and the registration indicator stored
	 * in the SSO Shared Preferences file.
	 * 
	 * @param context The caller Context.
	 * @return Returns true if the user is logged in or false otherwise.
	 */
	public static boolean isUserLoggedIn(Context context) {
		return (getSsoToken(context.getApplicationContext()) != null && isUserRegistered(context.getApplicationContext()));
	}
	
	/**
	 * Logs out the user by removing all tokens and secrets from the SSO Shared Preferences file.
	 * Performs additional operations described by the LogOutInterface implementation.
	 * 
	 * @param context The caller Context.
	 */
	private void logOut(Context context) {
		Log.d(TAG, "logOut() - User loging out.");
		
		simpleLogOut(context);
		
		// If the implementing class passed some additional operations, execute them.
		if (logOutInterface != null) {
			logOutInterface.logOut(context);	
		}
	}
	
	/**
	 * Logs out the user by removing all tokens and secrets from the SSO Shared Preferences file.
	 * This method can be called in a static invocation by the caller. The additional operations
	 * described in the LogOutInterface will be ignored.
	 * 
	 * @param context The caller Context.
	 */
	public static void simpleLogOut(Context context) {
		Log.d(TAG, "simpleLogOut() - User loging out.");
		setUserRegistered(context, false);
		SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(REQUEST_TOKEN, false);
		SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(REQUEST_SECRET, false);
		SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(USER_TOKEN, false);
		SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(USER_SECRET, false);
	}
	
	/**
	 * Helper handler for the OnClickListener of the Dialog.
	 */
	private View.OnClickListener clickListenerPositiveButtonFinal;
	
	/**
	 * Build and show a custom Alert Dialog. The negative button is set to exit the Activity.
	 * The positive button can be set with any action described in the clickListenerPositiveButton parameter.
	 * 
	 * @param message The message to display to the user.
	 * @param clickListenerPositiveButton The DialogInterface.OnClickListener for the positive button.
	 */
	protected void showDialog(String message, View.OnClickListener clickListenerPositiveButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		if (customNotificationsInterface != null && customNotificationsInterface.getCustomAlertDialogLayout() != null) {
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(customNotificationsInterface.getCustomAlertDialogLayout(), (ViewGroup) findViewById(R.id.customDialog_layoutRoot));
			
			((TextView) layout.findViewById(R.id.customDialog_text)).setText(message);
			
			Button okBtn = (Button) layout.findViewById(R.id.customDialog_okBtn);
			okBtn.setText(R.string.sapo_connect_error_button_try_again);
			okBtn.setOnClickListener(clickListenerPositiveButton);
			
			Button nokBtn = (Button) layout.findViewById(R.id.customDialog_nokBtn);
			nokBtn.setText(R.string.sapo_connect_error_button_cancel);
			nokBtn.setOnClickListener(new View.OnClickListener() {
		    	@Override
		    	public void onClick(View v) {
		    		SAPOConnect.this.logOut(getApplicationContext());
		    		SAPOConnect.this.alertDialog.dismiss();
		    		goBackWithResults(false);
		    	}
		    });
			
			builder.setView(layout);
			builder.setCancelable(false);
			
		} else {
			clickListenerPositiveButtonFinal = clickListenerPositiveButton;
			builder
				.setMessage(message)
				.setCancelable(false)
			    .setPositiveButton(R.string.sapo_connect_error_button_try_again, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clickListenerPositiveButtonFinal.onClick(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));
					}
			    	
			    })
			    .setNegativeButton(R.string.sapo_connect_error_button_cancel, new DialogInterface.OnClickListener() {
			    	@Override
			    	public void onClick(DialogInterface dialog, int id) {
			    		SAPOConnect.this.logOut(getApplicationContext());
			    		SAPOConnect.this.alertDialog.dismiss();
			    		goBackWithResults(false);
			    	}
			    });
			alertDialog = builder.create();
		}
		
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/**
	 * If true, the showDialogOneButton will not perform it's actions on the positive button.
	 */
	private boolean dialogWithoutAction;

	/**
	 * Build and show a custom Alert Dialog with only one button that will exit the Activity with the RESULT_OK result.
	 * 
	 * @param message The message to display to the user.
	 */
	protected void showDialogOneButton(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		if (customNotificationsInterface != null && customNotificationsInterface.getCustomAlertDialogLayoutOneButton() != null) {
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(customNotificationsInterface.getCustomAlertDialogLayoutOneButton(), (ViewGroup) findViewById(R.id.customDialog_layoutRoot));
			
			((TextView) layout.findViewById(R.id.customDialog_text)).setText(message);
			
			Button nokBtn = (Button) layout.findViewById(R.id.customDialog_okBtn);
			nokBtn.setText(R.string.sapo_connect_dialog_button_ok);
			nokBtn.setOnClickListener(new View.OnClickListener() {
		    	@Override
		    	public void onClick(View v) {
		    		SAPOConnect.this.alertDialog.dismiss();
		    		if (!dialogWithoutAction) {
		    			goBackWithResults(true);
		    		}
		    		dialogWithoutAction = false;
		    	}
		    });
			
			builder.setView(layout);
			builder.setCancelable(false);
			
		} else {
			builder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.sapo_connect_dialog_button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						SAPOConnect.this.alertDialog.dismiss();
						if (!dialogWithoutAction) {
							goBackWithResults(true);	
						}
						dialogWithoutAction = false;
					}
				});
		}
		
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/**
	 * Build and show a custom Alert Dialog with only one button that will exit the Activity with the RESULT_OK result.
	 * 
	 * @param message The message to display to the user.
	 */
	protected void showDialogOneButtonWithCustomAction(String message, View.OnClickListener positiveButtonClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		if (customNotificationsInterface != null && customNotificationsInterface.getCustomAlertDialogLayoutOneButton() != null) {
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(customNotificationsInterface.getCustomAlertDialogLayoutOneButton(), (ViewGroup) findViewById(R.id.customDialog_layoutRoot));
			
			((TextView) layout.findViewById(R.id.customDialog_text)).setText(message);
			
			Button nokBtn = (Button) layout.findViewById(R.id.customDialog_okBtn);
			nokBtn.setText(R.string.sapo_connect_dialog_button_ok);
			nokBtn.setOnClickListener(positiveButtonClickListener);
			
			builder.setView(layout);
			builder.setCancelable(false);
			
		} else {
			clickListenerPositiveButtonFinal = positiveButtonClickListener;
			builder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.sapo_connect_dialog_button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						clickListenerPositiveButtonFinal.onClick(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));
					}
				});
		}
		
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/**
	 * Build and show a custom Alert Dialog with only one button that will only display a message without any action.
	 * 
	 * @param message The message to display to the user.
	 */
	protected void showDialogOneButtonWithoutAction(String message) {
		dialogWithoutAction = true;
		showDialogOneButton(message);
	}
	
	/**
	 * The Activity operations are now completed. Return with the result code OK or CANCELED.
	 * 
	 * @param success If true, the operation was successful. If false, the operation was not completed.
	 */
	protected void goBackWithResults(boolean success) {
		Intent goBackWithResults = getIntent();
		
		if (success) {
			Log.d(TAG, "goBackWithResults() - User is Logged in and registered. Exiting SAPO Connect.");
			setResult(RESULT_OK, goBackWithResults);
		} else {
			Log.d(TAG, "goBackWithResults() - User is not Logged-in or registered. Exiting SAPO Connect.");
			buildCustomToast(getString(R.string.sapo_connect_registration_incomplete), Toast.LENGTH_LONG);
			logOut(getApplicationContext());
			setResult(RESULT_CANCELED, goBackWithResults);
		}
		
		clearWebView();
		finish();
	}
		
	/**
	 * Clears the WebView cache(redundant) and cookies (strictly necessary) so that in the same session we can logout and login with different users.
	 */
	private void clearWebView() {
		Log.d(TAG, "clearWebView() - Clearing the WebView cache and cookies.");
		if (webView != null) {
			webView.clearCache(true);
			webView.clearFormData();
			webView.clearHistory();
			CookieSyncManager.createInstance(getApplicationContext());
			CookieManager.getInstance().removeAllCookie();
		}
	}
	
	/**
	 * Builds a custom Toast with the application toast layout.
	 * 
	 * @param message The main message of the Toast.
	 * @param toastLength The toast length. E.g.: Toast.LENGTH_LONG.
	 */
	protected void buildCustomToast(String message, int toastLength) {
		if (customNotificationsInterface != null && customNotificationsInterface.getCustomToastLayout() != null) {
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(customNotificationsInterface.getCustomToastLayout(), (ViewGroup) findViewById(R.id.toast_layout_root));
			TextView text = (TextView) layout.findViewById(R.id.toast_text);
			text.setText(message);	
			Toast toast = new Toast(getApplicationContext());
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(toastLength);
			toast.setView(layout);
			toast.show();			
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), message, toastLength);
			toast.show();
		}
	}
	
	/**
	 * Used internally to determine if the user is registered on the server.
	 * 
	 * @param context The caller Context.
	 * @return Returns true if the user is registered on the server.
	 */
	private static boolean isUserRegistered(Context context) {
		return SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).retrieveBooleanValue(USER_REGISTERED, false);
	}
	
	/**
	 * Stores in the SharedPreferences if the user is registered in the server.
	 * 
	 * @param context The caller Context.
	 * @param register If true, it will set the user registered condition. False to unregister the user.
	 */
	protected static void setUserRegistered(Context context, boolean register) {
		SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).storeValue(USER_REGISTERED, register, false);
	}
	
	
	// ********************************************************************************************************************************** //
    //                                                     WEB SERVICES AUX METHODS                                                       //
    // ********************************************************************************************************************************** //
    
	/**
	 * Invokes a WebService with GET and with an OAuth signature in the URL parameters. At this date, the SAPO Bus doesn't support OAuth
	 * signatures in the headers. The response is then retrieved as a String and passed on to the caller.
	 * 
	 * @param context The caller Context.
	 * @param url The complete URL to the WebService.
	 * @return The response as a String.
	 * @throws IOException Some connection error occurred.
	 * @throws OAuthException Some OAuth exception. Does not imply that the user auth is invalid.
	 * @throws URISyntaxException Some error with the provided URL.
	 * @throws SapoException The user auth data stored in the session is invalid.
	 */
	public static String invokeWebServiceGet(Context context, String url) throws IOException, OAuthException, URISyntaxException, SapoException {
		// Get the configured HTTL Client
		OAuthClient oAuthClient = getOAuthClient();
		
		// Get the OAuthAccessor
		OAuthAccessor oAuthAccessor = getOAuthAccessor(context, context.getString(R.string.sapo_connect_consumer_key), context.getString(R.string.sapo_connect_consumer_secret));
		
		if (oAuthAccessor == null) {
			// Login is invalid
			Log.i(TAG, "invokeWebService() - Impossible to retrieve OAuth credentials stored in SharedPreferences.");
			throw new SapoException();
		}
		
		// Invoke the Service
		OAuthMessage oAuthMessage = oAuthClient.invoke(oAuthAccessor, "GET", url, null);
		
		Log.d(TAG, "invokeWebService() - OAuthMessage URL: " + oAuthMessage.URL);
		
		// Return the response as a String
		return oAuthMessage.readBodyAsString();
	}
	
	/**
	 * Invokes a WebService with POST and with an OAuth signature in the URL parameters. At this date, the SAPO Bus doesn't support OAuth
	 * signatures in the headers. The response is then retrieved as a String and passed on to the caller.
	 * 
	 * @param context The caller Context.
	 * @param url The complete URL to the WebService.
	 * @param body The POST body.
	 * @return The response as a String.
	 * @throws IOException Some connection error occurred.
	 * @throws OAuthException Some OAuth exception. Does not imply that the user auth is invalid.
	 * @throws URISyntaxException Some error with the provided URL.
	 * @throws SapoException The user auth data stored in the session is invalid.
	 */
	public static String invokeWebServicePatch(Context context, String url, String body) throws IOException, OAuthException, URISyntaxException, SapoException {
		return invokeWebServiceWithMethod("PATCH", context, url, body);
	}
	
	/**
	 * Invokes a WebService with PATCH and with an OAuth signature in the URL parameters. At this date, the SAPO Bus doesn't support OAuth
	 * signatures in the headers. The response is then retrieved as a String and passed on to the caller.
	 * 
	 * @param context The caller Context.
	 * @param url The complete URL to the WebService.
	 * @param body The POST body.
	 * @return The response as a String.
	 * @throws IOException Some connection error occurred.
	 * @throws OAuthException Some OAuth exception. Does not imply that the user auth is invalid.
	 * @throws URISyntaxException Some error with the provided URL.
	 * @throws SapoException The user auth data stored in the session is invalid.
	 */
	public static String invokeWebServicePost(Context context, String url, String body) throws IOException, OAuthException, URISyntaxException, SapoException {
		return invokeWebServiceWithMethod("POST", context, url, body);
	}
	
	/**
	 * Invokes a WebService with a defined HTTP Method and with an OAuth signature in the URL parameters. At this date, the SAPO Bus doesn't
	 * support OAuth signatures in the headers. The response is then retrieved as a String and passed on to the caller.
	 * 
	 * @param httpMethod The HTTP Method for the WebService invocation.
	 * @param context The caller Context.
	 * @param url The complete URL to the WebService.
	 * @param body The POST body.
	 * @return The response as a String.
	 * @throws IOException Some connection error occurred.
	 * @throws OAuthException Some OAuth exception. Does not imply that the user auth is invalid.
	 * @throws URISyntaxException Some error with the provided URL.
	 * @throws SapoException The user auth data stored in the session is invalid.
	 */
	private static String invokeWebServiceWithMethod(String httpMethod, Context context, String url, String body) throws IOException, OAuthException, URISyntaxException, SapoException {
		// Get the configured HTTL Client
		OAuthClient oAuthClient = getOAuthClient();
		
		// Get the OAuthAccessor
		OAuthAccessor oAuthAccessor = getOAuthAccessor(context, context.getString(R.string.sapo_connect_consumer_key), context.getString(R.string.sapo_connect_consumer_secret));
		
		if (oAuthAccessor == null) {
			// Login is invalid
			Log.i(TAG, "invokeWebServicePost() - Impossible to retrieve OAuth credentials stored in SharedPreferences.");
			throw new SapoException();
		}
		
		// Invoke the Service
		ByteArrayInputStream bodyInputStream = null;
		int bodyLength = 0;
		
		if (!TextUtils.isEmpty(body)) {
			bodyInputStream = new ByteArrayInputStream(body.getBytes());
			bodyLength = body.length();	
		}
		
		OAuthMessage oAuthMessage = invoke(oAuthClient, oAuthAccessor, httpMethod, url, bodyInputStream, bodyLength);
		
		Log.d(TAG, "invokeWebServicePost() - OAuthMessage URL: " + oAuthMessage.URL);
		
		// Return the response as a String
		return oAuthMessage.readBodyAsString();
	}
	
    /**
     * This is a copy of the OAuthClient invoke method, with the modification of the OAuthMessage to include a body as a string.
     */
	private static OAuthMessage invoke(OAuthClient oAuthClient, OAuthAccessor accessor, String httpMethod, String url, InputStream body, int bodyLenght) throws IOException, OAuthException, URISyntaxException {
    	String ps = (String) accessor.consumer.getProperty(OAuthClient.PARAMETER_STYLE);
        ParameterStyle style = (ps == null) ? ParameterStyle.BODY : Enum.valueOf(ParameterStyle.class, ps);
        
        OAuthMessage request = accessor.newRequestMessage(httpMethod, url, null, body);
        
        request.getHeaders().add(new OAuth.Parameter(HttpMessage.CONTENT_LENGTH, bodyLenght + ""));
        
		@SuppressWarnings("deprecation")
		Object accepted = accessor.consumer.getProperty(OAuthClient.ACCEPT_ENCODING);
        if (accepted != null) {
            request.getHeaders().add(new OAuth.Parameter(HttpMessage.ACCEPT_ENCODING, accepted.toString()));
        }
        return oAuthClient.invoke(request, style);
    }
    
	/**
	 * Creates an OAuthAccessor with the application data and stored User Token and User Secret.  If NULL is returned, the authentication
	 * is invalid and it's up to the caller to determine what to do. Usually, it will require a logout() and a new login.
	 *  
	 * @param context The caller context.
	 * @param sapoConsumerKey The Consumer Key obtained from the SAPO Connect registration site.
	 * @param sapoConsumerSecret The Consumer Secret obtained from the SAPO Connect registration site.
	 * @return The OAuthAccessor to invoke the protected resource.
	 */
	private static OAuthAccessor getOAuthAccessor(Context context, String sapoConsumerKey, String sapoConsumerSecret) {
		callbackUri = Uri.parse(context.getString(R.string.sapo_connect_callback_url));
		
		OAuthServiceProvider oAuthServiceProvider = new OAuthServiceProvider(sapoRequestTokenUrl, sapoAuthorizeUrl, sapoAccessTokenUrl);
	    OAuthConsumer oAuthConsumer = new OAuthConsumer(callbackUri.toString(), sapoConsumerKey, sapoConsumerSecret, oAuthServiceProvider);
	    OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
	    
	    String accessToken = getAccessToken(context);
	    String tokenSecret = getAccessSecret(context);
	    
	    if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(tokenSecret)) {
	    	Log.e(TAG, "getOAuthAccessor() - Invalid information stored on the device: accessToken=" + accessToken + "; tokenSecret=" + tokenSecret);
	    	return null;
	    }
	    
	    oAuthAccessor.accessToken = getAccessToken(context);
	    oAuthAccessor.tokenSecret = getAccessSecret(context);
	    
	    return oAuthAccessor;
	}
	
	/**
	 * Creates a new OAuthClient to invoke the protected URL.
	 * 
	 * @return The OAuthClient properly configured with the application configuration.
	 */
	private static OAuthClient getOAuthClient() {
		return new OAuthClient(new HttpClient4(new SsoHttpClient()));
	}
	
	
	// ********************************************************************************************************************************** //
    //                                                   SHARED PREFERENCES OPERATIONS                                                    //
    // ********************************************************************************************************************************** //
    	
	/**
	 * Stores in the SharedPreferences the OAuthAccessor 'requestToken' and 'tokenSecret' on the request.
	 * If the values are NULL, then the previous values will be removed.
	 * 
	 * @param context The application context.
	 * @param token The Token value to store in the SharedPreferences.
	 * @param secret The Secret value to store in the SharedPreferences.
	 */
	private void saveRequestInformation(Context context, String token, String secret) {
		if (token == null) {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(REQUEST_TOKEN, false);
			Log.d(TAG, "saveRequestInformation() - Clearing Request Token.");
		} else {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).storeValue(REQUEST_TOKEN, token, false);
			Log.d(TAG, "saveRequestInformation() - Saving Request Token: " + token);
		}
		
		if (secret == null) {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(REQUEST_SECRET, false);
			Log.d(TAG, "saveRequestInformation() - Clearing Request Secret.");
		} else {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).storeValue(REQUEST_SECRET, secret, false);
			Log.d(TAG, "saveRequestInformation() - Saving Request Secret: " + secret);
		}
	}
	
	/**
	 * Retrieved from the Shared Preferences the USER_TOKEN.
	 * 
	 * @param context The application context.
	 * @return The string containing the USER_TOKEN stored in the Shared Preferences.
	 */
	private static String getAccessToken(Context context) {
		final String NULL_VALUE = "NULL";
		String accessToken = SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).retrieveStringValue(USER_TOKEN, NULL_VALUE);
		if (accessToken.equals(NULL_VALUE)) {
			return null;
		} else {
			return accessToken;
		}
	}
	
	/**
	 * Retrieved from the Shared Preferences the USER_SECRET.
	 * 
	 * @param context The application context.
	 * @return The string containing the USER_SECRET stored in the Shared Preferences.
	 */
	private static String getAccessSecret(Context context) {
		final String NULL_VALUE = "NULL";
		String accessSecret = SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).retrieveStringValue(USER_SECRET, NULL_VALUE);
		if (accessSecret.equals(NULL_VALUE)) {
			return null;
		} else {
			return accessSecret;
		}
	}
	
	/**
	 * Stores in the SharedPreferences the OAuthAccessor 'requestToken' and 'tokenSecret' on the auth confirmation.
	 * If the values are NULL, then the previous values will be removed.
	 * 
	 * @param context The application context.
	 * @param token The Token value to store in the SharedPreferences.
	 * @param secret The Secret value to store in the SharedPreferences.
	 */
	private void saveAuthInformation(Context context, String token, String secret) {
		if (token == null) {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(USER_TOKEN, false);
			Log.d(TAG, "saveAuthInformation - Clearing OAuth Token.");
		} else {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).storeValue(USER_TOKEN, token, false);
			Log.d(TAG, "saveAuthInformation - Saving OAuth Token: " + token);
		}
		
		if (secret == null) {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).removeKey(USER_SECRET, false);
			Log.d(TAG, "saveAuthInformation - Clearing OAuth Secret");
		} else {
			SharedPreferencesOperations.getInstance(context, SHARED_PREFS_FILE).storeValue(USER_SECRET, secret, false);
			Log.d(TAG, "saveAuthInformation - Saving OAuth Secret: " + secret);
		}
	}
	
	
    // ********************************************************************************************************************************** //
    //                                                          SSO HTTP CLIENT                                                           //
    // ********************************************************************************************************************************** //
    
    /**
     * This will implement the HttpClientPool necessary to the OAuth HttpClient4. In order to use the same application configuration,
     * including the User Agent, we just return the MyHttpClient with our configuration in HttpClientConfiguration.
     * 
     * @author Rui Roque
     */
    private static class SsoHttpClient implements HttpClientPool {
    	
		public SsoHttpClient() {
			super();
		}

		@Override
		public HttpClient getHttpClient(URL url) {
			return MyHttpClient.getInstance(ConnectHttpClientConfiguration.getInstance()).getHttpClient(true);
		}
    }
    
}
