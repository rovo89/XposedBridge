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
package external.org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;

/**
 * <p>Utility library to provide helper methods for Java enums.</p>
 *
 * <p>#ThreadSafe#</p>
 *
 * @since 3.0
 * @version $Id: EnumUtils.java 1199894 2011-11-09 17:53:59Z ggregory $
 */
public class EnumUtils {

    /**
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     */
    public EnumUtils() {
    }

    /**
     * <p>Gets the {@code Map} of enums by name.</p>
     *
     * <p>This method is useful when you need a map of enums by name.</p>
     *
     * @param <E> the type of the enumeration
     * @param enumClass  the class of the enum to query, not null
     * @return the modifiable map of enum names to enums, never null
     */
    public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumClass) {
        Map<String, E> map = new LinkedHashMap<String, E>();
        for (E e: enumClass.getEnumConstants()) {
            map.put(e.name(), e);
        }
        return map;
    }

    /**
     * <p>Gets the {@code List} of enums.</p>
     *
     * <p>This method is useful when you need a list of enums rather than an array.</p>
     *
     * @param <E> the type of the enumeration
     * @param enumClass  the class of the enum to query, not null
     * @return the modifiable list of enums, never null
     */
    public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
        return new ArrayList<E>(Arrays.asList(enumClass.getEnumConstants()));
    }

    /**
     * <p>Checks if the specified name is a valid enum for the class.</p>
     *
     * <p>This method differs from {@link Enum#valueOf} in that checks if the name is
     * a valid enum without needing to catch the exception.</p>
     *
     * @param <E> the type of the enumeration
     * @param enumClass  the class of the enum to query, not null
     * @param enumName   the enum name, null returns false
     * @return true if the enum name is valid, otherwise false
     */
    public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
        if (enumName == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * <p>Gets the enum for the class, returning {@code null} if not found.</p>
     *
     * <p>This method differs from {@link Enum#valueOf} in that it does not throw an exception
     * for an invalid enum name.</p>
     *
     * @param <E> the type of the enumeration
     * @param enumClass  the class of the enum to query, not null
     * @param enumName   the enum name, null returns null
     * @return the enum, null if not found
     */
    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName) {
        if (enumName == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, enumName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * <p>Creates a long bit vector representation of the given subset of an Enum.</p>
     *
     * <p>This generates a value that is usable by {@link EnumUtils#processBitVector}.</p>
     *
     * <p>Do not use this method if you have more than 64 values in your Enum, as this
     * would create a value greater than a long can hold.</p>
     *
     * @param enumClass the class of the enum we are working with, not {@code null}
     * @param values    the values we want to convert, not {@code null}
     * @param <E>       the type of the enumeration
     * @return a long whose binary value represents the given set of enum values.
     * @throws NullPointerException if {@code enumClass} or {@code values} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class or has more than 64 values
     * @since 3.0.1
     */
    public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, Iterable<E> values) {
        checkBitVectorable(enumClass);
        Validate.notNull(values);
        long total = 0;
        for (E constant : values) {
            total |= 1 << constant.ordinal();
        }
        return total;
    }

    /**
     * <p>Creates a long bit vector representation of the given array of Enum values.</p>
     *
     * <p>This generates a value that is usable by {@link EnumUtils#processBitVector}.</p>
     *
     * <p>Do not use this method if you have more than 64 values in your Enum, as this
     * would create a value greater than a long can hold.</p>
     *
     * @param enumClass the class of the enum we are working with, not {@code null}
     * @param values    the values we want to convert, not {@code null}
     * @param <E>       the type of the enumeration
     * @return a long whose binary value represents the given set of enum values.
     * @throws NullPointerException if {@code enumClass} or {@code values} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class or has more than 64 values
     * @since 3.0.1
     */
    public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, E... values) {
        Validate.noNullElements(values);
        return generateBitVector(enumClass, Arrays.<E> asList(values));
    }

    /**
     * <p>Convert a long value created by {@link EnumUtils#generateBitVector} into the set of
     * enum values that it represents.</p>
     *
     * <p>If you store this value, beware any changes to the enum that would affect ordinal values.</p>
     * @param enumClass the class of the enum we are working with, not {@code null}
     * @param value     the long value representation of a set of enum values
     * @param <E>       the type of the enumeration
     * @return a set of enum values
     * @throws NullPointerException if {@code enumClass} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class or has more than 64 values
     * @since 3.0.1
     */
    public static <E extends Enum<E>> EnumSet<E> processBitVector(Class<E> enumClass, long value) {
        final E[] constants = checkBitVectorable(enumClass).getEnumConstants();
        final EnumSet<E> results = EnumSet.noneOf(enumClass);
        for (E constant : constants) {
            if ((value & 1 << constant.ordinal()) != 0) {
                results.add(constant);
            }
        }
        return results;
    }

    /**
     * Validate that {@code enumClass} is compatible with representation in a {@code long}.
     * @param <E> the type of the enumeration
     * @param enumClass to check
     * @return {@code enumClass}
     * @throws NullPointerException if {@code enumClass} is {@code null}
     * @throws IllegalArgumentException if {@code enumClass} is not an enum class or has more than 64 values
     * @since 3.0.1
     */
    private static <E extends Enum<E>> Class<E> checkBitVectorable(Class<E> enumClass) {
        Validate.notNull(enumClass, "EnumClass must be defined.");

        final E[] constants = enumClass.getEnumConstants();
        Validate.isTrue(constants != null, "%s does not seem to be an Enum type", enumClass);
        Validate.isTrue(constants.length <= Long.SIZE, "Cannot store %s %s values in %s bits", constants.length,
            enumClass.getSimpleName(), Long.SIZE);

        return enumClass;
    }
}
