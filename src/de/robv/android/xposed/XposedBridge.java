package de.robv.android.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import java.io.BufferedReader;
import java.io.File;
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
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.ActivityThread;
import android.app.AndroidAppHelper;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XResources;
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
	private static PrintWriter logWriter = null;
	// log for initialization of a few mods is about 500 bytes, so 2*20 kB (2*~350 lines) should be enough
	private static final int MAX_LOGFILE_SIZE = 20*1024; 
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
	public static final ClassLoader BOOTCLASSLOADER = ClassLoader.getSystemClassLoader();
	
	// built-in handlers
	private static final Map<Member, TreeSet<XC_MethodHook>> hookedMethodCallbacks
									= new HashMap<Member, TreeSet<XC_MethodHook>>();
	private static final TreeSet<XC_LoadPackage> loadedPackageCallbacks = new TreeSet<XC_LoadPackage>();
	private static final TreeSet<XC_InitPackageResources> initPackageResourcesCallbacks = new TreeSet<XC_InitPackageResources>();
	
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
				File logFile = new File("/data/xposed/debug.log");
				if (startClassName == null && logFile.length() > MAX_LOGFILE_SIZE)
					logFile.renameTo(new File("/data/xposed/debug.log.old"));
				logWriter = new PrintWriter(new FileWriter(logFile, true));
				logFile.setReadable(true, false);
				logFile.setWritable(true, false);
			} catch (IOException ignored) {}
			
			String date = DateFormat.getDateTimeInstance().format(new Date());
			log("-----------------\n" + date + " UTC\n"
					+ "Loading Xposed (for " + (startClassName == null ? "Zygote" : startClassName) + ")...");
			
			if (startClassName == null) {
				// Initializations for Zygote
				initXbridgeZygote();
			}
			
			loadModules(startClassName);			
		} catch (Throwable t) {
			log("Errors during Xposed initialization");
			log(t);
		}
		
		// call the original startup code
		if (startClassName == null)
			ZygoteInit.main(args);
		else
			RuntimeInit.main(args);
	}
	
	private static native String getStartClassName();
	
	/**
	 * Hook some methods which we want to create an easier interface for developers.
	 */
	private static void initXbridgeZygote() throws Exception {
		final HashSet<String> loadedPackagesInProcess = new HashSet<String>(1);
		
		// normal process initialization (for new Activity, Service, BroadcastReceiver etc.) 
		findAndHookMethod(ActivityThread.class, "handleBindApplication", "android.app.ActivityThread.AppBindData", new XC_MethodHook() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ActivityThread activityThread = (ActivityThread) param.thisObject;
				ApplicationInfo appInfo = (ApplicationInfo) getObjectField(param.args[0], "appInfo");
				CompatibilityInfo compatInfo = (CompatibilityInfo) getObjectField(param.args[0], "compatInfo");
				if (appInfo.sourceDir == null)
					return;
				
				setObjectField(activityThread, "mBoundApplication", param.args[0]);
				loadedPackagesInProcess.add(appInfo.packageName);
				LoadedApk loadedApk = activityThread.getPackageInfoNoCheck(appInfo, compatInfo);
				XResources.setPackageNameForResDir(appInfo.packageName, loadedApk.getResDir());
				
				LoadPackageParam lpparam = new LoadPackageParam(loadedPackageCallbacks);
				lpparam.packageName = appInfo.packageName;
				lpparam.processName = (String) getObjectField(param.args[0], "processName");
				lpparam.classLoader = loadedApk.getClassLoader();
				lpparam.appInfo = appInfo;
				lpparam.isFirstApplication = true;
				XC_LoadPackage.callAll(lpparam);
			}
		});
		
		// system thread initialization
		findAndHookMethod("com.android.server.ServerThread", null, "run", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				loadedPackagesInProcess.add("android");
				
				LoadPackageParam lpparam = new LoadPackageParam(loadedPackageCallbacks);
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
				
				LoadPackageParam lpparam = new LoadPackageParam(loadedPackageCallbacks);
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
		
		if (Build.VERSION.SDK_INT <= 16)
			findAndHookMethod(ActivityThread.class, "getTopLevelResources",
					String.class, CompatibilityInfo.class,
					callbackGetTopLevelResources);
		else
			findAndHookMethod(ActivityThread.class, "getTopLevelResources",
					String.class, int.class, Configuration.class, CompatibilityInfo.class,
					callbackGetTopLevelResources);
		
		// Replace system resources
		Resources systemResources = new XResources(Resources.getSystem(), null);
		setStaticObjectField(Resources.class, "mSystem", systemResources);
		
		XResources.init();
	}
	
	/**
	 * Try to load all modules defined in <code>/data/xposed/modules.list</code>
	 */
	private static void loadModules(String startClassName) throws IOException {
		BufferedReader apks = new BufferedReader(new FileReader("/data/xposed/modules.list"));
		String apk;
		while ((apk = apks.readLine()) != null) {
			loadModule(apk, startClassName);
		}
		apks.close();
	}
	
	/**
	 * Load a module from an APK by calling the init(String) method for all classes defined
	 * in <code>assets/xposed_init</code>.
	 * @see MethodSignatureGuide#init
	 */
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
	 * Writes a message to /data/xposed/debug.log (needs to have chmod 777)
	 * @param text log message
	 */
	public synchronized static void log(String text) {
		Log.i("Xposed", text);
		if (logWriter != null) {
			logWriter.println(text);
			logWriter.flush();
		}
	}
	
	/**
	 * Log the stack trace
	 * @param t The Throwable object for the stacktrace
	 * @see XposedBridge#log(String)
	 */
	public synchronized static void log(Throwable t) {
		Log.i("Xposed", Log.getStackTraceString(t));
		if (logWriter != null) {
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
			throw new IllegalArgumentException("only methods and constructors can be hooked");
		}
		
		boolean newMethod = false;
		TreeSet<XC_MethodHook> callbacks;
		synchronized (hookedMethodCallbacks) {
			callbacks = hookedMethodCallbacks.get(hookMethod);
			if (callbacks == null) {
				callbacks = new TreeSet<XC_MethodHook>();
				hookedMethodCallbacks.put(hookMethod, callbacks);
				newMethod = true;
			}
		}
		synchronized (callbacks) {
			callbacks.add(callback);
		}
		if (newMethod) {
			Class<?> declaringClass = hookMethod.getDeclaringClass();
			int slot = (int) getIntField(hookMethod, "slot");
			hookMethodNative(declaringClass, slot);
		}
		
		return callback.new Unhook(hookMethod);
	}
	
	/** 
	 * Removes the callback for a hooked method
	 * @param hookMethod The method for which the callback should be removed
	 * @param callback The reference to the callback as specified in {@link #hookMethod}
	 */
	public static void unhookMethod(Member hookMethod, XC_MethodHook callback) {
		TreeSet<XC_MethodHook> callbacks;
		synchronized (hookedMethodCallbacks) {
			callbacks = hookedMethodCallbacks.get(hookMethod);
			if (callbacks == null)
				return;
		}	
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
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
	@SuppressWarnings("unchecked")
	private static Object handleHookedMethod(Member method, Object thisObject, Object[] args) throws Throwable {
		TreeSet<XC_MethodHook> callbacks;
		synchronized (hookedMethodCallbacks) {
			callbacks = hookedMethodCallbacks.get(method);
		}
		if (callbacks == null || callbacks.isEmpty()) {
			try {
				return invokeOriginalMethod(method, thisObject, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
		synchronized (callbacks) {
			callbacks = ((TreeSet<XC_MethodHook>) callbacks.clone());
		}
		
		MethodHookParam param = new MethodHookParam();
		param.method  = method;
		param.thisObject = thisObject;
		param.args = args;
		
		Iterator<XC_MethodHook> before = callbacks.iterator();
		Iterator<XC_MethodHook> after  = callbacks.descendingIterator();
		
		// call "before method" callbacks
		while (before.hasNext()) {
			try {
				before.next().beforeHookedMethod(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
				
				// reset result (ignoring what the unexpectedly exiting callback did)
				param.setResult(null);
				param.returnEarly = false;
				continue;
			}
			
	        if (param.returnEarly) {
	        	// skip remaining "before" callbacks and corresponding "after" callbacks
	        	while (before.hasNext() && after.hasNext()) {
	        		before.next();
	        		after.next();
	        	}
	        	break;
	        }
		}
		
		// call original method if not requested otherwise
		if (!param.returnEarly) {
			try {
				param.setResult(invokeOriginalMethod(method, param.thisObject, param.args));
			} catch (InvocationTargetException e) {
				param.setThrowable(e.getCause());
			}
		}
		
		// call "after method" callbacks
		while (after.hasNext()) {
			Object lastResult =  param.getResult();
			Throwable lastThrowable = param.getThrowable();
			
			try {
				after.next().afterHookedMethod(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
				
				// reset to last result (ignoring what the unexpectedly exiting callback did)
				if (lastThrowable == null)
					param.setResult(lastResult);
				else
					param.setThrowable(lastThrowable);
			}
		}
		
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
		synchronized (loadedPackageCallbacks) {
			loadedPackageCallbacks.add(callback);
		}
		return callback.new Unhook();
	}
	
	public static void unhookLoadPackage(XC_LoadPackage callback) {		
		synchronized (loadedPackageCallbacks) {
			loadedPackageCallbacks.remove(callback);
		}
	}
	
	/**
	 * Get notified when the resources for a package are loaded. In callbacks, resource replacements can be created.
	 * @return 
	 */
	public static XC_InitPackageResources.Unhook hookInitPackageResources(XC_InitPackageResources callback) {		
		synchronized (initPackageResourcesCallbacks) {
			initPackageResourcesCallbacks.add(callback);
		}
		return callback.new Unhook();
	}
	
	public static void unhookInitPackageResources(XC_InitPackageResources callback) {		
		synchronized (initPackageResourcesCallbacks) {
			initPackageResourcesCallbacks.remove(callback);
		}
	}
	
	
	/**
	 * Called when the resources for a specific package are requested and instead returns an instance of {@link XResources}.
	 */
	private static XC_MethodHook callbackGetTopLevelResources = new XC_MethodHook(XCallback.PRIORITY_HIGHEST - 10) {
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			XResources newRes = null;
			final Object result = param.getResult();
			if (result instanceof XResources) {
				newRes = (XResources) result;
				
			} else if (result != null) {
				// replace the returned resources with our subclass
				ActivityThread thisActivityThread = (ActivityThread) param.thisObject;
				Resources origRes = (Resources) result;
				String resDir = (String) param.args[0];
				CompatibilityInfo compInfo = (CompatibilityInfo)
						((Build.VERSION.SDK_INT <= 16) ? param.args[1] : param.args[3]);
				
				newRes = new XResources(origRes, resDir);
				
				Map<Object, WeakReference<Resources>> mActiveResources =
						(Map<Object, WeakReference<Resources>>) AndroidAppHelper.getActivityThread_mActiveResources(thisActivityThread);
				Object mPackages = AndroidAppHelper.getActivityThread_mPackages(thisActivityThread);
				
				Object key = (Build.VERSION.SDK_INT <= 16)
					? AndroidAppHelper.createResourcesKey(resDir, compInfo)
					: AndroidAppHelper.createResourcesKey(resDir, (Integer) param.args[1], (Configuration) param.args[2], compInfo);
				
				synchronized (mPackages) {
					WeakReference<Resources> existing = mActiveResources.get(key);
					if (existing != null && existing.get() != null && existing.get().getAssets() != newRes.getAssets())
						existing.get().getAssets().close();
					mActiveResources.put(key, new WeakReference<Resources>(newRes));
				}
				
				newRes.setInited(resDir == null || !newRes.checkFirstLoad());
				param.setResult(newRes);
				
			} else {
				return;
			}

			if (!newRes.isInited()) {
				String packageName = newRes.getPackageName();
				if (packageName != null) {
					InitPackageResourcesParam resparam = new InitPackageResourcesParam(initPackageResourcesCallbacks);
					resparam.packageName = packageName;
					resparam.res = newRes;
					XCallback.callAll(resparam);
					newRes.setInited(true);
				}
			}
		}
	};
	

	/**
	 * Intercept every call to the specified method and call a handler function instead.
	 * @param method The method to intercept
	 */
	private native synchronized static void hookMethodNative(Class<?> declaringClass, int slot);
	
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
        	
		return invokeOriginalMethodNative(method, parameterTypes, returnType, thisObject, args);
	}
}
