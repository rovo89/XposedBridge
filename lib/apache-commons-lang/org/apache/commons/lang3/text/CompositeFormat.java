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
package org.apache.commons.lang3.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Formats using one formatter and parses using a different formatter. An
 * example of use for this would be a webapp where data is taken in one way and
 * stored in a database another way.
 * 
 * @version $Id: CompositeFormat.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class CompositeFormat extends Format {

    /**
     * Required for serialization support.
     * 
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = -4329119827877627683L;

    /** The parser to use. */
    private final Format parser;
    /** The formatter to use. */
    private final Format formatter;

    /**
     * Create a format that points its parseObject method to one implementation
     * and its format method to another.
     * 
     * @param parser implementation
     * @param formatter implementation
     */
    public CompositeFormat(Format parser, Format formatter) {
        this.parser = parser;
        this.formatter = formatter;
    }

    /**
     * Uses the formatter Format instance.
     * 
     * @param obj the object to format
     * @param toAppendTo the {@link StringBuffer} to append to
     * @param pos the FieldPosition to use (or ignore).
     * @return <code>toAppendTo</code>
     * @see Format#format(Object, StringBuffer, FieldPosition)
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos) {
        return formatter.format(obj, toAppendTo, pos);
    }

    /**
     * Uses the parser Format instance.
     * 
     * @param source the String source
     * @param pos the ParsePosition containing the position to parse from, will
     *            be updated according to parsing success (index) or failure
     *            (error index)
     * @return the parsed Object
     * @see Format#parseObject(String, ParsePosition)
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return parser.parseObject(source, pos);
    }

    /**
     * Provides access to the parser Format implementation.
     * 
     * @return parser Format implementation
     */
    public Format getParser() {
        return this.parser;
    }

    /**
     * Provides access to the parser Format implementation.
     * 
     * @return formatter Format implementation
     */
    public Format getFormatter() {
        return this.formatter;
    }

    /**
     * Utility method to parse and then reformat a String.
     * 
     * @param input String to reformat
     * @return A reformatted String
     * @throws ParseException thrown by parseObject(String) call
     */
    public String reformat(String input) throws ParseException {
        return format(parseObject(input));
    }

}
