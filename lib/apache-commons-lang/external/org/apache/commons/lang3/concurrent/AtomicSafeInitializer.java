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
 * A specialized {@code ConcurrentInitializer} implementation which is similar
 * to {@link AtomicInitializer}, but ensures that the {@link #initialize()}
 * method is called only once.
 * </p>
 * <p>
 * As {@link AtomicInitializer} this class is based on atomic variables, so it
 * can create an object under concurrent access without synchronization.
 * However, it implements an additional check to guarantee that the
 * {@link #initialize()} method which actually creates the object cannot be
 * called multiple times.
 * </p>
 * <p>
 * Because of this additional check this implementation is slightly less
 * efficient than {@link AtomicInitializer}, but if the object creation in the
 * {@code initialize()} method is expensive or if multiple invocations of
 * {@code initialize()} are problematic, it is the better alternative.
 * </p>
 * <p>
 * From its semantics this class has the same properties as
 * {@link LazyInitializer}. It is a &quot;save&quot; implementation of the lazy
 * initializer pattern. Comparing both classes in terms of efficiency is
 * difficult because which one is faster depends on multiple factors. Because
 * {@code AtomicSafeInitializer} does not use synchronization at all it probably
 * outruns {@link LazyInitializer}, at least under low or moderate concurrent
 * access. Developers should run their own benchmarks on the expected target
 * platform to decide which implementation is suitable for their specific use
 * case.
 * </p>
 *
 * @since 3.0
 * @version $Id: AtomicSafeInitializer.java 1088899 2011-04-05 05:31:27Z bayard $
 * @param <T> the type of the object managed by this initializer class
 */
public abstract class AtomicSafeInitializer<T> implements
        ConcurrentInitializer<T> {
    /** A guard which ensures that initialize() is called only once. */
    private final AtomicReference<AtomicSafeInitializer<T>> factory =
            new AtomicReference<AtomicSafeInitializer<T>>();

    /** Holds the reference to the managed object. */
    private final AtomicReference<T> reference = new AtomicReference<T>();

    /**
     * Get (and initialize, if not initialized yet) the required object
     *
     * @return lazily initialized object
     * @throws ConcurrentException if the initialization of the object causes an
     * exception
     */
    public final T get() throws ConcurrentException {
        T result;

        while ((result = reference.get()) == null) {
            if (factory.compareAndSet(null, this)) {
                reference.set(initialize());
            }
        }

        return result;
    }

    /**
     * Creates and initializes the object managed by this
     * {@code AtomicInitializer}. This method is called by {@link #get()} when
     * the managed object is not available yet. An implementation can focus on
     * the creation of the object. No synchronization is needed, as this is
     * already handled by {@code get()}. This method is guaranteed to be called
     * only once.
     *
     * @return the managed data object
     * @throws ConcurrentException if an error occurs during object creation
     */
    protected abstract T initialize() throws ConcurrentException;
}
