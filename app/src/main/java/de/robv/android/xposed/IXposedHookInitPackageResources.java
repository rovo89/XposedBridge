package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

/**
 * Use the module class as a handler for {@link XC_InitPackageResources#handleInitPackageResources}
 */
public interface IXposedHookInitPackageResources extends IXposedMod {
	/** @see XC_InitPackageResources#handleInitPackageResources */
	void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable;

	class Wrapper extends XC_InitPackageResources {
		private final IXposedHookInitPackageResources instance;
		public Wrapper(IXposedHookInitPackageResources instance) {
			this.instance = instance;
		}
		@Override
		public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
			instance.handleInitPackageResources(resparam);
		}
	}
}
