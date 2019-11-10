/*
 * Copyright 2019 The Heartwood Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.thewhitakers.heartwood.lang;

import java.util.Optional;

public abstract class Either<L, R> {


    /**
     * Creates a new Either with either left or right value.  One of the parameters must be null.
     *
     * @param left The left value or {@code null}
     * @param right The right value or {@code null}
     * @param <L> Left value type.
     * @param <R> Right value type.
     * @return Either holding non-null value.
     * @throws IllegalArgumentException Thrown if both parameters are either null or non-null.
     */
    public static <L, R> Either<L, R> oneOf(final L left, final R right) {
        if ((left == null && right == null) || (left != null && right != null)) {
            throw new IllegalArgumentException("Either left or right must be null");
        }

        if (left == null) {
            return right(right);
        }

        return left(left);
    }

    /**
     * Checks if this Either has the left value.
     *
     * @return {@code true} if this is the left value.
     */
    public abstract boolean isLeft();

    /**
     * Checks if this Either has the right value.
     *
     * @return {@code true} if this is the right value.
     */
    public abstract boolean isRight();

    /**
     * Retrieve the left value. Will throw exception if in this instance has no left value.
     *
     * @return The left value.
     * @throws IllegalStateException if this instance has no left value.
     */
    public abstract L getLeft();

    /**
     * Retrieve the right value. Will throw exception if in this instance has no right value.
     *
     * @return The right value.
     * @throws IllegalStateException if this instance has no right value.
     */
    public abstract R getRight();

    /**
     * Create the left value of an Either instance.
     *
     * @param left The left value.
     * @param <L> Left value type.
     * @param <R> Right value type.
     * @return Either instance with the left value.
     */
    public static <L, R> Either<L, R> left(final L left) {
        return new Left<>(left);
    }

    /**
     * Retrieves optional left value.
     *
     * @return {@link Optional} with the left value, or emtpy Optional if this is the right value.
     */
    public Optional<L> left() {
        return isLeft() ? Optional.ofNullable(getLeft()) : Optional.<L>empty();
    }

    /**
     * Retrieves optional right value.
     *
     * @return {@link Optional} with the right value, or emtpy Optional if this is the left value.
     */
    public Optional<R> right() {
        return isRight() ? Optional.ofNullable(getRight()) : Optional.<R>empty();
    }

    /**
     * Create the right value of an Either instance.
     *
     * @param right The right value.
     * @param <L> Left value type.
     * @param <R> Right value type.
     * @return Either instance with the right value.
     */
    public static <L, R> Either<L, R> right(final R right) {
        return new Right<>(right);
    }
}
