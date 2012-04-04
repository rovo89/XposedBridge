package de.robv.android.xposed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Instrumentation;
import dalvik.system.PathClassLoader;

public final class XposedBridge {
	public static boolean DEBUG = false;
	
	private static PrintWriter logWriter = null;
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	// built-in handlers
	private static final Map<Method, SortedSet<Callback>> hookedMethodCallbacks = new HashMap<Method, SortedSet<Callback>>();
	private static final Class<?>[] HOOK_METHOD_CALLBACK_PARAMS = new Class[] { Iterator.class, Method.class, Object.class, Object[].class };

	private static final SortedSet<Callback> loadedPackageCallbacks = new TreeSet<Callback>();
	private static final Class<?>[] LOADED_PACKAGE_CALLBACK_PARAMS = new Class[] { String.class, ClassLoader.class };
	
	// cached methods
	private static Method loadedApkGetClassLoader;
	private static Method loadedApkGetPackageName;
	private static Method loadedApkGetApplication;
	
	/**
	 * Called when the VM has just been created. Be careful, not everything is possible at this time
	 * because native methods have not been linked yet. Calling such a method will result in a
	 * {@link UnsatisfiedLinkError}.
	 * 
	 * @param startClassName Name of the class the VM has been created for. @code{null} for the Zygote process
	 * @return Whether you want to keep on using Xposed for this process 
	 */
	private static boolean onVmCreated(String startClassName) throws Exception {
		boolean isZygote = (startClassName == null);
		
		// initialize log file
		try {
			File logFile = new File("/data/xposed/debug.log");
			logWriter = new PrintWriter(new FileWriter(logFile, true));
			logFile.setReadable(true, false);
			logFile.setWritable(true, false);
		} catch (IOException ignored) {}
		
		log("-----------------\nLoading Xposed...");

		// load our internal stuff
		try {
			if (isZygote) {
				initXbridgeInternal();
				if (DEBUG)
					DebugHandlers.init();
			}
		} catch (Throwable t) {
			log("Could not init Xbridge");
			log(t);
			return false;
		}
		
		// load modules
		try {
			loadModules(startClassName);			
		} catch (Throwable t) {
			log("Loading modules failed");
			log(t);
		}
		
		return true;
	}
	
	/**
	 * Hook some methods which we want to create an easier interface for
	 */
	private static void initXbridgeInternal() throws NoSuchMethodException, ClassNotFoundException {
		Class<?> classLoadedApk = Class.forName("android.app.LoadedApk");
		Method makeApplication= classLoadedApk.getDeclaredMethod("makeApplication", Boolean.TYPE, Instrumentation.class);
		hookMethod(makeApplication, XposedBridge.class, "handleMakeApplication", Callback.PRIORITY_DEFAULT);
		
		// Get references to some methods and make them accessible for later use
		loadedApkGetClassLoader = classLoadedApk.getDeclaredMethod("getClassLoader");
		loadedApkGetPackageName = classLoadedApk.getDeclaredMethod("getPackageName");
		loadedApkGetApplication = classLoadedApk.getDeclaredMethod("getApplication");
		
		Method.setAccessible(new AccessibleObject[] {
			loadedApkGetClassLoader,
			loadedApkGetPackageName,
			loadedApkGetApplication
		}, true);
	}
	
	private static void loadModules(String startClassName) throws IOException {
		BufferedReader apks = new BufferedReader(new FileReader("/data/xposed/modules.list"));
		String apk;
		while ((apk = apks.readLine()) != null) {
			loadModule(apk, startClassName);
		}
		apks.close();
	}
	
	private static void loadModule(String apk, String startClassName) {
		log("Loading modules from " + apk);
		
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
					Class<?> moduleClass;moduleClass = mcl.loadClass(moduleClassName);
					Method moduleInit = moduleClass.getDeclaredMethod("init", String.class);
					System.out.println(moduleInit);
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
	
	/**
	 * Called directly before executing the main method of a command line app
	 */
	private static void onStartedBeforeMain() {
		if (DEBUG)
			log("onStartedBeforeMain");
	}
	
	/**
	 * Called directly after executing the main method of a command line app
	 */
	private static void onStartedAfterMain() {
		if (DEBUG)
			log("onStartedAfterMain");
	}
	
	private static void onZygoteInit() throws Exception {
		if (DEBUG)
			log("onZygoteInit");
	}
	
	private static void onExit() {
		if (DEBUG)
			log("onExit");
	}
	
	/**
	 * Writes a message to /data/xposed/debug.log (needs to have chmod 777)
	 * @param text log message
	 */
	public synchronized static void log(String text) {
		System.out.println(text);
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
		t.printStackTrace();
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
	
	/**
	 * Hook any method and call the specified handler method.
	 * The handler method needs to have the following signature:<br/>
	 * <code>public static Object anyName(Iterator&lt;Callback&gt; iterator, Method method, Object thisObject, Object[] args)</code>
	 * <br/>
	 * It should use {@link #callNext} to call the next handler method in the queue. The last handler will be
	 * the method that was originally hooked.
	 * 
	 * @param hookMethod The method to be hooked
	 * @param handlerClass Class of the handler
	 * @param handlerMethodName Method of the handler
	 * @param priority The higher the priority, the earlier this handler called.
	 * @throws NoSuchMethodException The handler method was not found
	 */
	public synchronized static void hookMethod(Method hookMethod, Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {		
		Callback c = new Callback(handlerClass, handlerMethodName, priority, HOOK_METHOD_CALLBACK_PARAMS);
		if (c.method.getReturnType().equals(Void.TYPE))
			throw new NoSuchMethodException("Method must have a return type (not void)");
		
		SortedSet<Callback> callbacks = hookedMethodCallbacks.get(hookMethod);
		if (callbacks == null) {
			callbacks = new TreeSet<Callback>();
			// add the original method always as the last one to be called
			callbacks.add(new Callback(XposedBridge.class, "invokeOriginalMethodCallback", Callback.PRIORITY_LOWEST, HOOK_METHOD_CALLBACK_PARAMS));
			hookedMethodCallbacks.put(hookMethod, callbacks);
		}
		c.method.setAccessible(true);
		callbacks.add(c);
		hookMethodNative(hookMethod);
	}
	
	/**
	 * This method is called as a replacement for hooked methods.
	 * Use {@link #invokeOriginalMethod} to call the method that was hooked.
	 * 
	 * @param method The Method object, describing which method was called
	 * @param thisObject For non-static calls, the "this" pointer
	 * @param args Arguments for the method call as Object[] array
	 * @return The object to return to the original caller of the method
	 * @throws Throwable
	 */
	private static Object handleHookedMethod(Method method, Object thisObject, Object[] args) throws Throwable {
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
	 * Get notified when a package is loaded
	 * The handler method needs to have the following signature:<br/>
	 * <code>public static void anyName(String packageName, ClassLoader classLoader)</code>
	 * <br/>
	 */
	public synchronized static void hookLoadPackage(Class<?> handlerClass, String handlerMethodName, int priority) throws NoSuchMethodException {		
		Callback c = new Callback(handlerClass, handlerMethodName, priority, LOADED_PACKAGE_CALLBACK_PARAMS);
		c.method.setAccessible(true);
		loadedPackageCallbacks.add(c);
	}
	
	/**
	 * Built-in handler for the LoadedApk.makeApplication method that allows handlers to be registered
	 * for the first initialization of a package via {@link #hookLoadPackage}.
	 */
	private static Object handleMakeApplication(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		try {
			String packageName = (String) loadedApkGetPackageName.invoke(thisObject);
			boolean firstLoad = (loadedApkGetApplication.invoke(thisObject) == null);
			ClassLoader classLoader = (ClassLoader)loadedApkGetClassLoader.invoke(thisObject);
			if (firstLoad)
				callAll(loadedPackageCallbacks, packageName, classLoader);
		} catch (Exception e) {
			log(e);
		}
		return callNext(iterator, method, thisObject, args);
	}
	

	/**
	 * Intercept every call to the specified method and call a handler function instead.
	 * @param method The method to intercept
	 */
	private native static void hookMethodNative(Method method);
	
	/**
	 * Change the modifiers (access flags) for a class.
	 * Do not use for now!
	 * @param clazz The class to modify
	 * @param modifiers New modifiers to set
	 */
	private native static void setClassModifiersNative(Class<?> clazz, int modifiers);	
	
	private native static Object invokeOriginalMethodNative(Method method, Class<?>[] parameterTypes, Class<?> returnType, Object thisObject, Object[] args)
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
	private static Object invokeOriginalMethod(Method method, Object thisObject, Object[] args)
				throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (args == null) {
            args = EMPTY_ARRAY;
        }
		return invokeOriginalMethodNative(method, method.getParameterTypes(), method.getReturnType(), thisObject, args);
	}
	
	/**
	 * Callback handler that is added automatically when hooking a method.
	 * It invokes the original method.
	 */
	private static Object invokeOriginalMethodCallback(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		try {
			return invokeOriginalMethod(method, thisObject, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
