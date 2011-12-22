package pt.sapo.mobile.android.connect.system.sharedpreference;

import java.lang.ref.SoftReference;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;

/**
 * Utility class for operations in the SharedPreferences targeting Android versions pre-Froyo.
 * 
 * @author Rui Roque
 */
public class SharedPreferencesOperationsLegacy extends SharedPreferencesOperations {    
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                    CONSTRUCTOR AND INSTANCE GETTERS                                        //       
	// ---------------------------------------------------------------------------------------------------------- //

	public SharedPreferencesOperationsLegacy(Context context, String sharedPreferencesName) {
		super(context, sharedPreferencesName);
	}
	
	public synchronized static SharedPreferencesOperations getInstance(Context context, String mSharedPreferencesName) {
		if (TextUtils.isEmpty(mSharedPreferencesName)) {
			throw new UnsupportedOperationException("SharedPreferencesName must not be empty or null");
		}
		if (sharedPreferencesName.equals(mSharedPreferencesName) && instanceRef != null && instanceRef.get() != null) {
			return instanceRef.get();
		} else {
			instanceRef = new SoftReference<SharedPreferencesOperations>(new SharedPreferencesOperationsLegacy(context, mSharedPreferencesName));
			return instanceRef.get();	
		}
	}
		
	// ---------------------------------------------------------------------------------------------------------- //
	//                                          RETRIEVAL OPERATIONS                                              //       
	// ---------------------------------------------------------------------------------------------------------- //
    
	@Override
    public Long retrieveLongValue(String key, Long defaultValue) {
    	Long value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getLong(key, defaultValue);
    	}
    	return value;
    }

    @Override
    public String retrieveStringValue(String key, String defaultValue) {
    	String value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getString(key, defaultValue);
    	}
    	return value;
    }
    
	@Override
	public Set<String> retrieveStringSetValue(String key, Set<String> defaultValue) {
		throw new UnsupportedOperationException("retrieveStringSetValue is only available in API level 11");
	}
    
    @Override
    public Boolean retrieveBooleanValue(String key, Boolean defaultValue) {
    	Boolean value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getBoolean(key, defaultValue);
    	}
    	return value;
    }
    
    @Override
    public Float retrieveFloatValue(String key, Float defaultValue) {
    	Float value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getFloat(key, defaultValue);
    	}
    	return value;
    }
    
    @Override
    public Integer retrieveIntegerValue(String key, Integer defaultValue) {
    	Integer value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getInt(key, defaultValue);
    	}
    	return value;
    }
    
    // ---------------------------------------------------------------------------------------------------------- //
	//                                            STORING METHODS                                                 //       
	// ---------------------------------------------------------------------------------------------------------- //
    
    @Override
    public void storeValue(String key, long value, boolean backup) {
        editor.putLong(key, value);
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }
    
    @Override
    public void storeValue(String key, String value, boolean backup) {
        editor.putString(key, value);
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }
    

	@Override
	public void storeValue(String key, Set<String> value, boolean backup) {
		throw new UnsupportedOperationException("storeValue with StringSet is only available in API level 11");
	}
    
    @Override
    public void storeValue(String key, boolean value, boolean backup) {
        editor.putBoolean(key, value);
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }
    
    @Override
    public void storeValue(String key, float value, boolean backup) {
        editor.putFloat(key, value);
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }
    
    @Override
    public void storeValue(String key, int value, boolean backup) {
        editor.putInt(key, value);
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }
    
    @Override
    public void removeKey(String key, boolean backup) {
        editor.remove(key);
        commitOrApply();
        if (!(this instanceof SharedPreferencesOperationsGingerbread)) {
        	commitOrApply();
        }
    }

	@Override
	public void commitOrApply() {
		editor.commit();
	}
        
}
