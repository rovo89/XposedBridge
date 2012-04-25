package de.robv.android.xposed;

import java.lang.reflect.Method;
import java.util.Iterator;

import android.content.res.XResources;

/**
 * Callback methods have to follow these method signatures.
 * They do NOT have to implement this interface, just the parameters
 * have to be the same. Furthermore, callback methods have to be static.
 */
public interface MethodSignatureGuide {
	/**
	 * This method is called to load the module.
	 * 
	 * @param startClassName Name of the class the VM has been created for or {@code null} for the Zygote process
	 */
	void init(String startClassName);
	
	/**
	 * Signature of callbacks for {@link XposedBridge#hookMethod}.<br/>
	 * Callbacks should use {@link XposedBridge#callNext} to call the next handler method in the queue. The last handler will be
	 * the method that was originally hooked.
	 * 
	 * @param iterator Iterator for the handler list. Just pass this on to {@link XposedBridge#callNext}
	 * @param method The Method object, describing which method was called
	 * @param thisObject For non-static calls, the "this" pointer
	 * @param args Arguments for the method call as Object[] array
	 * @return The object to return to the original caller of the method
	 * @throws Throwable As this hook is very generic, any type of exception could be thrown.
	 */
	Object handleHookedMethod(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable;
	
	/**
	 * Signature of callbacks for {@link XposedBridge#hookLoadPackage}.
	 * 
	 * @param packageName The name of the package being loaded
	 * @param classLoader The ClassLoader used for this package 
	 */
	void handleLoadPackage(String packageName, ClassLoader classLoader);
	
	/**
	 * Signature of callbacks for {@link XposedBridge#hookInitPackageResources}.
	 * 
	 * @param packageName The name of the package for which resources are being loaded
	 * @param classLoader Reference to the resources that can be used for calls to {@link XResources#setReplacement}
	 */
	void handleInitPackageResources(String packageName, XResources res);
}
