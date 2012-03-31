package de.robv.android.xposed;

import java.lang.reflect.Method;

/**
 * Structure for one callback method, including its priority
 */
public class Callback implements Comparable<Callback> {
	public final Method method;
	public final int priority;
	public Callback(Class<?> clazz, String methodName, int priority, Class<?> ... parameterTypes) throws NoSuchMethodException {
		this.method = clazz.getDeclaredMethod(methodName, parameterTypes);
		this.priority = priority;
	}
	
	@Override
	public int compareTo(Callback another) {
		// order descending by priority
		return another.priority - this.priority;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		
		if (o == null || !(o instanceof Callback))
			return false;
		
		return method.equals(((Callback)o).method);
	}
	
	public static final int PRIORITY_DEFAULT = 50;
	/** Call this handler last */
	public static final int PRIORITY_LOWEST = -10000;
	/** Call this handler first */
	public static final int PRIORITY_HIGHEST = 10000;
}
