package android.app;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

/**
 * Accessor for package level methods/fields in package android.app
 */
public class AndroidAppHelper {
	private static Class<?> CLASS_RESOURCES_KEY;
	private static boolean HAS_IS_THEMEABLE = false;

	static {
		CLASS_RESOURCES_KEY = (Build.VERSION.SDK_INT < 19) ?
			  findClass("android.app.ActivityThread$ResourcesKey", null)
			: findClass("android.content.res.ResourcesKey", null);

		try {
			// T-Mobile theming engine (CyanogenMod etc.)
			findField(CLASS_RESOURCES_KEY, "mIsThemeable");
			HAS_IS_THEMEABLE = true;
		} catch (NoSuchFieldError ignored) {
		} catch (Throwable t) { XposedBridge.log(t); }
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<Object, WeakReference<Resources>> getActiveResources(ActivityThread activityThread) {
		if (Build.VERSION.SDK_INT <= 18) {
			return (Map) getObjectField(activityThread, "mActiveResources");
		} else {
			Object resourcesManager = getObjectField(activityThread, "mResourcesManager");
			return (Map) getObjectField(resourcesManager, "mActiveResources");
		}
	}

	/* For SDK 15 & 16 */
	private static Object createResourcesKey(String resDir, float scale, boolean isThemeable) {
		try {
			if (HAS_IS_THEMEABLE)
				return newInstance(CLASS_RESOURCES_KEY, resDir, scale, isThemeable);
			else
				return newInstance(CLASS_RESOURCES_KEY, resDir, scale);
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	/* For SDK 17 & 18 */
	private static Object createResourcesKey(String resDir, int displayId, Configuration overrideConfiguration, float scale, boolean isThemeable) {
		try {
			if (HAS_IS_THEMEABLE)
				return newInstance(CLASS_RESOURCES_KEY, resDir, displayId, overrideConfiguration, scale, isThemeable);
			else
				return newInstance(CLASS_RESOURCES_KEY, resDir, displayId, overrideConfiguration, scale);
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	/* For SDK 19+ */
	private static Object createResourcesKey(String resDir, int displayId, Configuration overrideConfiguration, float scale, IBinder token, boolean isThemeable) {
		try {
			if (HAS_IS_THEMEABLE)
				return newInstance(CLASS_RESOURCES_KEY, resDir, displayId, overrideConfiguration, scale, isThemeable, token);
			else
				return newInstance(CLASS_RESOURCES_KEY, resDir, displayId, overrideConfiguration, scale, token);
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	public static void addActiveResource(String resDir, float scale, boolean isThemeable, Resources resources) {
		ActivityThread thread = ActivityThread.currentActivityThread();
		if (thread == null)
			return;

		Object resourcesKey;
		if (Build.VERSION.SDK_INT <= 16)
			resourcesKey = createResourcesKey(resDir, scale, isThemeable);
		else if (Build.VERSION.SDK_INT <= 18)
			resourcesKey = createResourcesKey(resDir, Display.DEFAULT_DISPLAY, null, scale, isThemeable);
		else
			resourcesKey = createResourcesKey(resDir, Display.DEFAULT_DISPLAY, null, scale, null, isThemeable);

		if (resourcesKey != null)
			getActiveResources(thread).put(resourcesKey, new WeakReference<Resources>(resources));
	}

	public static String currentProcessName() {
		String processName = ActivityThread.currentPackageName();
		if (processName == null)
			return "android";
		return processName;
	}

	public static ApplicationInfo currentApplicationInfo() {
		ActivityThread am = ActivityThread.currentActivityThread();
		if (am == null)
			return null;

		Object boundApplication = getObjectField(am, "mBoundApplication");
		if (boundApplication == null)
			return null;

		return (ApplicationInfo) getObjectField(boundApplication, "appInfo");
	}

	public static String currentPackageName() {
		ApplicationInfo ai = currentApplicationInfo();
		return (ai != null) ? ai.packageName : "android";
	}

	public static Application currentApplication() {
		return ActivityThread.currentApplication();
	}

	/** use class {@link XSharedPreferences} instead */
	@Deprecated
	public static SharedPreferences getSharedPreferencesForPackage(String packageName, String prefFileName, int mode) {
		return new XSharedPreferences(packageName, prefFileName);
	}

	/** use class {@link XSharedPreferences} instead */
	@Deprecated
	public static SharedPreferences getDefaultSharedPreferencesForPackage(String packageName) {
		return new XSharedPreferences(packageName);
	}

	/** use {@link XSharedPreferences#reload()}instead */
	@Deprecated
	public static void reloadSharedPreferencesIfNeeded(SharedPreferences pref) {
		if (pref instanceof XSharedPreferences) {
			((XSharedPreferences) pref).reload();
		}
	}
}
