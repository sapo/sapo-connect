package pt.sapo.android.connect.example.network;

import pt.sapo.mobile.android.connect.network.NetworkObject;
import pt.sapo.mobile.android.connect.network.NetworkOperations.HttpMethod;
import pt.sapo.mobile.android.connect.network.OnNetworkResultsListener;
import pt.sapo.mobile.android.connect.network.RequestObject;
import pt.sapo.mobile.android.connect.network.Services;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

/**
 * Specifies the ImageGetListByUser WebService call.
 * 
 * @author Rui Roque
 */
public class ImageGetListByUser extends RequestObject {
	
	@Override
	public String toUrlParamaters() {
		return null;
	}

	@Override
	public boolean requiresOAuth() {
		return true;
	}

	@Override
	public Object[] getOptionalParameters() {
		return null;
	}

	@Override
	public String getBaseUrl() {
		return "https://" + Services.SAPO_SERVICES_HOST + "/Photos/";
	}

	@Override
	public boolean requiresExplicitJsonResponse() {
		return true;
	}

	@Override
	public boolean requiresClientId() {
		return true;
	}

	@Override
	public Integer getTtlString() {
		return null;
	}

	@Override
	public HttpMethod getHttpMethod() {
		return HttpMethod.GET;
	}

	@Override
	public String getXmlPost() {
		return null;
	}

	@Override
	public NetworkObject executeOperations(Context context, Handler handler, OnNetworkResultsListener callback, boolean unthreaded, String responseString, Cursor cursor, RequestObject requestObject) {
		// Just pass the result to the caller as a String in the successResult field, giving it a result=true no matter the result.
		NetworkObject networkResponseObject = new NetworkObject(requestObject, cursor);
		networkResponseObject.result = true;
		networkResponseObject.successResult = responseString;
		return networkResponseObject;
	}

}
