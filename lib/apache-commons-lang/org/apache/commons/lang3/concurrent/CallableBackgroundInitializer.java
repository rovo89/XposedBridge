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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 * A specialized {@link BackgroundInitializer} implementation that wraps a
 * {@code Callable} object.
 * </p>
 * <p>
 * An instance of this class is initialized with a {@code Callable} object when
 * it is constructed. The implementation of the {@link #initialize()} method
 * defined in the super class delegates to this {@code Callable} so that the
 * {@code Callable} is executed in the background thread.
 * </p>
 * <p>
 * The {@code java.util.concurrent.Callable} interface is a standard mechanism
 * of the JDK to define tasks to be executed by another thread. The {@code
 * CallableBackgroundInitializer} class allows combining this standard interface
 * with the background initializer API.
 * </p>
 * <p>
 * Usage of this class is very similar to the default usage pattern of the
 * {@link BackgroundInitializer} class: Just create an instance and provide the
 * {@code Callable} object to be executed, then call the initializer's
 * {@link #start()} method. This causes the {@code Callable} to be executed in
 * another thread. When the results of the {@code Callable} are needed the
 * initializer's {@link #get()} method can be called (which may block until
 * background execution is complete). The following code fragment shows a
 * typical usage example:
 *
 * <pre>
 * // a Callable that performs a complex computation
 * Callable&lt;Integer&gt; computationCallable = new MyComputationCallable();
 * // setup the background initializer
 * CallableBackgroundInitializer&lt;Integer&gt; initializer =
 *     new CallableBackgroundInitializer(computationCallable);
 * initializer.start();
 * // Now do some other things. Initialization runs in a parallel thread
 * ...
 * // Wait for the end of initialization and access the result
 * Integer result = initializer.get();
 * </pre>
 *
 * </p>
 *
 * @since 3.0
 * @version $Id: CallableBackgroundInitializer.java 1082044 2011-03-16 04:26:58Z bayard $
 * @param <T> the type of the object managed by this initializer class
 */
public class CallableBackgroundInitializer<T> extends BackgroundInitializer<T> {
    /** The Callable to be executed. */
    private final Callable<T> callable;

    /**
     * Creates a new instance of {@code CallableBackgroundInitializer} and sets
     * the {@code Callable} to be executed in a background thread.
     *
     * @param call the {@code Callable} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code Callable} is <b>null</b>
     */
    public CallableBackgroundInitializer(Callable<T> call) {
        checkCallable(call);
        callable = call;
    }

    /**
     * Creates a new instance of {@code CallableBackgroundInitializer} and
     * initializes it with the {@code Callable} to be executed in a background
     * thread and the {@code ExecutorService} for managing the background
     * execution.
     *
     * @param call the {@code Callable} (must not be <b>null</b>)
     * @param exec an external {@code ExecutorService} to be used for task
     * execution
     * @throws IllegalArgumentException if the {@code Callable} is <b>null</b>
     */
    public CallableBackgroundInitializer(Callable<T> call, ExecutorService exec) {
        super(exec);
        checkCallable(call);
        callable = call;
    }

    /**
     * Performs initialization in a background thread. This implementation
     * delegates to the {@code Callable} passed at construction time of this
     * object.
     *
     * @return the result of the initialization
     * @throws Exception if an error occurs
     */
    @Override
    protected T initialize() throws Exception {
        return callable.call();
    }

    /**
     * Tests the passed in {@code Callable} and throws an exception if it is
     * undefined.
     *
     * @param call the object to check
     * @throws IllegalArgumentException if the {@code Callable} is <b>null</b>
     */
    private void checkCallable(Callable<T> call) {
        if (call == null) {
            throw new IllegalArgumentException("Callable must not be null!");
        }
    }
}
