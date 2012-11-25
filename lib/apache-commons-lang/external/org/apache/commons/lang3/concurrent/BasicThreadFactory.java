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

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * An implementation of the {@code ThreadFactory} interface that provides some
 * configuration options for the threads it creates.
 * </p>
 * <p>
 * A {@code ThreadFactory} is used for instance by an {@code ExecutorService} to
 * create the threads it uses for executing tasks. In many cases users do not
 * have to care about a {@code ThreadFactory} because the default one used by an
 * {@code ExecutorService} will do. However, if there are special requirements
 * for the threads, a custom {@code ThreadFactory} has to be created.
 * </p>
 * <p>
 * This class provides some frequently needed configuration options for the
 * threads it creates. These are the following:
 * <ul>
 * <li>A name pattern for the threads created by this factory can be specified.
 * This is often useful if an application uses multiple executor services for
 * different purposes. If the names of the threads used by these services have
 * meaningful names, log output or exception traces can be much easier to read.
 * Naming patterns are <em>format strings</em> as used by the {@code
 * String.format()} method. The string can contain the place holder {@code %d}
 * which will be replaced by the number of the current thread ({@code
 * ThreadFactoryImpl} keeps a counter of the threads it has already created).
 * For instance, the naming pattern {@code "My %d. worker thread"} will result
 * in thread names like {@code "My 1. worker thread"}, {@code
 * "My 2. worker thread"} and so on.</li>
 * <li>A flag whether the threads created by this factory should be daemon
 * threads. This can impact the exit behavior of the current Java application
 * because the JVM shuts down if there are only daemon threads running.</li>
 * <li>The priority of the thread. Here an integer value can be provided. The
 * {@code java.lang.Thread} class defines constants for valid ranges of priority
 * values.</li>
 * <li>The {@code UncaughtExceptionHandler} for the thread. This handler is
 * called if an uncaught exception occurs within the thread.</li>
 * </ul>
 * </p>
 * <p>
 * {@code BasicThreadFactory} wraps another thread factory which actually
 * creates new threads. The configuration options are set on the threads created
 * by the wrapped thread factory. On construction time the factory to be wrapped
 * can be specified. If none is provided, a default {@code ThreadFactory} is
 * used.
 * </p>
 * <p>
 * Instances of {@code BasicThreadFactory} are not created directly, but the
 * nested {@code Builder} class is used for this purpose. Using the builder only
 * the configuration options an application is interested in need to be set. The
 * following example shows how a {@code BasicThreadFactory} is created and
 * installed in an {@code ExecutorService}:
 *
 * <pre>
 * // Create a factory that produces daemon threads with a naming pattern and
 * // a priority
 * BasicThreadFactory factory = new BasicThreadFactory.Builder()
 *     .namingPattern(&quot;workerthread-%d&quot;)
 *     .daemon(true)
 *     .priority(Thread.MAX_PRIORITY)
 *     .build();
 * // Create an executor service for single-threaded execution
 * ExecutorService exec = Executors.newSingleThreadExecutor(factory);
 * </pre>
 * </p>
 *
 * @since 3.0
 * @version $Id: BasicThreadFactory.java 1079423 2011-03-08 16:38:09Z sebb $
 */
public class BasicThreadFactory implements ThreadFactory {
    /** A counter for the threads created by this factory. */
    private final AtomicLong threadCounter;

    /** Stores the wrapped factory. */
    private final ThreadFactory wrappedFactory;

    /** Stores the uncaught exception handler. */
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /** Stores the naming pattern for newly created threads. */
    private final String namingPattern;

    /** Stores the priority. */
    private final Integer priority;

    /** Stores the daemon status flag. */
    private final Boolean daemonFlag;

    /**
     * Creates a new instance of {@code ThreadFactoryImpl} and configures it
     * from the specified {@code Builder} object.
     *
     * @param builder the {@code Builder} object
     */
    private BasicThreadFactory(Builder builder) {
        if (builder.wrappedFactory == null) {
            wrappedFactory = Executors.defaultThreadFactory();
        } else {
            wrappedFactory = builder.wrappedFactory;
        }

        namingPattern = builder.namingPattern;
        priority = builder.priority;
        daemonFlag = builder.daemonFlag;
        uncaughtExceptionHandler = builder.exceptionHandler;

        threadCounter = new AtomicLong();
    }

    /**
     * Returns the wrapped {@code ThreadFactory}. This factory is used for
     * actually creating threads. This method never returns <b>null</b>. If no
     * {@code ThreadFactory} was passed when this object was created, a default
     * thread factory is returned.
     *
     * @return the wrapped {@code ThreadFactory}
     */
    public final ThreadFactory getWrappedFactory() {
        return wrappedFactory;
    }

    /**
     * Returns the naming pattern for naming newly created threads. Result can
     * be <b>null</b> if no naming pattern was provided.
     *
     * @return the naming pattern
     */
    public final String getNamingPattern() {
        return namingPattern;
    }

    /**
     * Returns the daemon flag. This flag determines whether newly created
     * threads should be daemon threads. If <b>true</b>, this factory object
     * calls {@code setDaemon(true)} on the newly created threads. Result can be
     * <b>null</b> if no daemon flag was provided at creation time.
     *
     * @return the daemon flag
     */
    public final Boolean getDaemonFlag() {
        return daemonFlag;
    }

    /**
     * Returns the priority of the threads created by this factory. Result can
     * be <b>null</b> if no priority was specified.
     *
     * @return the priority for newly created threads
     */
    public final Integer getPriority() {
        return priority;
    }

    /**
     * Returns the {@code UncaughtExceptionHandler} for the threads created by
     * this factory. Result can be <b>null</b> if no handler was provided.
     *
     * @return the {@code UncaughtExceptionHandler}
     */
    public final Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    /**
     * Returns the number of threads this factory has already created. This
     * class maintains an internal counter that is incremented each time the
     * {@link #newThread(Runnable)} method is invoked.
     *
     * @return the number of threads created by this factory
     */
    public long getThreadCount() {
        return threadCounter.get();
    }

    /**
     * Creates a new thread. This implementation delegates to the wrapped
     * factory for creating the thread. Then, on the newly created thread the
     * corresponding configuration options are set.
     *
     * @param r the {@code Runnable} to be executed by the new thread
     * @return the newly created thread
     */
    public Thread newThread(Runnable r) {
        Thread t = getWrappedFactory().newThread(r);
        initializeThread(t);

        return t;
    }

    /**
     * Initializes the specified thread. This method is called by
     * {@link #newThread(Runnable)} after a new thread has been obtained from
     * the wrapped thread factory. It initializes the thread according to the
     * options set for this factory.
     *
     * @param t the thread to be initialized
     */
    private void initializeThread(Thread t) {

        if (getNamingPattern() != null) {
            Long count = Long.valueOf(threadCounter.incrementAndGet());
            t.setName(String.format(getNamingPattern(), count));
        }

        if (getUncaughtExceptionHandler() != null) {
            t.setUncaughtExceptionHandler(getUncaughtExceptionHandler());
        }

        if (getPriority() != null) {
            t.setPriority(getPriority().intValue());
        }

        if (getDaemonFlag() != null) {
            t.setDaemon(getDaemonFlag().booleanValue());
        }
    }

    /**
     * <p>
     * A <em>builder</em> class for creating instances of {@code
     * BasicThreadFactory}.
     * </p>
     * <p>
     * Using this builder class instances of {@code BasicThreadFactory} can be
     * created and initialized. The class provides methods that correspond to
     * the configuration options supported by {@code BasicThreadFactory}. Method
     * chaining is supported. Refer to the documentation of {@code
     * BasicThreadFactory} for a usage example.
     * </p>
     *
     * @version $Id: BasicThreadFactory.java 1079423 2011-03-08 16:38:09Z sebb $
     */
    public static class Builder 
        implements external.org.apache.commons.lang3.builder.Builder<BasicThreadFactory> {
        
        /** The wrapped factory. */
        private ThreadFactory wrappedFactory;

        /** The uncaught exception handler. */
        private Thread.UncaughtExceptionHandler exceptionHandler;

        /** The naming pattern. */
        private String namingPattern;

        /** The priority. */
        private Integer priority;

        /** The daemon flag. */
        private Boolean daemonFlag;

        /**
         * Sets the {@code ThreadFactory} to be wrapped by the new {@code
         * BasicThreadFactory}.
         *
         * @param factory the wrapped {@code ThreadFactory} (must not be
         * <b>null</b>)
         * @return a reference to this {@code Builder}
         * @throws NullPointerException if the passed in {@code ThreadFactory}
         * is <b>null</b>
         */
        public Builder wrappedFactory(ThreadFactory factory) {
            if (factory == null) {
                throw new NullPointerException(
                        "Wrapped ThreadFactory must not be null!");
            }

            wrappedFactory = factory;
            return this;
        }

        /**
         * Sets the naming pattern to be used by the new {@code
         * BasicThreadFactory}.
         *
         * @param pattern the naming pattern (must not be <b>null</b>)
         * @return a reference to this {@code Builder}
         * @throws NullPointerException if the naming pattern is <b>null</b>
         */
        public Builder namingPattern(String pattern) {
            if (pattern == null) {
                throw new NullPointerException(
                        "Naming pattern must not be null!");
            }

            namingPattern = pattern;
            return this;
        }

        /**
         * Sets the daemon flag for the new {@code BasicThreadFactory}. If this
         * flag is set to <b>true</b> the new thread factory will create daemon
         * threads.
         *
         * @param f the value of the daemon flag
         * @return a reference to this {@code Builder}
         */
        public Builder daemon(boolean f) {
            daemonFlag = Boolean.valueOf(f);
            return this;
        }

        /**
         * Sets the priority for the threads created by the new {@code
         * BasicThreadFactory}.
         *
         * @param prio the priority
         * @return a reference to this {@code Builder}
         */
        public Builder priority(int prio) {
            priority = Integer.valueOf(prio);
            return this;
        }

        /**
         * Sets the uncaught exception handler for the threads created by the
         * new {@code BasicThreadFactory}.
         *
         * @param handler the {@code UncaughtExceptionHandler} (must not be
         * <b>null</b>)
         * @return a reference to this {@code Builder}
         * @throws NullPointerException if the exception handler is <b>null</b>
         */
        public Builder uncaughtExceptionHandler(
                Thread.UncaughtExceptionHandler handler) {
            if (handler == null) {
                throw new NullPointerException(
                        "Uncaught exception handler must not be null!");
            }

            exceptionHandler = handler;
            return this;
        }

        /**
         * Resets this builder. All configuration options are set to default
         * values. Note: If the {@link #build()} method was called, it is not
         * necessary to call {@code reset()} explicitly because this is done
         * automatically.
         */
        public void reset() {
            wrappedFactory = null;
            exceptionHandler = null;
            namingPattern = null;
            priority = null;
            daemonFlag = null;
        }

        /**
         * Creates a new {@code BasicThreadFactory} with all configuration
         * options that have been specified by calling methods on this builder.
         * After creating the factory {@link #reset()} is called.
         *
         * @return the new {@code BasicThreadFactory}
         */
        public BasicThreadFactory build() {
            BasicThreadFactory factory = new BasicThreadFactory(this);
            reset();
            return factory;
        }
    }
}
