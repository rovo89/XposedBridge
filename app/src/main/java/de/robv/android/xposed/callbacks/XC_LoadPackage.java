package de.robv.android.xposed.callbacks;

import android.content.pm.ApplicationInfo;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedBridge.CopyOnWriteSortedSet;

/**
 * Stand-alone callback for {@link XposedBridge#hookLoadPackage}.
 *
 * <p class="warning">It's highly recommended to implement {@link IXposedHookLoadPackage}
 * in the module's main class instead!
 */
public abstract class XC_LoadPackage extends XCallback implements IXposedHookLoadPackage {
	/**
	 * Creates a new callback with default priority.
	 */
	@SuppressWarnings("deprecation")
	public XC_LoadPackage() {
		super();
	}

	/**
	 * Creates a new callback with a specific priority.
	 *
	 * @param priority See {@link XCallback#priority}.
	 */
	public XC_LoadPackage(int priority) {
		super(priority);
	}

	/**
	 * Wraps information about the app being loaded.
	 */
	public static final class LoadPackageParam extends XCallback.Param {
		/** @hide */
		public LoadPackageParam(CopyOnWriteSortedSet<XC_LoadPackage> callbacks) {
			super(callbacks);
		}

		/** The name of the package being loaded. */
		public String packageName;

		/** The process in which the package is executed. */
		public String processName;

		/** The ClassLoader used for this package. */
		public ClassLoader classLoader;

		/** More information about the application being loaded. */
		public ApplicationInfo appInfo;

		/** Set to {@code true} if this is the first (and main) application for this process. */
		public boolean isFirstApplication;
	}

	/** @hide */
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LoadPackageParam)
			handleLoadPackage((LoadPackageParam) param);
	}

	/**
	 * An object with which the callback can be removed.
	 */
	public class Unhook implements IXUnhook<XC_LoadPackage> {
		/** @hide */
		public Unhook() {}

		@Override
		public XC_LoadPackage getCallback() {
			return XC_LoadPackage.this;
		}

		@Override
		public void unhook() {
			XposedBridge.unhookLoadPackage(XC_LoadPackage.this);
		}
	}
}
