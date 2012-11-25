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
package external.org.apache.commons.lang3.exception;

import java.util.List;
import java.util.Set;

import external.org.apache.commons.lang3.tuple.Pair;

/**
 * <p>
 * A runtime exception that provides an easy and safe way to add contextual information.
 * </p><p>
 * An exception trace itself is often insufficient to provide rapid diagnosis of the issue.
 * Frequently what is needed is a select few pieces of local contextual data.
 * Providing this data is tricky however, due to concerns over formatting and nulls.
 * </p><p>
 * The contexted exception approach allows the exception to be created together with a
 * list of context label-value pairs. This additional information is automatically included in
 * the message and printed stack trace.
 * </p><p>
 * A checked version of this exception is provided by ContextedException.
 * </p>
 * <p>
 * To use this class write code as follows:
 * </p>
 * <pre>
 *   try {
 *     ...
 *   } catch (Exception e) {
 *     throw new ContextedRuntimeException("Error posting account transaction", e)
 *          .addContextValue("Account Number", accountNumber)
 *          .addContextValue("Amount Posted", amountPosted)
 *          .addContextValue("Previous Balance", previousBalance)
 *   }
 * }
 * </pre> or improve diagnose data at a higher level:
 * <pre>
 *   try {
 *     ...
 *   } catch (ContextedRuntimeException e) {
 *     throw e.setContextValue("Transaction Id", transactionId);
 *   } catch (Exception e) {
 *     if (e instanceof ExceptionContext) {
 *       e.setContextValue("Transaction Id", transactionId);
 *     }
 *     throw e;
 *   }
 * }
 * </pre>
 * </p><p>
 * The output in a printStacktrace() (which often is written to a log) would look something like the following:
 * <pre>
 * org.apache.commons.lang3.exception.ContextedRuntimeException: java.lang.Exception: Error posting account transaction
 *  Exception Context:
 *  [1:Account Number=null]
 *  [2:Amount Posted=100.00]
 *  [3:Previous Balance=-2.17]
 *  [4:Transaction Id=94ef1d15-d443-46c4-822b-637f26244899]
 *
 *  ---------------------------------
 *  at org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest.testAddValue(ContextedExceptionTest.java:88)
 *  ..... (rest of trace)
 * </pre>
 * </p>
 * 
 * @see ContextedException
 * @since 3.0
 */
public class ContextedRuntimeException extends RuntimeException implements ExceptionContext {

    /** The serialization version. */
    private static final long serialVersionUID = 20110706L;
    /** The context where the data is stored. */
    private final ExceptionContext exceptionContext;

    /**
     * Instantiates ContextedRuntimeException without message or cause.
     * <p>
     * The context information is stored using a default implementation.
     */
    public ContextedRuntimeException() {
        super();
        exceptionContext = new DefaultExceptionContext();
    }

    /**
     * Instantiates ContextedRuntimeException with message, but without cause.
     * <p>
     * The context information is stored using a default implementation.
     * 
     * @param message  the exception message, may be null
     */
    public ContextedRuntimeException(String message) {
        super(message);
        exceptionContext = new DefaultExceptionContext();
    }

    /**
     * Instantiates ContextedRuntimeException with cause, but without message.
     * <p>
     * The context information is stored using a default implementation.
     * 
     * @param cause  the underlying cause of the exception, may be null
     */
    public ContextedRuntimeException(Throwable cause) {
        super(cause);
        exceptionContext = new DefaultExceptionContext();
    }

    /**
     * Instantiates ContextedRuntimeException with cause and message.
     * <p>
     * The context information is stored using a default implementation.
     * 
     * @param message  the exception message, may be null
     * @param cause  the underlying cause of the exception, may be null
     */
    public ContextedRuntimeException(String message, Throwable cause) {
        super(message, cause);
        exceptionContext = new DefaultExceptionContext();
    }

    /**
     * Instantiates ContextedRuntimeException with cause, message, and ExceptionContext.
     * 
     * @param message  the exception message, may be null
     * @param cause  the underlying cause of the exception, may be null
     * @param context  the context used to store the additional information, null uses default implementation
     */
    public ContextedRuntimeException(String message, Throwable cause, ExceptionContext context) {
        super(message, cause);
        if (context == null) {
            context = new DefaultExceptionContext();
        }
        exceptionContext = context;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds information helpful to a developer in diagnosing and correcting the problem.
     * For the information to be meaningful, the value passed should have a reasonable
     * toString() implementation.
     * Different values can be added with the same label multiple times.
     * <p>
     * Note: This exception is only serializable if the object added is serializable.
     * </p>
     * 
     * @param label  a textual label associated with information, {@code null} not recommended
     * @param value  information needed to understand exception, may be {@code null}
     * @return {@code this}, for method chaining, not {@code null}
     */
    public ContextedRuntimeException addContextValue(String label, Object value) {        
        exceptionContext.addContextValue(label, value);
        return this;
    }

    /**
     * Sets information helpful to a developer in diagnosing and correcting the problem.
     * For the information to be meaningful, the value passed should have a reasonable
     * toString() implementation.
     * Any existing values with the same labels are removed before the new one is added.
     * <p>
     * Note: This exception is only serializable if the object added as value is serializable.
     * </p>
     * 
     * @param label  a textual label associated with information, {@code null} not recommended
     * @param value  information needed to understand exception, may be {@code null}
     * @return {@code this}, for method chaining, not {@code null}
     */
    public ContextedRuntimeException setContextValue(String label, Object value) {        
        exceptionContext.setContextValue(label, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public List<Object> getContextValues(String label) {
        return this.exceptionContext.getContextValues(label);
    }

    /**
     * {@inheritDoc}
     */
    public Object getFirstContextValue(String label) {
        return this.exceptionContext.getFirstContextValue(label);
    }

    /**
     * {@inheritDoc}
     */
    public List<Pair<String, Object>> getContextEntries() {
        return this.exceptionContext.getContextEntries();
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getContextLabels() {
        return exceptionContext.getContextLabels();
    }

    /**
     * Provides the message explaining the exception, including the contextual data.
     * 
     * @see java.lang.Throwable#getMessage()
     * @return the message, never null
     */
    @Override
    public String getMessage(){
        return getFormattedExceptionMessage(super.getMessage());
    }

    /**
     * Provides the message explaining the exception without the contextual data.
     * 
     * @see java.lang.Throwable#getMessage()
     * @return the message
     * @since 3.0.1
     */
    public String getRawMessage() {
        return super.getMessage();
    }

    /**
     * {@inheritDoc}
     */
    public String getFormattedExceptionMessage(String baseMessage) {
        return exceptionContext.getFormattedExceptionMessage(baseMessage);
    }

}
