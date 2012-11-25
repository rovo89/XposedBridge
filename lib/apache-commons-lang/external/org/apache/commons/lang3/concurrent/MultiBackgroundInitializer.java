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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 * A specialized {@link BackgroundInitializer} implementation that can deal with
 * multiple background initialization tasks.
 * </p>
 * <p>
 * This class has a similar purpose as {@link BackgroundInitializer}. However,
 * it is not limited to a single background initialization task. Rather it
 * manages an arbitrary number of {@code BackgroundInitializer} objects,
 * executes them, and waits until they are completely initialized. This is
 * useful for applications that have to perform multiple initialization tasks
 * that can run in parallel (i.e. that do not depend on each other). This class
 * takes care about the management of an {@code ExecutorService} and shares it
 * with the {@code BackgroundInitializer} objects it is responsible for; so the
 * using application need not bother with these details.
 * </p>
 * <p>
 * The typical usage scenario for {@code MultiBackgroundInitializer} is as
 * follows:
 * <ul>
 * <li>Create a new instance of the class. Optionally pass in a pre-configured
 * {@code ExecutorService}. Alternatively {@code MultiBackgroundInitializer} can
 * create a temporary {@code ExecutorService} and delete it after initialization
 * is complete.</li>
 * <li>Create specialized {@link BackgroundInitializer} objects for the
 * initialization tasks to be performed and add them to the {@code
 * MultiBackgroundInitializer} using the
 * {@link #addInitializer(String, BackgroundInitializer)} method.</li>
 * <li>After all initializers have been added, call the {@link #start()} method.
 * </li>
 * <li>When access to the result objects produced by the {@code
 * BackgroundInitializer} objects is needed call the {@link #get()} method. The
 * object returned here provides access to all result objects created during
 * initialization. It also stores information about exceptions that have
 * occurred.</li>
 * </ul>
 * </p>
 * <p>
 * {@code MultiBackgroundInitializer} starts a special controller task that
 * starts all {@code BackgroundInitializer} objects added to the instance.
 * Before the an initializer is started it is checked whether this initializer
 * already has an {@code ExecutorService} set. If this is the case, this {@code
 * ExecutorService} is used for running the background task. Otherwise the
 * current {@code ExecutorService} of this {@code MultiBackgroundInitializer} is
 * shared with the initializer.
 * </p>
 * <p>
 * The easiest way of using this class is to let it deal with the management of
 * an {@code ExecutorService} itself: If no external {@code ExecutorService} is
 * provided, the class creates a temporary {@code ExecutorService} (that is
 * capable of executing all background tasks in parallel) and destroys it at the
 * end of background processing.
 * </p>
 * <p>
 * Alternatively an external {@code ExecutorService} can be provided - either at
 * construction time or later by calling the
 * {@link #setExternalExecutor(ExecutorService)} method. In this case all
 * background tasks are scheduled at this external {@code ExecutorService}.
 * <strong>Important note:</strong> When using an external {@code
 * ExecutorService} be sure that the number of threads managed by the service is
 * large enough. Otherwise a deadlock can happen! This is the case in the
 * following scenario: {@code MultiBackgroundInitializer} starts a task that
 * starts all registered {@code BackgroundInitializer} objects and waits for
 * their completion. If for instance a single threaded {@code ExecutorService}
 * is used, none of the background tasks can be executed, and the task created
 * by {@code MultiBackgroundInitializer} waits forever.
 * </p>
 *
 * @since 3.0
 * @version $Id: MultiBackgroundInitializer.java 1082301 2011-03-16 21:02:15Z oheger $
 */
public class MultiBackgroundInitializer
        extends
        BackgroundInitializer<MultiBackgroundInitializer.MultiBackgroundInitializerResults> {
    /** A map with the child initializers. */
    private final Map<String, BackgroundInitializer<?>> childInitializers =
        new HashMap<String, BackgroundInitializer<?>>();

    /**
     * Creates a new instance of {@code MultiBackgroundInitializer}.
     */
    public MultiBackgroundInitializer() {
        super();
    }

    /**
     * Creates a new instance of {@code MultiBackgroundInitializer} and
     * initializes it with the given external {@code ExecutorService}.
     *
     * @param exec the {@code ExecutorService} for executing the background
     * tasks
     */
    public MultiBackgroundInitializer(ExecutorService exec) {
        super(exec);
    }

    /**
     * Adds a new {@code BackgroundInitializer} to this object. When this
     * {@code MultiBackgroundInitializer} is started, the given initializer will
     * be processed. This method must not be called after {@link #start()} has
     * been invoked.
     *
     * @param name the name of the initializer (must not be <b>null</b>)
     * @param init the {@code BackgroundInitializer} to add (must not be
     * <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is missing
     * @throws IllegalStateException if {@code start()} has already been called
     */
    public void addInitializer(String name, BackgroundInitializer<?> init) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Name of child initializer must not be null!");
        }
        if (init == null) {
            throw new IllegalArgumentException(
                    "Child initializer must not be null!");
        }

        synchronized (this) {
            if (isStarted()) {
                throw new IllegalStateException(
                        "addInitializer() must not be called after start()!");
            }
            childInitializers.put(name, init);
        }
    }

    /**
     * Returns the number of tasks needed for executing all child {@code
     * BackgroundInitializer} objects in parallel. This implementation sums up
     * the required tasks for all child initializers (which is necessary if one
     * of the child initializers is itself a {@code MultiBackgroundInitializer}
     * ). Then it adds 1 for the control task that waits for the completion of
     * the children.
     *
     * @return the number of tasks required for background processing
     */
    @Override
    protected int getTaskCount() {
        int result = 1;

        for (BackgroundInitializer<?> bi : childInitializers.values()) {
            result += bi.getTaskCount();
        }

        return result;
    }

    /**
     * Creates the results object. This implementation starts all child {@code
     * BackgroundInitializer} objects. Then it collects their results and
     * creates a {@code MultiBackgroundInitializerResults} object with this
     * data. If a child initializer throws a checked exceptions, it is added to
     * the results object. Unchecked exceptions are propagated.
     *
     * @return the results object
     * @throws Exception if an error occurs
     */
    @Override
    protected MultiBackgroundInitializerResults initialize() throws Exception {
        Map<String, BackgroundInitializer<?>> inits;
        synchronized (this) {
            // create a snapshot to operate on
            inits = new HashMap<String, BackgroundInitializer<?>>(
                    childInitializers);
        }

        // start the child initializers
        ExecutorService exec = getActiveExecutor();
        for (BackgroundInitializer<?> bi : inits.values()) {
            if (bi.getExternalExecutor() == null) {
                // share the executor service if necessary
                bi.setExternalExecutor(exec);
            }
            bi.start();
        }

        // collect the results
        Map<String, Object> results = new HashMap<String, Object>();
        Map<String, ConcurrentException> excepts = new HashMap<String, ConcurrentException>();
        for (Map.Entry<String, BackgroundInitializer<?>> e : inits.entrySet()) {
            try {
                results.put(e.getKey(), e.getValue().get());
            } catch (ConcurrentException cex) {
                excepts.put(e.getKey(), cex);
            }
        }

        return new MultiBackgroundInitializerResults(inits, results, excepts);
    }

    /**
     * A data class for storing the results of the background initialization
     * performed by {@code MultiBackgroundInitializer}. Objects of this inner
     * class are returned by {@link MultiBackgroundInitializer#initialize()}.
     * They allow access to all result objects produced by the
     * {@link BackgroundInitializer} objects managed by the owning instance. It
     * is also possible to retrieve status information about single
     * {@link BackgroundInitializer}s, i.e. whether they completed normally or
     * caused an exception.
     */
    public static class MultiBackgroundInitializerResults {
        /** A map with the child initializers. */
        private final Map<String, BackgroundInitializer<?>> initializers;

        /** A map with the result objects. */
        private final Map<String, Object> resultObjects;

        /** A map with the exceptions. */
        private final Map<String, ConcurrentException> exceptions;

        /**
         * Creates a new instance of {@code MultiBackgroundInitializerResults}
         * and initializes it with maps for the {@code BackgroundInitializer}
         * objects, their result objects and the exceptions thrown by them.
         *
         * @param inits the {@code BackgroundInitializer} objects
         * @param results the result objects
         * @param excepts the exceptions
         */
        private MultiBackgroundInitializerResults(
                Map<String, BackgroundInitializer<?>> inits,
                Map<String, Object> results,
                Map<String, ConcurrentException> excepts) {
            initializers = inits;
            resultObjects = results;
            exceptions = excepts;
        }

        /**
         * Returns the {@code BackgroundInitializer} with the given name. If the
         * name cannot be resolved, an exception is thrown.
         *
         * @param name the name of the {@code BackgroundInitializer}
         * @return the {@code BackgroundInitializer} with this name
         * @throws NoSuchElementException if the name cannot be resolved
         */
        public BackgroundInitializer<?> getInitializer(String name) {
            return checkName(name);
        }

        /**
         * Returns the result object produced by the {@code
         * BackgroundInitializer} with the given name. This is the object
         * returned by the initializer's {@code initialize()} method. If this
         * {@code BackgroundInitializer} caused an exception, <b>null</b> is
         * returned. If the name cannot be resolved, an exception is thrown.
         *
         * @param name the name of the {@code BackgroundInitializer}
         * @return the result object produced by this {@code
         * BackgroundInitializer}
         * @throws NoSuchElementException if the name cannot be resolved
         */
        public Object getResultObject(String name) {
            checkName(name);
            return resultObjects.get(name);
        }

        /**
         * Returns a flag whether the {@code BackgroundInitializer} with the
         * given name caused an exception.
         *
         * @param name the name of the {@code BackgroundInitializer}
         * @return a flag whether this initializer caused an exception
         * @throws NoSuchElementException if the name cannot be resolved
         */
        public boolean isException(String name) {
            checkName(name);
            return exceptions.containsKey(name);
        }

        /**
         * Returns the {@code ConcurrentException} object that was thrown by the
         * {@code BackgroundInitializer} with the given name. If this
         * initializer did not throw an exception, the return value is
         * <b>null</b>. If the name cannot be resolved, an exception is thrown.
         *
         * @param name the name of the {@code BackgroundInitializer}
         * @return the exception thrown by this initializer
         * @throws NoSuchElementException if the name cannot be resolved
         */
        public ConcurrentException getException(String name) {
            checkName(name);
            return exceptions.get(name);
        }

        /**
         * Returns a set with the names of all {@code BackgroundInitializer}
         * objects managed by the {@code MultiBackgroundInitializer}.
         *
         * @return an (unmodifiable) set with the names of the managed {@code
         * BackgroundInitializer} objects
         */
        public Set<String> initializerNames() {
            return Collections.unmodifiableSet(initializers.keySet());
        }

        /**
         * Returns a flag whether the whole initialization was successful. This
         * is the case if no child initializer has thrown an exception.
         *
         * @return a flag whether the initialization was successful
         */
        public boolean isSuccessful() {
            return exceptions.isEmpty();
        }

        /**
         * Checks whether an initializer with the given name exists. If not,
         * throws an exception. If it exists, the associated child initializer
         * is returned.
         *
         * @param name the name to check
         * @return the initializer with this name
         * @throws NoSuchElementException if the name is unknown
         */
        private BackgroundInitializer<?> checkName(String name) {
            BackgroundInitializer<?> init = initializers.get(name);
            if (init == null) {
                throw new NoSuchElementException(
                        "No child initializer with name " + name);
            }

            return init;
        }
    }
}
