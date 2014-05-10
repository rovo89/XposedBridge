package de.robv.android.xposed;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.android.internal.util.XmlUtils;

/**
 * This class is basically the same as SharedPreferencesImpl from AOSP, but
 * read-only and without listeners support. Instead, it is made to be
 * compatible with all ROMs.
 */
public final class XSharedPreferences implements SharedPreferences {
	private static final String TAG = "ReadOnlySharedPreferences";
	private final File mFile;
	private Map<String, Object> mMap;
	private boolean mLoaded = false;
	private long mLastModified;
	private long mFileSize;

	public XSharedPreferences(File prefFile) {
		mFile = prefFile;
		startLoadFromDisk();
	}

	public XSharedPreferences(String packageName) {
		this(packageName, packageName + "_preferences");
	}

	public XSharedPreferences(String packageName, String prefFileName) {
		mFile = new File(Environment.getDataDirectory(), "data/" + packageName + "/shared_prefs/" + prefFileName + ".xml");
		startLoadFromDisk();
	}

	public boolean makeWorldReadable() {
		if (!mFile.exists()) // just in case - the file should never be created if it doesn'e exist
			return false;

		return mFile.setReadable(true, false);
	}

	private void startLoadFromDisk() {
		synchronized (this) {
			mLoaded = false;
		}
		new Thread("XSharedPreferences-load") {
			@Override
			public void run() {
				synchronized (XSharedPreferences.this) {
					loadFromDiskLocked();
				}
			}
		}.start();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadFromDiskLocked() {
		if (mLoaded) {
			return;
		}

		Map map = null;
		long lastModified = 0;
		long fileSize = 0;
		if (mFile.canRead()) {
			lastModified = mFile.lastModified();
			fileSize = mFile.length();
			BufferedInputStream str = null;
			try {
				str = new BufferedInputStream(
						new FileInputStream(mFile), 16*1024);
				map = XmlUtils.readMapXml(str);
				str.close();
			} catch (XmlPullParserException e) {
				Log.w(TAG, "getSharedPreferences", e);
			} catch (IOException e) {
				Log.w(TAG, "getSharedPreferences", e);
			} finally {
				if (str != null) {
					try {
						str.close();
					} catch (RuntimeException rethrown) {
						throw rethrown;
					} catch (Exception ignored) {
					}
				}
			}
		}
		mLoaded = true;
		if (map != null) {
			mMap = map;
			mLastModified = lastModified;
			mFileSize = fileSize;
		} else {
			mMap = new HashMap<String, Object>();
		}
		notifyAll();
	}

	/**
	 * Reload the settings from file if they have changed.
	 */
	public void reload() {
		synchronized (this) {
			if (hasFileChanged())
				startLoadFromDisk();
		}
	}

	private boolean hasFileChanged() {
		if (!mFile.canRead()) {
			return true;
		}
		long lastModified = mFile.lastModified();
		long fileSize = mFile.length();
		synchronized (this) {
			return mLastModified != lastModified || mFileSize != fileSize;
		}
	}

	private void awaitLoadedLocked() {
		while (!mLoaded) {
			try {
				wait();
			} catch (InterruptedException unused) {
			}
		}
	}

	@Override
	public Map<String, ?> getAll() {
		synchronized (this) {
			awaitLoadedLocked();
			return new HashMap<String, Object>(mMap);
		}
	}

	@Override
	public String getString(String key, String defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			String v = (String)mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getStringSet(String key, Set<String> defValues) {
		synchronized (this) {
			awaitLoadedLocked();
			Set<String> v = (Set<String>) mMap.get(key);
			return v != null ? v : defValues;
		}
	}

	@Override
	public int getInt(String key, int defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Integer v = (Integer)mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	@Override
	public long getLong(String key, long defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Long v = (Long)mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	@Override
	public float getFloat(String key, float defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Float v = (Float)mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		synchronized (this) {
			awaitLoadedLocked();
			Boolean v = (Boolean)mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	@Override
	public boolean contains(String key) {
		synchronized (this) {
			awaitLoadedLocked();
			return mMap.containsKey(key);
		}
	}

	@Override
	public Editor edit() {
		throw new UnsupportedOperationException("read-only implementation");
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		throw new UnsupportedOperationException("listeners are not supported in this implementation");
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		throw new UnsupportedOperationException("listeners are not supported in this implementation");
	}

}
