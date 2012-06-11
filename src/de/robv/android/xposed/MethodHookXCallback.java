package de.robv.android.xposed;

import java.lang.reflect.Member;
import java.util.Iterator;

import de.robv.android.xposed.callbacks.XCallback;

public abstract class MethodHookXCallback extends XCallback {
	public MethodHookXCallback() {
		super();
	}
	public MethodHookXCallback(int priority) {
		super(priority);
	}
	
	public static class MethodHookParam extends XCallback.Param {
		public Member method;
		public Object thisObject;
		public Object[] args;
		
		private Object result = null;
		public Object getResult() {
			return result;
		}
		public Object getResultOrThrowable() throws Throwable {
			if (throwable != null)
				throw throwable;
			return result;
		}
		public void setResult(Object result) {
			this.result = result;
			this.returnEarly = true;
		}
		
		private Throwable throwable = null;
		public Throwable getThrowable() {
			return throwable;
		}
		public boolean hasThrowable() {
			return throwable != null;
		}
		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
			this.returnEarly = true;
		}
		
		/* package */ Iterator<MethodHookXCallback> beforeIterator;
		/* package */ Iterator<MethodHookXCallback> afterIterator;
		/* package */ boolean returnEarly = false;
	}
	
	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
	protected void afterHookedMethod(MethodHookParam param) throws Throwable  {}
}
