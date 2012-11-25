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

import external.org.apache.commons.lang3.math.NumberUtils;

/**
 * <p>Operations on boolean primitives and Boolean objects.</p>
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will not be thrown for a {@code null} input.
 * Each method documents its behaviour in more detail.</p>
 *
 * <p>#ThreadSafe#</p>
 * @since 2.0
 * @version $Id: BooleanUtils.java 1199894 2011-11-09 17:53:59Z ggregory $
 */
public class BooleanUtils {

    /**
     * <p>{@code BooleanUtils} instances should NOT be constructed in standard programming.
     * Instead, the class should be used as {@code BooleanUtils.negate(true);}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public BooleanUtils() {
      super();
    }

    // Boolean utilities
    //--------------------------------------------------------------------------
    /**
     * <p>Negates the specified boolean.</p>
     *
     * <p>If {@code null} is passed in, {@code null} will be returned.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.negate(Boolean.TRUE)  = Boolean.FALSE;
     *   BooleanUtils.negate(Boolean.FALSE) = Boolean.TRUE;
     *   BooleanUtils.negate(null)          = null;
     * </pre>
     *
     * @param bool  the Boolean to negate, may be null
     * @return the negated Boolean, or {@code null} if {@code null} input
     */
    public static Boolean negate(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    }

    // boolean Boolean methods
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if a {@code Boolean} value is {@code true},
     * handling {@code null} by returning {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.isTrue(Boolean.TRUE)  = true
     *   BooleanUtils.isTrue(Boolean.FALSE) = false
     *   BooleanUtils.isTrue(null)          = false
     * </pre>
     *
     * @param bool  the boolean to check, null returns {@code false}
     * @return {@code true} only if the input is non-null and true
     * @since 2.1
     */
    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    /**
     * <p>Checks if a {@code Boolean} value is <i>not</i> {@code true},
     * handling {@code null} by returning {@code true}.</p>
     *
     * <pre>
     *   BooleanUtils.isNotTrue(Boolean.TRUE)  = false
     *   BooleanUtils.isNotTrue(Boolean.FALSE) = true
     *   BooleanUtils.isNotTrue(null)          = true
     * </pre>
     *
     * @param bool  the boolean to check, null returns {@code true}
     * @return {@code true} if the input is null or false
     * @since 2.3
     */
    public static boolean isNotTrue(Boolean bool) {
        return !isTrue(bool);
    }

    /**
     * <p>Checks if a {@code Boolean} value is {@code false},
     * handling {@code null} by returning {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.isFalse(Boolean.TRUE)  = false
     *   BooleanUtils.isFalse(Boolean.FALSE) = true
     *   BooleanUtils.isFalse(null)          = false
     * </pre>
     *
     * @param bool  the boolean to check, null returns {@code false}
     * @return {@code true} only if the input is non-null and false
     * @since 2.1
     */
    public static boolean isFalse(Boolean bool) {
        return Boolean.FALSE.equals(bool);
    }

    /**
     * <p>Checks if a {@code Boolean} value is <i>not</i> {@code false},
     * handling {@code null} by returning {@code true}.</p>
     *
     * <pre>
     *   BooleanUtils.isNotFalse(Boolean.TRUE)  = true
     *   BooleanUtils.isNotFalse(Boolean.FALSE) = false
     *   BooleanUtils.isNotFalse(null)          = true
     * </pre>
     *
     * @param bool  the boolean to check, null returns {@code true}
     * @return {@code true} if the input is null or true
     * @since 2.3
     */
    public static boolean isNotFalse(Boolean bool) {
        return !isFalse(bool);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Converts a Boolean to a boolean handling {@code null}
     * by returning {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(Boolean.TRUE)  = true
     *   BooleanUtils.toBoolean(Boolean.FALSE) = false
     *   BooleanUtils.toBoolean(null)          = false
     * </pre>
     *
     * @param bool  the boolean to convert
     * @return {@code true} or {@code false}, {@code null} returns {@code false}
     */
    public static boolean toBoolean(Boolean bool) {
        return bool != null && bool.booleanValue();
    }

    /**
     * <p>Converts a Boolean to a boolean handling {@code null}.</p>
     *
     * <pre>
     *   BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false) = true
     *   BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true) = false
     *   BooleanUtils.toBooleanDefaultIfNull(null, true)          = true
     * </pre>
     *
     * @param bool  the boolean to convert
     * @param valueIfNull  the boolean value to return if {@code null}
     * @return {@code true} or {@code false}
     */
    public static boolean toBooleanDefaultIfNull(Boolean bool, boolean valueIfNull) {
        if (bool == null) {
            return valueIfNull;
        }
        return bool.booleanValue();
    }

    // Integer to Boolean methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts an int to a boolean using the convention that {@code zero}
     * is {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(0) = false
     *   BooleanUtils.toBoolean(1) = true
     *   BooleanUtils.toBoolean(2) = true
     * </pre>
     *
     * @param value  the int to convert
     * @return {@code true} if non-zero, {@code false}
     *  if zero
     */
    public static boolean toBoolean(int value) {
        return value != 0;
    }

    /**
     * <p>Converts an int to a Boolean using the convention that {@code zero}
     * is {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(0) = Boolean.FALSE
     *   BooleanUtils.toBoolean(1) = Boolean.TRUE
     *   BooleanUtils.toBoolean(2) = Boolean.TRUE
     * </pre>
     *
     * @param value  the int to convert
     * @return Boolean.TRUE if non-zero, Boolean.FALSE if zero,
     *  {@code null} if {@code null}
     */
    public static Boolean toBooleanObject(int value) {
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * <p>Converts an Integer to a Boolean using the convention that {@code zero}
     * is {@code false}.</p>
     *
     * <p>{@code null} will be converted to {@code null}.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(Integer.valueOf(0))    = Boolean.FALSE
     *   BooleanUtils.toBoolean(Integer.valueOf(1))    = Boolean.TRUE
     *   BooleanUtils.toBoolean(Integer.valueOf(null)) = null
     * </pre>
     *
     * @param value  the Integer to convert
     * @return Boolean.TRUE if non-zero, Boolean.FALSE if zero,
     *  {@code null} if {@code null} input
     */
    public static Boolean toBooleanObject(Integer value) {
        if (value == null) {
            return null;
        }
        return value.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * <p>Converts an int to a boolean specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(0, 1, 0) = false
     *   BooleanUtils.toBoolean(1, 1, 0) = true
     *   BooleanUtils.toBoolean(2, 1, 2) = false
     *   BooleanUtils.toBoolean(2, 2, 0) = true
     * </pre>
     *
     * @param value  the Integer to convert
     * @param trueValue  the value to match for {@code true}
     * @param falseValue  the value to match for {@code false}
     * @return {@code true} or {@code false}
     * @throws IllegalArgumentException if no match
     */
    public static boolean toBoolean(int value, int trueValue, int falseValue) {
        if (value == trueValue) {
            return true;
        }
        if (value == falseValue) {
            return false;
        }
        // no match
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }

    /**
     * <p>Converts an Integer to a boolean specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0)) = false
     *   BooleanUtils.toBoolean(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0)) = true
     *   BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2)) = false
     *   BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(0)) = true
     *   BooleanUtils.toBoolean(null, null, Integer.valueOf(0))                     = true
     * </pre>
     *
     * @param value  the Integer to convert
     * @param trueValue  the value to match for {@code true}, may be {@code null}
     * @param falseValue  the value to match for {@code false}, may be {@code null}
     * @return {@code true} or {@code false}
     * @throws IllegalArgumentException if no match
     */
    public static boolean toBoolean(Integer value, Integer trueValue, Integer falseValue) {
        if (value == null) {
            if (trueValue == null) {
                return true;
            }
            if (falseValue == null) {
                return false;
            }
        } else if (value.equals(trueValue)) {
            return true;
        } else if (value.equals(falseValue)) {
            return false;
        }
        // no match
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }

    /**
     * <p>Converts an int to a Boolean specifying the conversion values.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.toBooleanObject(0, 0, 2, 3) = Boolean.TRUE
     *   BooleanUtils.toBooleanObject(2, 1, 2, 3) = Boolean.FALSE
     *   BooleanUtils.toBooleanObject(3, 1, 2, 3) = null
     * </pre>
     *
     * @param value  the Integer to convert
     * @param trueValue  the value to match for {@code true}
     * @param falseValue  the value to match for {@code false}
     * @param nullValue  the value to to match for {@code null}
     * @return Boolean.TRUE, Boolean.FALSE, or {@code null}
     * @throws IllegalArgumentException if no match
     */
    public static Boolean toBooleanObject(int value, int trueValue, int falseValue, int nullValue) {
        if (value == trueValue) {
            return Boolean.TRUE;
        }
        if (value == falseValue) {
            return Boolean.FALSE;
        }
        if (value == nullValue) {
            return null;
        }
        // no match
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }

    /**
     * <p>Converts an Integer to a Boolean specifying the conversion values.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(3)) = Boolean.TRUE
     *   BooleanUtils.toBooleanObject(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)) = Boolean.FALSE
     *   BooleanUtils.toBooleanObject(Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)) = null
     * </pre>
     *
     * @param value  the Integer to convert
     * @param trueValue  the value to match for {@code true}, may be {@code null}
     * @param falseValue  the value to match for {@code false}, may be {@code null}
     * @param nullValue  the value to to match for {@code null}, may be {@code null}
     * @return Boolean.TRUE, Boolean.FALSE, or {@code null}
     * @throws IllegalArgumentException if no match
     */
    public static Boolean toBooleanObject(Integer value, Integer trueValue, Integer falseValue, Integer nullValue) {
        if (value == null) {
            if (trueValue == null) {
                return Boolean.TRUE;
            }
            if (falseValue == null) {
                return Boolean.FALSE;
            }
            if (nullValue == null) {
                return null;
            }
        } else if (value.equals(trueValue)) {
            return Boolean.TRUE;
        } else if (value.equals(falseValue)) {
            return Boolean.FALSE;
        } else if (value.equals(nullValue)) {
            return null;
        }
        // no match
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }

    // Boolean to Integer methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a boolean to an int using the convention that
     * {@code zero} is {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.toInteger(true)  = 1
     *   BooleanUtils.toInteger(false) = 0
     * </pre>
     *
     * @param bool  the boolean to convert
     * @return one if {@code true}, zero if {@code false}
     */
    public static int toInteger(boolean bool) {
        return bool ? 1 : 0;
    }

    /**
     * <p>Converts a boolean to an Integer using the convention that
     * {@code zero} is {@code false}.</p>
     *
     * <pre>
     *   BooleanUtils.toIntegerObject(true)  = Integer.valueOf(1)
     *   BooleanUtils.toIntegerObject(false) = Integer.valueOf(0)
     * </pre>
     *
     * @param bool  the boolean to convert
     * @return one if {@code true}, zero if {@code false}
     */
    public static Integer toIntegerObject(boolean bool) {
        return bool ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO;
    }

    /**
     * <p>Converts a Boolean to a Integer using the convention that
     * {@code zero} is {@code false}.</p>
     *
     * <p>{@code null} will be converted to {@code null}.</p>
     *
     * <pre>
     *   BooleanUtils.toIntegerObject(Boolean.TRUE)  = Integer.valueOf(1)
     *   BooleanUtils.toIntegerObject(Boolean.FALSE) = Integer.valueOf(0)
     * </pre>
     *
     * @param bool  the Boolean to convert
     * @return one if Boolean.TRUE, zero if Boolean.FALSE, {@code null} if {@code null}
     */
    public static Integer toIntegerObject(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool.booleanValue() ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO;
    }

    /**
     * <p>Converts a boolean to an int specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toInteger(true, 1, 0)  = 1
     *   BooleanUtils.toInteger(false, 1, 0) = 0
     * </pre>
     *
     * @param bool  the to convert
     * @param trueValue  the value to return if {@code true}
     * @param falseValue  the value to return if {@code false}
     * @return the appropriate value
     */
    public static int toInteger(boolean bool, int trueValue, int falseValue) {
        return bool ? trueValue : falseValue;
    }

    /**
     * <p>Converts a Boolean to an int specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toInteger(Boolean.TRUE, 1, 0, 2)  = 1
     *   BooleanUtils.toInteger(Boolean.FALSE, 1, 0, 2) = 0
     *   BooleanUtils.toInteger(null, 1, 0, 2)          = 2
     * </pre>
     *
     * @param bool  the Boolean to convert
     * @param trueValue  the value to return if {@code true}
     * @param falseValue  the value to return if {@code false}
     * @param nullValue  the value to return if {@code null}
     * @return the appropriate value
     */
    public static int toInteger(Boolean bool, int trueValue, int falseValue, int nullValue) {
        if (bool == null) {
            return nullValue;
        }
        return bool.booleanValue() ? trueValue : falseValue;
    }

    /**
     * <p>Converts a boolean to an Integer specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toIntegerObject(true, Integer.valueOf(1), Integer.valueOf(0))  = Integer.valueOf(1)
     *   BooleanUtils.toIntegerObject(false, Integer.valueOf(1), Integer.valueOf(0)) = Integer.valueOf(0)
     * </pre>
     *
     * @param bool  the to convert
     * @param trueValue  the value to return if {@code true}, may be {@code null}
     * @param falseValue  the value to return if {@code false}, may be {@code null}
     * @return the appropriate value
     */
    public static Integer toIntegerObject(boolean bool, Integer trueValue, Integer falseValue) {
        return bool ? trueValue : falseValue;
    }

    /**
     * <p>Converts a Boolean to an Integer specifying the conversion values.</p>
     *
     * <pre>
     *   BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2))  = Integer.valueOf(1)
     *   BooleanUtils.toIntegerObject(Boolean.FALSE, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2)) = Integer.valueOf(0)
     *   BooleanUtils.toIntegerObject(null, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2))          = Integer.valueOf(2)
     * </pre>
     *
     * @param bool  the Boolean to convert
     * @param trueValue  the value to return if {@code true}, may be {@code null}
     * @param falseValue  the value to return if {@code false}, may be {@code null}
     * @param nullValue  the value to return if {@code null}, may be {@code null}
     * @return the appropriate value
     */
    public static Integer toIntegerObject(Boolean bool, Integer trueValue, Integer falseValue, Integer nullValue) {
        if (bool == null) {
            return nullValue;
        }
        return bool.booleanValue() ? trueValue : falseValue;
    }

    // String to Boolean methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to a Boolean.</p>
     *
     * <p>{@code 'true'}, {@code 'on'} or {@code 'yes'}
     * (case insensitive) will return {@code true}.
     * {@code 'false'}, {@code 'off'} or {@code 'no'}
     * (case insensitive) will return {@code false}.
     * Otherwise, {@code null} is returned.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.toBooleanObject(null)    = null
     *   BooleanUtils.toBooleanObject("true")  = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("false") = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("on")    = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("ON")    = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("off")   = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("oFf")   = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("blue")  = null
     * </pre>
     *
     * @param str  the String to check
     * @return the Boolean value of the string, {@code null} if no match or {@code null} input
     */
    public static Boolean toBooleanObject(String str) {
        // Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        //
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
            return Boolean.TRUE;
        }
        if (str == null) {
            return null;
        }
        switch (str.length()) {
            case 1: {
                char ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' ||
                    ch0 == 't' || ch0 == 'T') {
                    return Boolean.TRUE;
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                    ch0 == 'f' || ch0 == 'F') {
                    return Boolean.FALSE;
                }
                break;
            }
            case 2: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'n' || ch1 == 'N') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'n' || ch0 == 'N') &&
                    (ch1 == 'o' || ch1 == 'O') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 3: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') &&
                    (ch1 == 'e' || ch1 == 'E') &&
                    (ch2 == 's' || ch2 == 'S') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'f' || ch1 == 'F') &&
                    (ch2 == 'f' || ch2 == 'F') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 4: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                char ch3 = str.charAt(3);
                if ((ch0 == 't' || ch0 == 'T') &&
                    (ch1 == 'r' || ch1 == 'R') &&
                    (ch2 == 'u' || ch2 == 'U') &&
                    (ch3 == 'e' || ch3 == 'E') ) {
                    return Boolean.TRUE;
                }
                break;
            }
            case 5: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                char ch3 = str.charAt(3);
                char ch4 = str.charAt(4);
                if ((ch0 == 'f' || ch0 == 'F') &&
                    (ch1 == 'a' || ch1 == 'A') &&
                    (ch2 == 'l' || ch2 == 'L') &&
                    (ch3 == 's' || ch3 == 'S') &&
                    (ch4 == 'e' || ch4 == 'E') ) {
                    return Boolean.FALSE;
                }
                break;
            }
        }

        return null;
    }

    /**
     * <p>Converts a String to a Boolean throwing an exception if no match.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   BooleanUtils.toBooleanObject("true", "true", "false", "null")  = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("false", "true", "false", "null") = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("null", "true", "false", "null")  = null
     * </pre>
     *
     * @param str  the String to check
     * @param trueString  the String to match for {@code true} (case sensitive), may be {@code null}
     * @param falseString  the String to match for {@code false} (case sensitive), may be {@code null}
     * @param nullString  the String to match for {@code null} (case sensitive), may be {@code null}
     * @return the Boolean value of the string, {@code null} if either the String matches {@code nullString}
     *  or if {@code null} input and {@code nullString} is {@code null}
     * @throws IllegalArgumentException if the String doesn't match
     */
    public static Boolean toBooleanObject(String str, String trueString, String falseString, String nullString) {
        if (str == null) {
            if (trueString == null) {
                return Boolean.TRUE;
            }
            if (falseString == null) {
                return Boolean.FALSE;
            }
            if (nullString == null) {
                return null;
            }
        } else if (str.equals(trueString)) {
            return Boolean.TRUE;
        } else if (str.equals(falseString)) {
            return Boolean.FALSE;
        } else if (str.equals(nullString)) {
            return null;
        }
        // no match
        throw new IllegalArgumentException("The String did not match any specified value");
    }

    // String to boolean methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to a boolean (optimised for performance).</p>
     *
     * <p>{@code 'true'}, {@code 'on'} or {@code 'yes'}
     * (case insensitive) will return {@code true}. Otherwise,
     * {@code false} is returned.</p>
     *
     * <p>This method performs 4 times faster (JDK1.4) than
     * {@code Boolean.valueOf(String)}. However, this method accepts
     * 'on' and 'yes' as true values.
     *
     * <pre>
     *   BooleanUtils.toBoolean(null)    = false
     *   BooleanUtils.toBoolean("true")  = true
     *   BooleanUtils.toBoolean("TRUE")  = true
     *   BooleanUtils.toBoolean("tRUe")  = true
     *   BooleanUtils.toBoolean("on")    = true
     *   BooleanUtils.toBoolean("yes")   = true
     *   BooleanUtils.toBoolean("false") = false
     *   BooleanUtils.toBoolean("x gti") = false
     * </pre>
     *
     * @param str  the String to check
     * @return the boolean value of the string, {@code false} if no match or the String is null
     */
    public static boolean toBoolean(String str) {
        return toBooleanObject(str) == Boolean.TRUE;
    }

    /**
     * <p>Converts a String to a Boolean throwing an exception if no match found.</p>
     *
     * <pre>
     *   BooleanUtils.toBoolean("true", "true", "false")  = true
     *   BooleanUtils.toBoolean("false", "true", "false") = false
     * </pre>
     *
     * @param str  the String to check
     * @param trueString  the String to match for {@code true} (case sensitive), may be {@code null}
     * @param falseString  the String to match for {@code false} (case sensitive), may be {@code null}
     * @return the boolean value of the string
     * @throws IllegalArgumentException if the String doesn't match
     */
    public static boolean toBoolean(String str, String trueString, String falseString) {
        if (str == trueString) {
            return true;
        } else if (str == falseString) {
            return false;
        } else if (str != null) {
            if (str.equals(trueString)) {
                return true;
            } else if (str.equals(falseString)) {
                return false;
            }
        }
        // no match
        throw new IllegalArgumentException("The String did not match either specified value");
    }

    // Boolean to String methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a Boolean to a String returning {@code 'true'},
     * {@code 'false'}, or {@code null}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringTrueFalse(Boolean.TRUE)  = "true"
     *   BooleanUtils.toStringTrueFalse(Boolean.FALSE) = "false"
     *   BooleanUtils.toStringTrueFalse(null)          = null;
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'true'}, {@code 'false'}, or {@code null}
     */
    public static String toStringTrueFalse(Boolean bool) {
        return toString(bool, "true", "false", null);
    }

    /**
     * <p>Converts a Boolean to a String returning {@code 'on'},
     * {@code 'off'}, or {@code null}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringOnOff(Boolean.TRUE)  = "on"
     *   BooleanUtils.toStringOnOff(Boolean.FALSE) = "off"
     *   BooleanUtils.toStringOnOff(null)          = null;
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'on'}, {@code 'off'}, or {@code null}
     */
    public static String toStringOnOff(Boolean bool) {
        return toString(bool, "on", "off", null);
    }

    /**
     * <p>Converts a Boolean to a String returning {@code 'yes'},
     * {@code 'no'}, or {@code null}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringYesNo(Boolean.TRUE)  = "yes"
     *   BooleanUtils.toStringYesNo(Boolean.FALSE) = "no"
     *   BooleanUtils.toStringYesNo(null)          = null;
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'yes'}, {@code 'no'}, or {@code null}
     */
    public static String toStringYesNo(Boolean bool) {
        return toString(bool, "yes", "no", null);
    }

    /**
     * <p>Converts a Boolean to a String returning one of the input Strings.</p>
     *
     * <pre>
     *   BooleanUtils.toString(Boolean.TRUE, "true", "false", null)   = "true"
     *   BooleanUtils.toString(Boolean.FALSE, "true", "false", null)  = "false"
     *   BooleanUtils.toString(null, "true", "false", null)           = null;
     * </pre>
     *
     * @param bool  the Boolean to check
     * @param trueString  the String to return if {@code true}, may be {@code null}
     * @param falseString  the String to return if {@code false}, may be {@code null}
     * @param nullString  the String to return if {@code null}, may be {@code null}
     * @return one of the three input Strings
     */
    public static String toString(Boolean bool, String trueString, String falseString, String nullString) {
        if (bool == null) {
            return nullString;
        }
        return bool.booleanValue() ? trueString : falseString;
    }

    // boolean to String methods
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a boolean to a String returning {@code 'true'}
     * or {@code 'false'}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringTrueFalse(true)   = "true"
     *   BooleanUtils.toStringTrueFalse(false)  = "false"
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'true'}, {@code 'false'}, or {@code null}
     */
    public static String toStringTrueFalse(boolean bool) {
        return toString(bool, "true", "false");
    }

    /**
     * <p>Converts a boolean to a String returning {@code 'on'}
     * or {@code 'off'}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringOnOff(true)   = "on"
     *   BooleanUtils.toStringOnOff(false)  = "off"
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'on'}, {@code 'off'}, or {@code null}
     */
    public static String toStringOnOff(boolean bool) {
        return toString(bool, "on", "off");
    }

    /**
     * <p>Converts a boolean to a String returning {@code 'yes'}
     * or {@code 'no'}.</p>
     *
     * <pre>
     *   BooleanUtils.toStringYesNo(true)   = "yes"
     *   BooleanUtils.toStringYesNo(false)  = "no"
     * </pre>
     *
     * @param bool  the Boolean to check
     * @return {@code 'yes'}, {@code 'no'}, or {@code null}
     */
    public static String toStringYesNo(boolean bool) {
        return toString(bool, "yes", "no");
    }

    /**
     * <p>Converts a boolean to a String returning one of the input Strings.</p>
     *
     * <pre>
     *   BooleanUtils.toString(true, "true", "false")   = "true"
     *   BooleanUtils.toString(false, "true", "false")  = "false"
     * </pre>
     *
     * @param bool  the Boolean to check
     * @param trueString  the String to return if {@code true}, may be {@code null}
     * @param falseString  the String to return if {@code false}, may be {@code null}
     * @return one of the two input Strings
     */
    public static String toString(boolean bool, String trueString, String falseString) {
        return bool ? trueString : falseString;
    }

    // logical operations
    // ----------------------------------------------------------------------
    /**
     * <p>Performs an and on a set of booleans.</p>
     *
     * <pre>
     *   BooleanUtils.and(true, true)         = true
     *   BooleanUtils.and(false, false)       = false
     *   BooleanUtils.and(true, false)        = false
     *   BooleanUtils.and(true, true, false)  = false
     *   BooleanUtils.and(true, true, true)   = true
     * </pre>
     *
     * @param array  an array of {@code boolean}s
     * @return {@code true} if the and is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     * @since 3.0.1
     */
    public static boolean and(boolean... array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        for (boolean element : array) {
            if (!element) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Performs an and on an array of Booleans.</p>
     *
     * <pre>
     *   BooleanUtils.and(Boolean.TRUE, Boolean.TRUE)                 = Boolean.TRUE
     *   BooleanUtils.and(Boolean.FALSE, Boolean.FALSE)               = Boolean.FALSE
     *   BooleanUtils.and(Boolean.TRUE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanUtils.and(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanUtils.and(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE) = Boolean.FALSE
     *   BooleanUtils.and(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)  = Boolean.FALSE
     * </pre>
     *
     * @param array  an array of {@code Boolean}s
     * @return {@code true} if the and is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     * @throws IllegalArgumentException if {@code array} contains a {@code null}
     * @since 3.0.1
     */
    public static Boolean and(Boolean... array) {
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            boolean[] primitive = ArrayUtils.toPrimitive(array);
            return and(primitive) ? Boolean.TRUE : Boolean.FALSE;
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

    /**
     * <p>Performs an or on a set of booleans.</p>
     *
     * <pre>
     *   BooleanUtils.or(true, true)          = true
     *   BooleanUtils.or(false, false)        = false
     *   BooleanUtils.or(true, false)         = true
     *   BooleanUtils.or(true, true, false)   = true
     *   BooleanUtils.or(true, true, true)    = true
     *   BooleanUtils.or(false, false, false) = false
     * </pre>
     *
     * @param array  an array of {@code boolean}s
     * @return {@code true} if the or is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     * @since 3.0.1
     */
    public static boolean or(boolean... array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        for (boolean element : array) {
            if (element) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Performs an or on an array of Booleans.</p>
     *
     * <pre>
     *   BooleanUtils.or(Boolean.TRUE, Boolean.TRUE)                  = Boolean.TRUE
     *   BooleanUtils.or(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanUtils.or(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     *   BooleanUtils.or(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     *   BooleanUtils.or(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE)  = Boolean.TRUE
     *   BooleanUtils.or(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanUtils.or(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     * </pre>
     *
     * @param array  an array of {@code Boolean}s
     * @return {@code true} if the or is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     * @throws IllegalArgumentException if {@code array} contains a {@code null}
     * @since 3.0.1
     */
    public static Boolean or(Boolean... array) {
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            boolean[] primitive = ArrayUtils.toPrimitive(array);
            return or(primitive) ? Boolean.TRUE : Boolean.FALSE;
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

    /**
     * <p>Performs an xor on a set of booleans.</p>
     *
     * <pre>
     *   BooleanUtils.xor(true, true)   = false
     *   BooleanUtils.xor(false, false) = false
     *   BooleanUtils.xor(true, false)  = true
     *   BooleanUtils.xor(true, true)   = false
     *   BooleanUtils.xor(false, false) = false
     *   BooleanUtils.xor(true, false)  = true
     * </pre>
     *
     * @param array  an array of {@code boolean}s
     * @return {@code true} if the xor is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     */
    public static boolean xor(boolean... array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }

        // Loops through array, comparing each item
        int trueCount = 0;
        for (boolean element : array) {
            // If item is true, and trueCount is < 1, increments count
            // Else, xor fails
            if (element) {
                if (trueCount < 1) {
                    trueCount++;
                } else {
                    return false;
                }
            }
        }

        // Returns true if there was exactly 1 true item
        return trueCount == 1;
    }

    /**
     * <p>Performs an xor on an array of Booleans.</p>
     *
     * <pre>
     *   BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE })   = Boolean.FALSE
     *   BooleanUtils.xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE }) = Boolean.FALSE
     *   BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE })  = Boolean.TRUE
     * </pre>
     *
     * @param array  an array of {@code Boolean}s
     * @return {@code true} if the xor is successful.
     * @throws IllegalArgumentException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty.
     * @throws IllegalArgumentException if {@code array} contains a {@code null}
     */
    public static Boolean xor(Boolean... array) {
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            boolean[] primitive = ArrayUtils.toPrimitive(array);
            return xor(primitive) ? Boolean.TRUE : Boolean.FALSE;
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

}
