package de.robv.android.xposed.callbacks;

/**
 * Interface for object that can be used to remove callbacks.
 *
 * @param <T> The class of the callback.
 */
public interface IXUnhook<T> {
	/**
	 * Returns the callback that has been registered.
	 */
	T getCallback();

	/**
	 * Removes the callback.
	 */
	void unhook();
}
