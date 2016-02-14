package de.robv.android.xposed.callbacks;

import android.content.pm.ApplicationInfo;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedBridge.CopyOnWriteSortedSet;

public abstract class XC_LoadPackage extends XCallback {
	@SuppressWarnings("deprecation")
	public XC_LoadPackage() {
		super();
	}

	public XC_LoadPackage(int priority) {
		super(priority);
	}

	public static final class LoadPackageParam extends XCallback.Param {
		/** @hide */
		public LoadPackageParam(CopyOnWriteSortedSet<XC_LoadPackage> callbacks) {
			super(callbacks);
		}
		/** The name of the package being loaded */
		public String packageName;
		/** The process in which the package is executed */
		public String processName;
		/** The ClassLoader used for this package */
		public ClassLoader classLoader;
		/** More information about the application to be loaded */
		public ApplicationInfo appInfo;
		/** Set to true if this is the first (and main) application for this process */
		public boolean isFirstApplication;
	}

	/** @hide */
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LoadPackageParam)
			handleLoadPackage((LoadPackageParam) param);
	}

	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;

	public class Unhook implements IXUnhook {
		/** @hide */
		public Unhook() {}

		public XC_LoadPackage getCallback() {
			return XC_LoadPackage.this;
		}

		@Override
		public void unhook() {
			XposedBridge.unhookLoadPackage(XC_LoadPackage.this);
		}
	}
}
