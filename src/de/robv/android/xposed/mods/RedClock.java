package de.robv.android.xposed.mods;

import java.lang.reflect.Method;
import java.util.Iterator;

import android.graphics.Color;
import android.widget.TextView;
import de.robv.android.xposed.Callback;
import de.robv.android.xposed.XposedBridge;

/**
 * Example module which changes the color of the clock in the statusbar to red and
 * also appends a smiley to it. It does so by hooking the updateClock method of the
 * Clock class in the systemui as soon as the com.android.systemui is being loaded.
 * <br/>
 * This demonstrates how a very central component can be modified without changing
 * the APKs (including deodexing, recompiling, signing etc).
 */
public class RedClock {
	private static Method methodGetText;
	private static Method methodSetText;
	private static Method methodSetTextColor;
		
	public static void init() {
		try {
			XposedBridge.hookLoadPackage(RedClock.class, "handleLoadPackage", Callback.PRIORITY_DEFAULT);
			methodGetText = TextView.class.getDeclaredMethod("getText");
			methodSetText = TextView.class.getDeclaredMethod("setText", CharSequence.class);
			methodSetTextColor = TextView.class.getDeclaredMethod("setTextColor", Integer.TYPE);
		} catch (Throwable t) {
			XposedBridge.log(t);
		} 
	}
	
	@SuppressWarnings("unused")
	private static void handleLoadPackage(String packageName, ClassLoader classLoader) {
		// the status bar belongs to package com.android.systemui
		if (!packageName.equals("com.android.systemui"))
			return;
		
		try {
			Method updateClock =
				Class.forName("com.android.systemui.statusbar.policy.Clock", false, classLoader)
				.getDeclaredMethod("updateClock");
			XposedBridge.hookMethod(updateClock, RedClock.class, "handleUpdateClock", Callback.PRIORITY_DEFAULT);
		} catch (Exception e) {
			XposedBridge.log(e);
		}
	}
	
	@SuppressWarnings("unused")
	private static Object handleUpdateClock(Iterator<Callback> iterator, Method method, Object thisObject, Object[] args) throws Throwable {
		if (XposedBridge.DEBUG)
			XposedBridge.log("updating the clock");

		// first let the original implementation perform its work
		Object result = XposedBridge.callNext(iterator, method, thisObject, args);
		// then change text and color
		try {
			String text = (String)methodGetText.invoke(thisObject);
			methodSetText.invoke(thisObject, text + " :)");
			methodSetTextColor.invoke(thisObject, Color.RED);
		} catch (Exception e) {
			// replacing did not work.. but no reason to crash the VM! Log the error and go on.
			XposedBridge.log(e);
		}
		return result;
	}
}
