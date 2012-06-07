package de.robv.android.xposed.callbacks;

import java.util.Iterator;
import java.util.TreeSet;

import android.os.Bundle;
import de.robv.android.xposed.XposedBridge;

public abstract class XCallback implements Comparable<XCallback> {
	public final int priority;
	public XCallback() {
		this.priority = PRIORITY_DEFAULT;
	}
	public XCallback(int priority) {
		this.priority = priority;
	}
	
	public static class Param<T extends XCallback> {
		private final Iterator<T> callbackIt;
		private final T first;
		public final Bundle extra = new Bundle();
		
		@SuppressWarnings("unchecked")
		protected Param(TreeSet<T> callbacks) {
			synchronized (callbacks) {
				callbackIt = ((TreeSet<T>) callbacks.clone()).iterator();
			}
			first = callbackIt.hasNext() ? callbackIt.next() : null;
		}
		
		public final T first() {
			if (first == null) {
				IllegalStateException e = new IllegalStateException("at least one callback is required");
				XposedBridge.log(e);
				throw(e);
			}
			return first;
		}
		
		protected final T next() {
			if (!callbackIt.hasNext()) {
				IllegalStateException e = new IllegalStateException("unexpected end of chain, no callback was the final handler");
				XposedBridge.log(e);
				throw(e);
			}
			return callbackIt.next();
		}
	}
	
	public static final void callAll(Param<?> param) {
		if (param.first == null)
			return;
		
		try {
			param.first.call(param);
		} catch (Throwable t) { XposedBridge.log(t); }
		
		while (param.callbackIt.hasNext()) {
			try {
				param.callbackIt.next().call(param);
			} catch (Throwable t) { XposedBridge.log(t); }
		}
	}
	
	protected void call(Param<?> param) throws Throwable {};
	
	@Override
	public int compareTo(XCallback other) {
		if (this == other)
			return 0;
		
		// order descending by priority
		if (other.priority != this.priority)
			return other.priority - this.priority;
		// then randomly
		else if (this.hashCode() < other.hashCode())
			return -1;
		else
			return 1;
	}
	
	public static final int PRIORITY_DEFAULT = 50;
	/** Call this handler last */
	public static final int PRIORITY_LOWEST = -10000;
	/** Call this handler first */
	public static final int PRIORITY_HIGHEST = 10000;
}
