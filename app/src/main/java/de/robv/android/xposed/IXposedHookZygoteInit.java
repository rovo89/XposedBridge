package de.robv.android.xposed;

/**
 * Hook the initialization of Zygote process/processes, from which all the apps are forked.
 */
public interface IXposedHookZygoteInit extends IXposedMod {
	/**
	 * Called very early during startup of Zygote.
	 * @throws Throwable everything is caught, but will prevent further initialization of the module.
	 */
	public void initZygote(StartupParam startupParam) throws Throwable;

	public static class StartupParam {
		/** The path to the module's APK. */
		public String modulePath;

		/**
		 * Always {@code true} on 32-bit ROMs.<br>
		 * On 64-bit, it's only {@code true} for the primary process that starts the system_server.
		 */
		public boolean startsSystemServer;
	}
}
