package android.content.res;

import android.app.AndroidAppHelper;
import android.util.DisplayMetrics;
import de.robv.android.xposed.XposedBridge;

/**
 * Resources that can be created for an Xposed module.
 */
public class XModuleResources extends Resources {
	private XModuleResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
	}
	
	/**
	 *  Usually called with the automatically injected {@code MODULE_PATH} constant of the first parameter
	 *  and the resources received in the callback for {@link XposedBridge#hookInitPackageResources} (or
	 *  {@code null} for system-wide replacements.
	 */
	public static XModuleResources createInstance(String modulePath, XResources origRes) {
		if (modulePath == null)
			throw new IllegalArgumentException("modulePath must not be null");
		
		AssetManager assets = new AssetManager();
		assets.addAssetPath(modulePath);
		
		XModuleResources res;
		if (origRes != null)
			res = new XModuleResources(assets, origRes.getDisplayMetrics(),	origRes.getConfiguration());
		else
			res = new XModuleResources(assets, null, null);
		
		AndroidAppHelper.addActiveResource(modulePath, res.hashCode(), false, res);
		return res;
	}
	
	/**
	 * Create an {@link XResForwarder} instances that forwards requests to {@code id} in this resource.
	 */
	public XResForwarder fwd(int id) {
		return new XResForwarder(this, id);
	}
}
