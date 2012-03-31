package de.robv.android.xposed;

import java.lang.reflect.Method;
import java.util.Iterator;

import android.os.Process;

/**
 * Handlers used for debugging purposes
 */
public class DebugHandlers {
	public static void init() {
		try {
			XposedBridge.hookLoadPackage(DebugHandlers.class, "logLoadPackage", Callback.PRIORITY_HIGHEST);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	/**
	 * Logs all new packages that are being loaded
	 */
	@SuppressWarnings("unused")
	private static void logLoadPackage(String packageName, ClassLoader classLoader) {
		XposedBridge.log("----- Loading new package: -----");
		XposedBridge.log("pid: " + Process.myPid());
		XposedBridge.log("uid: " + Process.myUid());
		XposedBridge.log("tid: " + Process.myTid());
		XposedBridge.log("packageName: " + packageName);
		XposedBridge.log("-----");
	}
	
	/**
	 * Logs all calls to hooked methods, including their arguments etc.
	 * Can create lots of logging data!<br/>
	 * Add this hook with:<br/>
	 * <code>XposedBridge.hookMethod(yourmethod, DebugHandlers.class, "logHookedMethodParams", Callback.PRIORITY_HIGHEST);</code>
	 */
	@SuppressWarnings("unused")
	private static Object logHookedMethodParams(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		XposedBridge.log("------------------------------------------");
		XposedBridge.log("handleHookedMethod");
		XposedBridge.log("originalMethod: " + method);
		XposedBridge.log("thisObject: " + thisObject);
		XposedBridge.log("args.length: " + args.length);
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				XposedBridge.log("args[" + i + "] = null");
			} else {
				XposedBridge.log("args[" + i + "].type = " + args[i].getClass());
				XposedBridge.log("args[" + i + "].toString = " + args[i].toString());
			}
		}
		Object result = null;
		XposedBridge.log("##### invoking original method now... #####");
		try {
			result = XposedBridge.callNext(iterator, method, thisObject, args);
		} catch (Exception e) {
			XposedBridge.log("not successful, exception: " + e);
			XposedBridge.log("------------------------------------------");
			throw e;
		}
		XposedBridge.log("result: " + result);
		XposedBridge.log("------------------------------------------");
		return result;
	}
}