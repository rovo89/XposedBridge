package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

import android.content.res.XResources;

public abstract class InitPackageResourcesXCallback extends XCallback {
	public InitPackageResourcesXCallback() {
		super();
	}
	public InitPackageResourcesXCallback(int priority) {
		super(priority);
	}
	
	public static class InitPackageResourcesParam extends XCallback.Param<InitPackageResourcesXCallback> {
		public InitPackageResourcesParam(TreeSet<InitPackageResourcesXCallback> callbacks) {
			super(callbacks);
		}
		public String packageName;
		public XResources res;
	}
	
	@Override
	protected void call(Param<?> param) throws Throwable {
		if (param instanceof InitPackageResourcesParam)
			handleInitPackageResources((InitPackageResourcesParam) param);
	}
	
	public abstract void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable;
}
