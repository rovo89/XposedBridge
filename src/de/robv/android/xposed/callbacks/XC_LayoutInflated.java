package de.robv.android.xposed.callbacks;

import android.content.res.XResources;
import android.content.res.XResources.ResourceNames;
import android.view.View;
import de.robv.android.xposed.XposedBridge.CopyOnWriteSortedSet;

public abstract class XC_LayoutInflated extends XCallback {
	public XC_LayoutInflated() {
		super();
	}
	public XC_LayoutInflated(int priority) {
		super(priority);
	}

	public static class LayoutInflatedParam extends XCallback.Param {
		public LayoutInflatedParam(CopyOnWriteSortedSet<XC_LayoutInflated> callbacks) {
			super(callbacks);
		}
		/** The view that has been created from the layout */
		public View view;
		/** Container with the id and name of the underlying resource */
		public ResourceNames resNames;
		/** Directory from which the layout was actually loaded (e.g. "layout-sw600dp") */
		public String variant;
		/** Resources containing the layout */
		public XResources res;
	}

	@Override
	protected void call(Param param) throws Throwable {
		if (param instanceof LayoutInflatedParam)
			handleLayoutInflated((LayoutInflatedParam) param);
	}

	public abstract void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable;

	public class Unhook implements IXUnhook {
		private final String resDir;
		private final int id;

		public Unhook(String resDir, int id) {
			this.resDir = resDir;
			this.id = id;
		}

		public String getResDir() {
			return resDir;
		}

		public int getId() {
			return id;
		}

		public XC_LayoutInflated getCallback() {
			return XC_LayoutInflated.this;
		}

		@Override
		public void unhook() {
			XResources.unhookLayout(resDir, id, XC_LayoutInflated.this);
		}

	}
}
