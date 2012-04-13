package de.robv.android.xposed;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.res.Resources;

public class XResources extends Resources {
	private static final HashMap<Integer, HashMap<String, Object>> replacements = new HashMap<Integer, HashMap<String, Object>>();
	private static final HashMap<String, String> resDirToPackage = new HashMap<String, String>();
	private static final HashMap<String, Long> resDirLastModified = new HashMap<String, Long>();
	private static Field field_mCompatibilityInfo;

	private final String resDir;
	
	public XResources(Resources parent, String resDir) {
		super(parent.getAssets(), parent.getDisplayMetrics(), parent.getConfiguration());
		this.resDir = resDir;
		
		try {
			if (field_mCompatibilityInfo == null) {
				field_mCompatibilityInfo = Resources.class.getDeclaredField("mCompatibilityInfo");
				field_mCompatibilityInfo.setAccessible(true);
			}
			field_mCompatibilityInfo.set(this, field_mCompatibilityInfo.get(parent));
		} catch (Exception e) {
			XposedBridge.log(e);
			return;
		}
	}
	
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
			for (HashMap<String, Object> inner : replacements.values()) {
				inner.remove(resDir);
			}
			return true;
		}
	}

	public static void setPackageNameForResDir(String resDir, String packageName) {
		resDirToPackage.put(resDir, packageName);
	}
	
	public String getResDir() {
		return resDir;
	}
	
	public String getPackageName() {
		return resDirToPackage.get(resDir);
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
		HashMap<String, Object> inner = replacements.get(id);
		if (inner == null)
			return null;
		Object result = inner.get(resDir);
		if (result != null || resDir == null)
			return result;
		return inner.get(null);
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement != null && replacement instanceof Boolean)
			return (Boolean) replacement;
		return super.getBoolean(id);
	}

	@Override
	public int getInteger(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement != null && replacement instanceof Integer)
			return (Integer) replacement;	
		return super.getInteger(id);
	}
	
	@Override
	public CharSequence getText(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement != null && replacement instanceof CharSequence)
			return (CharSequence) replacement;
		return super.getText(id);
	}
	
	@Override
	public int getColor(int id) throws NotFoundException {
		Object replacement = getReplacement(id);
		if (replacement != null && replacement instanceof Integer)
			return (Integer) replacement;
		return super.getColor(id);
	}

}
