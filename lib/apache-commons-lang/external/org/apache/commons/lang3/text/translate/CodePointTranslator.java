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
package external.org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper subclass to CharSequenceTranslator to allow for translations that 
 * will replace up to one character at a time.
 * 
 * @since 3.0
 * @version $Id: CodePointTranslator.java 1139924 2011-06-26 19:32:14Z mbenson $
 */
public abstract class CodePointTranslator extends CharSequenceTranslator {

    /**
     * Implementation of translate that maps onto the abstract translate(int, Writer) method. 
     * {@inheritDoc}
     */
    @Override
    public final int translate(CharSequence input, int index, Writer out) throws IOException {
        int codepoint = Character.codePointAt(input, index);
        boolean consumed = translate(codepoint, out);
        if (consumed) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Translate the specified codepoint into another. 
     * 
     * @param codepoint int character input to translate
     * @param out Writer to optionally push the translated output to
     * @return boolean as to whether translation occurred or not
     * @throws IOException if and only if the Writer produces an IOException
     */
    public abstract boolean translate(int codepoint, Writer out) throws IOException;

}
