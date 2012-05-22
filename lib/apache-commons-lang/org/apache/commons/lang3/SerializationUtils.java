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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * <p>Assists with the serialization process and performs additional functionality based
 * on serialization.</p>
 * <p>
 * <ul>
 * <li>Deep clone using serialization
 * <li>Serialize managing finally and IOException
 * <li>Deserialize managing finally and IOException
 * </ul>
 *
 * <p>This class throws exceptions for invalid {@code null} inputs.
 * Each method documents its behaviour in more detail.</p>
 *
 * <p>#ThreadSafe#</p>
 * @since 1.0
 * @version $Id: SerializationUtils.java 1199718 2011-11-09 12:43:20Z sebb $
 */
public class SerializationUtils {

    /**
     * <p>SerializationUtils instances should NOT be constructed in standard programming.
     * Instead, the class should be used as {@code SerializationUtils.clone(object)}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     * @since 2.0
     */
    public SerializationUtils() {
        super();
    }

    // Clone
    //-----------------------------------------------------------------------
    /**
     * <p>Deep clone an {@code Object} using serialization.</p>
     *
     * <p>This is many times slower than writing clone methods by hand
     * on all objects in your object graph. However, for complex object
     * graphs, or for those that don't support deep cloning this can
     * be a simple alternative implementation. Of course all the objects
     * must be {@code Serializable}.</p>
     *
     * @param <T> the type of the object involved
     * @param object  the {@code Serializable} object to clone
     * @return the cloned object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static <T extends Serializable> T clone(T object) {
        if (object == null) {
            return null;
        }
        byte[] objectData = serialize(object);
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);

        ClassLoaderAwareObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ClassLoaderAwareObjectInputStream(bais, object.getClass().getClassLoader());
            /*
             * when we serialize and deserialize an object,
             * it is reasonable to assume the deserialized object
             * is of the same type as the original serialized object
             */
            @SuppressWarnings("unchecked") // see above
            T readObject = (T) in.readObject();
            return readObject;

        } catch (ClassNotFoundException ex) {
            throw new SerializationException("ClassNotFoundException while reading cloned object data", ex);
        } catch (IOException ex) {
            throw new SerializationException("IOException while reading cloned object data", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                throw new SerializationException("IOException on closing cloned object data InputStream.", ex);
            }
        }
    }

    // Serialize
    //-----------------------------------------------------------------------
    /**
     * <p>Serializes an {@code Object} to the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj  the object to serialize to bytes, may be null
     * @param outputStream  the stream to write to, must not be null
     * @throws IllegalArgumentException if {@code outputStream} is {@code null}
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(Serializable obj, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out = null;
        try {
            // stream closed in the finally
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);

        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) { // NOPMD
                // ignore close exception
            }
        }
    }

    /**
     * <p>Serializes an {@code Object} to a byte array for
     * storage/serialization.</p>
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    // Deserialize
    //-----------------------------------------------------------------------
    /**
     * <p>Deserializes an {@code Object} from the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written. This
     * avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param inputStream  the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code inputStream} is {@code null}
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object deserialize(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return in.readObject();

        } catch (ClassNotFoundException ex) {
            throw new SerializationException(ex);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) { // NOPMD
                // ignore close exception
            }
        }
    }

    /**
     * <p>Deserializes a single {@code Object} from an array of bytes.</p>
     *
     * @param objectData  the serialized object, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code objectData} is {@code null}
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object deserialize(byte[] objectData) {
        if (objectData == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        return deserialize(bais);
    }

    /**
     * <p>Custom specialization of the standard JDK {@link java.io.ObjectInputStream}
     * that uses a custom  <code>ClassLoader</code> to resolve a class.
     * If the specified <code>ClassLoader</code> is not able to resolve the class,
     * the context classloader of the current thread will be used.
     * This way, the standard deserialization work also in web-application
     * containers and application servers, no matter in which of the
     * <code>ClassLoader</code> the particular class that encapsulates
     * serialization/deserialization lives. </p>
     * 
     * <p>For more in-depth information about the problem for which this
     * class here is a workaround, see the JIRA issue LANG-626. </p>
     */
     static class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
        private ClassLoader classLoader;

        /**
         * Constructor.
         * @param in The <code>InputStream</code>.
         * @param classLoader classloader to use
         * @throws IOException if an I/O error occurs while reading stream header.
         * @see java.io.ObjectInputStream
         */
        public ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        /**
         * Overriden version that uses the parametrized <code>ClassLoader</code> or the <code>ClassLoader</code>
         * of the current <code>Thread</code> to resolve the class.
         * @param desc An instance of class <code>ObjectStreamClass</code>.
         * @return A <code>Class</code> object corresponding to <code>desc</code>.
         * @throws IOException Any of the usual Input/Output exceptions.
         * @throws ClassNotFoundException If class of a serialized object cannot be found.
         */
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException ex) {
                return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }

    }

}
