package pt.sapo.mobile.android.connect.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;

import pt.sapo.mobile.android.connect.R;
import pt.sapo.mobile.android.connect.system.Log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Network Utilities.
 * 
 * @author Rui Roque
 */
public class NetworkUtilities {
	
    /**
     * The Log tag for this class.
     */
    private static final String TAG = "NetworkUtilities";

    /**
     * Status codes for the checkConnectionStatus method.
     */
    public static final int STATUS_CONNECTION_OK = 0;
    public static final int STATUS_NO_NETWORK_CONNECTION = 1;
    public static final int STATUS_NO_CONNECTION_TO_SERVER = 2;

    /**
     * Available messages (in PT and EN).
     */
    public static final int MESSAGE_NO_NETWORK_CONNECTION   = R.string.sapo_network_no_network_connection;
    public static final int MESSAGE_NO_CONNECTION_TO_SERVER = R.string.sapo_network_no_server_connection;

    /**
     * The common usage for this method is to be called whenever there is an IOException in a Network call. E.g.:
     * 
     * try{
     *     // Do the network call in here
     * } catch (IOException e) {
     *     int result = NetworkOperations.checkConnectionStatus(context, httpClient, "services.sapo.pt", "https://services.sapo.pt/Pond/");
     *     String errorMessage = null;
     *     switch (result) {
     *         case NetworkOperations.STATUS_NO_NETWORK_CONNECTION:
     *             errorMessage = context.getString(NetworkOperations.MESSAGE_NO_NETWORK_CONNECTION);
     *             // Deal with it.
     *             break;
     *         case NetworkOperations.STATUS_NO_CONNECTION_TO_SERVER:
     *             errorMessage = context.getString(NetworkOperations.MESSAGE_NO_CONNECTION_TO_SERVER);
     *             // Deal with it.
     *             break;
     *         case NetworkOperations.STATUS_CONNECTION_OK:
     *             // Connection is OK, but there was an IOError. Deal with it.
     *             break;
     *     }
     * }
     * 
     * This way, it is possible to determine if the service call failure was due to no connectivity in the device, or server available or other I/O Error.
     * 
     * @param context The caller context.
     * @param httpClient A configured instance of the HTTP Client.
     * @param host A hostname for the service (e.g: services.sapo.pt).
     * @param servicesEndpoint The services endpoint (e.g.: https://services.sapo.pt/Pond/).
     * @return The evaluation code. Can be one of STATUS_CONNECTION_OK, STATUS_NO_NETWORK_CONNECTION or STATUS_NO_CONNECTION_TO_SERVER.
     */
    public static int checkConnectionStatus(Context context, HttpClient httpClient, String host, String servicesEndpoint) {
        // See if there is a network connection
        if (!checkNetworkConnection(context)) {
            // There is no Network connection
            Log.i(TAG, "checkConnectionStatus() - No network connection available.");
            return STATUS_NO_NETWORK_CONNECTION;

            // See if there is a server connection.
        } else if (!checkServerState(context, httpClient, host, servicesEndpoint)) {
            // There is no server connection
            Log.i(TAG, "checkConnectionStatus() - Network connection is available but server is not available.");
            return STATUS_NO_CONNECTION_TO_SERVER;
        }

        Log.i(TAG, "checkConnectionStatus() - Network connection and server connection is OK.");
        return STATUS_CONNECTION_OK;
    }

    /**
     * Determines if there is an open network connection.
     * 
     * @param context
     *            The application Context.
     * @return True if there is network connectivity or false if it isn't.
     */
    public static boolean checkNetworkConnection(Context context) {
        Log.d(TAG, "checkNetworkConnection() - Start");

        // Get hold of the ConnectivityManager
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the Network information through the ConectivityManager
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();

        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            Log.d(TAG, "checkNetworkConnection() - getActiveNetworkInfo() is NULL or getBackgroundDataSetting() returned FALSE");
            return false;
        }

        int netType = info.getType();

        if (netType == ConnectivityManager.TYPE_WIFI || netType == ConnectivityManager.TYPE_MOBILE) {
            Log.d(TAG, "checkNetworkConnection() - ConnectivityManager type is WIFI(1) or MOBILE(0). Type=" + netType);
            return info.isConnected();
        } else {
            Log.d(TAG, "checkNetworkConnection() - ConnectivityManager type is not WIFI(1) or MOBILE(0). Type=" + netType);
        }

        // There is no network available
        return false;
    }

    /**
     * Determines if the Pond server is reachable. Requires an active network connection. First invoke checkNetworkConnection(Context context).
     * 
     * @param context
     *            The application Context.
     * @return True if the Pond server can be reached or false if it doesn't.
     */
    private static boolean checkServerState(Context context, HttpClient httpClient, String host, String servicesEndpoint) {
        Log.d(TAG, "checkServerState() - Start");

        // Check if the hostname can be resolved in the DNS server
        boolean lookupHostResult = lookupHost(host);
        Log.d(TAG, "checkServerState() - lookupHostResult=" + lookupHostResult);

        if (!lookupHostResult) {
            // Hostname could not be resolved
            return false;
        }

        // Prepare the HTTP HEAD request for the Pond services
        HttpHead httpHead = null;
        if (TextUtils.isEmpty(servicesEndpoint)) {
            return false;
        } else {
            httpHead = new HttpHead(servicesEndpoint);
        }
        boolean serverReachable = false;

        try {
            HttpResponse httpResponse = httpClient.execute(httpHead);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.d(TAG, "checkServerState() - Status code for the HTTP HEAD request=" + statusCode);
            if (statusCode == 500) {
                // Internal server error: it's OK, because services doesn't support HEAD, but responded
                Log.d(TAG, "checkServerState() - Server reachable");
                serverReachable = true;
            } else {
                Log.d(TAG, "checkServerState() - Server unreachable");
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                httpEntity.consumeContent();
            }
        } catch (ClientProtocolException e) {
            Log.d(TAG, "checkServerState() - ClientProtocolException: ", e);
        } catch (IOException e) {
            Log.d(TAG, "checkServerState() - IOException: ", e);
        } catch (IllegalStateException e) {
            Log.d(TAG, "checkServerState() - IllegalStateException: ", e);
        } catch (Exception e) {
            Log.d(TAG, "checkServerState() - Exception: ", e);
        }

        return serverReachable;
    }

    /**
     * Try to resolve in the DNS the hostname passed as the argument.
     * 
     * @param hostname
     *            The name of the host (or the IP address in dot format).
     * @return True if it can resolve the hostname, or false if the host is unknown.
     */
    private static boolean lookupHost(String hostname) {
        try {
            InetAddress inetAddress = InetAddress.getByName(hostname);
            Log.d(TAG, "lookupHost() - The hostname " + hostname + " was found with IP address " + inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            Log.i(TAG, "lookupHost() - Unable to find hostname " + hostname);
            return false;
        }
        return true;
    }

}
