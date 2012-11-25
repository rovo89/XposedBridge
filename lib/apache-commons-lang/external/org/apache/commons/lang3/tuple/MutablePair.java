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
package external.org.apache.commons.lang3.tuple;

/**
 * <p>A mutable pair consisting of two {@code Object} elements.</p>
 * 
 * <p>Not #ThreadSafe#</p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 *
 * @since Lang 3.0
 * @version $Id: MutablePair.java 1127544 2011-05-25 14:35:42Z scolebourne $
 */
public class MutablePair<L, R> extends Pair<L, R> {

    /** Serialization version */
    private static final long serialVersionUID = 4954918890077093841L;

    /** Left object */
    public L left;
    /** Right object */
    public R right;

    /**
     * <p>Obtains an immutable pair of from two objects inferring the generic types.</p>
     * 
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     * 
     * @param <L> the left element type
     * @param <R> the right element type
     * @param left  the left element, may be null
     * @param right  the right element, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static <L, R> MutablePair<L, R> of(L left, R right) {
        return new MutablePair<L, R>(left, right);
    }

    /**
     * Create a new pair instance of two nulls.
     */
    public MutablePair() {
        super();
    }

    /**
     * Create a new pair instance.
     *
     * @param left  the left value, may be null
     * @param right  the right value, may be null
     */
    public MutablePair(L left, R right) {
        super();
        this.left = left;
        this.right = right;
    }

    //-----------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public L getLeft() {
        return left;
    }

    /**
     * Sets the left element of the pair.
     * 
     * @param left  the new value of the left element, may be null
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public R getRight() {
        return right;
    }

    /**
     * Sets the right element of the pair.
     * 
     * @param right  the new value of the right element, may be null
     */
    public void setRight(R right) {
        this.right = right;
    }

    /**
     * Sets the {@code Map.Entry} value.
     * This sets the right element of the pair.
     * 
     * @param value  the right value to set, not null
     * @return the old value for the right element
     */
    public R setValue(R value) {
        R result = getRight();
        setRight(value);
        return result;
    }

}
