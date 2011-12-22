package pt.sapo.mobile.android.connect.network;

import android.text.format.DateUtils;

public interface Services {

	// ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                     CONTROL FLAGS AND VALUES                                                        //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
	
    /**
     * Timeout for the connection.
     */
    public static final int CONNECTION_TIMEOUT = (int)(DateUtils.SECOND_IN_MILLIS * 6);
    
	// ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                         SERVICES ENDPOINTS                                                          //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
    /**
     * Host for the SAPO services.
     */
    public static final String SAPO_SERVICES_HOST = "services.sapo.pt";
    
	// ----------------------------------------------------------------------------------------------------------------------------------- //
    //                                                        SERVICES PARAMETERS                                                          //
    // ----------------------------------------------------------------------------------------------------------------------------------- //
    
    /**
     * Parameter key for the SAPO Voucher Client ID.
     */
    public static final String PARAM_CLIENT_ID = "client_id";
    
    /**
     * Parameter key for getting JSON error responses from the BUS, instead of XML responses.
     */
    public static final String PARAM_JSON_ARG = "jsonArg";
        	
}
