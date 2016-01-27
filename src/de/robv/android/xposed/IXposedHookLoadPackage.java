package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Use the module class as a handler for {@link XC_LoadPackage#handleLoadPackage}
 */
public interface IXposedHookLoadPackage extends IXposedMod {
	/** @see XC_LoadPackage#handleLoadPackage */
	public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;

	public static class Wrapper extends XC_LoadPackage {
		private final IXposedHookLoadPackage instance;
		public Wrapper(IXposedHookLoadPackage instance) {
			this.instance = instance;
		}
		@Override
		public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
			instance.handleLoadPackage(lpparam);
		}
	}
}
