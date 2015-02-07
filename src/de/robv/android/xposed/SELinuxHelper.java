package de.robv.android.xposed;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import android.os.Build;
import android.os.Process;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.services.BaseService;
import de.robv.android.xposed.services.BinderService;
import de.robv.android.xposed.services.DirectAccessService;
import de.robv.android.xposed.services.ZygoteService;

/**
 * A helper to work with (or without) SELinux, abstracting much of its big complexity.
 */
public final class SELinuxHelper {
	 /**
	* Determine whether SELinux is disabled or enabled.
	* @return A boolean indicating whether SELinux is enabled.
	*/
	public static boolean isSELinuxEnabled() {
		return sIsSELinuxEnabled;
	}

	/**
	* Determine whether SELinux is permissive or enforcing.
	* @return A boolean indicating whether SELinux is enforcing.
	*/
	public static boolean isSELinuxEnforced() {
		return sIsSELinuxEnforced;
	}

	/**
	* Gets the security context of the current process.
	* @return A String representing the security context of the current process.
	*/
	public static String getContext() {
		return sContext;
	}

	/**
	 * Retrieve the service to be used when accessing files in /data/data/*.
	 * <p><strong>IMPORTANT:</strong> If you call this from the Zygote process,
	 * don't re-use the result in different process!
	 * @return An instance of the service.
	 */
	public static BaseService getAppDataFileService() {
		if (sServiceAppDataFile != null)
			return sServiceAppDataFile;
		throw new UnsupportedOperationException();
	}


	// ----------------------------------------------------------------------------
	private static Class<?> sClassSELinux = null;
	private static boolean sIsSELinuxEnabled = false;
	private static boolean sIsSELinuxEnforced = false;
	private static String sContext = null;

	private static BaseService sServiceAppDataFile = null;

	static void initOnce() {
		if (Build.VERSION.SDK_INT < 17)
			return;

		try {
			sClassSELinux = findClass("android.os.SELinux", null);
			sIsSELinuxEnabled = (Boolean) callStaticMethod(sClassSELinux, "isSELinuxEnabled");
			sIsSELinuxEnforced = sIsSELinuxEnabled && (Boolean) callStaticMethod(sClassSELinux, "isSELinuxEnforced");
		} catch (ClassNotFoundError ignored) {};
	}

	static void initForProcess() {
		if (sIsSELinuxEnabled)
			sContext = (String) callStaticMethod(sClassSELinux, "getContext");

		if (sIsSELinuxEnforced) {
			int uid = Process.myUid();
			if (uid == 0) {
				sServiceAppDataFile = new ZygoteService();
			} else if (uid == Process.SYSTEM_UID) {
				sServiceAppDataFile = BinderService.getService(BinderService.TARGET_APP);
			} else {
				sServiceAppDataFile = new DirectAccessService();
			}
		} else {
			sServiceAppDataFile = new DirectAccessService();
		}
	}
}
