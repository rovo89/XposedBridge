package de.robv.android.xposed.callbacks;

import java.util.TreeSet;

import android.content.res.XResources;
import android.content.res.XResources.ResourceNames;
import android.view.View;

public abstract class LayoutInflatedXCallback extends XCallback {
	public LayoutInflatedXCallback() {
		super();
	}
	public LayoutInflatedXCallback(int priority) {
		super(priority);
	}
	
	public static class LayoutInflatedParam extends XCallback.Param {
		public LayoutInflatedParam(TreeSet<LayoutInflatedXCallback> callbacks) {
			super(callbacks);
		}
		public View view;
		public ResourceNames resNames;
		public String variant;
		public XResources res;
	}
	
	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LayoutInflatedParam)
			handleLayoutInflated((LayoutInflatedParam) param);
	}
	
	public abstract void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable;
}
