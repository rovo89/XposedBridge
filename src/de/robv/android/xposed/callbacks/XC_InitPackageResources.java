package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

import android.content.res.XResources;

public abstract class XC_InitPackageResources extends XCallback {
	private TreeSet<XC_InitPackageResources> initResourcesCallbacks;

	public XC_InitPackageResources() {
		super();
	}
	public XC_InitPackageResources(int priority) {
		super(priority);
	}
	
	public static class InitPackageResourcesParam extends XCallback.Param {
		public InitPackageResourcesParam(TreeSet<XC_InitPackageResources> callbacks) {
			super(callbacks);
		}
		/** The name of the package for which resources are being loaded */
		public String packageName;
		/** Reference to the resources that can be used for calls to {@link XResources#setReplacement} */
		public XResources res;
	}
	
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof InitPackageResourcesParam)
			handleInitPackageResources((InitPackageResourcesParam) param);
	}
	
	public abstract void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable;


	public void setCallbacksCollection(TreeSet<XC_InitPackageResources> initResourcesCallbacks) {
		this.initResourcesCallbacks = initResourcesCallbacks;
	}

	@Override
	public void detachCallback() {
		if (initResourcesCallbacks != null) {
			synchronized (initResourcesCallbacks) {
				initResourcesCallbacks.remove(this);
			}
			initResourcesCallbacks = null;
		}
	}
}
