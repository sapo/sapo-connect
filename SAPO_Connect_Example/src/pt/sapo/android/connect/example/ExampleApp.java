package pt.sapo.android.connect.example;

import pt.sapo.mobile.android.connect.system.Log;
import android.app.Application;

public class ExampleApp extends Application {
		
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Logging configuration
		Log.configFromContext(getApplicationContext());
	}
	
}