package de.robv.android.xposed.callbacks;

import android.os.Bundle;

import java.io.Serializable;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedBridge.CopyOnWriteSortedSet;

public abstract class XCallback implements Comparable<XCallback> {
	public final int priority;

	/** @deprecated This method can't be hidden for technical reasons. Nevertheless, don't use it! */
	@Deprecated
	public XCallback() {
		this.priority = PRIORITY_DEFAULT;
	}

	/** @hide */
	public XCallback(int priority) {
		this.priority = priority;
	}

	public static abstract class Param {
		/** @hide */
		public final Object[] callbacks;
		private Bundle extra;

		/** @deprecated This method can't be hidden for technical reasons. Nevertheless, don't use it! */
		@Deprecated
		protected Param() {
			callbacks = null;
		}

		/** @hide */
		protected Param(CopyOnWriteSortedSet<? extends XCallback> callbacks) {
			this.callbacks = callbacks.getSnapshot();
		}

		/**
		 * This can be used to store anything for the scope of the callback.
		 * Use this instead of instance variables.
		 * @see #getObjectExtra
		 * @see #setObjectExtra
		 */
		public synchronized Bundle getExtra() {
			if (extra == null)
				extra = new Bundle();
			return extra;
		}

		/** @see #setObjectExtra */
		public Object getObjectExtra(String key) {
			Serializable o = getExtra().getSerializable(key);
			if (o instanceof SerializeWrapper)
				return ((SerializeWrapper) o).object;
			return null;
		}

		/** Provides a wrapper to store <code>Object</code>s in <code>extra</code>. */
		public void setObjectExtra(String key, Object o) {
			getExtra().putSerializable(key, new SerializeWrapper(o));
		}

		private static class SerializeWrapper implements Serializable {
			private static final long serialVersionUID = 1L;
			private final Object object;
			public SerializeWrapper(Object o) {
				object = o;
			}
		}
	}

	/** @hide */
	public static void callAll(Param param) {
		if (param.callbacks == null)
			throw new IllegalStateException("This object was not created for use with callAll");

		for (int i = 0; i < param.callbacks.length; i++) {
			try {
				((XCallback) param.callbacks[i]).call(param);
			} catch (Throwable t) { XposedBridge.log(t); }
		}
	}

	/** @hide */
	protected void call(Param param) throws Throwable {}

	/** @hide */
	@Override
	public int compareTo(XCallback other) {
		if (this == other)
			return 0;

		// order descending by priority
		if (other.priority != this.priority)
			return other.priority - this.priority;
		// then randomly
		else if (System.identityHashCode(this) < System.identityHashCode(other))
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
