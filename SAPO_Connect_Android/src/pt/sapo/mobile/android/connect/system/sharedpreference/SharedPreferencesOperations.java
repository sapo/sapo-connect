package pt.sapo.mobile.android.connect.system.sharedpreference;

import java.lang.ref.SoftReference;
import java.util.Set;

import pt.sapo.mobile.android.connect.system.VersionCodes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Abstract utility class for operations in the SharedPreferences. Each implementing class must implement methods
 * for retrieving and storing values according to the targeted platform.
 * 
 * @author Rui Roque
 */
public abstract class SharedPreferencesOperations {

	/**
	 * The application Context.
	 */
	protected Context context;
	
	/**
	 * The name of the SharedPreferences file. Can be NULL if using the application default SharedPreferences. 
	 */
	protected static String sharedPreferencesName;
	
	/**
	 * The SoftReference for an instance of this class to implement the Singleton pattern.
	 */
	protected static SoftReference<SharedPreferencesOperations> instanceRef;

	/**
	 * SharedPreferences referenced by 'sharedPreferencesName'.
	 */
	protected SharedPreferences sharedPreferences;
	
	/**
	 * An Editor to the SharedPreferences referenced by 'sharedPreferencesName'.
	 */
	protected SharedPreferences.Editor editor;
    
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                    CONSTRUCTOR AND INSTANCE GETTERS                                        //       
	// ---------------------------------------------------------------------------------------------------------- //
	
	public SharedPreferencesOperations(Context context, String mSharedPreferencesName) {
		sharedPreferencesName = mSharedPreferencesName;
		this.context = context.getApplicationContext();
		this.sharedPreferences = getSharedPreferences();
        this.editor = sharedPreferences.edit();
	}
	
	public synchronized static SharedPreferencesOperations getInstance(Context context, String sharedPreferencesName) {
		return VersionCodes.SUPPORTS_HONEYCOMB ? new SharedPreferencesOperationsHoneycomb(context, sharedPreferencesName) :
			(VersionCodes.SUPPORTS_GINGERBREAD ? new SharedPreferencesOperationsGingerbread(context, sharedPreferencesName) :
				VersionCodes.SUPPORTS_FROYO ? new SharedPreferencesOperationsFroyo(context, sharedPreferencesName) :
					new SharedPreferencesOperationsLegacy(context, sharedPreferencesName));
	}
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                          RETRIEVAL OPERATIONS                                              //       
	// ---------------------------------------------------------------------------------------------------------- //
    
    /**
     * Retrieves from the SharedPreferences the Long value corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The Long value stored in the SharedPreferences.
     */
    public abstract Long retrieveLongValue(String key, Long defaultValue);

    /**
     * Retrieves from the SharedPreferences the String value corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The String value stored in the SharedPreferences.
     */
    public abstract String retrieveStringValue(String key, String defaultValue);
    
    /**
     * Retrieves from the SharedPreferences the String Set corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The String Set stored in the SharedPreferences.
     */
    public abstract Set<String> retrieveStringSetValue(String key, Set<String> defaultValue);
    
    /**
     * Retrieves from the SharedPreferences the Boolean value corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The Boolean value stored in the SharedPreferences.
     */
    public abstract Boolean retrieveBooleanValue(String key, Boolean defaultValue);
    
    /**
     * Retrieves from the SharedPreferences the Float value corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The Float value stored in the SharedPreferences.
     */
    public abstract Float retrieveFloatValue(String key, Float defaultValue);
    
    /**
     * Retrieves from the SharedPreferences the Integer value corresponding to the parameter key.
     * 
     * @param key The string identifier for the pair value.
     * @return The Integer value stored in the SharedPreferences.
     */
    public abstract Integer retrieveIntegerValue(String key, Integer defaultValue);
    
    // ---------------------------------------------------------------------------------------------------------- //
	//                                            STORING METHODS                                                 //       
	// ---------------------------------------------------------------------------------------------------------- //
    
    /**
     * Stores a long value with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The long value to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, long value, boolean backup);
    
    /**
     * Stores a String value with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The String value to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, String value, boolean backup);

    /**
     * Stores a String Set with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The String Set to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, Set<String> value, boolean backup);
    
    /**
     * Stores a boolean value with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The boolean value to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, boolean value, boolean backup);

    /**
     * Stores a float value with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The float value to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, float value, boolean backup);
    
    /**
     * Stores an integer value with an associated key into the Shared Preferences.
     * 
     * @param key The string identifier for the pair value.
     * @param value The integer value to store under the provided key.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void storeValue(String key, int value, boolean backup);
    
    /**
     * Removes a key from the Shared Preferences.
     * 
     * @param key The string key to remove.
     * @param backup Backup to the cloud if possible.
     */
    public abstract void removeKey(String key, boolean backup);
        
    /**
     * Depending on the API level, performs a commit() or an apply()
     */
    public abstract void commitOrApply();
    
    
	// ---------------------------------------------------------------------------------------------------------- //
	//                                             HELPER METHODS                                                 //       
	// ---------------------------------------------------------------------------------------------------------- //
    
    /**
     * Get the SharedPreferences referred in the 'sharedPreferencesName'. If the reference is NULL,
     * retrieves the default SharedPreferences.
     * 
     * @return This instance of SharedPreferencesOperations.
     */
    protected SharedPreferences getSharedPreferences() {
    	if (sharedPreferencesName != null) {
    		return context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);	
    	} else {
    		return PreferenceManager.getDefaultSharedPreferences(context);
    	}
    }
    
}
