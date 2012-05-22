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
import java.util.HashMap;

/**
 * Translates a value using a lookup table.
 * 
 * @since 3.0
 * @version $Id: LookupTranslator.java 1091096 2011-04-11 15:07:29Z mbenson $
 */
public class LookupTranslator extends CharSequenceTranslator {

    private final HashMap<CharSequence, CharSequence> lookupMap;
    private final int shortest;
    private final int longest;

    /**
     * Define the lookup table to be used in translation
     *
     * @param lookup CharSequence[][] table of size [*][2]
     */
    public LookupTranslator(CharSequence[]... lookup) {
        lookupMap = new HashMap<CharSequence, CharSequence>();
        int _shortest = Integer.MAX_VALUE;
        int _longest = 0;
        if (lookup != null) {
            for (CharSequence[] seq : lookup) {
                this.lookupMap.put(seq[0], seq[1]);
                int sz = seq[0].length();
                if (sz < _shortest) {
                    _shortest = sz;
                }
                if (sz > _longest) {
                    _longest = sz;
                }
            }
        }
        shortest = _shortest;
        longest = _longest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(CharSequence input, int index, Writer out) throws IOException {
        int max = longest;
        if (index + longest > input.length()) {
            max = input.length() - index;
        }
        // descend so as to get a greedy algorithm
        for (int i = max; i >= shortest; i--) {
            CharSequence subSeq = input.subSequence(index, index + i);
            CharSequence result = lookupMap.get(subSeq);
            if (result != null) {
                out.write(result.toString());
                return i;
            }
        }
        return 0;
    }
}
