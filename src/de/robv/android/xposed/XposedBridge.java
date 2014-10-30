package de.robv.android.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.AndroidAppHelper;
import android.app.LoadedApk;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XResources;
import android.content.res.XResources.XTypedArray;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.android.internal.os.RuntimeInit;
import com.android.internal.os.ZygoteInit;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;

public final class XposedBridge {
	public static final String INSTALLER_PACKAGE_NAME = "de.robv.android.xposed.installer";
	public static int XPOSED_BRIDGE_VERSION;

	private static File logFile = null;
	private static PrintWriter logWriter = null;
	// log for initialization of a few mods is about 500 bytes, so 2*20 kB (2*~350 lines) should be enough
	private static final int MAX_LOGFILE_SIZE_SOFT = 20*1024;
	private static final int MAX_LOGFILE_SIZE_HARD = 5*1024*1024;
	private static boolean disableHooks = false;
	public static boolean disableResources = false;

	private static final Object[] EMPTY_ARRAY = new Object[0];
	public static final ClassLoader BOOTCLASSLOADER = ClassLoader.getSystemClassLoader();
	@SuppressLint("SdCardPath")
	public static final String BASE_DIR = "/data/data/" + INSTALLER_PACKAGE_NAME + "/";

	// built-in handlers
	private static final Map<Member, CopyOnWriteSortedSet<XC_MethodHook>> sHookedMethodCallbacks
									= new HashMap<Member, CopyOnWriteSortedSet<XC_MethodHook>>();
	private static final CopyOnWriteSortedSet<XC_LoadPackage> sLoadedPackageCallbacks
									= new CopyOnWriteSortedSet<XC_LoadPackage>();
	private static final CopyOnWriteSortedSet<XC_InitPackageResources> sInitPackageResourcesCallbacks
									= new CopyOnWriteSortedSet<XC_InitPackageResources>();

	/**
	 * Called when native methods and other things are initialized, but before preloading classes etc.
	 */
	private static void main(String[] args) {
		// the class the VM has been created for or null for the Zygote process
		String startClassName = getStartClassName();

		// initialize the Xposed framework and modules
		try {
			// initialize log file
			try {
				logFile = new File(BASE_DIR + "log/error.log");
				if (startClassName == null && logFile.length() > MAX_LOGFILE_SIZE_SOFT)
					logFile.renameTo(new File(BASE_DIR + "log/error.log.old"));
				logWriter = new PrintWriter(new FileWriter(logFile, true));
				logFile.setReadable(true, false);
				logFile.setWritable(true, false);
			} catch (IOException ignored) {}

			String date = DateFormat.getDateTimeInstance().format(new Date());
			determineXposedVersion();
			log("-----------------\n" + date + " UTC\n"
					+ "Loading Xposed v" + XPOSED_BRIDGE_VERSION
					+ " (for " + (startClassName == null ? "Zygote" : startClassName) + ")...");
			if (startClassName == null) {
				// Zygote
				log("Running ROM '" + Build.DISPLAY + "' with fingerprint '" + Build.FINGERPRINT + "'");
			}

			if (initNative()) {
				if (startClassName == null) {
					// Initializations for Zygote
					initXbridgeZygote();
				}

				loadModules(startClassName);
			} else {
				log("Errors during native Xposed initialization");
			}
		} catch (Throwable t) {
			log("Errors during Xposed initialization");
			log(t);
			disableHooks = true;
		}

		// call the original startup code
		if (startClassName == null)
			ZygoteInit.main(args);
		else
			RuntimeInit.main(args);
	}

	private static native String getStartClassName();

	private static void determineXposedVersion() throws IOException {
		ZipInputStream is = new ZipInputStream(new FileInputStream(BASE_DIR + "bin/XposedBridge.jar"));
		ZipEntry entry;
		try {
			while ((entry = is.getNextEntry()) != null) {
				if (!entry.getName().equals("assets/VERSION"))
					continue;

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String version = br.readLine();
				br.close();

				XPOSED_BRIDGE_VERSION = extractIntPart(version);
				if (XPOSED_BRIDGE_VERSION == 0)
					throw new RuntimeException("could not parse XposedBridge version from \"" + version + "\"");
				return;
			}
			throw new RuntimeException("could not find assets/VERSION in " + BASE_DIR + "bin/XposedBridge.jar");
		} finally {
			try {
				is.close();
			} catch (Exception e) { }
		}
	}

	private static int extractIntPart(String str) {
		int result = 0, length = str.length();
		for (int offset = 0; offset < length; offset++) {
			char c = str.charAt(offset);
			if ('0' <= c && c <= '9')
				result = result * 10 + (c - '0');
			else
				break;
		}
		return result;
	}

	/**
	 * Hook some methods which we want to create an easier interface for developers.
	 */
	private static void initXbridgeZygote() throws Throwable {
		final HashSet<String> loadedPackagesInProcess = new HashSet<String>(1);

		// normal process initialization (for new Activity, Service, BroadcastReceiver etc.)
		findAndHookMethod(ActivityThread.class, "handleBindApplication", "android.app.ActivityThread.AppBindData", new XC_MethodHook() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ActivityThread activityThread = (ActivityThread) param.thisObject;
				ApplicationInfo appInfo = (ApplicationInfo) getObjectField(param.args[0], "appInfo");
				ComponentName instrumentationName = (ComponentName) getObjectField(param.args[0], "instrumentationName");
				if (instrumentationName != null) {
					XposedBridge.log("Instrumentation detected, disabling framework for " + appInfo.packageName);
					disableHooks = true;
					return;
				}
				CompatibilityInfo compatInfo = (CompatibilityInfo) getObjectField(param.args[0], "compatInfo");
				if (appInfo.sourceDir == null)
					return;

				setObjectField(activityThread, "mBoundApplication", param.args[0]);
				loadedPackagesInProcess.add(appInfo.packageName);
				LoadedApk loadedApk = activityThread.getPackageInfoNoCheck(appInfo, compatInfo);
				XResources.setPackageNameForResDir(appInfo.packageName, loadedApk.getResDir());

				LoadPackageParam lpparam = new LoadPackageParam(sLoadedPackageCallbacks);
				lpparam.packageName = appInfo.packageName;
				lpparam.processName = (String) getObjectField(param.args[0], "processName");
				lpparam.classLoader = loadedApk.getClassLoader();
				lpparam.appInfo = appInfo;
				lpparam.isFirstApplication = true;
				XC_LoadPackage.callAll(lpparam);

				if (appInfo.packageName.equals(INSTALLER_PACKAGE_NAME))
					hookXposedInstaller(lpparam.classLoader);
			}
		});

		// system thread initialization
		findAndHookMethod("com.android.server.ServerThread", null,
				Build.VERSION.SDK_INT < 19 ? "run" : "initAndLoop", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				loadedPackagesInProcess.add("android");

				LoadPackageParam lpparam = new LoadPackageParam(sLoadedPackageCallbacks);
				lpparam.packageName = "android";
				lpparam.processName = "android"; // it's actually system_server, but other functions return this as well
				lpparam.classLoader = BOOTCLASSLOADER;
				lpparam.appInfo = null;
				lpparam.isFirstApplication = true;
				XC_LoadPackage.callAll(lpparam);
			}
		});

		// when a package is loaded for an existing process, trigger the callbacks as well
		hookAllConstructors(LoadedApk.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				LoadedApk loadedApk = (LoadedApk) param.thisObject;

				String packageName = loadedApk.getPackageName();
				XResources.setPackageNameForResDir(packageName, loadedApk.getResDir());
				if (packageName.equals("android") || !loadedPackagesInProcess.add(packageName))
					return;

				if ((Boolean) getBooleanField(loadedApk, "mIncludeCode") == false)
					return;

				LoadPackageParam lpparam = new LoadPackageParam(sLoadedPackageCallbacks);
				lpparam.packageName = packageName;
				lpparam.processName = AndroidAppHelper.currentProcessName();
				lpparam.classLoader = loadedApk.getClassLoader();
				lpparam.appInfo = loadedApk.getApplicationInfo();
				lpparam.isFirstApplication = false;
				XC_LoadPackage.callAll(lpparam);
			}
		});

		findAndHookMethod("android.app.ApplicationPackageManager", null, "getResourcesForApplication",
				ApplicationInfo.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ApplicationInfo app = (ApplicationInfo) param.args[0];
				XResources.setPackageNameForResDir(app.packageName,
					app.uid == Process.myUid() ? app.sourceDir : app.publicSourceDir);
			}
		});

		if (!new File(BASE_DIR + "conf/disable_resources").exists()) {
			hookResources();
		} else {
			disableResources = true;
		}
	}

	private static void hookResources() throws Throwable {
		/*
		 * getTopLevelResources(a)
		 *   -> getTopLevelResources(b)
		 *     -> key = new ResourcesKey()
		 *     -> r = new Resources()
		 *     -> mActiveResources.put(key, r)
		 *     -> return r
		 */

		final Class<?> classGTLR;
		final Class<?> classResKey;
		final ThreadLocal<Object> latestResKey = new ThreadLocal<Object>();

		if (Build.VERSION.SDK_INT <= 18) {
			classGTLR = ActivityThread.class;
			classResKey = Class.forName("android.app.ActivityThread$ResourcesKey");
		} else {
			classGTLR = Class.forName("android.app.ResourcesManager");
			classResKey = Class.forName("android.content.res.ResourcesKey");
		}

		hookAllConstructors(classResKey, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				latestResKey.set(param.thisObject);
			}
		});

		hookAllMethods(classGTLR, "getTopLevelResources", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				latestResKey.set(null);
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Object key = latestResKey.get();
				if (key == null)
					return;

				latestResKey.set(null);

				Object result = param.getResult();
				if (result == null || result instanceof XResources)
					return;

				// replace the returned resources with our subclass
				XResources newRes = (XResources) cloneToSubclass(result, XResources.class);
				String resDir = (String) getObjectField(key, "mResDir");
				newRes.initObject(resDir);

				@SuppressWarnings("unchecked")
				Map<Object, WeakReference<Resources>> mActiveResources =
						(Map<Object, WeakReference<Resources>>) getObjectField(param.thisObject, "mActiveResources");
				Object lockObject = (Build.VERSION.SDK_INT <= 18)
						? getObjectField(param.thisObject, "mPackages") : param.thisObject;

				synchronized (lockObject) {
					WeakReference<Resources> existing = mActiveResources.get(key);
					if (existing != null && existing.get() != null && existing.get().getAssets() != newRes.getAssets())
						existing.get().getAssets().close();
					mActiveResources.put(key, new WeakReference<Resources>(newRes));
				}

				// Invoke handleInitPackageResources()
				if (newRes.isFirstLoad()) {
					String packageName = newRes.getPackageName();
					InitPackageResourcesParam resparam = new InitPackageResourcesParam(sInitPackageResourcesCallbacks);
					resparam.packageName = packageName;
					resparam.res = newRes;
					XCallback.callAll(resparam);
				}

				param.setResult(newRes);
			}
		});

		// Replace TypedArrays with XTypedArrays
		XposedBridge.hookAllConstructors(TypedArray.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				TypedArray typedArray = (TypedArray) param.thisObject;
				Resources res = typedArray.getResources();
				if (res instanceof XResources) {
					setObjectClass(param.thisObject, XTypedArray.class);
					((XTypedArray) typedArray).initObject((XResources) res);
				}
			}
		});

		// Replace system resources
		XResources systemRes = (XResources) cloneToSubclass(Resources.getSystem(), XResources.class);
		systemRes.initObject(null);
		setStaticObjectField(Resources.class, "mSystem", systemRes);

		XResources.init(latestResKey);
	}

	private static void hookXposedInstaller(ClassLoader classLoader) {
		try {
			findAndHookMethod(INSTALLER_PACKAGE_NAME + ".XposedApp", classLoader, "getActiveXposedVersion",
				XC_MethodReplacement.returnConstant(XPOSED_BRIDGE_VERSION));
		} catch (Throwable t) { XposedBridge.log(t); }
	}

	/**
	 * Try to load all modules defined in <code>BASE_DIR/conf/modules.list</code>
	 */
	private static void loadModules(String startClassName) throws IOException {
		BufferedReader apks = new BufferedReader(new FileReader(BASE_DIR + "conf/modules.list"));
		String apk;
		while ((apk = apks.readLine()) != null) {
			loadModule(apk, startClassName);
		}
		apks.close();
	}

	/**
	 * Load a module from an APK by calling the init(String) method for all classes defined
	 * in <code>assets/xposed_init</code>.
	 */
	@SuppressWarnings("deprecation")
	private static void loadModule(String apk, String startClassName) {
		log("Loading modules from " + apk);

		if (!new File(apk).exists()) {
			log("  File does not exist");
			return;
		}

		ClassLoader mcl = new PathClassLoader(apk, BOOTCLASSLOADER);
		InputStream is = mcl.getResourceAsStream("assets/xposed_init");
		if (is == null) {
			log("assets/xposed_init not found in the APK");
			return;
		}

		BufferedReader moduleClassesReader = new BufferedReader(new InputStreamReader(is));
		try {
			String moduleClassName;
			while ((moduleClassName = moduleClassesReader.readLine()) != null) {
				moduleClassName = moduleClassName.trim();
				if (moduleClassName.isEmpty() || moduleClassName.startsWith("#"))
					continue;

				try {
					log ("  Loading class " + moduleClassName);
					Class<?> moduleClass = mcl.loadClass(moduleClassName);

					if (!IXposedMod.class.isAssignableFrom(moduleClass)) {
						log ("    This class doesn't implement any sub-interface of IXposedMod, skipping it");
						continue;
					} else if (disableResources && IXposedHookInitPackageResources.class.isAssignableFrom(moduleClass)) {
						log ("    This class requires resource-related hooks (which are disabled), skipping it.");
						continue;
					}

					// call the init(String) method of the module
					final Object moduleInstance = moduleClass.newInstance();
					if (startClassName == null) {
						if (moduleInstance instanceof IXposedHookZygoteInit) {
							IXposedHookZygoteInit.StartupParam param = new IXposedHookZygoteInit.StartupParam();
							param.modulePath = apk;
							((IXposedHookZygoteInit) moduleInstance).initZygote(param);
						}

						if (moduleInstance instanceof IXposedHookLoadPackage)
							hookLoadPackage(new IXposedHookLoadPackage.Wrapper((IXposedHookLoadPackage) moduleInstance));

						if (moduleInstance instanceof IXposedHookInitPackageResources)
							hookInitPackageResources(new IXposedHookInitPackageResources.Wrapper((IXposedHookInitPackageResources) moduleInstance));
					} else {
						if (moduleInstance instanceof IXposedHookCmdInit) {
							IXposedHookCmdInit.StartupParam param = new IXposedHookCmdInit.StartupParam();
							param.modulePath = apk;
							param.startClassName = startClassName;
							((IXposedHookCmdInit) moduleInstance).initCmdApp(param);
						}
					}
				} catch (Throwable t) {
					log(t);
				}
			}
		} catch (IOException e) {
			log(e);
		} finally {
			try {
				is.close();
			} catch (IOException ignored) {}
		}
	}

	/**
	 * Writes a message to the Xposed error log.
	 *
	 * <p>DON'T FLOOD THE LOG!!! This is only meant for error logging.
	 * If you want to write information/debug messages, use logcat.
	 *
	 * @param text The log message.
	 */
	public synchronized static void log(String text) {
		Log.i("Xposed", text);
		if (logWriter != null && logFile.length() < MAX_LOGFILE_SIZE_HARD) {
			logWriter.println(text);
			logWriter.flush();
		}
	}

        public static File getXposedErrorLogFile() {
		return logFile;
		}
	}
	/**
	 * Logs a stack trace to the Xposed error log.
	 *
	 * <p>DON'T FLOOD THE LOG!!! This is only meant for error logging.
	 * If you want to write information/debug messages, use logcat.
	 *
	 * @param t The Throwable object for the stack trace.
	 */
	public synchronized static void log(Throwable t) {
		Log.i("Xposed", Log.getStackTraceString(t));
		if (logWriter != null && logFile.length() < MAX_LOGFILE_SIZE_HARD) {
			t.printStackTrace(logWriter);
			logWriter.flush();
		}
	}

	/**
	 * Hook any method with the specified callback
	 *
	 * @param hookMethod The method to be hooked
	 * @param callback
	 */
	public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
		if (!(hookMethod instanceof Method) && !(hookMethod instanceof Constructor<?>)) {
			throw new IllegalArgumentException("Only methods and constructors can be hooked: " + hookMethod.toString());
		} else if (hookMethod.getDeclaringClass().isInterface()) {
			throw new IllegalArgumentException("Cannot hook interfaces: " + hookMethod.toString());
		} else if (Modifier.isAbstract(hookMethod.getModifiers())) {
			throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod.toString());
		}

		boolean newMethod = false;
		CopyOnWriteSortedSet<XC_MethodHook> callbacks;
		synchronized (sHookedMethodCallbacks) {
			callbacks = sHookedMethodCallbacks.get(hookMethod);
			if (callbacks == null) {
				callbacks = new CopyOnWriteSortedSet<XC_MethodHook>();
				sHookedMethodCallbacks.put(hookMethod, callbacks);
				newMethod = true;
			}
		}
		callbacks.add(callback);
		if (newMethod) {
			Class<?> declaringClass = hookMethod.getDeclaringClass();
			int slot = (int) getIntField(hookMethod, "slot");

			Class<?>[] parameterTypes;
			Class<?> returnType;
			if (hookMethod instanceof Method) {
				parameterTypes = ((Method) hookMethod).getParameterTypes();
				returnType = ((Method) hookMethod).getReturnType();
			} else {
				parameterTypes = ((Constructor<?>) hookMethod).getParameterTypes();
				returnType = null;
			}

			AdditionalHookInfo additionalInfo = new AdditionalHookInfo(callbacks, parameterTypes, returnType);
			hookMethodNative(hookMethod, declaringClass, slot, additionalInfo);
		}

		return callback.new Unhook(hookMethod);
	}

	/**
	 * Removes the callback for a hooked method
	 * @param hookMethod The method for which the callback should be removed
	 * @param callback The reference to the callback as specified in {@link #hookMethod}
	 */
	public static void unhookMethod(Member hookMethod, XC_MethodHook callback) {
		CopyOnWriteSortedSet<XC_MethodHook> callbacks;
		synchronized (sHookedMethodCallbacks) {
			callbacks = sHookedMethodCallbacks.get(hookMethod);
			if (callbacks == null)
				return;
		}
		callbacks.remove(callback);
	}

	public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
		Set<XC_MethodHook.Unhook> unhooks = new HashSet<XC_MethodHook.Unhook>();
		for (Member method : hookClass.getDeclaredMethods())
			if (method.getName().equals(methodName))
				unhooks.add(hookMethod(method, callback));
		return unhooks;
	}

	public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
		Set<XC_MethodHook.Unhook> unhooks = new HashSet<XC_MethodHook.Unhook>();
		for (Member constructor : hookClass.getDeclaredConstructors())
			unhooks.add(hookMethod(constructor, callback));
		return unhooks;
	}

	/**
	 * This method is called as a replacement for hooked methods.
	 */
	private static Object handleHookedMethod(Member method, int originalMethodId, Object additionalInfoObj,
			Object thisObject, Object[] args) throws Throwable {
		AdditionalHookInfo additionalInfo = (AdditionalHookInfo) additionalInfoObj;

		if (disableHooks) {
			try {
				return invokeOriginalMethodNative(method, originalMethodId, additionalInfo.parameterTypes,
						additionalInfo.returnType, thisObject, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		Object[] callbacksSnapshot = additionalInfo.callbacks.getSnapshot();
		final int callbacksLength = callbacksSnapshot.length;
		if (callbacksLength == 0) {
			try {
				return invokeOriginalMethodNative(method, originalMethodId, additionalInfo.parameterTypes,
						additionalInfo.returnType, thisObject, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		MethodHookParam param = new MethodHookParam();
		param.method = method;
		param.thisObject = thisObject;
		param.args = args;

		// call "before method" callbacks
		int beforeIdx = 0;
		do {
			try {
				((XC_MethodHook) callbacksSnapshot[beforeIdx]).beforeHookedMethod(param);
			} catch (Throwable t) {
				XposedBridge.log(t);

				// reset result (ignoring what the unexpectedly exiting callback did)
				param.setResult(null);
				param.returnEarly = false;
				continue;
			}

			if (param.returnEarly) {
				// skip remaining "before" callbacks and corresponding "after" callbacks
				beforeIdx++;
				break;
			}
		} while (++beforeIdx < callbacksLength);

		// call original method if not requested otherwise
		if (!param.returnEarly) {
			try {
				param.setResult(invokeOriginalMethodNative(method, originalMethodId,
						additionalInfo.parameterTypes, additionalInfo.returnType, param.thisObject, param.args));
			} catch (InvocationTargetException e) {
				param.setThrowable(e.getCause());
			}
		}

		// call "after method" callbacks
		int afterIdx = beforeIdx - 1;
		do {
			Object lastResult =  param.getResult();
			Throwable lastThrowable = param.getThrowable();

			try {
				((XC_MethodHook) callbacksSnapshot[afterIdx]).afterHookedMethod(param);
			} catch (Throwable t) {
				XposedBridge.log(t);

				// reset to last result (ignoring what the unexpectedly exiting callback did)
				if (lastThrowable == null)
					param.setResult(lastResult);
				else
					param.setThrowable(lastThrowable);
			}
		} while (--afterIdx >= 0);

		// return
		if (param.hasThrowable())
			throw param.getThrowable();
		else
			return param.getResult();
	}

	/**
	 * Get notified when a package is loaded. This is especially useful to hook some package-specific methods.
	 */
	public static XC_LoadPackage.Unhook hookLoadPackage(XC_LoadPackage callback) {
		synchronized (sLoadedPackageCallbacks) {
			sLoadedPackageCallbacks.add(callback);
		}
		return callback.new Unhook();
	}

	public static void unhookLoadPackage(XC_LoadPackage callback) {
		synchronized (sLoadedPackageCallbacks) {
			sLoadedPackageCallbacks.remove(callback);
		}
	}

	/**
	 * Get notified when the resources for a package are loaded. In callbacks, resource replacements can be created.
	 * @return
	 */
	public static XC_InitPackageResources.Unhook hookInitPackageResources(XC_InitPackageResources callback) {
		synchronized (sInitPackageResourcesCallbacks) {
			sInitPackageResourcesCallbacks.add(callback);
		}
		return callback.new Unhook();
	}

	public static void unhookInitPackageResources(XC_InitPackageResources callback) {
		synchronized (sInitPackageResourcesCallbacks) {
			sInitPackageResourcesCallbacks.remove(callback);
		}
	}

	private native static boolean initNative();

	/**
	 * Intercept every call to the specified method and call a handler function instead.
	 * @param method The method to intercept
	 */
	private native synchronized static void hookMethodNative(Member method, Class<?> declaringClass, int slot, Object additionalInfo);

	private native static Object invokeOriginalMethodNative(Member method, int methodId,
			Class<?>[] parameterTypes, Class<?> returnType, Object thisObject, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	/** Old method signature to avoid crashes if only XposedBridge.jar is updated, will be removed in the next version */
	@Deprecated
	private native synchronized static void hookMethodNative(Class<?> declaringClass, int slot);

	@Deprecated
	private native static Object invokeOriginalMethodNative(Member method, Class<?>[] parameterTypes, Class<?> returnType, Object thisObject, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	/**
	 * Basically the same as {@link Method#invoke}, but calls the original method
	 * as it was before the interception by Xposed. Also, access permissions are not checked.
	 *
	 * @param method Method to be called
	 * @param thisObject For non-static calls, the "this" pointer
	 * @param args Arguments for the method call as Object[] array
	 * @return The result returned from the invoked method
	 * @throws NullPointerException
	 *             if {@code receiver == null} for a non-static method
	 * @throws IllegalAccessException
	 *             if this method is not accessible (see {@link AccessibleObject})
	 * @throws IllegalArgumentException
	 *             if the number of arguments doesn't match the number of parameters, the receiver
	 *             is incompatible with the declaring class, or an argument could not be unboxed
	 *             or converted by a widening conversion to the corresponding parameter type
	 * @throws InvocationTargetException
	 *             if an exception was thrown by the invoked method

	 */
	public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args)
			throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (args == null) {
			args = EMPTY_ARRAY;
		}

		Class<?>[] parameterTypes;
		Class<?> returnType;
		if (method instanceof Method) {
			parameterTypes = ((Method) method).getParameterTypes();
			returnType = ((Method) method).getReturnType();
		} else if (method instanceof Constructor) {
			parameterTypes = ((Constructor<?>) method).getParameterTypes();
			returnType = null;
		} else {
			throw new IllegalArgumentException("method must be of type Method or Constructor");
		}

		return invokeOriginalMethodNative(method, 0, parameterTypes, returnType, thisObject, args);
	}

	/** Framework only, don't call this from your module! */
	private static void setObjectClass(Object obj, Class<?> clazz) {
		if (obj == null)
			return;

		/*
		 * Whitelist for classes we have prepared for this substitution in native code.
		 * These classes must be de-facto final, otherwise subclasses which are not loaded by the
		 * boot classloader will have gaps in their field offsets, which causes issues with code
		 * where DexOpt replaced field accesses with direct accesses byte offsets.
		 */
		if (clazz != XTypedArray.class)
			throw new IllegalArgumentException("Target class " + clazz + " is not allowed");

		if (obj.getClass() != clazz.getSuperclass())
			throw new IllegalArgumentException("Cannot transfer object from " + obj.getClass() + " to " + clazz);

		setObjectClassNative(obj, clazz);
	}

	private static native void setObjectClassNative(Object obj, Class<?> clazz);
	/*package*/ static native void dumpObjectNative(Object obj);

	/** Framework only, don't call this from your module! */
	private static Object cloneToSubclass(Object obj, Class<?> targetClazz) {
		if (obj == null)
			return null;

		if (!obj.getClass().isAssignableFrom(targetClazz))
			throw new ClassCastException(targetClazz + " doesn't extend " + obj.getClass());

		return cloneToSubclassNative(obj, targetClazz);
	}

	private static native Object cloneToSubclassNative(Object obj, Class<?> targetClazz);

	public static class CopyOnWriteSortedSet<E> {
		private transient volatile Object[] elements = EMPTY_ARRAY;

		public synchronized boolean add(E e) {
			int index = indexOf(e);
			if (index >= 0)
				return false;

			Object[] newElements = new Object[elements.length + 1];
			System.arraycopy(elements, 0, newElements, 0, elements.length);
			newElements[elements.length] = e;
			Arrays.sort(newElements);
			elements = newElements;
			return true;
		}

		public synchronized boolean remove(E e) {
			int index = indexOf(e);
			if (index == -1)
				return false;

			Object[] newElements = new Object[elements.length - 1];
			System.arraycopy(elements, 0, newElements, 0, index);
			System.arraycopy(elements, index + 1, newElements, index, elements.length - index - 1);
			elements = newElements;
			return true;
		}

		private int indexOf(Object o) {
			for (int i = 0; i < elements.length; i++) {
				if (o.equals(elements[i]))
					return i;
			}
			return -1;
		}

		public Object[] getSnapshot() {
			return elements;
		}
	}

	private static class AdditionalHookInfo {
		final CopyOnWriteSortedSet<XC_MethodHook> callbacks;
		final Class<?>[] parameterTypes;
		final Class<?> returnType;

		private AdditionalHookInfo(CopyOnWriteSortedSet<XC_MethodHook> callbacks, Class<?>[] parameterTypes, Class<?> returnType) {
			this.callbacks = callbacks;
			this.parameterTypes = parameterTypes;
			this.returnType = returnType;
		}
	}
}
