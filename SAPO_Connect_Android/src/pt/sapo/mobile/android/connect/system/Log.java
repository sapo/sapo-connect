package pt.sapo.mobile.android.connect.system;

import pt.sapo.mobile.android.connect.R;
import android.app.Application;
import android.content.Context;

/**
 * Wrapper class for android.util.Log intended to seamlessly replace imports of
 * android.util.Log in the application for this class.
 *
 * Uses an internal mechanism for log level check.
 *
 * Change the LOG_LEVEL field for controlling the log output. Possible values are:
 *
 *     VERBOSE = 2
 *     DEBUG   = 3
 *     INFO    = 4
 *     WARN    = 5
 *     ERROR   = 6
 *     ASSERT  = 7
 *
 * @author Rui Roque
 * @author António Alegria
 */
@SuppressWarnings("unused")
public class Log {

	/**
	 * Application available log levels.
	 */
	public static final int VERBOSE   = android.util.Log.VERBOSE;
	public static final int DEBUG     = android.util.Log.DEBUG;
	public static final int INFO      = android.util.Log.INFO;
	public static final int WARN      = android.util.Log.WARN;
	public static final int ERROR     = android.util.Log.ERROR;
	public static final int ASSERT    = android.util.Log.ASSERT;
	private static final int SUPPRESS = 8;

	// VERBOSE starts at 2 so we have to pad the array so that we LEVEL_STRINGS[i] = levelName(i)
	private static final String[] LEVEL_STRINGS = {"verbose", "verbose", "verbose", "debug", "info", "warn", "error", "assert", "suppress"};

	/**
	 * Application log level. Use values above to select.
	 */
	private static int LOG_LEVEL = WARN;


	/**
	 * Query if a specific logging level is active.
	 *
	 * @author António Alegria
	 */
	public static boolean isVerbose() { return LOG_LEVEL == VERBOSE;  }
	public static boolean isDebug()   { return LOG_LEVEL <= DEBUG;    }
	public static boolean isInfo()    { return LOG_LEVEL <= INFO;     }
	public static boolean isWarn()    { return LOG_LEVEL <= WARN;     }
	public static boolean isError()   { return LOG_LEVEL <= ERROR;    }
	public static boolean isAssert()  { return LOG_LEVEL <= ASSERT;   }
	public static boolean isSuppress(){ return LOG_LEVEL == SUPPRESS; }


	private static final String TAG = Log.class.getCanonicalName();


	/**
	 * Configure logging for application, according to the resource values set in its context.
	 *
	 * Currently, you may configure the following string attributes:
	 * - pt.sapo.mobile.android.connect.system.Log.level: string representations of logging level (e.g. "debug", "info", ...)
	 *
	 * @param context The application context.
	 * @author António Alegria
	 */
	public static void configFromContext(Context context) {
		String levelStr = context.getResources().getString(R.string.pt_sapo_mobile_android_connect_Log_level);
		Log.w(context.getPackageName(), "configFromContext() - Loaded log level from resources: " + levelStr);

		if (levelStr.equals("verbose")) {
			setLevel(VERBOSE);
		} else if (levelStr.equals("debug")) {
			setLevel(DEBUG);
		} else if (levelStr.equals("info")) {
			setLevel(INFO);
		} else if (levelStr.equals("warn")) {
			setLevel(WARN);
		} else if (levelStr.equals("error")) {
			setLevel(ERROR);
		} else if (levelStr.equals("assert")) {
			setLevel(ASSERT);
		} else if (levelStr.equals("supress")) {
			setLevel(SUPPRESS);
		} else {
			Log.e(TAG, "Incorrect level: " + levelStr);
			return;
		}
	}

	/**
	 * Set logging level for your application.
	 *
	 * @param level The log level. You can choose from VERBOSE, DEBUG, INFO, WARN, ERROR or ASSERT.
	 * @author António Alegria
	 */
	public static void setLevel(int level) {
		if (level >= VERBOSE && level <= SUPPRESS) {
			LOG_LEVEL = level;
			Log.w(TAG, "setLevel() - Log level set to: " + LEVEL_STRINGS[level] );
		} else {
			Log.e(TAG, "setLevel() - Incorrect level: " + level);
		}
	}

	/**
	 * Mirror method for android.util.Log.isLoggable. Doesn't call the native method.
	 * Instead, calls the internal method isInternalLoggable.
	 *
	 * @param tag The log tag.
	 * @param level The log level.
	 * @return The boolean result.
	 */
	public static boolean isLoggable(String tag, int level) {
		return isInternalLoggable(tag, level);
	}

	/**
	 * Used internally, only for this class. Calls the overloaded method without the
	 * log tag.
	 *
	 * @param tag The log level.
	 * @param level The log level.
	 * @return The boolean result.
	 */
	private static boolean isInternalLoggable(String tag, int level) {
		return isInternalLoggable(level);
	}

	/**
	 * Used internally, only for this class. Determines if the log level is allowed.
	 *
	 * @param level The log level.
	 * @return The boolean result.
	 */
	private static boolean isInternalLoggable(int level) {
		return level >= LOG_LEVEL;
	}

	/**
	 * Mirrors the android.util.Log.d(String tag, String msg) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 */
	public static void d(String tag, String msg) {
		if (isInternalLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(tag, msg);
		}
	}

	/**
	 * Same behavior of android.util.Log.d(String tag, String msg) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 */
	public static void d(Object object, String msg) {
		if (isInternalLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(object.getClass().getSimpleName(), msg);
		}
	}

	/**
	 * Mirrors the android.util.Log.d(String tag, String msg, Throwable tr)
	 * with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void d(String tag, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(tag, msg, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.d(String tag, String msg, Throwable tr) with
	 * internal log level check. But instead of requiring a TAG, uses the object passed
	 * as parameter for getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void d(Object object, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(object.getClass().getSimpleName(), msg, tr);
		}
	}

	/**
	 * Mirrors the android.util.Log.e(String tag, String msg) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 */
	public static void e(String tag, String msg) {
		if (isInternalLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(tag, msg);
		}
	}

	/**
	 * Same behavior of android.util.Log.e(String tag, String msg) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 */
	public static void e(Object object, String msg) {
		if (isInternalLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(object.getClass().getSimpleName(), msg);
		}
	}

	/**
	 * Mirrors the android.util.Log.e(String tag, String msg, Throwable tr)
	 * with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void e(String tag, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(tag, msg, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.e(String tag, String msg, Throwable tr) with
	 * internal log level check. But instead of requiring a TAG, uses the object passed
	 * as parameter for getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void e(Object object, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(object.getClass().getSimpleName(), msg, tr);
		}
	}

	/**
	 * Mirrors the android.util.Log.i(String tag, String msg) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 */
	public static void i(String tag, String msg) {
		if (isInternalLoggable(android.util.Log.INFO)) {
			android.util.Log.i(tag, msg);
		}
	}

	/**
	 * Same behavior of android.util.Log.i(String tag, String msg) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 */
	public static void i(Object object, String msg) {
		if (isInternalLoggable(android.util.Log.INFO)) {
			android.util.Log.i(object.getClass().getSimpleName(), msg);
		}
	}

	/**
	 * Mirrors the android.util.Log.i(String tag, String msg, Throwable tr)
	 * with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void i(String tag, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.INFO)) {
			android.util.Log.i(tag, msg, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.i(String tag, String msg, Throwable tr) with
	 * internal log level check. But instead of requiring a TAG, uses the object passed
	 * as parameter for getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void i(Object object, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.INFO)) {
			android.util.Log.i(object.getClass().getSimpleName(), msg, tr);
		}
	}

	/**
	 * Mirrors the android.util.Log.v(String tag, String msg) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 */
	public static void v(String tag, String msg) {
		if (isInternalLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(tag, msg);
		}
	}

	/**
	 * Same behavior of android.util.Log.v(String tag, String msg) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 */
	public static void v(Object object, String msg) {
		if (isInternalLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(object.getClass().getSimpleName(), msg);
		}
	}

	/**
	 * Mirrors the android.util.Log.v(String tag, String msg, Throwable tr)
	 * with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void v(String tag, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(tag, msg, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.v(String tag, String msg, Throwable tr) with
	 * internal log level check. But instead of requiring a TAG, uses the object passed
	 * as parameter for getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void v(Object object, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(object.getClass().getSimpleName(), msg, tr);
		}
	}

	/**
	 * Mirrors the android.util.Log.w(String tag, String msg) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 */
	public static void w(String tag, String msg) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(tag, msg);
		}
	}

	/**
	 * Same behavior of android.util.Log.w(String tag, String msg) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 */
	public static void w(Object object, String msg) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(object.getClass().getSimpleName(), msg);
		}
	}

	/**
	 * Mirrors the android.util.Log.w(String tag, Throwable tr) with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void w(String tag, Throwable tr) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(tag, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.w(String tag, Throwable tr) with internal log level
	 * check. But instead of requiring a TAG, uses the object passed as parameter for
	 * getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void w(Object object, Throwable tr) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(object.getClass().getSimpleName(), tr);
		}
	}

	/**
	 * Mirrors the android.util.Log.w(String tag, String msg, Throwable tr)
	 * with internal log level check.
	 *
	 * @param tag The log tag.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void w(String tag, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(tag, msg, tr);
		}
	}

	/**
	 * Same behavior of android.util.Log.w(String tag, String msg, Throwable tr) with
	 * internal log level check. But instead of requiring a TAG, uses the object passed
	 * as parameter for getting the class name for the tag.
	 *
	 * @param object The object to get the tag from.
	 * @param msg The message for logging.
	 * @param tr The throwable for displaying the stack trace in the log.
	 */
	public static void w(Object object, String msg, Throwable tr) {
		if (isInternalLoggable(android.util.Log.WARN)) {
			android.util.Log.w(object.getClass().getSimpleName(), msg, tr);
		}
	}

}
