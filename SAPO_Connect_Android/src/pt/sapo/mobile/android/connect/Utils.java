package pt.sapo.mobile.android.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

/**
 * General utility methods. 
 * 
 * @author Rui Roque
 */
public class Utils {
	
    /**
     * Given an asset name, loads it as a String.
     * 
     * @param context The caller Context.
     * @param assetName The asset name.
     * @return The String containing the asset content.
     * @throws IOException If it cannot load the asset.
     */
    public static String getAssetAsString(Context context, String assetName) throws IOException {
    	InputStream is = context.getAssets().open(assetName);
    	InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
	}
    
}
