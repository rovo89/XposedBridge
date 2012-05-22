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
package org.apache.commons.lang3.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * <p>Provides utilities for manipulating and examining 
 * <code>Throwable</code> objects.</p>
 *
 * @since 1.0
 * @version $Id: ExceptionUtils.java 1199894 2011-11-09 17:53:59Z ggregory $
 */
public class ExceptionUtils {
    
    /**
     * <p>Used when printing stack frames to denote the start of a
     * wrapped exception.</p>
     *
     * <p>Package private for accessibility by test suite.</p>
     */
    static final String WRAPPED_MARKER = " [wrapped] ";

    /**
     * <p>The names of methods commonly used to access a wrapped exception.</p>
     */
    // TODO: Remove in Lang 4.0
    private static final String[] CAUSE_METHOD_NAMES = {
        "getCause",
        "getNextException",
        "getTargetException",
        "getException",
        "getSourceException",
        "getRootCause",
        "getCausedByException",
        "getNested",
        "getLinkedException",
        "getNestedException",
        "getLinkedCause",
        "getThrowable",
    };

    /**
     * <p>
     * Public constructor allows an instance of <code>ExceptionUtils</code> to be created, although that is not
     * normally necessary.
     * </p>
     */
    public ExceptionUtils() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the default names used when searching for the cause of an exception.</p>
     *
     * <p>This may be modified and used in the overloaded getCause(Throwable, String[]) method.</p>
     *
     * @return cloned array of the default method names
     * @since 3.0
     * @deprecated This feature will be removed in Lang 4.0
     */
    @Deprecated
    public static String[] getDefaultCauseMethodNames() {
        return ArrayUtils.clone(CAUSE_METHOD_NAMES);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
     *
     * <p>The method searches for methods with specific names that return a 
     * <code>Throwable</code> object. This will pick up most wrapping exceptions,
     * including those from JDK 1.4.
     *
     * <p>The default list searched for are:</p>
     * <ul>
     *  <li><code>getCause()</code></li>
     *  <li><code>getNextException()</code></li>
     *  <li><code>getTargetException()</code></li>
     *  <li><code>getException()</code></li>
     *  <li><code>getSourceException()</code></li>
     *  <li><code>getRootCause()</code></li>
     *  <li><code>getCausedByException()</code></li>
     *  <li><code>getNested()</code></li>
     * </ul>
     * 
     * <p>If none of the above is found, returns <code>null</code>.</p>
     *
     * @param throwable  the throwable to introspect for a cause, may be null
     * @return the cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     * @since 1.0
     * @deprecated This feature will be removed in Lang 4.0
     */
    @Deprecated
    public static Throwable getCause(Throwable throwable) {
        return getCause(throwable, CAUSE_METHOD_NAMES);
    }

    /**
     * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
     *
     * <p>A <code>null</code> set of method names means use the default set.
     * A <code>null</code> in the set of method names will be ignored.</p>
     *
     * @param throwable  the throwable to introspect for a cause, may be null
     * @param methodNames  the method names, null treated as default set
     * @return the cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     * @since 1.0
     * @deprecated This feature will be removed in Lang 4.0
     */
    @Deprecated
    public static Throwable getCause(Throwable throwable, String[] methodNames) {
        if (throwable == null) {
            return null;
        }

        if (methodNames == null) {
            methodNames = CAUSE_METHOD_NAMES;
        }

        for (String methodName : methodNames) {
            if (methodName != null) {
                Throwable cause = getCauseUsingMethodName(throwable, methodName);
                if (cause != null) {
                    return cause;
                }
            }
        }

        return null;
    }

    /**
     * <p>Introspects the <code>Throwable</code> to obtain the root cause.</p>
     *
     * <p>This method walks through the exception chain to the last element,
     * "root" of the tree, using {@link #getCause(Throwable)}, and
     * returns that exception.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. If the throwable parameter
     * has a cause of itself, then null will be returned. If the throwable
     * parameter cause chain loops, the last element in the chain before the
     * loop is returned.</p>
     *
     * @param throwable  the throwable to get the root cause for, may be null
     * @return the root cause of the <code>Throwable</code>,
     *  <code>null</code> if none found or null throwable input
     */
    public static Throwable getRootCause(Throwable throwable) {
        List<Throwable> list = getThrowableList(throwable);
        return list.size() < 2 ? null : (Throwable)list.get(list.size() - 1);
    }

    /**
     * <p>Finds a <code>Throwable</code> by method name.</p>
     *
     * @param throwable  the exception to examine
     * @param methodName  the name of the method to find and invoke
     * @return the wrapped exception, or <code>null</code> if not found
     */
    // TODO: Remove in Lang 4.0
    private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
        Method method = null;
        try {
            method = throwable.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) { // NOPMD
            // exception ignored
        } catch (SecurityException ignored) { // NOPMD
            // exception ignored
        }

        if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Throwable) method.invoke(throwable);
            } catch (IllegalAccessException ignored) { // NOPMD
                // exception ignored
            } catch (IllegalArgumentException ignored) { // NOPMD
                // exception ignored
            } catch (InvocationTargetException ignored) { // NOPMD
                // exception ignored
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Counts the number of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return <code>1</code>.
     * A throwable with one cause will return <code>2</code> and so on.
     * A <code>null</code> throwable will return <code>0</code>.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. The cause chain is
     * processed until the end is reached, or until the next item in the
     * chain is already in the result set.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @return the count of throwables, zero if null input
     */
    public static int getThrowableCount(Throwable throwable) {
        return getThrowableList(throwable).size();
    }

    /**
     * <p>Returns the list of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return an array containing
     * one element - the input throwable.
     * A throwable with one cause will return an array containing
     * two elements. - the input throwable and the cause throwable.
     * A <code>null</code> throwable will return an array of size zero.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. The cause chain is
     * processed until the end is reached, or until the next item in the
     * chain is already in the result set.</p>
     *
     * @see #getThrowableList(Throwable)
     * @param throwable  the throwable to inspect, may be null
     * @return the array of throwables, never null
     */
    public static Throwable[] getThrowables(Throwable throwable) {
        List<Throwable> list = getThrowableList(throwable);
        return list.toArray(new Throwable[list.size()]);
    }

    /**
     * <p>Returns the list of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return a list containing
     * one element - the input throwable.
     * A throwable with one cause will return a list containing
     * two elements. - the input throwable and the cause throwable.
     * A <code>null</code> throwable will return a list of size zero.</p>
     *
     * <p>This method handles recursive cause structures that might
     * otherwise cause infinite loops. The cause chain is processed until
     * the end is reached, or until the next item in the chain is already
     * in the result set.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @return the list of throwables, never null
     * @since Commons Lang 2.2
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = ExceptionUtils.getCause(throwable);
        }
        return list;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified class (exactly) in the exception chain.
     * Subclasses of the specified class do not match - see
     * {@link #indexOfType(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param clazz  the class to search for, subclasses do not match, null returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     */
    public static int indexOfThrowable(Throwable throwable, Class<?> clazz) {
        return indexOf(throwable, clazz, 0, false);
    }

    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified type in the exception chain from
     * a specified index.
     * Subclasses of the specified class do not match - see
     * {@link #indexOfType(Throwable, Class, int)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.
     * A negative start index is treated as zero.
     * A start index greater than the number of throwables returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param clazz  the class to search for, subclasses do not match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     */
    public static int indexOfThrowable(Throwable throwable, Class<?> clazz, int fromIndex) {
        return indexOf(throwable, clazz, fromIndex, false);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified class or subclass in the exception chain.
     * Subclasses of the specified class do match - see
     * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     * @since 2.1
     */
    public static int indexOfType(Throwable throwable, Class<?> type) {
        return indexOf(throwable, type, 0, true);
    }

    /**
     * <p>Returns the (zero based) index of the first <code>Throwable</code>
     * that matches the specified type in the exception chain from
     * a specified index.
     * Subclasses of the specified class do match - see
     * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
     *
     * <p>A <code>null</code> throwable returns <code>-1</code>.
     * A <code>null</code> type returns <code>-1</code>.
     * No match in the chain returns <code>-1</code>.
     * A negative start index is treated as zero.
     * A start index greater than the number of throwables returns <code>-1</code>.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     * @since 2.1
     */
    public static int indexOfType(Throwable throwable, Class<?> type, int fromIndex) {
        return indexOf(throwable, type, fromIndex, true);
    }

    /**
     * <p>Worker method for the <code>indexOfType</code> methods.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @param fromIndex  the (zero based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @param subclass if <code>true</code>, compares with {@link Class#isAssignableFrom(Class)}, otherwise compares
     * using references
     * @return index of the <code>type</code> within throwables nested withing the specified <code>throwable</code>
     */
    private static int indexOf(Throwable throwable, Class<?> type, int fromIndex, boolean subclass) {
        if (throwable == null || type == null) {
            return -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        if (fromIndex >= throwables.length) {
            return -1;
        }
        if (subclass) {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.isAssignableFrom(throwables[i].getClass())) {
                    return i;
                }
            }
        } else {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.equals(throwables[i].getClass())) {
                    return i;
                }
            }
        }
        return -1;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Prints a compact stack trace for the root cause of a throwable
     * to <code>System.err</code>.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output
     * @since 2.0
     */
    public static void printRootCauseStackTrace(Throwable throwable) {
        printRootCauseStackTrace(throwable, System.err);
    }

    /**
     * <p>Prints a compact stack trace for the root cause of a throwable.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output, may be null
     * @param stream  the stream to output to, may not be null
     * @throws IllegalArgumentException if the stream is <code>null</code>
     * @since 2.0
     */
    public static void printRootCauseStackTrace(Throwable throwable, PrintStream stream) {
        if (throwable == null) {
            return;
        }
        if (stream == null) {
            throw new IllegalArgumentException("The PrintStream must not be null");
        }
        String trace[] = getRootCauseStackTrace(throwable);
        for (String element : trace) {
            stream.println(element);
        }
        stream.flush();
    }

    /**
     * <p>Prints a compact stack trace for the root cause of a throwable.</p>
     *
     * <p>The compact stack trace starts with the root cause and prints
     * stack frames up to the place where it was caught and wrapped.
     * Then it prints the wrapped exception and continues with stack frames
     * until the wrapper exception is caught and wrapped again, etc.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * Note that this is the opposite order to the JDK1.4 display.</p>
     *
     * <p>The method is equivalent to <code>printStackTrace</code> for throwables
     * that don't have nested causes.</p>
     *
     * @param throwable  the throwable to output, may be null
     * @param writer  the writer to output to, may not be null
     * @throws IllegalArgumentException if the writer is <code>null</code>
     * @since 2.0
     */
    public static void printRootCauseStackTrace(Throwable throwable, PrintWriter writer) {
        if (throwable == null) {
            return;
        }
        if (writer == null) {
            throw new IllegalArgumentException("The PrintWriter must not be null");
        }
        String trace[] = getRootCauseStackTrace(throwable);
        for (String element : trace) {
            writer.println(element);
        }
        writer.flush();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Creates a compact stack trace for the root cause of the supplied
     * <code>Throwable</code>.</p>
     *
     * <p>The output of this method is consistent across JDK versions.
     * It consists of the root exception followed by each of its wrapping
     * exceptions separated by '[wrapped]'. Note that this is the opposite
     * order to the JDK1.4 display.</p>
     *
     * @param throwable  the throwable to examine, may be null
     * @return an array of stack trace frames, never null
     * @since 2.0
     */
    public static String[] getRootCauseStackTrace(Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        Throwable throwables[] = getThrowables(throwable);
        int count = throwables.length;
        List<String> frames = new ArrayList<String>();
        List<String> nextTrace = getStackFrameList(throwables[count - 1]);
        for (int i = count; --i >= 0;) {
            List<String> trace = nextTrace;
            if (i != 0) {
                nextTrace = getStackFrameList(throwables[i - 1]);
                removeCommonFrames(trace, nextTrace);
            }
            if (i == count - 1) {
                frames.add(throwables[i].toString());
            } else {
                frames.add(WRAPPED_MARKER + throwables[i].toString());
            }
            for (int j = 0; j < trace.size(); j++) {
                frames.add(trace.get(j));
            }
        }
        return frames.toArray(new String[frames.size()]);
    }

    /**
     * <p>Removes common frames from the cause trace given the two stack traces.</p>
     *
     * @param causeFrames  stack trace of a cause throwable
     * @param wrapperFrames  stack trace of a wrapper throwable
     * @throws IllegalArgumentException if either argument is null
     * @since 2.0
     */
    public static void removeCommonFrames(List<String> causeFrames, List<String> wrapperFrames) {
        if (causeFrames == null || wrapperFrames == null) {
            throw new IllegalArgumentException("The List must not be null");
        }
        int causeFrameIndex = causeFrames.size() - 1;
        int wrapperFrameIndex = wrapperFrames.size() - 1;
        while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
            // Remove the frame from the cause trace if it is the same
            // as in the wrapper trace
            String causeFrame = causeFrames.get(causeFrameIndex);
            String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
                causeFrames.remove(causeFrameIndex);
            }
            causeFrameIndex--;
            wrapperFrameIndex--;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Gets the stack trace from a Throwable as a String.</p>
     *
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(java.io.PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     *
     * @param throwable  the <code>Throwable</code> to be examined
     * @return the stack trace as generated by the exception's
     *  <code>printStackTrace(PrintWriter)</code> method
     */
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * <p>Captures the stack trace associated with the specified
     * <code>Throwable</code> object, decomposing it into a list of
     * stack frames.</p>
     *
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(java.io.PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     *
     * @param throwable  the <code>Throwable</code> to examine, may be null
     * @return an array of strings describing each stack frame, never null
     */
    public static String[] getStackFrames(Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return getStackFrames(getStackTrace(throwable));
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Returns an array where each element is a line from the argument.</p>
     *
     * <p>The end of line is determined by the value of {@link SystemUtils#LINE_SEPARATOR}.</p>
     *
     * @param stackTrace  a stack trace String
     * @return an array where each element is a line from the argument
     */
    static String[] getStackFrames(String stackTrace) {
        String linebreak = SystemUtils.LINE_SEPARATOR;
        StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        List<String> list = new ArrayList<String>();
        while (frames.hasMoreTokens()) {
            list.add(frames.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * <p>Produces a <code>List</code> of stack frames - the message
     * is not included. Only the trace of the specified exception is
     * returned, any caused by trace is stripped.</p>
     *
     * <p>This works in most cases - it will only fail if the exception
     * message contains a line that starts with:
     * <code>&quot;&nbsp;&nbsp;&nbsp;at&quot;.</code></p>
     * 
     * @param t is any throwable
     * @return List of stack frames
     */
    static List<String> getStackFrameList(Throwable t) {
        String stackTrace = getStackTrace(t);
        String linebreak = SystemUtils.LINE_SEPARATOR;
        StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        List<String> list = new ArrayList<String>();
        boolean traceStarted = false;
        while (frames.hasMoreTokens()) {
            String token = frames.nextToken();
            // Determine if the line starts with <whitespace>at
            int at = token.indexOf("at");
            if (at != -1 && token.substring(0, at).trim().length() == 0) {
                traceStarted = true;
                list.add(token);
            } else if (traceStarted) {
                break;
            }
        }
        return list;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a short message summarising the exception.
     * <p>
     * The message returned is of the form
     * {ClassNameWithoutPackage}: {ThrowableMessage}
     *
     * @param th  the throwable to get a message for, null returns empty string
     * @return the message, non-null
     * @since Commons Lang 2.2
     */
    public static String getMessage(Throwable th) {
        if (th == null) {
            return "";
        }
        String clsName = ClassUtils.getShortClassName(th, null);
        String msg = th.getMessage();
        return clsName + ": " + StringUtils.defaultString(msg);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a short message summarising the root cause exception.
     * <p>
     * The message returned is of the form
     * {ClassNameWithoutPackage}: {ThrowableMessage}
     *
     * @param th  the throwable to get a message for, null returns empty string
     * @return the message, non-null
     * @since Commons Lang 2.2
     */
    public static String getRootCauseMessage(Throwable th) {
        Throwable root = ExceptionUtils.getRootCause(th);
        root = root == null ? th : root;
        return getMessage(root);
    }

}
