package pt.sapo.mobile.android.connect.system.sharedpreference;

import java.lang.ref.SoftReference;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;

/**
 * Utility class for operations in the SharedPreferences targeting Gingerbread.
 * 
 * @author Rui Roque
 */
public class SharedPreferencesOperationsHoneycomb extends SharedPreferencesOperationsGingerbread {
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                    CONSTRUCTOR AND INSTANCE GETTERS                                        //       
	// ---------------------------------------------------------------------------------------------------------- //
        
	public SharedPreferencesOperationsHoneycomb(Context context, String sharedPreferencesName) {
		super(context, sharedPreferencesName);
	}
	
	public synchronized static SharedPreferencesOperations getInstance(Context context, String mSharedPreferencesName) {
		if (TextUtils.isEmpty(mSharedPreferencesName)) {
			throw new UnsupportedOperationException("SharedPreferencesName must not be empty or null");
		}
		if (sharedPreferencesName.equals(mSharedPreferencesName) && instanceRef != null && instanceRef.get() != null) {
			return instanceRef.get();
		} else {
			instanceRef = new SoftReference<SharedPreferencesOperations>(new SharedPreferencesOperationsHoneycomb(context, mSharedPreferencesName));
			return instanceRef.get();	
		}
	}
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                          RETRIEVAL OPERATIONS                                              //       
	// ---------------------------------------------------------------------------------------------------------- //
    
	@Override
    public Long retrieveLongValue(String key, Long defaultValue) {
		return super.retrieveLongValue(key, defaultValue);
    }

    @Override
    public String retrieveStringValue(String key, String defaultValue) {
    	return super.retrieveStringValue(key, defaultValue);
    }
    
    @Override
	public Set<String> retrieveStringSetValue(String key, Set<String> defaultValue) {
		Set<String> value = defaultValue;
    	if (sharedPreferences.contains(key)) {
    		value = sharedPreferences.getStringSet(key, defaultValue);
    	}
    	return value;
	}
    
    @Override
    public Boolean retrieveBooleanValue(String key, Boolean defaultValue) {
    	return super.retrieveBooleanValue(key, defaultValue);
    }
    
    @Override
    public Float retrieveFloatValue(String key, Float defaultValue) {
    	return super.retrieveFloatValue(key, defaultValue);
    }
    
    @Override
    public Integer retrieveIntegerValue(String key, Integer defaultValue) {
    	return super.retrieveIntegerValue(key, defaultValue);
    }
    
    
    // ---------------------------------------------------------------------------------------------------------- //
	//                                            STORING METHODS                                                 //       
	// ---------------------------------------------------------------------------------------------------------- //
    
    @Override
    public void storeValue(String key, long value, boolean backup) {
    	super.storeValue(key, value, backup);
    }
    
    @Override
    public void storeValue(String key, String value, boolean backup) {
    	super.storeValue(key, value, backup);
    }
    
    @Override
	public void storeValue(String key, Set<String> value, boolean backup) {
        editor.putStringSet(key, value);
        if (this instanceof SharedPreferencesOperationsGingerbread) {
        	editor.apply();
        } else {
        	editor.commit();	
        }
        if (backup) {
        	backupManager.dataChanged();	
        }
	}
    
    @Override
    public void storeValue(String key, boolean value, boolean backup) {
    	super.storeValue(key, value, backup);
    }
    
    @Override
    public void storeValue(String key, float value, boolean backup) {
    	super.storeValue(key, value, backup);
    }
    
    @Override
    public void storeValue(String key, int value, boolean backup) {
    	super.storeValue(key, value, backup);
    }
    
    @Override
    public void removeKey(String key, boolean backup) {
        super.removeKey(key, backup);
    }
    
    @Override
    public void commitOrApply() {
    	super.commitOrApply();
    }
            
}
