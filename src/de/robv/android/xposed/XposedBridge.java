package de.robv.android.xposed;

import static de.robv.android.xposed.XposedHelpers.getNativeLibraryMemoryRange;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getProcessPid;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import android.app.ActivityThread;
import android.app.AndroidAppHelper;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.content.res.XResources;
import android.util.Log;

import com.android.internal.os.RuntimeInit;
import com.android.internal.os.ZygoteInit;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.callbacks.InitPackageResourcesXCallback;
import de.robv.android.xposed.callbacks.InitPackageResourcesXCallback.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.LoadPackageXCallback;
import de.robv.android.xposed.callbacks.LoadPackageXCallback.LoadPackageParam;
import de.robv.android.xposed.callbacks.MethodHookXCallback;
import de.robv.android.xposed.callbacks.MethodHookXCallback.MethodHookParam;
import de.robv.android.xposed.callbacks.XCallback;

public final class XposedBridge {
	public static boolean DEBUG = false;
	
	private static PrintWriter logWriter = null;
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
	private static final ClassLoader BOOTCLASSLOADER = XposedBridge.class.getClassLoader();
	
	// built-in handlers
	private static final Map<Member, TreeSet<MethodHookXCallback>> hookedMethodCallbacks
									= new HashMap<Member, TreeSet<MethodHookXCallback>>();
	private static MethodHookXCallback ORIGINAL_METHOD_CALLBACK = new MethodHookXCallback(XCallback.PRIORITY_LOWEST*2) {
		@Override
		public Object handleHookedMethod(MethodHookParam param) throws Throwable {
			try {
				return invokeOriginalMethod(param.method, param.thisObject, param.args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		};
	};

	private static final TreeSet<LoadPackageXCallback> loadedPackageCallbacks = new TreeSet<LoadPackageXCallback>();
	private static final TreeSet<InitPackageResourcesXCallback> initPackageResourcesCallbacks = new TreeSet<InitPackageResourcesXCallback>();
	
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
				logWriter = new PrintWriter(new FileWriter(logFile, true));
				logFile.setReadable(true, false);
				logFile.setWritable(true, false);
			} catch (IOException ignored) {}
			
			log("-----------------\nLoading Xposed...");
			if (startClassName == null) {
				// Initializations for Zygote
				log("Loading some internal stuff");
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
		// Built-in handler for the LoadedApk.makeApplication method that allows handlers to be registered
		// for the first initialization of a package via {@link #hookLoadPackage}.
		Method makeApplication = LoadedApk.class.getDeclaredMethod("makeApplication", boolean.class, Instrumentation.class);
		hookMethod(makeApplication, new MethodHookXCallback() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				LoadedApk loadedApk = (LoadedApk) param.thisObject;
				boolean firstLoad = (getObjectField(loadedApk, "mApplication") == null);
				if (firstLoad) {
					LoadPackageParam lpparam = new LoadPackageParam(loadedPackageCallbacks);
					lpparam.packageName = loadedApk.getPackageName();
					lpparam.classLoader = loadedApk.getClassLoader();
					LoadPackageXCallback.callAll(lpparam);
				}
			}
		});
		
		Method getTopLevelResources = ActivityThread.class.getDeclaredMethod("getTopLevelResources", String.class, CompatibilityInfo.class);
		hookMethod(getTopLevelResources, callbackGetTopLevelResources);
		
		// Make Xposed classes available to other applications, so they can use the same logging and helper
		Class<?> classApplicationLoaders = Class.forName("android.app.ApplicationLoaders", false, BOOTCLASSLOADER);
		Method getClassLoader = classApplicationLoaders.getDeclaredMethod("getClassLoader", String.class, String.class, ClassLoader.class);
		hookMethod(getClassLoader, new MethodHookXCallback() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.args[0] = "/data/xposed/XposedBridge.jar:" + param.args[0];
			}
		});

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
			log("File does not exist");
			return;
		}
		
		ClassLoader xbcl = XposedBridge.class.getClassLoader();
		ClassLoader mcl = new PathClassLoader(apk, xbcl);
		
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
					
					// set the static field MODULE_PATH to the path of the module's APK if found
					try {
						Field modulePath = moduleClass.getDeclaredField("MODULE_PATH");
						modulePath.setAccessible(true);
						modulePath.set(null, apk);
					} catch (Throwable ignored) {};
					
					// call the init(String) method of the module
					Method moduleInit = moduleClass.getDeclaredMethod("init", String.class);
					moduleInit.invoke(null, startClassName);
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
	
	private static void onZygoteInit() throws Exception {
		if (DEBUG)
			log("onZygoteInit");
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
	public static void hookMethod(Member hookMethod, MethodHookXCallback callback) {
		if (!(hookMethod instanceof Method) && !(hookMethod instanceof Constructor<?>)) {
			throw new IllegalArgumentException("only methods and constructors can be hooked");
		}
		
		TreeSet<MethodHookXCallback> callbacks;
		synchronized (hookedMethodCallbacks) {
			callbacks = hookedMethodCallbacks.get(hookMethod);
			if (callbacks == null) {
				callbacks = new TreeSet<MethodHookXCallback>();
				// add the original method always as the last one to be called
				callbacks.add(ORIGINAL_METHOD_CALLBACK);
				hookedMethodCallbacks.put(hookMethod, callbacks);
			}
		}
		synchronized (callbacks) {
			callbacks.add(callback);
		}
		hookMethodNative(hookMethod);
	}
	
	public static void hookAllMethods(Class<?> hookClass, String methodName, MethodHookXCallback callback) {
		for (Member method : hookClass.getDeclaredMethods())
			if (method.getName().equals(methodName))
				hookMethod(method, callback);
	}
	
	public static void hookAllConstructors(Class<?> hookClass, MethodHookXCallback callback) {
		for (Member constructor : hookClass.getDeclaredConstructors())
			hookMethod(constructor, callback);
	}
	
	/**
	 * This method is called as a replacement for hooked methods.
	 * Use {@link #invokeOriginalMethod} to call the method that was hooked.
	 */
	private static Object handleHookedMethod(Member method, Object thisObject, Object[] args) throws Throwable {
		TreeSet<MethodHookXCallback> callbacks;
		synchronized (hookedMethodCallbacks) {
			callbacks = hookedMethodCallbacks.get(method);
		}
		if (callbacks == null) {
			try {
				return invokeOriginalMethod(method, thisObject, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
		
		MethodHookParam param = new MethodHookParam(callbacks);
		param.method  = method;
		param.thisObject = thisObject;
		param.args = args;
		return param.first().handleHookedMethod(param);
	}

	/**
	 * Get notified when a package is loaded. This is especially useful to hook some package-specific methods.
	 */
	public static void hookLoadPackage(LoadPackageXCallback callback) {
		synchronized (loadedPackageCallbacks) {
			loadedPackageCallbacks.add(callback);
		}
	}
	
	/**
	 * Get notified when the resources for a package are loaded. In callbacks, resource replacements can be created.
	 */
	public static void hookInitPackageResources(InitPackageResourcesXCallback callback) {		
		synchronized (initPackageResourcesCallbacks) {
			initPackageResourcesCallbacks.add(callback);
		}
	}
	
	
	/**
	 * Called when the resources for a specific package are requested and instead returns an instance of {@link XResources}.
	 */
	private static MethodHookXCallback callbackGetTopLevelResources = new MethodHookXCallback(XCallback.PRIORITY_HIGHEST - 10) {
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			XResources newRes = null;
			if (param.result instanceof XResources) {
				newRes = (XResources) param.result;
				
			} else if (param.result != null) {
				// replace the returned resources with our subclass
				ActivityThread thisActivityThread = (ActivityThread) param.thisObject;
				Resources origRes = (Resources) param.result;
				String resDir = (String) param.args[0];
				CompatibilityInfo compInfo = (CompatibilityInfo) param.args[1];
				
				newRes = new XResources(origRes, resDir);
				param.result = newRes;
				
				Map<Object, WeakReference<Resources>> mActiveResources =
						(Map<Object, WeakReference<Resources>>) AndroidAppHelper.getActivityThread_mActiveResources(thisActivityThread);
				Object mPackages = AndroidAppHelper.getActivityThread_mPackages(thisActivityThread);
				
				Object key = AndroidAppHelper.createResourcesKey(resDir, compInfo);
				synchronized (mPackages) {
					WeakReference<Resources> existing = mActiveResources.get(key);
					if (existing != null && existing.get().getAssets() != newRes.getAssets())
						existing.get().getAssets().close();
					mActiveResources.put(key, new WeakReference<Resources>(newRes));
				}
				
				newRes.setInited(resDir == null || !newRes.checkFirstLoad());
				
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
	private native synchronized static void hookMethodNative(Member method);
	
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

	/**
	 * Patch a native library in the current process.
	 * @param libraryPath The path to the library.
	 * @param patch A patch created by bsdiff.
	 * @return <code>true</code> if the file was successfully patched.
	 */
	public static boolean patchNativeLibrary(String libraryPath, byte[] patch) {
		long[] memRange = getNativeLibraryMemoryRange("self", libraryPath);
		if (memRange == null)
			return false;
		
		return patchNativeLibrary(libraryPath, patch, 0, memRange[0], memRange[1] - memRange[0]);
	}
	
	/**
	 * Patch a native library in a foreign process.
	 * @param libraryPath The path to the library.
	 * @param patch A patch created by bsdiff.
	 * @param process The name of the process to be patched (see {@link XposedHelpers#getProcessPid}).
	 * @return <code>true</code> if the file was successfully patched.
	 */
	public static boolean patchNativeLibrary(String libraryPath, byte[] patch, String process) {
		String pid = getProcessPid(process);
		if (pid == null)
			return false;
		
		long[] memRange = getNativeLibraryMemoryRange(pid, libraryPath);
		if (memRange == null)
			return false;
		
		return patchNativeLibrary(libraryPath, patch, Integer.parseInt(pid), memRange[0], memRange[1] - memRange[0]);
	}
	
	private static native boolean patchNativeLibrary(String libraryPath, byte[] patch, int pid, long base, long size);
}
