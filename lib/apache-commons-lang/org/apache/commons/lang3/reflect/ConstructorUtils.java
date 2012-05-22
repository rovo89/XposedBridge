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
package org.apache.commons.lang3.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * <p> Utility reflection methods focused on constructors, modeled after
 * {@link MethodUtils}. </p>
 *
 * <h3>Known Limitations</h3> <h4>Accessing Public Constructors In A Default
 * Access Superclass</h4> <p>There is an issue when invoking public constructors
 * contained in a default access superclass. Reflection locates these
 * constructors fine and correctly assigns them as public. However, an
 * <code>IllegalAccessException</code> is thrown if the constructors is
 * invoked.</p>
 *
 * <p><code>ConstructorUtils</code> contains a workaround for this situation. It
 * will attempt to call <code>setAccessible</code> on this constructor. If this
 * call succeeds, then the method can be invoked as normal. This call will only
 * succeed when the application has sufficient security privileges. If this call
 * fails then a warning will be logged and the method may fail.</p>
 *
 * @since 2.5
 * @version $Id: ConstructorUtils.java 1144010 2011-07-07 20:02:10Z joehni $
 */
public class ConstructorUtils {

    /**
     * <p>ConstructorUtils instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * <code>ConstructorUtils.invokeConstructor(cls, args)</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public ConstructorUtils() {
        super();
    }

    /**
     * <p>Returns a new instance of the specified class inferring the right constructor
     * from the types of the arguments.</p>
     * 
     * <p>This locates and calls a constructor.
     * The constructor signature must match the argument types by assignment compatibility.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not null
     * @param args  the array of arguments, null treated as empty
     * @return new instance of <code>cls</code>, not null
     *
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see #invokeConstructor(java.lang.Class, java.lang.Object[], java.lang.Class[])
     */
    public static <T> T invokeConstructor(Class<T> cls, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        if (args == null) {
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        Class<?> parameterTypes[] = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeConstructor(cls, args, parameterTypes);
    }

    /**
     * <p>Returns a new instance of the specified class choosing the right constructor
     * from the list of parameter types.</p>
     * 
     * <p>This locates and calls a constructor.
     * The constructor signature must match the parameter types by assignment compatibility.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not null
     * @param args  the array of arguments, null treated as empty
     * @param parameterTypes  the array of parameter types, null treated as empty
     * @return new instance of <code>cls</code>, not null
     *
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see Constructor#newInstance
     */
    public static <T> T invokeConstructor(Class<T> cls, Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        if (parameterTypes == null) {
            parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if (args == null) {
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        Constructor<T> ctor = getMatchingAccessibleConstructor(cls, parameterTypes);
        if (ctor == null) {
            throw new NoSuchMethodException(
                "No such accessible constructor on object: " + cls.getName());
        }
        return ctor.newInstance(args);
    }

    /**
     * <p>Returns a new instance of the specified class inferring the right constructor
     * from the types of the arguments.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the argument types exactly.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not null
     * @param args  the array of arguments, null treated as empty
     * @return new instance of <code>cls</code>, not null
     *
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see #invokeExactConstructor(java.lang.Class, java.lang.Object[], java.lang.Class[])
     */
    public static <T> T invokeExactConstructor(Class<T> cls, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        if (args == null) {
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        int arguments = args.length;
        Class<?> parameterTypes[] = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeExactConstructor(cls, args, parameterTypes);
    }

    /**
     * <p>Returns a new instance of the specified class choosing the right constructor
     * from the list of parameter types.</p>
     *
     * <p>This locates and calls a constructor.
     * The constructor signature must match the parameter types exactly.</p>
     *
     * @param <T> the type to be constructed
     * @param cls  the class to be constructed, not null
     * @param args  the array of arguments, null treated as empty
     * @param parameterTypes  the array of parameter types, null treated as empty
     * @return new instance of <code>cls</code>, not null
     *
     * @throws NoSuchMethodException if a matching constructor cannot be found
     * @throws IllegalAccessException if invocation is not permitted by security
     * @throws InvocationTargetException if an error occurs on invocation
     * @throws InstantiationException if an error occurs on instantiation
     * @see Constructor#newInstance
     */
    public static <T> T invokeExactConstructor(Class<T> cls, Object[] args,
            Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        if (args == null) {
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        if (parameterTypes == null) {
            parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Constructor<T> ctor = getAccessibleConstructor(cls, parameterTypes);
        if (ctor == null) {
            throw new NoSuchMethodException(
                "No such accessible constructor on object: "+ cls.getName());
        }
        return ctor.newInstance(args);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Finds a constructor given a class and signature, checking accessibility.</p>
     * 
     * <p>This finds the constructor and ensures that it is accessible.
     * The constructor signature must match the parameter types exactly.</p>
     *
     * @param <T> the constructor type
     * @param cls  the class to find a constructor for, not null
     * @param parameterTypes  the array of parameter types, null treated as empty
     * @return the constructor, null if no matching accessible constructor found
     * @see Class#getConstructor
     * @see #getAccessibleConstructor(java.lang.reflect.Constructor)
     */
    public static <T> Constructor<T> getAccessibleConstructor(Class<T> cls,
            Class<?>... parameterTypes) {
        try {
            return getAccessibleConstructor(cls.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * <p>Checks if the specified constructor is accessible.</p>
     * 
     * <p>This simply ensures that the constructor is accessible.</p>
     *
     * @param <T> the constructor type
     * @param ctor  the prototype constructor object, not null
     * @return the constructor, null if no matching accessible constructor found
     * @see java.lang.SecurityManager
     */
    public static <T> Constructor<T> getAccessibleConstructor(Constructor<T> ctor) {
        return MemberUtils.isAccessible(ctor)
                && Modifier.isPublic(ctor.getDeclaringClass().getModifiers()) ? ctor : null;
    }

    /**
     * <p>Finds an accessible constructor with compatible parameters.</p>
     * 
     * <p>This checks all the constructor and finds one with compatible parameters
     * This requires that every parameter is assignable from the given parameter types.
     * This is a more flexible search than the normal exact matching algorithm.</p>
     *
     * <p>First it checks if there is a constructor matching the exact signature.
     * If not then all the constructors of the class are checked to see if their
     * signatures are assignment compatible with the parameter types.
     * The first assignment compatible matching constructor is returned.</p>
     *
     * @param <T> the constructor type
     * @param cls  the class to find a constructor for, not null
     * @param parameterTypes find method with compatible parameters
     * @return the constructor, null if no matching accessible constructor found
     */
    public static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> cls,
            Class<?>... parameterTypes) {
        // see if we can find the constructor directly
        // most of the time this works and it's much faster
        try {
            Constructor<T> ctor = cls.getConstructor(parameterTypes);
            MemberUtils.setAccessibleWorkaround(ctor);
            return ctor;
        } catch (NoSuchMethodException e) { // NOPMD - Swallow
        }
        Constructor<T> result = null;
        /*
         * (1) Class.getConstructors() is documented to return Constructor<T> so as
         * long as the array is not subsequently modified, everything's fine.
         */
        Constructor<?>[] ctors = cls.getConstructors();

        // return best match:
        for (Constructor<?> ctor : ctors) {
            // compare parameters
            if (ClassUtils.isAssignable(parameterTypes, ctor.getParameterTypes(), true)) {
                // get accessible version of constructor
                ctor = getAccessibleConstructor(ctor);
                if (ctor != null) {
                    MemberUtils.setAccessibleWorkaround(ctor);
                    if (result == null
                            || MemberUtils.compareParameterTypes(ctor.getParameterTypes(), result
                                    .getParameterTypes(), parameterTypes) < 0) {
                        // temporary variable for annotation, see comment above (1)
                        @SuppressWarnings("unchecked")
                        Constructor<T> constructor = (Constructor<T>)ctor;
                        result = constructor;
                    }
                }
            }
        }
        return result;
    }

}
