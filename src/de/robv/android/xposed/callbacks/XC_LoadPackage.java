package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

import android.content.pm.ApplicationInfo;
import de.robv.android.xposed.XposedBridge;

public abstract class XC_LoadPackage extends XCallback {
	public XC_LoadPackage() {
		super();
	}
	public XC_LoadPackage(int priority) {
		super(priority);
	}
	
	public static class LoadPackageParam extends XCallback.Param {
		public LoadPackageParam(TreeSet<XC_LoadPackage> callbacks) {
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
	
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LoadPackageParam)
			handleLoadPackage((LoadPackageParam) param);
	}
	
	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;
	
	public class Unhook implements IXUnhook {
		public XC_LoadPackage getCallback() {
			return XC_LoadPackage.this;
		}

		@Override
		public void unhook() {
			XposedBridge.unhookLoadPackage(XC_LoadPackage.this);
		}
	}
}
