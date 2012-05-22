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

import java.text.Format;
import java.util.Locale;

/**
 * Format factory.
 * 
 * @since 2.4
 * @version $Id: FormatFactory.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public interface FormatFactory {

    /**
     * Create or retrieve a format instance.
     *
     * @param name The format type name
     * @param arguments Arguments used to create the format instance. This allows the
     *                  <code>FormatFactory</code> to implement the "format style"
     *                  concept from <code>java.text.MessageFormat</code>.
     * @param locale The locale, may be null
     * @return The format instance
     */
    Format getFormat(String name, String arguments, Locale locale);

}
