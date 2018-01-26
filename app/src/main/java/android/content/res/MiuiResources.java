package android.content.res;

import android.util.DisplayMetrics;

/**
 * This class is used on non-MIUI ROMs only and shouldn't have any negative impact on them.
 * On MIUI ROM, this class is never seen because the ROM's class hides it (this is intended).
 * @hide
 */
public class MiuiResources extends Resources {
	/** Dummy, will never be called (objects are transferred to this class only). */
	/*package*/ MiuiResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(null, null, null);
		throw new UnsupportedOperationException();
	}
}
