package pt.sapo.android.connect.example;

import pt.sapo.mobile.android.connect.SAPOConnect;
import pt.sapo.mobile.android.connect.system.Log;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/**
 * This is the maximum configuration for the SAPOConnect extended class with the FEATURE_INDETERMINATE_PROGRESS feature
 * configured in the WindowTitleBarControlInterface, additional login and logout information in the LogInInterface and
 * in the LogOutInterface, and finally, custom layouts for Toasts and Dialogs in the CustomNotificationsInterface. 
 * 
 * A simple usage of this class can be found in the LaunchActivity class.
 * 
 * @author Rui Roque
 */
public class ExampleCustomSapoConnect extends SAPOConnect {
	
	/**
	 * Log tag for this Activity.
	 */
	private static final String TAG = "ExampleCustomSapoConnect"; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// We can access the Intent data.
		if (getIntent().getExtras() != null && getIntent().getExtras().getInt(SAPO_CONNECT_OPERATION) == SAPO_CONNECT_LOGIN) {
			showDialogOneButtonWithoutAction(getString(R.string.custom_example_message));	
		}
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------------- //
    //                                                 CUSTOM INTERFACES IMPLEMENTATION                                                   //
	// ---------------------------------------------------------------------------------------------------------------------------------- //
    
	/**
	 * Implementation of the interface with custom layouts for Toasts and Dialogs.
	 */
	private WindowTitleBarControlInterface windowTitleBarControlInterface = new WindowTitleBarControlInterface() {
		@Override
		public int[] getWindowFeatures() {
			return new int[]{ Window.FEATURE_INDETERMINATE_PROGRESS };
		}
		
		@Override
		public void stopRefreshAnimation() {
			setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		public void startRefreshAnimation() {
			setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		public void setUpWindowTitleBar() {}
	};
	
	/**
	 * Implementation of the interface with the additional LogIn operations to be executed after the completions of the SSO web process.
	 */
	private LogInInterface logInInterface = new LogInInterface() {
		@Override
		public void logIn(Context context) {
			Log.d(TAG, "logInInterface.logIn() - This is just a dummy operation for the LogInInterface.");
	    	View.OnClickListener clickListenerPositiveButton = new View.OnClickListener() {
		    	@Override
		    	public void onClick(View v) {
					// When we implement this Interface, we must call setUserRegistered and goBackWithResults in order to complete the login.
		    		setUserRegistered(getApplicationContext(), true);
		    		goBackWithResults(true);
		    		alertDialog.dismiss();
		    	}
		    };
		    showDialogOneButtonWithCustomAction(getString(R.string.login_sucess), clickListenerPositiveButton);
		}
	};
	
	/**
	 * Implementation of the interface with the additional LogOut operations to be executed in the end of the logOut() method call.
	 */
	private LogOutInterface logOutInterface = new LogOutInterface() {
		@Override
		public void logOut(Context context) {
			Log.d(TAG, "logOutnterface.logOut() - This is just a dummy operation for the LogOutInterface.");
			buildCustomToast(getString(R.string.logout_sucess), Toast.LENGTH_SHORT);
		}
	};
	
	/**
	 * The interface implementation to get the custom layouts for the Toast and AlertDialog.
	 */
	private CustomNotificationsInterface customNotificationsInterface = new CustomNotificationsInterface() {
		@Override
		public Integer getCustomToastLayout() {
			return R.layout.custom_toast;
		}
		
		@Override
		public Integer getCustomAlertDialogLayout() {
			return R.layout.custom_dialog;
		}

		@Override
		public Integer getCustomAlertDialogLayoutOneButton() {
			return R.layout.custom_dialog_one_button;
		}
	};
	
	// ---------------------------------------------------------------------------------------------------------------------------------- //
    //                                                  ABSTRACT METHODS IMPLEMENTATION                                                   //
	// ---------------------------------------------------------------------------------------------------------------------------------- //
	
	@Override
	public LogInInterface getAditionalLogInOperations() {
		return logInInterface;
	}

	@Override
	public LogOutInterface getAditionalLogOutOperations() {
		return logOutInterface;
	}

	@Override
	public WindowTitleBarControlInterface getWindowTitleBarControl() {
		return windowTitleBarControlInterface;
	}
	
	@Override
	public CustomNotificationsInterface getCustomNotificationsLayouts() {
		return customNotificationsInterface;
	}
	
}
