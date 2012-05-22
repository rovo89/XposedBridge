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
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Translates codepoints to their Unicode escaped value.
 *
 * @since 3.0
 * @version $Id: UnicodeEscaper.java 1148520 2011-07-19 20:53:23Z ggregory $
 */
public class UnicodeEscaper extends CodePointTranslator {

    private final int below;
    private final int above;
    private final boolean between;

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> for all characters. </p>
     */
    public UnicodeEscaper(){
        this(0, Integer.MAX_VALUE, true);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> for the specified range. This is
     * the underlying method for the other constructors/builders. The <code>below</code>
     * and <code>above</code> boundaries are inclusive when <code>between</code> is
     * <code>true</code> and exclusive when it is <code>false</code>. </p>
     *
     * @param below int value representing the lowest codepoint boundary
     * @param above int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    private UnicodeEscaper(int below, int above, boolean between) {
        this.below = below;
        this.above = above;
        this.between = between;
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> below the specified value (exclusive). </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper below(int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> above the specified value (exclusive). </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper above(int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> outside of the specified values (exclusive). </p>
     *
     * @param codepointLow below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper outsideOf(int codepointLow, int codepointHigh) {
        return new UnicodeEscaper(codepointLow, codepointHigh, false);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> between the specified values (inclusive). </p>
     *
     * @param codepointLow above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper between(int codepointLow, int codepointHigh) {
        return new UnicodeEscaper(codepointLow, codepointHigh, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean translate(int codepoint, Writer out) throws IOException {
        if(between) {
            if (codepoint < below || codepoint > above) {
                return false;
            }
        } else {
            if (codepoint >= below && codepoint <= above) {
                return false;
            }
        }

        // TODO: Handle potential + sign per various Unicode escape implementations
        if (codepoint > 0xffff) {
            // TODO: Figure out what to do. Output as two Unicodes?
            //       Does this make this a Java-specific output class?
            out.write("\\u" + hex(codepoint));
        } else if (codepoint > 0xfff) {
            out.write("\\u" + hex(codepoint));
        } else if (codepoint > 0xff) {
            out.write("\\u0" + hex(codepoint));
        } else if (codepoint > 0xf) {
            out.write("\\u00" + hex(codepoint));
        } else {
            out.write("\\u000" + hex(codepoint));
        }
        return true;
    }
}
