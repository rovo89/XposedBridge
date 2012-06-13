package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public interface IXposedHookLoadPackage extends IXposedMod {
	/** @see XC_LoadPackage#handleLoadPackage */
	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;
}
