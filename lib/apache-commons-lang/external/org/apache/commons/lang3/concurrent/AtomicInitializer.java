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
package external.org.apache.commons.lang3.concurrent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * A specialized implementation of the {@code ConcurrentInitializer} interface
 * based on an {@link AtomicReference} variable.
 * </p>
 * <p>
 * This class maintains a member field of type {@code AtomicReference}. It
 * implements the following algorithm to create and initialize an object in its
 * {@link #get()} method:
 * <ul>
 * <li>First it is checked whether the {@code AtomicReference} variable contains
 * already a value. If this is the case, the value is directly returned.</li>
 * <li>Otherwise the {@link #initialize()} method is called. This method must be
 * defined in concrete subclasses to actually create the managed object.</li>
 * <li>After the object was created by {@link #initialize()} it is checked
 * whether the {@code AtomicReference} variable is still undefined. This has to
 * be done because in the meantime another thread may have initialized the
 * object. If the reference is still empty, the newly created object is stored
 * in it and returned by this method.</li>
 * <li>Otherwise the value stored in the {@code AtomicReference} is returned.</li>
 * </ul>
 * </p>
 * <p>
 * Because atomic variables are used this class does not need any
 * synchronization. So there is no danger of deadlock, and access to the managed
 * object is efficient. However, if multiple threads access the {@code
 * AtomicInitializer} object before it has been initialized almost at the same
 * time, it can happen that {@link #initialize()} is called multiple times. The
 * algorithm outlined above guarantees that {@link #get()} always returns the
 * same object though.
 * </p>
 * <p>
 * Compared with the {@link LazyInitializer} class, this class can be more
 * efficient because it does not need synchronization. The drawback is that the
 * {@link #initialize()} method can be called multiple times which may be
 * problematic if the creation of the managed object is expensive. As a rule of
 * thumb this initializer implementation is preferable if there are not too many
 * threads involved and the probability that multiple threads access an
 * uninitialized object is small. If there is high parallelism,
 * {@link LazyInitializer} is more appropriate.
 * </p>
 *
 * @since 3.0
 * @version $Id: AtomicInitializer.java 1088899 2011-04-05 05:31:27Z bayard $
 * @param <T> the type of the object managed by this initializer class
 */
public abstract class AtomicInitializer<T> implements ConcurrentInitializer<T> {
    /** Holds the reference to the managed object. */
    private final AtomicReference<T> reference = new AtomicReference<T>();

    /**
     * Returns the object managed by this initializer. The object is created if
     * it is not available yet and stored internally. This method always returns
     * the same object.
     *
     * @return the object created by this {@code AtomicInitializer}
     * @throws ConcurrentException if an error occurred during initialization of
     * the object
     */
    public T get() throws ConcurrentException {
        T result = reference.get();

        if (result == null) {
            result = initialize();
            if (!reference.compareAndSet(null, result)) {
                // another thread has initialized the reference
                result = reference.get();
            }
        }

        return result;
    }

    /**
     * Creates and initializes the object managed by this {@code
     * AtomicInitializer}. This method is called by {@link #get()} when the
     * managed object is not available yet. An implementation can focus on the
     * creation of the object. No synchronization is needed, as this is already
     * handled by {@code get()}. As stated by the class comment, it is possible
     * that this method is called multiple times.
     *
     * @return the managed data object
     * @throws ConcurrentException if an error occurs during object creation
     */
    protected abstract T initialize() throws ConcurrentException;
}
