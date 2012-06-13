package de.robv.android.xposed;

public interface IXposedHookCmdInit extends IXposedMod {
	public void initCmdApp(String startClassName);
}
