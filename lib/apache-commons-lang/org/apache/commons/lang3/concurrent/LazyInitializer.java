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
package org.apache.commons.lang3.concurrent;

/**
 * <p>
 * This class provides a generic implementation of the lazy initialization
 * pattern.
 * </p>
 * <p>
 * Sometimes an application has to deal with an object only under certain
 * circumstances, e.g. when the user selects a specific menu item or if a
 * special event is received. If the creation of the object is costly or the
 * consumption of memory or other system resources is significant, it may make
 * sense to defer the creation of this object until it is really needed. This is
 * a use case for the lazy initialization pattern.
 * </p>
 * <p>
 * This abstract base class provides an implementation of the double-check idiom
 * for an instance field as discussed in Joshua Bloch's "Effective Java", 2nd
 * edition, item 71. The class already implements all necessary synchronization.
 * A concrete subclass has to implement the {@code initialize()} method, which
 * actually creates the wrapped data object.
 * </p>
 * <p>
 * As an usage example consider that we have a class {@code ComplexObject} whose
 * instantiation is a complex operation. In order to apply lazy initialization
 * to this class, a subclass of {@code LazyInitializer} has to be created:
 *
 * <pre>
 * public class ComplexObjectInitializer extends LazyInitializer&lt;ComplexObject&gt; {
 *     &#064;Override
 *     protected ComplexObject initialize() {
 *         return new ComplexObject();
 *     }
 * }
 * </pre>
 *
 * Access to the data object is provided through the {@code get()} method. So,
 * code that wants to obtain the {@code ComplexObject} instance would simply
 * look like this:
 *
 * <pre>
 * // Create an instance of the lazy initializer
 * ComplexObjectInitializer initializer = new ComplexObjectInitializer();
 * ...
 * // When the object is actually needed:
 * ComplexObject cobj = initializer.get();
 * </pre>
 *
 * </p>
 * <p>
 * If multiple threads call the {@code get()} method when the object has not yet
 * been created, they are blocked until initialization completes. The algorithm
 * guarantees that only a single instance of the wrapped object class is
 * created, which is passed to all callers. Once initialized, calls to the
 * {@code get()} method are pretty fast because no synchronization is needed
 * (only an access to a <b>volatile</b> member field).
 * </p>
 *
 * @since 3.0
 * @version $Id: LazyInitializer.java 1088899 2011-04-05 05:31:27Z bayard $
 * @param <T> the type of the object managed by this initializer class
 */
public abstract class LazyInitializer<T> implements ConcurrentInitializer<T> {
    /** Stores the managed object. */
    private volatile T object;

    /**
     * Returns the object wrapped by this instance. On first access the object
     * is created. After that it is cached and can be accessed pretty fast.
     *
     * @return the object initialized by this {@code LazyInitializer}
     * @throws ConcurrentException if an error occurred during initialization of
     * the object
     */
    public T get() throws ConcurrentException {
        // use a temporary variable to reduce the number of reads of the
        // volatile field
        T result = object;

        if (result == null) {
            synchronized (this) {
                result = object;
                if (result == null) {
                    object = result = initialize();
                }
            }
        }

        return result;
    }

    /**
     * Creates and initializes the object managed by this {@code
     * LazyInitializer}. This method is called by {@link #get()} when the object
     * is accessed for the first time. An implementation can focus on the
     * creation of the object. No synchronization is needed, as this is already
     * handled by {@code get()}.
     *
     * @return the managed data object
     * @throws ConcurrentException if an error occurs during object creation
     */
    protected abstract T initialize() throws ConcurrentException;
}
