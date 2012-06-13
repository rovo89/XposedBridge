package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;


public interface IXposedHookInitPackageResources extends IXposedMod {
	/** @see XC_InitPackageResources#handleInitPackageResources */
	public abstract void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable;
}
