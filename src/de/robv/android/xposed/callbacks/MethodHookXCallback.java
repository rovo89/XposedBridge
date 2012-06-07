package de.robv.android.xposed.callbacks;

import java.lang.reflect.Member;
import java.util.TreeSet;

import de.robv.android.xposed.XposedBridge;

public abstract class MethodHookXCallback extends XCallback {
	public MethodHookXCallback() {
		super();
	}
	public MethodHookXCallback(int priority) {
		super(priority);
	}
	
	public static class MethodHookParam extends XCallback.Param<MethodHookXCallback> {
		public MethodHookParam(TreeSet<MethodHookXCallback> callbacks) {
			super(callbacks);
		}
		public Member method;
		public Object thisObject;
		public Object[] args;
		public Object result = null;
		public boolean returnEarly = false;
	}
	
	public Object handleHookedMethod(MethodHookParam param) throws Throwable {
        try {
        	beforeHookedMethod(param);
        } catch (Throwable t) { XposedBridge.log(t); }
        
        if (param.returnEarly)
        	return param.result;
        
        param.result = param.next().handleHookedMethod(param);
        
        try {
        	afterHookedMethod(param);
        } catch (Throwable t) { XposedBridge.log(t); }
        
        return param.result;
    }
	
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable  {}
    
    public static final MethodHookXCallback DO_NOTHING = new MethodHookXCallback(PRIORITY_HIGHEST*2) {
    	public Object handleHookedMethod(MethodHookParam param) throws Throwable {
    		return null;
    	};
	};
}
