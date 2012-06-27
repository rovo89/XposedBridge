package de.robv.android.xposed;


/**
 * Hook the initialization of Java-based command-line tools (like pm)
 */
public interface IXposedHookCmdInit extends IXposedMod {
	/**
	 * Called very early during startup of a command-line tool
	 * @param startClassName The startup class
	 * @throws Throwable everything is caught, but will prevent further initialization of the module
	 */
	public void initCmdApp(StartupParam startupParam) throws Throwable;
	
	public static class StartupParam {
		public String modulePath;
		public String startClassName;
	}
}
