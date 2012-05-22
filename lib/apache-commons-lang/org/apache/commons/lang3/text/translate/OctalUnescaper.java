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
 * Translate escaped octal Strings back to their octal values.
 *
 * For example, "\45" should go back to being the specific value (a %).
 *
 * Note that this currently only supports the viable range of octal for Java; namely 
 * 1 to 377. This is both because parsing Java is the main use case and Integer.parseInt
 * throws an exception when values are larger than octal 377.
 * 
 * @since 3.0
 * @version $Id: OctalUnescaper.java 967237 2010-07-23 20:08:57Z mbenson $
 */
public class OctalUnescaper extends CharSequenceTranslator {

    private static int OCTAL_MAX = 377;

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(CharSequence input, int index, Writer out) throws IOException {
        if(input.charAt(index) == '\\' && index < (input.length() - 1) && Character.isDigit(input.charAt(index + 1)) ) {
            int start = index + 1;

            int end = index + 2;
            while ( end < input.length() && Character.isDigit(input.charAt(end)) ) {
                end++;
                if ( Integer.parseInt(input.subSequence(start, end).toString(), 10) > OCTAL_MAX) {
                    end--; // rollback
                    break;
                }
            }

            out.write( Integer.parseInt(input.subSequence(start, end).toString(), 8) );
            return 1 + end - start;
        }
        return 0;
    }
}
