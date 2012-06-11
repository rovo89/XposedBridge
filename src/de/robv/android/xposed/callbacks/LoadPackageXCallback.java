package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

public abstract class LoadPackageXCallback extends XCallback {
	public LoadPackageXCallback() {
		super();
	}
	public LoadPackageXCallback(int priority) {
		super(priority);
	}
	
	public static class LoadPackageParam extends XCallback.Param {
		public LoadPackageParam(TreeSet<LoadPackageXCallback> callbacks) {
			super(callbacks);
		}
		public String packageName;
		public ClassLoader classLoader;
	}
	
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LoadPackageParam)
			handleLoadPackage((LoadPackageParam) param);
	}
	
	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;
}
