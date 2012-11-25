/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package external.org.apache.commons.lang3.text;

import java.util.Map;

/**
 * Lookup a String key to a String value.
 * <p>
 * This class represents the simplest form of a string to string map.
 * It has a benefit over a map in that it can create the result on
 * demand based on the key.
 * <p>
 * This class comes complete with various factory methods.
 * If these do not suffice, you can subclass and implement your own matcher.
 * <p>
 * For example, it would be possible to implement a lookup that used the
 * key as a primary key, and looked up the value on demand from the database
 *
 * @since 2.2
 * @version $Id: StrLookup.java 1153484 2011-08-03 13:39:42Z ggregory $
 */
public abstract class StrLookup<V> {

    /**
     * Lookup that always returns null.
     */
    private static final StrLookup<String> NONE_LOOKUP;
    /**
     * Lookup that uses System properties.
     */
    private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP;
    static {
        NONE_LOOKUP = new MapStrLookup<String>(null);
        StrLookup<String> lookup = null;
        try {
            final Map<?, ?> propMap = System.getProperties();
            @SuppressWarnings("unchecked") // System property keys and values are always Strings
            final Map<String, String> properties = (Map<String, String>) propMap;
            lookup = new MapStrLookup<String>(properties);
        } catch (SecurityException ex) {
            lookup = NONE_LOOKUP;
        }
        SYSTEM_PROPERTIES_LOOKUP = lookup;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a lookup which always returns null.
     *
     * @return a lookup that always returns null, not null
     */
    public static StrLookup<?> noneLookup() {
        return NONE_LOOKUP;
    }

    /**
     * Returns a lookup which uses {@link System#getProperties() System properties}
     * to lookup the key to value.
     * <p>
     * If a security manager blocked access to system properties, then null will
     * be returned from every lookup.
     * <p>
     * If a null key is used, this lookup will throw a NullPointerException.
     *
     * @return a lookup using system properties, not null
     */
    public static StrLookup<String> systemPropertiesLookup() {
        return SYSTEM_PROPERTIES_LOOKUP;
    }

    /**
     * Returns a lookup which looks up values using a map.
     * <p>
     * If the map is null, then null will be returned from every lookup.
     * The map result object is converted to a string using toString().
     *
     * @param <V> the type of the values supported by the lookup
     * @param map  the map of keys to values, may be null
     * @return a lookup using the map, not null
     */
    public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
        return new MapStrLookup<V>(map);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     */
    protected StrLookup() {
        super();
    }

    /**
     * Looks up a String key to a String value.
     * <p>
     * The internal implementation may use any mechanism to return the value.
     * The simplest implementation is to use a Map. However, virtually any
     * implementation is possible.
     * <p>
     * For example, it would be possible to implement a lookup that used the
     * key as a primary key, and looked up the value on demand from the database
     * Or, a numeric based implementation could be created that treats the key
     * as an integer, increments the value and return the result as a string -
     * converting 1 to 2, 15 to 16 etc.
     * <p>
     * The {@link #lookup(String)} method always returns a String, regardless of
     * the underlying data, by converting it as necessary. For example:
     * <pre>
     * Map<String, Object> map = new HashMap<String, Object>();
     * map.put("number", Integer.valueOf(2));
     * assertEquals("2", StrLookup.mapLookup(map).lookup("number"));
     * </pre>
     * @param key  the key to be looked up, may be null
     * @return the matching value, null if no match
     */
    public abstract String lookup(String key);

    //-----------------------------------------------------------------------
    /**
     * Lookup implementation that uses a Map.
     */
    static class MapStrLookup<V> extends StrLookup<V> {

        /** Map keys are variable names and value. */
        private final Map<String, V> map;

        /**
         * Creates a new instance backed by a Map.
         *
         * @param map  the map of keys to values, may be null
         */
        MapStrLookup(Map<String, V> map) {
            this.map = map;
        }

        /**
         * Looks up a String key to a String value using the map.
         * <p>
         * If the map is null, then null is returned.
         * The map result object is converted to a string using toString().
         *
         * @param key  the key to be looked up, may be null
         * @return the matching value, null if no match
         */
        @Override
        public String lookup(String key) {
            if (map == null) {
                return null;
            }
            Object obj = map.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }
}
