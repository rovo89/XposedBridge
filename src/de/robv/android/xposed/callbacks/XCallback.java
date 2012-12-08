package de.robv.android.xposed.callbacks;

import java.io.Serializable;
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
		public XCallback thisCallback;

		/**
		 * This can be used to store anything for the scope of the callback.
		 * Use this instead of instance variables.
		 * @see #getObjectExtra
		 * @see #setObjectExtra
		 */
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
		
		/** @see #setObjectExtra */
		public Object getObjectExtra(String key) {
			Serializable o = extra.getSerializable(key);
			if (o instanceof SerializeWrapper)
				return ((SerializeWrapper) o).object;
			return null;
		}
		
		/** Provides a wrapper to store <code>Object</code>s in <code>extra</code>. */
		public void setObjectExtra(String key, Object o) {
			extra.putSerializable(key, new SerializeWrapper(o));
		}
		
		private static class SerializeWrapper implements Serializable {
			private static final long serialVersionUID = 1L;
			private Object object;
			public SerializeWrapper(Object o) {
				object = o;
			}
		}
	}
	
	public static final void callAll(Param param) {
		if (param.callbacks == null)
			throw new IllegalStateException("This object was not created for use with callAll");
		
		Iterator<? extends XCallback> it = param.callbacks.iterator();
		while (it.hasNext()) {
			try {
				XCallback callback = it.next();
				param.thisCallback = callback;
				callback.call(param);
			} catch (Throwable t) { XposedBridge.log(t); }
		}
	}
	
	protected void call(Param param) throws Throwable {	};

	public abstract void detachCallback();

	@Override
	public int compareTo(XCallback other) {
		if (this == other)
			return 0;
		
		// order descending by priority
		if (other.priority != this.priority)
			return other.priority - this.priority;
		// then randomly
		else if (System.identityHashCode(this.hashCode()) < System.identityHashCode(other.hashCode()))
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
