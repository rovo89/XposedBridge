package de.robv.android.xposed;

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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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

public final class XposedBridge {
	public static boolean DEBUG = false;
	
	private static PrintWriter logWriter = null;
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	// built-in handlers
	private static final Map<Member, SortedSet<Callback>> hookedMethodCallbacks = new HashMap<Member, SortedSet<Callback>>();
	private static final Class<?>[] HOOK_METHOD_CALLBACK_PARAMS_METHOD = new Class[] { Iterator.class, Method.class, Object.class, Object[].class };
	private static final Class<?>[] HOOK_METHOD_CALLBACK_PARAMS_CONSTRUCTOR = new Class[] { Iterator.class, Constructor.class, Object.class, Object[].class };
	private static final Class<?>[] HOOK_METHOD_CALLBACK_PARAMS_GENERIC = new Class[] { Iterator.class, Member.class, Object.class, Object[].class };

	private static final SortedSet<Callback> loadedPackageCallbacks = new TreeSet<Callback>();
	private static final Class<?>[] LOADED_PACKAGE_CALLBACK_PARAMS = new Class[] { String.class, ClassLoader.class };
	
	private static final SortedSet<Callback> initPackageResourcesCallbacks = new TreeSet<Callback>();
	private static final Class<?>[] INIT_PACKAGE_RESOURCES_CALLBACK_PARAMS = new Class[] { String.class, XResources.class };
	
	// cached methods
	private static Field field_LoadedApk_mApplication;
	
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
				if (DEBUG)
					DebugHandlers.init();
			}
				
			loadModules(startClassName);			
		} catch (Throwable t) {
			log("Errors during Xposed initialization");
			log(t);
		}
		
		// call the original startup code
		try {
			Class<?> initClass = (startClassName == null) ? ZygoteInit.class : RuntimeInit.class;
			Method initMain = initClass.getDeclaredMethod("main", String[].class);
			initMain.invoke(null, (Object) args);
		} catch (Throwable t) {
			log("Errors while calling original startup class");
			log(t);
		}
	}
	
	private static native String getStartClassName();
	
	/**
	 * Hook some methods which we want to create an easier interface for developers.
	 */
	private static void initXbridgeZygote() throws Exception {
		Method makeApplication = LoadedApk.class.getDeclaredMethod("makeApplication", boolean.class, Instrumentation.class);
		hookMethod(makeApplication, XposedBridge.class, "handleMakeApplication", Callback.PRIORITY_DEFAULT);
		
		Method getTopLevelResources = ActivityThread.class.getDeclaredMethod("getTopLevelResources", String.class, CompatibilityInfo.class);
		hookMethod(getTopLevelResources, XposedBridge.class, "handleGetTopLevelResources", Callback.PRIORITY_HIGHEST - 10);

		// Replace system resources 
		Field systemResources = Resources.class.getDeclaredField("mSystem");
		systemResources.setAccessible(true);
		systemResources.set(null, new XResources(Resources.getSystem(), null));

		// Get references to some methods and fields and make them accessible for later use
		field_LoadedApk_mApplication = LoadedApk.class.getDeclaredField("mApplication");
		
		
		AccessibleObject.setAccessible(new AccessibleObject[] {
			field_LoadedApk_mApplication,
		}, true);
		
		XResources.init();
	}
	
	/**
	 * Try to load all modules defined in /data/xposed/modules.list
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
	 * in assets/xposed_init.
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
	 * Call the next handler in the list and return the result
	 * @param iterator Handler iterator
	 * @param args arguments for the call (vary from hook to hook)
	 * @return The result of the call (if any; null if it was the last one)
	 * @throws Throwable The error thrown by the called method
	 */
	public static Object callNext(Iterator<Callback> iterator, Object ... args) throws Throwable {
		if (!iterator.hasNext())
			return null;
		
		Callback c = iterator.next();
		try {
			Object[] newArgs = new Object[args.length + 1];
			newArgs[0] = iterator;
			System.arraycopy(args, 0, newArgs, 1, args.length);
			return c.method.invoke(null, newArgs);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	/**
	 * Invokes all callbacks with the specified parameters.
	 * In contrast to {@link #callNext}, this makes only sense for methods without return value
	 * Any exceptions are caught, logged and ignored.
	 * 
	 * @param callbacks The set with the callbacks
	 * @param args arguments for the call (vary from hook to hook)
	 */
	public static void callAll(Set<Callback> callbacks, Object ... args) {
		Iterator<Callback> iterator = callbacks.iterator();
		while (iterator.hasNext()) {
			try {
				iterator.next().method.invoke(null, args);
			} catch (InvocationTargetException e) {
				log(e.getCause());
			} catch (Throwable t) {
				log(t);
			}
		}
	}
	
	private static void ensureMethodIsStatic(Method method) throws NoSuchMethodException {
		if (!Modifier.isStatic(method.getModifiers()))
			throw new NoSuchMethodException("Callback methods have to be static");
	}
		
	
	/**
	 * Hook any method and call the specified handler method.<br/>
	 * The handler methods has to have the signature described in {@link MethodSignatureGuide#handleHookedMethod}.
	 * 
	 * @param hookMethod The method to be hooked
	 * @param handlerClass Class of the handler
	 * @param handlerMethodName Method of the handler
	 * @param priority The higher the priority, the earlier this handler called.
	 * @throws NoSuchMethodException The handler method was not found
	 */
	public static void hookMethod(Method hookMethod, Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {
		hookMethod((Member) hookMethod, handlerClass, handlerMethodName, priority);
	}
	
	/** @see #hookMethod(Method, Class, String, int) */
	public static void hookMethod(Constructor<?> hookMethod, Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {
		hookMethod((Member) hookMethod, handlerClass, handlerMethodName, priority);
	}
	
	/** @see #hookMethod(Method, Class, String, int) */
	private synchronized static void hookMethod(Member hookMethod, Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {
		Callback c;
		try {
			c = new Callback(handlerClass, handlerMethodName, priority, HOOK_METHOD_CALLBACK_PARAMS_METHOD);
		} catch (NoSuchMethodException e) {
			try {
				c = new Callback(handlerClass, handlerMethodName, priority, HOOK_METHOD_CALLBACK_PARAMS_CONSTRUCTOR);
			} catch (NoSuchMethodException e1) {
				try {
					c = new Callback(handlerClass, handlerMethodName, priority, HOOK_METHOD_CALLBACK_PARAMS_GENERIC);
				} catch (NoSuchMethodException e2) {
					throw e2;
				}
			}
		}
		ensureMethodIsStatic(c.method);
		if (hookMethod instanceof Method && c.method.getReturnType().equals(void.class))
			throw new NoSuchMethodException("Handler method must have a return type (not void)");

		
		SortedSet<Callback> callbacks = hookedMethodCallbacks.get(hookMethod);
		if (callbacks == null) {
			callbacks = new TreeSet<Callback>();
			// add the original method always as the last one to be called
			callbacks.add(new Callback(XposedBridge.class, "invokeOriginalMethodCallback", Callback.PRIORITY_LOWEST, HOOK_METHOD_CALLBACK_PARAMS_GENERIC));
			hookedMethodCallbacks.put(hookMethod, callbacks);
		}
		c.method.setAccessible(true);
		callbacks.add(c);
		hookMethodNative(hookMethod);
	}
	
	/**
	 * This method is called as a replacement for hooked methods.
	 * Use {@link #invokeOriginalMethod} to call the method that was hooked.
	 * The arguments are basically the same that are passed on to {@link MethodSignatureGuide#handleHookedMethod}.
	 */
	private static Object handleHookedMethod(Member method, Object thisObject, Object[] args) throws Throwable {
		SortedSet<Callback> callbacks = hookedMethodCallbacks.get(method);
		if (callbacks == null) {
			try {
				return invokeOriginalMethod(method, thisObject, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
		return callNext(callbacks.iterator(), method, thisObject, args);		
	}

	/**
	 * Get notified when a package is loaded. This is especially useful to hook some package-specific methods.<br/>
	 * The handler methods has to have the signature described in {@link MethodSignatureGuide#handleLoadPackage}.
	 */
	public synchronized static void hookLoadPackage(Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {		
		Callback c = new Callback(handlerClass, handlerMethodName, priority, LOADED_PACKAGE_CALLBACK_PARAMS);
		ensureMethodIsStatic(c.method);
		c.method.setAccessible(true);
		loadedPackageCallbacks.add(c);
	}
	
	/**
	 * Built-in handler for the LoadedApk.makeApplication method that allows handlers to be registered
	 * for the first initialization of a package via {@link #hookLoadPackage}.
	 */
	private static Object handleMakeApplication(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		try {
			LoadedApk loadedApk = (LoadedApk) thisObject;
			boolean firstLoad = (field_LoadedApk_mApplication.get(loadedApk) == null);
			if (firstLoad) {
				String packageName = loadedApk.getPackageName();
				XResources.setPackageNameForResDir(loadedApk.getResDir(), packageName);
				callAll(loadedPackageCallbacks, packageName, loadedApk.getClassLoader());
			}
		} catch (Exception e) {
			log(e);
		}
		return callNext(iterator, method, thisObject, args);
	}
	
	/**
	 * Get notified when the resources for a package are loaded. In callbacks, resource replacements can be created.<br/>
	 * The handler methods has to have the signature described in {@link MethodSignatureGuide#handleInitPackageResources}.
	 */
	public synchronized static void hookInitPackageResources(Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {		
		Callback c = new Callback(handlerClass, handlerMethodName, priority, INIT_PACKAGE_RESOURCES_CALLBACK_PARAMS);
		ensureMethodIsStatic(c.method);
		c.method.setAccessible(true);
		initPackageResourcesCallbacks.add(c);
	}
	
	
	/**
	 * Called when the resources for a specific package are requested and instead returns an instance of {@link XResources}.
	 */
	private static Object handleGetTopLevelResources(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		Object result = callNext(iterator, method, thisObject, args);
		try {
			XResources newRes;
			if (result instanceof XResources) {
				newRes = (XResources) result;
			} else {
				// replace the returned resources with our subclass
				ActivityThread thisActivityThread = (ActivityThread) thisObject;
				Resources origRes = (Resources) result;
				String resDir = (String) args[0];
				CompatibilityInfo compInfo = (CompatibilityInfo) args[1];
				
				newRes = new XResources(origRes, resDir);
				
				Map<Object, WeakReference<Resources>> mActiveResources =
						(Map<Object, WeakReference<Resources>>) AndroidAppHelper.getActivityThread_mActiveResources(thisActivityThread);
				Object mPackages = AndroidAppHelper.getActivityThread_mPackages(thisActivityThread);
				
				Object key = AndroidAppHelper.createResourcesKey(resDir, compInfo.applicationScale);
				synchronized (mPackages) {
					WeakReference<Resources> existing = mActiveResources.get(key);
					if (existing != null && existing.get().getAssets() != newRes.getAssets())
						existing.get().getAssets().close();
					mActiveResources.put(key, new WeakReference<Resources>(newRes));
				}
				
				newRes.setInited(resDir == null || !newRes.checkFirstLoad());
			}

			if (!newRes.isInited()) {
				try {
					// the package name association will be set when the first Activity or Service
					// of this package is started. There are some calls that get the Resources before
					// that time, but it should be early enough to set the replacments when the
					// app is really loaded
					String packageName = newRes.getPackageName();
					if (packageName != null) {
						callAll(initPackageResourcesCallbacks, packageName, newRes);
						newRes.setInited(true);
					}
				} catch (Exception e) {
					// even if some of this fails, we want the app to use the subclass
					log(e);		
				}
			}
			
			return newRes;
			
		} catch (Throwable t) {
			log(t);
		}
		return result;
	}
	

	/**
	 * Intercept every call to the specified method and call a handler function instead.
	 * @param method The method to intercept
	 */
	private native static void hookMethodNative(Member method);
	
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
	 * Callback handler that is added automatically when hooking a method.
	 * It invokes the original method.
	 */
	private static Object invokeOriginalMethodCallback(Iterator<Callback> iterator, Member method, Object thisObject, Object[] args) throws Throwable {
		try {
			return invokeOriginalMethod(method, thisObject, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
