package android.content.res;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LayoutInflated.LayoutInflatedParam;
import de.robv.android.xposed.callbacks.XCallback;

/**
 * Resources class that allows replacements for selected resources
 */
public class XResources extends Resources {
	private static final SparseArray<HashMap<String, Object>> replacements = new SparseArray<HashMap<String, Object>>();
	private static final SparseArray<HashMap<String, ResourceNames>> resourceNames
		= new SparseArray<HashMap<String, ResourceNames>>();
	
	private static final SparseArray<HashMap<String, TreeSet<XC_LayoutInflated>>> layoutCallbacks
		= new SparseArray<HashMap<String, TreeSet<XC_LayoutInflated>>>();
	private static final WeakHashMap<XmlResourceParser, XMLInstanceDetails> xmlInstanceDetails
		= new WeakHashMap<XmlResourceParser, XMLInstanceDetails>();
	
	private static final HashMap<String, Long> resDirLastModified = new HashMap<String, Long>();
	private static final HashMap<String, String> resDirPackageNames = new HashMap<String, String>();
	private boolean inited = false;

	private final String resDir;
	
	public XResources(Resources parent, String resDir) {
		super(parent.getAssets(), null, null, null);
		this.resDir = resDir;
		if (Build.VERSION.SDK_INT > 10) {
			updateConfiguration(parent.getConfiguration(), parent.getDisplayMetrics(), parent.getCompatibilityInfo());
		} else {
			updateConfiguration(parent.getConfiguration(), parent.getDisplayMetrics());
		}
	}
	
	/** Framework only, don't call this from your module! */
	public boolean checkFirstLoad() {
		synchronized (replacements) {
			if (resDir == null)
				return false;
			
			Long lastModification = new File(resDir).lastModified();
			Long oldModified = resDirLastModified.get(resDir);
			if (lastModification.equals(oldModified))
				return false;
			
			resDirLastModified.put(resDir, lastModification);
			
			if (oldModified == null)
				return true;
			
			// file was changed meanwhile => remove old replacements 
			for(int i = 0; i < replacements.size(); i++) {
				replacements.valueAt(i).remove(resDir);
			}
			return true;
		}
	}

	public String getResDir() {
		return resDir;
	}
	
	/** Framework only, don't call this from your module! */
	public static void setPackageNameForResDir(String packageName, String resDir) {
		resDirPackageNames.put(resDir, packageName);
	}
	
	public String getPackageName() {
		if (resDir == null)
			return "android";
		
		String packageName = resDirPackageNames.get(resDir);
		if (packageName == null) {
			XposedBridge.log(new IllegalStateException("could not determine package name for " + resDir));
			return "";
		}
		return packageName;
	}
	
	/** Framework only, don't call this from your module! */
	public boolean isInited() {
		return inited;
	}
	
	/** Framework only, don't call this from your module! */
	public void setInited(boolean inited) {
		this.inited = inited;
	}
	
	/** Framework only, don't call this from your module! */
	public static void init() throws Exception {
		findAndHookMethod(Resources.class, "getCachedStyledAttributes", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final Object result = param.getResult();
				if (!(result instanceof XTypedArray) && param.thisObject instanceof XResources) {
					TypedArray orig = (TypedArray) result;
					XResources xres = (XResources) param.thisObject;
					param.setResult(xres.newXTypedArray(orig.mData, orig.mIndices, orig.mLength));
				}
			}
		});
		
		findAndHookMethod(LayoutInflater.class, "inflate", XmlPullParser.class, ViewGroup.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XMLInstanceDetails details;
				synchronized (xmlInstanceDetails) {
					details = xmlInstanceDetails.get(param.args[0]);
				}
				if (details != null) {
					LayoutInflatedParam liparam = new LayoutInflatedParam(details.callbacks);
					liparam.view = (View) param.getResult();
					liparam.resNames = details.resNames;
					liparam.variant = details.variant;
					liparam.res = details.res;
					XCallback.callAll(liparam);
				}
			}
		});
	}
	
	public static class ResourceNames {
		public final int id;
		public final String pkg;
		public final String name;
		public final String type;
		public final String fullName;
		
		private ResourceNames(int id, String pkg, String name, String type) {
			this.id = id;
			this.pkg = pkg;
			this.name = name;
			this.type = type;
			this.fullName = pkg + ":" + type + "/" + name;
		}
		
		/**
		 * Returns <code>true</code> if all non-null parameters match the values of this object.
		 */
		public boolean equals(String pkg, String name, String type, int id) {
			return (pkg  == null || pkg.equals(this.pkg))
				&& (name == null || name.equals(this.name))
				&& (type == null || type.equals(this.type))
				&& (id == 0 || id == this.id);
		}
	}
	
	private ResourceNames getResourceNames(int id) {
		return new ResourceNames(
				id,
				getResourcePackageName(id),
				getResourceTypeName(id),
				getResourceEntryName(id));
	}
	
	private static ResourceNames getSystemResourceNames(int id) {
		Resources sysRes = getSystem();
		return new ResourceNames(
				id,
				sysRes.getResourcePackageName(id),
				sysRes.getResourceTypeName(id),
				sysRes.getResourceEntryName(id));
	}
	
	private static void putResourceNames(String resDir, ResourceNames resNames) {
		int id = resNames.id;
		synchronized (resourceNames) {
			HashMap<String, ResourceNames> inner = resourceNames.get(id);
			if (inner == null) {
				inner = new HashMap<String, ResourceNames>();
				resourceNames.put(id, inner);
			}
			synchronized (inner) {
				inner.put(resDir, resNames);
			}
		}
	}

	// =======================================================
	//   DEFINING REPLACEMENTS
	// =======================================================
	
	public void setReplacement(int id, Object replacement) {
		setReplacement(id, replacement, resDir);
	}
	
	public void setReplacement(String fullName, Object replacement) {
		int id = getIdentifier(fullName, null, null);
		if (id == 0)
			throw new NotFoundException(fullName);
		setReplacement(id, replacement, resDir);
	}
	
	public void setReplacement(String pkg, String type, String name, Object replacement) {
		int id = getIdentifier(name, type, pkg);
		if (id == 0)
			throw new NotFoundException(pkg + ":" + type + "/" + name);
		setReplacement(id, replacement, resDir);
	}
	
	public static void setSystemWideReplacement(int id, Object replacement) {
		setReplacement(id, replacement, null);
	}
	
	public static void setSystemWideReplacement(String fullName, Object replacement) {
		int id = getSystem().getIdentifier(fullName, null, null);
		if (id == 0)
			throw new NotFoundException(fullName);
		setReplacement(id, replacement, null);
	}
	
	public static void setSystemWideReplacement(String pkg, String type, String name, Object replacement) {
		int id = getSystem().getIdentifier(name, type, pkg);
		if (id == 0)
			throw new NotFoundException(pkg + ":" + type + "/" + name);
		setReplacement(id, replacement, null);
	}
	
	private static void setReplacement(int id, Object replacement, String resDir) {
		if (id == 0)
			throw new IllegalArgumentException("id 0 is not an allowed resource identifier");
		else if (resDir == null && id >= 0x7f000000)
			throw new IllegalArgumentException("ids >= 0x7f000000 are app specific and cannot be set for the framework");
		
		if (replacement instanceof Drawable)
			throw new IllegalArgumentException("Drawable replacements are deprecated since Xposed 2.1. Use DrawableLoader instead.");
		
		synchronized (replacements) {
			HashMap<String, Object> inner = replacements.get(id);
			if (inner == null) {
				inner = new HashMap<String, Object>();
				replacements.put(id, inner);
			}
			inner.put(resDir, replacement);
		}
	}
	
	// =======================================================
	//   RETURNING REPLACEMENTS
	// =======================================================
	
	private Object getReplacement(int id) {
		if (id <= 0)
			return null;
		
		HashMap<String, Object> inner;
		synchronized (replacements) {
			inner = replacements.get(id); 
		}
		
		if (inner == null)
			return null;
		
		synchronized (inner) {
			Object result = inner.get(resDir);
			if (result != null || resDir == null)
				return result;
			return inner.get(null);
		}
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof Boolean) {
			return (Boolean) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getBoolean(repId);
		}
		return super.getBoolean(id);
	}
	
	@Override
	public int getColor(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof Integer) {
			return (Integer) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getColor(repId);
		}
		return super.getColor(id);
	}
	
	@Override
	public float getDimension(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getDimension(repId);
		}
		return super.getDimension(id);
	}
	
	@Override
	public int getDimensionPixelOffset(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getDimensionPixelOffset(repId);
		}
		return super.getDimensionPixelOffset(id);
	}
	
	@Override
	public int getDimensionPixelSize(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getDimensionPixelSize(repId);
		}
		return super.getDimensionPixelSize(id);
	}
	
	@Override
	public Drawable getDrawable(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof DrawableLoader) {
			try {
				Drawable result = ((DrawableLoader) replacement).newDrawable(this, id);
				if (result != null)
					return result;
			} catch (Throwable t) { XposedBridge.log(t); }
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getDrawable(repId);
		}
		return super.getDrawable(id);
	}
	
	@Override
	public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof DrawableLoader) {
			try {
				Drawable result = ((DrawableLoader) replacement).newDrawableForDensity(this, id, density);
				if (result != null)
					return result;
			} catch (Throwable t) { XposedBridge.log(t); }
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			if (Build.VERSION.SDK_INT > 10) {
				return repRes.getDrawableForDensity(repId, density);
			} else {
				return repRes.getDrawable(repId);
			}
		}
		if (Build.VERSION.SDK_INT > 10) {
			return super.getDrawableForDensity(id, density);
		} else {
			return super.getDrawable(id);
		}
	}
	
	@Override
	public float getFraction(int id, int base, int pbase) {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getFraction(repId, base, pbase);
		}
		return super.getFraction(id, base, pbase);
	}
	
	@Override
	public int getInteger(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof Integer) {
			return (Integer) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getInteger(repId);
		}
		return super.getInteger(id);
	}
	
	@Override
	public int[] getIntArray(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof int[]) {
			return (int[]) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getIntArray(repId);
		}
		return super.getIntArray(id);
	}
	
	@Override
	public Movie getMovie(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getMovie(repId);
		}
		return super.getMovie(id);
	}
	
	@Override
	public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getQuantityText(repId, quantity);
		}
		return super.getQuantityText(id, quantity);
	}
	// these are handled by getQuantityText:
	// public String getQuantityString(int id, int quantity);
	// public String getQuantityString(int id, int quantity, Object... formatArgs);
	
	@Override
	public String[] getStringArray(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof String[]) {
			return (String[]) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getStringArray(repId);
		}
		return super.getStringArray(id);
	}
	
	@Override
	public CharSequence getText(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof CharSequence) {
			return (CharSequence) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getText(repId);
		}
		return super.getText(id);
	}
	// these are handled by getText:
	// public String getString(int id);
	// public String getString(int id, Object... formatArgs);
	
	@Override
	public CharSequence getText(int id, CharSequence def) {
		Object replacement = getReplacement(id);
		if (replacement instanceof CharSequence) {
			return (CharSequence) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getText(repId, def);
		}
		return super.getText(id, def);
	}
	
	@Override
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement instanceof CharSequence[]) {
			return (CharSequence[]) replacement;
		} else if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			return repRes.getTextArray(repId);
		}
		return super.getTextArray(id);
	}
	
	@Override
	XmlResourceParser loadXmlResourceParser(int id, String type) throws NotFoundException {
		XmlResourceParser result;
		Object replacement = getReplacement(id);
		if (replacement instanceof XResForwarder) {
			Resources repRes = ((XResForwarder) replacement).getResources();
			int repId = ((XResForwarder) replacement).getId();
			
			boolean loadFromCache = false;
			int[] mCachedXmlBlockIds = (int[]) getObjectField(repRes, "mCachedXmlBlockIds");

			synchronized (mCachedXmlBlockIds) {
				// First see if this block is in our cache.
				final int num = mCachedXmlBlockIds.length;
				for (int i=0; i<num; i++) {
					if (mCachedXmlBlockIds[i] == repId) {
						loadFromCache = true;
					}
				}
			}

			result = repRes.loadXmlResourceParser(repId, type);

			if (!loadFromCache)
				rewriteXmlReferencesNative(((XmlBlock.Parser) result).mParseState, this, repRes);
		} else {
			result = super.loadXmlResourceParser(id, type);
		}
		
		if (type.equals("layout")) {
			HashMap<String, TreeSet<XC_LayoutInflated>> inner;
			synchronized (layoutCallbacks) {
				inner = layoutCallbacks.get(id);
			}
			if (inner != null) {
				TreeSet<XC_LayoutInflated> callbacks;
				synchronized (inner) {
					callbacks = inner.get(resDir);
					if (callbacks == null && resDir != null)
						callbacks = inner.get(null);
				}
				if (callbacks != null) {
					String variant = "layout";
					TypedValue value = mTmpValue;
					getValue(id, value, true);
					if (value.type == TypedValue.TYPE_STRING) {
						String[] components = value.string.toString().split("/", 3);
						if (components.length == 3)
							variant = components[1];
						else
							XposedBridge.log("Unexpected resource path \"" + value.string.toString()
									+ "\" for resource id 0x" + Integer.toHexString(id));
					} else {
						XposedBridge.log(new NotFoundException("Could not find file name for resource id 0x") + Integer.toHexString(id));
					}
					
					synchronized (xmlInstanceDetails) {
						synchronized (resourceNames) {
							HashMap<String, ResourceNames> resNamesInner = resourceNames.get(id);
							if (resNamesInner != null) {
								synchronized (resNamesInner) {
									xmlInstanceDetails.put(result, new XMLInstanceDetails(resNamesInner.get(resDir), variant, callbacks));
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	// these are handled via loadXmlResourceParser: 
	// public XmlResourceParser getAnimation(int id);
	// public ColorStateList getColorStateList(int id);
	// public XmlResourceParser getLayout(int id);
	// public XmlResourceParser getXml(int id);
	

	private static native void rewriteXmlReferencesNative(int parserPtr, XResources origRes, Resources repRes);
	
	/**
	 * Used to replace reference IDs in XMLs.
	 * 
	 * When resource requests are forwarded to modules, the may include references to resources with the same
	 * name as in the original resources, but the IDs generated by aapt will be different. rewriteXmlReferencesNative
	 * walks through all references and calls this function to find out the original ID, which it then writes to
	 * the compiled XML file in the memory.
	 */
	private static int translateResId(int id, XResources origRes, Resources repRes) {
		try {
			String entryName = repRes.getResourceEntryName(id);
			String entryType = repRes.getResourceTypeName(id);
			String origPackage = origRes.getPackageName();
			int origResId = 0;
			try {
				// look for a resource with the same name and type in the original package
				origResId = origRes.getIdentifier(entryName, entryType, origPackage);
			} catch (NotFoundException ignored) {}
			
			boolean repResDefined = false;
			try {
				final TypedValue tmpValue = new TypedValue();
				repRes.getValue(id, tmpValue, false);
				// if a resource has not been defined (i.e. only a resource ID has been created), it will equal "false"
				// this means a boolean "false" value is not detected of it is directly referenced in an XML file
				repResDefined = !(tmpValue.type == TypedValue.TYPE_INT_BOOLEAN && tmpValue.data == 0);
			} catch (NotFoundException ignored) {}
			
			if (!repResDefined && origResId == 0 && !entryType.equals("id")) {
				XposedBridge.log(entryType + "/" + entryName + " is neither defined in module nor in original resources");
				return 0;
			}
			
			// exists only in module, so create a fake resource id
			if (origResId == 0)
				origResId = getFakeResId(repRes, id);
			
			// IDs will never be loaded, no need to set a replacement
			if (repResDefined && !entryType.equals("id"))
				origRes.setReplacement(origResId, new XResForwarder(repRes, id));
			
			return origResId;
		} catch (Exception e) {
			XposedBridge.log(e);
			return id;
		}
	}
	
	public static int getFakeResId(String resName) {
		return 0x7e000000 | (resName.hashCode() & 0x00ffffff);
	}
	
	public static int getFakeResId(Resources res, int id) {
		return getFakeResId(res.getResourceName(id));
	}
	
	public int addResource(Resources res, int id) {
		int fakeId = getFakeResId(res, id);
		if (replacements.indexOfKey(fakeId) < 0)
			setReplacement(fakeId, new XResForwarder(res, id));
		return fakeId;
	}

	/**
	 * Similar to {@link #translateResId}, but used to determine the original ID of attribute names
	 */
	private static int translateAttrId(String attrName, XResources origRes) {
		String origPackage = origRes.getPackageName();
		int origAttrId = 0;
		try {
			origAttrId = origRes.getIdentifier(attrName, "attr", origPackage);
		} catch (NotFoundException e) {
			XposedBridge.log("Attribute " + attrName + " not found in original resources");
		}
		return origAttrId;
	}
	
	// =======================================================
	//   XTypedArray class
	// =======================================================
	
	private XTypedArray newXTypedArray(int[] data, int[] indices, int len) {
		return new XTypedArray(this, data, indices, len);
	}
	
	/**
	 * {@link TypedArray} replacement that replaces values on-the-fly.
	 * Mainly used when inflating layouts.
	 */
	public class XTypedArray extends TypedArray {
		XTypedArray(Resources resources, int[] data, int[] indices, int len) {
			super(resources, data, indices, len);
		}
		
		@Override
		public boolean getBoolean(int index, boolean defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof Boolean) {
				return (Boolean) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getBoolean(repId);
			}
			return super.getBoolean(index, defValue);
		}
		
		@Override
		public int getColor(int index, int defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof Integer) {
				return (Integer) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getColor(repId);
			}
			return super.getColor(index, defValue);
		}
		
		@Override
		public float getDimension(int index, float defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getDimension(repId);
			}
			return super.getDimension(index, defValue);
		}
		
		@Override
		public int getDimensionPixelOffset(int index, int defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getDimensionPixelOffset(repId);
			}
			return super.getDimensionPixelOffset(index, defValue);
		}
		
		@Override
		public int getDimensionPixelSize(int index, int defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getDimensionPixelSize(repId);
			}
			return super.getDimensionPixelSize(index, defValue);
		}
		
		@Override
		public Drawable getDrawable(int index) {
			final int resId = getResourceId(index, 0);
			Object replacement = getReplacement(resId);
			if (replacement instanceof DrawableLoader) {
				try {
					Drawable result = ((DrawableLoader) replacement).newDrawable(XResources.this, resId);
					if (result != null)
						return result;
				} catch (Throwable t) { XposedBridge.log(t); }
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getDrawable(repId);
			}
			return super.getDrawable(index);
		}
		
		@Override
		public float getFloat(int index, float defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				// dimensions seem to be the only way to define floats by references
				return repRes.getDimension(repId);
			}
			return super.getFloat(index, defValue);
		}
		
		@Override
		public float getFraction(int index, int base, int pbase, float defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				// dimensions seem to be the only way to define floats by references
				return repRes.getFraction(repId, base, pbase);
			}
			return super.getFraction(index, base, pbase, defValue);
		}
		
		@Override
		public int getInt(int index, int defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof Integer) {
				return (Integer) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getInteger(repId);
			}
			return super.getInt(index, defValue);
		}
		
		@Override
		public int getInteger(int index, int defValue) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof Integer) {
				return (Integer) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getInteger(repId);
			}
			return super.getInteger(index, defValue);
		}
		
		@Override
		public String getString(int index) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof CharSequence) {
				return replacement.toString();
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getString(repId);
			}
			return super.getString(index);
		}
		
		@Override
		public CharSequence getText(int index) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof CharSequence) {
				return (CharSequence) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getText(repId);
			}
			return super.getText(index);
		}
		
		@Override
		public CharSequence[] getTextArray(int index) {
			Object replacement = getReplacement(getResourceId(index, 0));
			if (replacement instanceof CharSequence[]) {
				return (CharSequence[]) replacement;
			} else if (replacement instanceof XResForwarder) {
				Resources repRes = ((XResForwarder) replacement).getResources();
				int repId = ((XResForwarder) replacement).getId();
				return repRes.getTextArray(repId);
			}
			return super.getTextArray(index);
		}
		
		// this is handled by XResources.loadXmlResourceParser:
		// public ColorStateList getColorStateList(int index);
	}
	
	
	// =======================================================
	//   DrawableLoader class
	// =======================================================
	/**
	 * callback function for {@link #getDrawable} and {@link #getDrawableForDensity}
	 */
	public static abstract class DrawableLoader {
		public abstract Drawable newDrawable(XResources res, int id) throws Throwable;
		
		public Drawable newDrawableForDensity(XResources res, int id, int density) throws Throwable {
			return newDrawable(res, id);
		}
	}
	
	// =======================================================
	//   INFLATING LAYOUTS
	// =======================================================
	
	private class XMLInstanceDetails {
		public final ResourceNames resNames;
		public final String variant;
		public final TreeSet<XC_LayoutInflated> callbacks;
		public final XResources res = XResources.this;
		
		private XMLInstanceDetails(ResourceNames resNames, String variant, TreeSet<XC_LayoutInflated> callbacks) {
			this.resNames = resNames;
			this.variant = variant;
			this.callbacks = callbacks;
		}
	}
	
	/** @see #hookLayout(String, String, String, XC_LayoutInflated) */
	public XC_LayoutInflated.Unhook hookLayout(int id, XC_LayoutInflated callback) {
		return hookLayoutInternal(resDir, id, getResourceNames(id), callback);
	}
	
	/** @see #hookLayout(String, String, String, XC_LayoutInflated) */
	public XC_LayoutInflated.Unhook hookLayout(String fullName, XC_LayoutInflated callback) {
		int id = getIdentifier(fullName, null, null);
		if (id == 0)
			throw new NotFoundException(fullName);
		return hookLayout(id, callback);
	}
	
	/**
	 * Add a function to be called once a specific layout has been inflated.
	 * @param pkg Package, e.g. <code>com.android.systemui</code>
	 * @param type Type (in this case always <code>layout</code>)
	 * @param name Name of the resource (e.g. <code>statusbar</code>)
	 * @param callback Handler to be called  
	 */
	public XC_LayoutInflated.Unhook hookLayout(String pkg, String type, String name, XC_LayoutInflated callback) {
		int id = getIdentifier(name, type, pkg);
		if (id == 0)
			throw new NotFoundException(pkg + ":" + type + "/" + name);
		return hookLayout(id, callback);
	}
	
	/** @see #hookLayout(String, String, String, XC_LayoutInflated) */
	public static XC_LayoutInflated.Unhook hookSystemWideLayout(int id, XC_LayoutInflated callback) {
		if (id >= 0x7f000000)
			throw new IllegalArgumentException("ids >= 0x7f000000 are app specific and cannot be set for the framework");
		return hookLayoutInternal(null, id, getSystemResourceNames(id), callback);
	}
	
	/** @see #hookLayout(String, String, String, XC_LayoutInflated) */
	public static XC_LayoutInflated.Unhook hookSystemWideLayout(String fullName, XC_LayoutInflated callback) {
		int id = getSystem().getIdentifier(fullName, null, null);
		if (id == 0)
			throw new NotFoundException(fullName);
		return hookSystemWideLayout(id, callback);
	}
	
	/** @see #hookLayout(String, String, String, XC_LayoutInflated) */
	public static XC_LayoutInflated.Unhook hookSystemWideLayout(String pkg, String type, String name, XC_LayoutInflated callback) {
		int id = getSystem().getIdentifier(name, type, pkg);
		if (id == 0)
			throw new NotFoundException(pkg + ":" + type + "/" + name);
		return hookSystemWideLayout(id, callback);
	}
	
	private static XC_LayoutInflated.Unhook hookLayoutInternal(String resDir, int id, ResourceNames resNames, XC_LayoutInflated callback) {
		if (id == 0)
			throw new IllegalArgumentException("id 0 is not an allowed resource identifier");

		HashMap<String, TreeSet<XC_LayoutInflated>> inner;
		synchronized (layoutCallbacks) {
			inner = layoutCallbacks.get(id);
			if (inner == null) {
				inner = new HashMap<String, TreeSet<XC_LayoutInflated>>();
				layoutCallbacks.put(id, inner);
			}
		}
		
		TreeSet<XC_LayoutInflated> callbacks;
		synchronized (inner) {
			callbacks = inner.get(resDir);
			if (callbacks == null) {
				callbacks = new TreeSet<XC_LayoutInflated>();
				inner.put(resDir, callbacks);
			}
		} 
		
		synchronized (callbacks) {
			callbacks.add(callback);
		}
		
		putResourceNames(resDir, resNames);
		
		return callback.new Unhook(resDir, id);
	}
	
	public static void unhookLayout(String resDir, int id, XC_LayoutInflated callback) {
		HashMap<String, TreeSet<XC_LayoutInflated>> inner;
		synchronized (layoutCallbacks) {
			inner = layoutCallbacks.get(id);
			if (inner == null)
				return;
		}
		
		TreeSet<XC_LayoutInflated> callbacks;
		synchronized (inner) {
			callbacks = inner.get(resDir);
			if (callbacks == null)
				return;
		} 
		
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
	}
}
