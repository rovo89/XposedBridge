package android.app;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.res.Resources;

/**
 * Accessor for package level methods/fields in package android.app
 */
public class AndroidAppHelper {
	public static HashMap<String, WeakReference<LoadedApk>> getActivityThread_mPackages(ActivityThread activityThread) {
		return activityThread.mPackages;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<Object, WeakReference<Resources>> getActivityThread_mActiveResources(ActivityThread activityThread) {
		HashMap map = activityThread.mActiveResources;
		return map;
	}
}
