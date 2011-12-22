package pt.sapo.android.connect.example;

import pt.sapo.android.connect.example.network.ImageGetListByUser;
import pt.sapo.mobile.android.connect.SAPOConnect;
import pt.sapo.mobile.android.connect.network.NetworkObject;
import pt.sapo.mobile.android.connect.network.NetworkOperations;
import pt.sapo.mobile.android.connect.network.OnNetworkResultsListener;
import pt.sapo.mobile.android.connect.system.sharedpreference.SharedPreferencesOperations;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is an example application for the usage of the SAPO Connect Android Library Project. This Activity will setup
 * it's layout according to the state of the user login, and if the user is logged in, it will invoke a WebService that
 * requires OAuth, with the stored credentials obtained from the SAPO Connect.
 * 
 * @author Rui Roque
 */
public class LaunchActivity extends Activity {
		
	/**
	 * Intent request codes.
	 */
	private static final int REQUEST_CODE_LOGIN = 0;
	private static final int REQUEST_CODE_LOGOUT = 1;
	
	/**
	 * Determines if the user is logged-in.
	 */
	private boolean userIsLoggedIn;
	
	/**
	 * Activity Views.
	 */
	private Button loginLogoutButton;
	private Button serviceButton;
	private TextView loginLogoutTextView;
	private TextView serviceResponseContentTextView;
	private TextView serviceResponseTitleTextView;
	
	/**
	 * Handler for the network callback.
	 */
	private Handler handler;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.main);

        // Create the Handler for the network callback.
        handler = new Handler();
        
        // Get all references of this Activity's Views.
        initializeActivityViews();
        
        // Determine if the user is Logged-in.
        userIsLoggedIn = ExampleSimpleSapoConnect.isUserLoggedIn(getApplicationContext());
        
        // Initialize the Activity Views according to the user's state.
        setLayout(userIsLoggedIn);
        
        // Set the behavior of the Login/Logout button.
        loginLogoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!userIsLoggedIn) {
		            // The user isn't LoggedIn yet. Go to SAPO Connect.
					showDialogConnect();
		        } else {
		            // The user is logged in. Do your stuff.
		        	Intent logOut = new Intent(LaunchActivity.this, isSapoConnectSimpleMode() ? ExampleSimpleSapoConnect.class : ExampleCustomSapoConnect.class);
		        	logOut.putExtra(SAPOConnect.SAPO_CONNECT_OPERATION, SAPOConnect.SAPO_CONNECT_LOGOUT);
		        	startActivityForResult(logOut,REQUEST_CODE_LOGOUT);
		        }
			}
		});

        // Set the behavior of the Service button.
        serviceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Just invoke the WS and wait for a response.
				invokeService();
			}
		});
    }
    
	@Override
	protected void onPause() {
		super.onPause();
    	// We are leaving, so cancel any Network threads callbacks to this Activity.
    	callback = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Restore or initialize the callback object to this Activity.
		if (callback == null) {
			callback = activityCallback;
		}
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            // User pressed BACK key. Do some stuff if you have to.
            
        } else {
            if (resultCode == RESULT_CANCELED) {
                switch (requestCode) {
                    case REQUEST_CODE_LOGIN:
                        // The Login was unsuccessful.
                        break;
                }
            } else {
                switch (requestCode) {
                    case REQUEST_CODE_LOGIN:
                        // Login was completed!
                    	setLayout(true);
                        break;
                    case REQUEST_CODE_LOGOUT:
                    	// Logout was completed!
                    	setLayout(false);
                    	break;
                }
            }
        }
    }
    
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                            AUX UI METHODS                                                           //
    // ----------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Get all references of this Activity's Views.
     */
    private void initializeActivityViews() {
        loginLogoutButton = (Button) findViewById(R.id.login_logout_btn);
        serviceButton = (Button) findViewById(R.id.service_btn);
        loginLogoutTextView = (TextView) findViewById(R.id.login_logout_text);
        serviceResponseContentTextView = (TextView) findViewById(R.id.service_response_content);
        serviceResponseTitleTextView = (TextView) findViewById(R.id.service_response_title);
    }
    
    /**
     * Initialize the Activity Views according to the userIsLoggedIn value.
     * 
     * @param userIsLoggedIn True if the user is logged-in.
     */
    private void setLayout(boolean userIsLoggedIn) {
    	this.userIsLoggedIn = userIsLoggedIn;
    	this.loginLogoutButton.setText(userIsLoggedIn ? R.string.logout_button : R.string.login_button);
    	this.loginLogoutTextView.setText(userIsLoggedIn ? R.string.login_text : R.string.logout_text);
    	this.serviceButton.setVisibility(userIsLoggedIn ? View.VISIBLE : View.GONE);
    	this.serviceResponseTitleTextView.setVisibility(View.INVISIBLE);
		this.serviceResponseContentTextView.setText("");
    }
    
    /**
     * Set's up a busy state in the Activity while waiting for network results.
     * 
     * @param busy True to set up a busy state. False to restore to the normal state.
     */
    private void busyState(boolean busy) {
    	setProgressBarIndeterminateVisibility(busy);
    	this.loginLogoutButton.setEnabled(!busy);
		this.serviceButton.setEnabled(!busy);
		this.serviceResponseTitleTextView.setVisibility(busy ? View.INVISIBLE : View.VISIBLE);
		this.serviceResponseContentTextView.setVisibility(busy ? View.INVISIBLE : View.VISIBLE);
    }
    
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                       WEB SERVICES AUX METHODS                                                      //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
    /**
     * Callback object implementation for the NetworkOperations.
     */
    private OnNetworkResultsListener activityCallback = new OnNetworkResultsListener() {
		@Override
		public void onNetworkResults(NetworkObject networkResponseObject) {
			onServiceResponse(networkResponseObject);
		}
	};
	
	/**
	 * Callback object for the NetworkOperations. This will receive the reference of activityCallback while the Activity is running.
	 */
	private OnNetworkResultsListener callback;
	
    /**
     * Invoke the WebService asynchronously. The result will be available in the onServiceResponse() method. The Activity will remain in the
     * busy state until we have an answer. This implementation can be any of the developer's choice.
     */
    private void invokeService() {
    	busyState(true);
    	NetworkOperations.invokeWebServiceFromRequestObject(handler, this, null, new ImageGetListByUser(), callback);
    }
    
    /**
     * Callback method from the NetworkOperations, defined in the callback object. This will deal with the results received from the WebService.
     * 
     * @param networkResponseObject The NetworkObject from the NetworkOperations containing the WebService invocation results.
     */
    private void onServiceResponse(NetworkObject networkResponseObject) {
    	serviceResponseContentTextView.setText(networkResponseObject.getSuccessResult());
    	busyState(false);
    }

	/**
	 * AlertDialog for choosing the SapoConnect implementation.
	 */
	private AlertDialog alertDialog;
	
	/**
	 * Shows a Dialog so the user can pick one of the two SapoConnect implementations.
	 */
	private void showDialogConnect() {
		final CharSequence[] items = { "Simple SAPO Connect", "Custom SAPO Connect" };
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title);
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	Intent goToLogin = null;
		    	if (item == 0) {
		    		goToLogin = new Intent(LaunchActivity.this, ExampleSimpleSapoConnect.class);
		    		setSapoConnectMode(true);
		    	} else {
		    		goToLogin = new Intent(LaunchActivity.this, ExampleCustomSapoConnect.class);
		    		setSapoConnectMode(false);
		    	}
		    	goToLogin.putExtra(SAPOConnect.SAPO_CONNECT_OPERATION, SAPOConnect.SAPO_CONNECT_LOGIN);
	            startActivityForResult(goToLogin, REQUEST_CODE_LOGIN);
	            alertDialog.dismiss();
		    }
		});
		alertDialog = builder.create();
		alertDialog.show();
	}
	
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                    SHARED PREFERENCES AUX METHODS                                                   //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
    /**
     * SharedPreferences file to store the values.
     */
    private static final String SHARED_PREFERENCES_FILE_MODE = "SapoConnectMode";
    
    /**
     * SharedPreferences key for storing the Mode value.
     */
    private static final String SHARED_PREFERENCES_KEY_MODE = "Mode";
    
    /**
     * Retrieves from the SharedPreferences if the user is logged in in SimpleMode (ExampleSimpleSapoConnect), so that the
     * LogOut operation can use the same class.
     * 
     * @return True, if the user is logged-in in simple mode. 
     */
    private boolean isSapoConnectSimpleMode() {
    	return SharedPreferencesOperations
    				.getInstance(getApplicationContext(), SHARED_PREFERENCES_FILE_MODE)
    				.retrieveBooleanValue(SHARED_PREFERENCES_KEY_MODE, true);
    }
    
    /**
     * Sets the SapoConnect mode in the SharedPrefenres according to the value of the simpleMode boolean parameter.
     * 
     * @param simple The value to store in the SharedPreferences.
     */
    private void setSapoConnectMode(boolean simpleMode) {
    	SharedPreferencesOperations
    		.getInstance(getApplicationContext(), SHARED_PREFERENCES_FILE_MODE)
    		.storeValue(SHARED_PREFERENCES_KEY_MODE, simpleMode, false);
    }
    
}