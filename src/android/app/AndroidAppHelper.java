package android.app;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.os.Environment;
import de.robv.android.xposed.XposedBridge;

/**
 * Accessor for package level methods/fields in package android.app
 */
public class AndroidAppHelper {
	private static Constructor<?> constructor_ResourcesKey;
	private static Field field_CompatibilityInfo_isThemeable;
	private static boolean searchIsThemeable = false;
	private static boolean resourcesKeyCM9 = false;
	
	public static HashMap<String, WeakReference<LoadedApk>> getActivityThread_mPackages(ActivityThread activityThread) {
		return activityThread.mPackages;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<Object, WeakReference<Resources>> getActivityThread_mActiveResources(ActivityThread activityThread) {
		HashMap map = activityThread.mActiveResources;
		return map;
	}
	
	public static Object createResourcesKey(String resDir, CompatibilityInfo compInfo) {
		if (!searchIsThemeable) {
			try {
				field_CompatibilityInfo_isThemeable = CompatibilityInfo.class.getDeclaredField("isThemeable");
				field_CompatibilityInfo_isThemeable.setAccessible(true);
			} catch (NoSuchFieldException ignored) {}
			searchIsThemeable = true;
		}
		
		boolean isThemeable = false;
		if (field_CompatibilityInfo_isThemeable != null) {
			try {
				isThemeable = field_CompatibilityInfo_isThemeable.getBoolean(compInfo);
			} catch (Exception e) {
				XposedBridge.log(e);
			}
		}
		
		return createResourcesKey(resDir, compInfo.applicationScale, isThemeable);
	}
	
	public static Object createResourcesKey(String resDir, float scale, boolean isThemeable) {
		try {
			if (constructor_ResourcesKey == null) {
				Class<?> classResourcesKey = Class.forName("android.app.ActivityThread$ResourcesKey");
				try {
				constructor_ResourcesKey = classResourcesKey.getDeclaredConstructor(String.class, float.class);
				} catch (NoSuchMethodException ignored) {
					resourcesKeyCM9 = true;
					constructor_ResourcesKey = classResourcesKey.getDeclaredConstructor(String.class, float.class, boolean.class);					
				}
				constructor_ResourcesKey.setAccessible(true);
			}
			
			if (!resourcesKeyCM9)
				return constructor_ResourcesKey.newInstance(resDir, scale);
			else
				return constructor_ResourcesKey.newInstance(resDir, scale, isThemeable);
		} catch (Exception e) {
			XposedBridge.log(e);
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
	
	public static String currentPackageName() {
		return ActivityThread.currentPackageName();
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
