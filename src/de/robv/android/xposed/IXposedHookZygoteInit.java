package de.robv.android.xposed;

/**
 * Hook the initialization of Zygote (the central part of the "Android OS"
 */
public interface IXposedHookZygoteInit extends IXposedMod {
	/**
	 * Called very early during startup of Zygote
	 * @throws Throwable everything is caught, but will prevent further initialization of the module
	 */
	public void initZygote() throws Throwable;
}
