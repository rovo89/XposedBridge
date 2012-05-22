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

/**
 * <p>Exception thrown when the Serialization process fails.</p>
 *
 * <p>The original error is wrapped within this one.</p>
 *
 * <p>#NotThreadSafe# because Throwable is not threadsafe</p>
 * @since 1.0
 * @version $Id: SerializationException.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class SerializationException extends RuntimeException {

    /**
     * Required for serialization support.
     * 
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 4029025366392702726L;

    /**
     * <p>Constructs a new {@code SerializationException} without specified
     * detail message.</p>
     */
    public SerializationException() {
        super();
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * detail message.</p>
     *
     * @param msg  The error message.
     */
    public SerializationException(String msg) {
        super(msg);
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * nested {@code Throwable}.</p>
     *
     * @param cause  The {@code Exception} or {@code Error}
     *  that caused this exception to be thrown.
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructs a new {@code SerializationException} with specified
     * detail message and nested {@code Throwable}.</p>
     *
     * @param msg    The error message.
     * @param cause  The {@code Exception} or {@code Error}
     *  that caused this exception to be thrown.
     */
    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
