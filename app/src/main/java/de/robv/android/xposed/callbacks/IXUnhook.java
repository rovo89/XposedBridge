package de.robv.android.xposed.callbacks;

public interface IXUnhook<T> {
	T getCallback();
	void unhook();
}
