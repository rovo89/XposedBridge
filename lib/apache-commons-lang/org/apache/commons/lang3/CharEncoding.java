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

package org.apache.commons.lang3;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * <p>Character encoding names required of every implementation of the Java platform.</p>
 *
 * <p>According to <a href="http://java.sun.com/j2se/1.3/docs/api/java/lang/package-summary.html#charenc">JRE character
 * encoding names</a>:</p>
 *
 * <p><cite>Every implementation of the Java platform is required to support the following character encodings.
 * Consult the release documentation for your implementation to see if any other encodings are supported.
 * </cite></p>
 *
 * @see <a href="http://download.oracle.com/javase/1.3/docs/guide/intl/encoding.doc.html">JRE character encoding names</a>
 * @since 2.1
 * @version $Id: CharEncoding.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class CharEncoding {

    /**
     * <p>ISO Latin Alphabet #1, also known as ISO-LATIN-1.</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * <p>Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block
     * of the Unicode character set.</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String US_ASCII = "US-ASCII";

    /**
     * <p>Sixteen-bit Unicode Transformation Format, byte order specified by a mandatory initial
     * byte-order mark (either order accepted on input, big-endian used on output).</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String UTF_16 = "UTF-16";

    /**
     * <p>Sixteen-bit Unicode Transformation Format, big-endian byte order.</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String UTF_16BE = "UTF-16BE";

    /**
     * <p>Sixteen-bit Unicode Transformation Format, little-endian byte order.</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String UTF_16LE = "UTF-16LE";

    /**
     * <p>Eight-bit Unicode Transformation Format.</p>
     *
     * <p>Every implementation of the Java platform is required to support this character encoding.</p>
     */
    public static final String UTF_8 = "UTF-8";

    //-----------------------------------------------------------------------
    /**
     * <p>Returns whether the named charset is supported.</p>
     *
     * <p>This is similar to <a
     * href="http://download.oracle.com/javase/1.4.2/docs/api/java/nio/charset/Charset.html#isSupported%28java.lang.String%29">
     * java.nio.charset.Charset.isSupported(String)</a> but handles more formats</p>
     *
     * @param name  the name of the requested charset; may be either a canonical name or an alias, null returns false
     * @return {@code true} if the charset is available in the current Java virtual machine
     */
    public static boolean isSupported(String name) {
        if (name == null) {
            return false;
        }
        try {
            return Charset.isSupported(name);
        } catch (IllegalCharsetNameException ex) {
            return false;
        }
    }

}
