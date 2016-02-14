package de.robv.android.xposed;


/**
 * Hook the initialization of Java-based command-line tools (like pm).
 *
 * @deprecated Xposed no longer hooks command-line tools, therefore this interface shouldn't be
 * implemented anymore.
 */
@Deprecated
public interface IXposedHookCmdInit extends IXposedMod {
	/**
	 * Called very early during startup of a command-line tool
	 * @param startupParam Details about the module itself and the started process
	 * @throws Throwable everything is caught, but will prevent further initialization of the module
	 */
	void initCmdApp(StartupParam startupParam) throws Throwable;

	final class StartupParam {
		/*package*/ StartupParam() {}

		public String modulePath;
		public String startClassName;
	}
}
