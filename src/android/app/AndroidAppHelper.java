package android.app;

import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.os.Environment;
import de.robv.android.xposed.XposedBridge;

/**
 * Accessor for package level methods/fields in package android.app
 */
public class AndroidAppHelper {
	private static boolean hasIsThemeable = false;
	
	static {
		try {
			// check if the field exists
			findField(CompatibilityInfo.class, "isThemeable");
			hasIsThemeable = true;
		} catch (NoSuchFieldError ignored) {
		} catch (Throwable t) { XposedBridge.log(t); }
	}
	
	public static HashMap<String, WeakReference<LoadedApk>> getActivityThread_mPackages(ActivityThread activityThread) {
		return activityThread.mPackages;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<Object, WeakReference<Resources>> getActivityThread_mActiveResources(ActivityThread activityThread) {
		HashMap map = activityThread.mActiveResources;
		return map;
	}
	
	public static Object createResourcesKey(String resDir, CompatibilityInfo compInfo) {
		boolean isThemeable = false;
		if (hasIsThemeable) {
			try {
				isThemeable = getBooleanField(compInfo, "isThemeable");
			} catch (Throwable t) { XposedBridge.log(t); }
		}
		
		return createResourcesKey(resDir, compInfo.applicationScale, isThemeable);
	}
	
	public static Object createResourcesKey(String resDir, float scale, boolean isThemeable) {
		try {
			Class<?> classResourcesKey = Class.forName("android.app.ActivityThread$ResourcesKey");
			if (hasIsThemeable)
				return newInstance(classResourcesKey, resDir, scale, isThemeable);
			else
				return newInstance(classResourcesKey, resDir, scale);
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}
	
	public static void addActiveResource(String resDir, float scale, boolean isThemeable, Resources resources) {
		ActivityThread thread = ActivityThread.currentActivityThread();
		if (thread == null)
			return;
		
		getActivityThread_mActiveResources(thread).put(
			createResourcesKey(resDir, scale, false),
			new WeakReference<Resources>(resources));
	}
	
	public static String currentProcessName() {
		String processName = ActivityThread.currentPackageName();
		if (processName == null)
			return "android";
		return processName;
	}
	
	public static ApplicationInfo currentApplicationInfo() {
        ActivityThread am = ActivityThread.currentActivityThread();
        return (am != null && am.mBoundApplication != null)
            ? am.mBoundApplication.appInfo : null;
	}
	
	public static String currentPackageName() {
		ApplicationInfo ai = currentApplicationInfo();
        return (ai != null) ? ai.packageName : "android";
	}
	
	public static Application currentApplication() {
		return ActivityThread.currentApplication();
	}
	
	
	public static SharedPreferences getSharedPreferencesForPackage(String packageName, String prefFileName, int mode) {
        File prefFile = new File(Environment.getDataDirectory(), "data/" + packageName + "/shared_prefs/" + prefFileName + ".xml");
        return new SharedPreferencesImpl(prefFile, mode);
	}
	
	public static SharedPreferences getDefaultSharedPreferencesForPackage(String packageName) {
		return getSharedPreferencesForPackage(packageName, packageName + "_preferences", Context.MODE_PRIVATE);
	}
	
	public static void reloadSharedPreferencesIfNeeded(SharedPreferences pref) {
		if (pref instanceof SharedPreferencesImpl) {
			((SharedPreferencesImpl) pref).startReloadIfChangedUnexpectedly();
		}
	}
}
