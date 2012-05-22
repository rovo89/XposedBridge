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

import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>
 * A very simple implementation of the {@link ConcurrentInitializer} interface
 * which always returns the same object.
 * </p>
 * <p>
 * An instance of this class is passed a reference to an object when it is
 * constructed. The {@link #get()} method just returns this object. No
 * synchronization is required.
 * </p>
 * <p>
 * This class is useful for instance for unit testing or in cases where a
 * specific object has to be passed to an object which expects a
 * {@link ConcurrentInitializer}.
 * </p>
 *
 * @since 3.0
 * @version $Id: ConstantInitializer.java 1199894 2011-11-09 17:53:59Z ggregory $
 * @param <T> the type of the object managed by this initializer
 */
public class ConstantInitializer<T> implements ConcurrentInitializer<T> {
    /** Constant for the format of the string representation. */
    private static final String FMT_TO_STRING = "ConstantInitializer@%d [ object = %s ]";

    /** Stores the managed object. */
    private final T object;

    /**
     * Creates a new instance of {@code ConstantInitializer} and initializes it
     * with the object to be managed. The {@code get()} method will always
     * return the object passed here. This class does not place any restrictions
     * on the object. It may be <b>null</b>, then {@code get()} will return
     * <b>null</b>, too.
     *
     * @param obj the object to be managed by this initializer
     */
    public ConstantInitializer(T obj) {
        object = obj;
    }

    /**
     * Directly returns the object that was passed to the constructor. This is
     * the same object as returned by {@code get()}. However, this method does
     * not declare that it throws an exception.
     *
     * @return the object managed by this initializer
     */
    public final T getObject() {
        return object;
    }

    /**
     * Returns the object managed by this initializer. This implementation just
     * returns the object passed to the constructor.
     *
     * @return the object managed by this initializer
     * @throws ConcurrentException if an error occurs
     */
    public T get() throws ConcurrentException {
        return getObject();
    }

    /**
     * Returns a hash code for this object. This implementation returns the hash
     * code of the managed object.
     *
     * @return a hash code for this object
     */
    @Override
    public int hashCode() {
        return getObject() != null ? getObject().hashCode() : 0;
    }

    /**
     * Compares this object with another one. This implementation returns
     * <b>true</b> if and only if the passed in object is an instance of
     * {@code ConstantInitializer} which refers to an object equals to the
     * object managed by this instance.
     *
     * @param obj the object to compare to
     * @return a flag whether the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConstantInitializer<?>)) {
            return false;
        }

        ConstantInitializer<?> c = (ConstantInitializer<?>) obj;
        return ObjectUtils.equals(getObject(), c.getObject());
    }

    /**
     * Returns a string representation for this object. This string also
     * contains a string representation of the object managed by this
     * initializer.
     *
     * @return a string for this object
     */
    @Override
    public String toString() {
        return String.format(FMT_TO_STRING, Integer.valueOf(System.identityHashCode(this)),
                String.valueOf(getObject()));
    }
}
