package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

public abstract class XC_LoadPackage extends XCallback {
	private TreeSet<XC_LoadPackage> loadedPackageCallbacks;

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
		/** The ClassLoader used for this package */
		public ClassLoader classLoader;
	}
	
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LoadPackageParam)
			handleLoadPackage((LoadPackageParam) param);
	}
	
	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;


	public void setCallbacksCollection(TreeSet<XC_LoadPackage> loadedPackageCallbacks) {
		this.loadedPackageCallbacks = loadedPackageCallbacks;
	}

	@Override
	public void detachCallback() {
		if (loadedPackageCallbacks != null) {
			synchronized (loadedPackageCallbacks) {
				loadedPackageCallbacks.remove(this);
			}
			loadedPackageCallbacks = null;
		}
	}
}
