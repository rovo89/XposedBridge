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
	
	public static class Param {
		public final TreeSet<? extends XCallback> callbacks;
		public final Bundle extra = new Bundle();
		
		protected Param() {
			callbacks = null;
		}
		
		@SuppressWarnings("unchecked")
		protected Param(TreeSet<? extends XCallback> callbacks) {
			synchronized (callbacks) {
				this.callbacks = (TreeSet<? extends XCallback>) callbacks.clone();
			}
		}
	}
	
	public static final void callAll(Param param) {
		if (param.callbacks == null)
			throw new IllegalStateException("This object was not created for use with callAll");
		
		Iterator<? extends XCallback> it = param.callbacks.iterator();
		while (it.hasNext()) {
			try {
				it.next().call(param);
			} catch (Throwable t) { XposedBridge.log(t); }
		}
	}
	
	protected void call(Param param) throws Throwable {};
	
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
