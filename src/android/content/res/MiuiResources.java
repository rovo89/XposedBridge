package android.content.res;

import android.util.DisplayMetrics;

/**
 * This method is used on non-MIUI ROMs only and shouldn't have any negative impact on them.
 * On MIUI ROM, this class is never seen because the ROM's class hides it (this is intended).
 */
public class MiuiResources extends Resources {
	public MiuiResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
	}
}
