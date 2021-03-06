package pt.sapo.mobile.android.connect.system.sharedpreference;

import java.lang.ref.SoftReference;
import java.util.Set;

import android.app.backup.BackupManager;
import android.content.Context;
import android.text.TextUtils;

/**
 * Utility class for operations in the SharedPreferences targeting Froyo.
 * 
 * @author Rui Roque
 */
public class SharedPreferencesOperationsFroyo extends SharedPreferencesOperationsLegacy {

	/**
	 * Interaction with the Android Backup Service.
	 */
	protected BackupManager backupManager;
	
	// ---------------------------------------------------------------------------------------------------------- //
	//                                    CONSTRUCTOR AND INSTANCE GETTERS                                        //       
	// ---------------------------------------------------------------------------------------------------------- //
    
	public SharedPreferencesOperationsFroyo(Context context, String mSharedPreferencesName) {
		super(context, mSharedPreferencesName);
		backupManager = new BackupManager(context);
	}
	
	public synchronized static SharedPreferencesOperations getInstance(Context context, String mSharedPreferencesName) {
		if (TextUtils.isEmpty(mSharedPreferencesName)) {
			throw new UnsupportedOperationException("SharedPreferencesName must not be empty or null");
		}
		if (sharedPreferencesName.equals(mSharedPreferencesName) && instanceRef != null && instanceRef.get() != null) {
			return instanceRef.get();
		} else {
			instanceRef = new SoftReference<SharedPreferencesOperations>(new SharedPreferencesOperationsFroyo(context, mSharedPreferencesName));
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
    	return super.retrieveStringSetValue(key, defaultValue);
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
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void storeValue(String key, String value, boolean backup) {
        super.storeValue(key, value, backup);
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void storeValue(String key, Set<String> value, boolean backup) {
        super.storeValue(key, value, backup);
    }
    
    @Override
    public void storeValue(String key, boolean value, boolean backup) {
        super.storeValue(key, value, backup);
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void storeValue(String key, float value, boolean backup) {
        super.storeValue(key, value, backup);
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void storeValue(String key, int value, boolean backup) {
        super.storeValue(key, value, backup);
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void removeKey(String key, boolean backup) {
        super.removeKey(key, backup);
        if (backup) {
        	backupManager.dataChanged();	
        }
    }
    
    @Override
    public void commitOrApply() {
    	super.commitOrApply();
    }
        
}
