package pt.sapo.android.connect.example;

import pt.sapo.mobile.android.connect.SAPOConnect;
import android.view.Window;

/**
 * This is the minimal configuration for the SAPOConnect extended class with the FEATURE_INDETERMINATE_PROGRESS feature
 * configured in the WindowTitleBarControlInterface. All other three configuration interfaces are returning NULL.
 * 
 * A simple usage of this class can be found in the LaunchActivity class.
 * 
 * @author Rui Roque
 */
public class ExampleSimpleSapoConnect extends SAPOConnect {
	
	@Override
	public LogInInterface getAditionalLogInOperations() {
		return null;
	}

	@Override
	public LogOutInterface getAditionalLogOutOperations() {
		return null;
	}

	@Override
	public WindowTitleBarControlInterface getWindowTitleBarControl() {
		return new WindowTitleBarControlInterface() {
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
	}

	@Override
	public CustomNotificationsInterface getCustomNotificationsLayouts() {
		return null;
	}
	
}
