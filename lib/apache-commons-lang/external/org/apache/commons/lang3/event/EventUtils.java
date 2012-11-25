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

package external.org.apache.commons.lang3.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import external.org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Provides some useful event-based utility methods.
 *
 * @since 3.0
 * @version $Id: EventUtils.java 1091072 2011-04-11 13:42:03Z mbenson $
 */
public class EventUtils {

    /**
     * Adds an event listener to the specified source.  This looks for an "add" method corresponding to the event
     * type (addActionListener, for example).
     * @param eventSource   the event source
     * @param listenerType  the event listener type
     * @param listener      the listener
     * @param <L>           the event listener type
     *
     * @throws IllegalArgumentException if the object doesn't support the listener type
     */
    public static <L> void addEventListener(Object eventSource, Class<L> listenerType, L listener) {
        try {
            MethodUtils.invokeMethod(eventSource, "add" + listenerType.getSimpleName(), listener);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + eventSource.getClass().getName()
                    + " does not have a public add" + listenerType.getSimpleName()
                    + " method which takes a parameter of type " + listenerType.getName() + ".");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + eventSource.getClass().getName()
                    + " does not have an accessible add" + listenerType.getSimpleName ()
                    + " method which takes a parameter of type " + listenerType.getName() + ".");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to add listener.", e.getCause());
        }
    }

    /**
     * Binds an event listener to a specific method on a specific object.
     *
     * @param <L>          the event listener type
     * @param target       the target object
     * @param methodName   the name of the method to be called
     * @param eventSource  the object which is generating events (JButton, JList, etc.)
     * @param listenerType the listener interface (ActionListener.class, SelectionListener.class, etc.)
     * @param eventTypes   the event types (method names) from the listener interface (if none specified, all will be
     *                     supported)
     */
    public static <L> void bindEventsToMethod(Object target, String methodName, Object eventSource,
            Class<L> listenerType, String... eventTypes) {
        final L listener = listenerType.cast(Proxy.newProxyInstance(target.getClass().getClassLoader(),
                new Class[] { listenerType }, new EventBindingInvocationHandler(target, methodName, eventTypes)));
        addEventListener(eventSource, listenerType, listener);
    }

    private static class EventBindingInvocationHandler implements InvocationHandler {
        private final Object target;
        private final String methodName;
        private final Set<String> eventTypes;

        /**
         * Creates a new instance of {@code EventBindingInvocationHandler}.
         *
         * @param target the target object for method invocations
         * @param methodName the name of the method to be invoked
         * @param eventTypes the names of the supported event types
         */
        EventBindingInvocationHandler(final Object target, final String methodName, String[] eventTypes) {
            this.target = target;
            this.methodName = methodName;
            this.eventTypes = new HashSet<String>(Arrays.asList(eventTypes));
        }

        /**
         * Handles a method invocation on the proxy object.
         *
         * @param proxy the proxy instance
         * @param method the method to be invoked
         * @param parameters the parameters for the method invocation
         * @return the result of the method call
         * @throws Throwable if an error occurs
         */
        public Object invoke(final Object proxy, final Method method, final Object[] parameters) throws Throwable {
            if (eventTypes.isEmpty() || eventTypes.contains(method.getName())) {
                if (hasMatchingParametersMethod(method)) {
                    return MethodUtils.invokeMethod(target, methodName, parameters);
                } else {
                    return MethodUtils.invokeMethod(target, methodName);
                }
            }
            return null;
        }

        /**
         * Checks whether a method for the passed in parameters can be found.
         *
         * @param method the listener method invoked
         * @return a flag whether the parameters could be matched
         */
        private boolean hasMatchingParametersMethod(final Method method) {
            return MethodUtils.getAccessibleMethod(target.getClass(), methodName, method.getParameterTypes()) != null;
        }
    }
}
