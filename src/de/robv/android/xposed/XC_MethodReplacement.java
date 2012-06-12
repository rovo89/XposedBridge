package de.robv.android.xposed;

public abstract class XC_MethodReplacement extends XC_MethodHook {
	public XC_MethodReplacement() {
		super();
	}
	public XC_MethodReplacement(int priority) {
		super(priority);
	}
	
	@Override
	protected final void beforeHookedMethod(MethodHookParam param) throws Throwable {
		try {
			Object result = replaceHookedMethod(param);
			param.setResult(result);
		} catch (Throwable t) {
			param.setThrowable(t);
		}
	}
	
	protected final void afterHookedMethod(MethodHookParam param) throws Throwable {}
	
	/**
	 * Shortcut for replacing a method completely. Whatever is returned/thrown here is taken
	 * instead of the result of the original method (which will not be called).
	 */
	protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;
	
	public static final XC_MethodReplacement DO_NOTHING = new XC_MethodReplacement(PRIORITY_HIGHEST*2) {
    	@Override
    	protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
    		return null;
    	};
	};
}
