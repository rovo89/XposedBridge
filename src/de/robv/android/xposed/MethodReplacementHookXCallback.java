package de.robv.android.xposed;

public abstract class MethodReplacementHookXCallback extends MethodHookXCallback {
	public MethodReplacementHookXCallback() {
		super();
	}
	public MethodReplacementHookXCallback(int priority) {
		super(priority);
	}
	
	@Override
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		try {
			Object result = replaceHookedMethod(param);
			param.setResult(result);
		} catch (Throwable t) {
			param.setThrowable(t);
		}
	}
	
	protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;
	
	public static final MethodReplacementHookXCallback DO_NOTHING = new MethodReplacementHookXCallback(PRIORITY_HIGHEST*2) {
    	@Override
    	protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
    		return null;
    	};
	};
}
