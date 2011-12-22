package pt.sapo.mobile.android.connect.system;

/**
 * Enumeration of the currently known SDK version codes.  These are the values that can be found in {@link VERSION#SDK}.
 * Version numbers increment monotonically with each official platform release.
 * 
 * @author António Alegria
 * @author Rui Roque
 */
public class VersionCodes {
	
	public static final int CUR_DEVELOPMENT = 10000;
	public static final int BASE            = 1;
	public static final int BASE_1_1        = 2;
	public static final int CUPCAKE         = 3;
	public static final int DONUT           = 4;
	public static final int ECLAIR          = 5;
	public static final int ECLAIR_0_1      = 6;
	public static final int ECLAIR_MR1      = 7;
	public static final int FROYO           = 8;
	public static final int GINGERBREAD     = 9;
	public static final int GINGERBREAD_MR1 = 10;
	public static final int HONEYCOMB       = 11;
	public static final int HONEYCOMB_MR1   = 12;
	
	/**
	 * Warning: Do not use SDK_INT because it is only available on API Level 4.
	 */
	public static final int SDK_LEVEL = Integer.parseInt(android.os.Build.VERSION.SDK);
	
	public static final boolean SUPPORTS_GINGERBREAD = SDK_LEVEL >= GINGERBREAD;
	public static final boolean SUPPORTS_HONEYCOMB = SDK_LEVEL >= HONEYCOMB;
	public static final boolean SUPPORTS_FROYO = SDK_LEVEL >= FROYO;
	public static final boolean SUPPORTS_ECLAIR = SDK_LEVEL >= ECLAIR;
	public static final boolean BELOW_CUPCAKE_INCLUDED = SDK_LEVEL <= CUPCAKE;
	
}