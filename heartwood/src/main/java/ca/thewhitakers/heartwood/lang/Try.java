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

import ca.thewhitakers.heartwood.util.function.ThrowableFunction;
import ca.thewhitakers.heartwood.util.function.ThrowableRunnable;
import ca.thewhitakers.heartwood.util.function.ThrowableSupplier;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A Try monad.
 * Represents a type that may have succeeded with a value of T or failed with a throwable.
 *
 * @param <T> Succeeded value type.
 */
@SuppressWarnings({"unused"})
public abstract class Try<T> {

    private Try() {
    }

    /**
     * Execute the supplied function that produces a result wrapped in a {@code Try<T>}.
     *
     * @param func the function to execute
     * @param <T> the return value of the function being called.
     * @return the result of the operation wrapped in {@code Try<T>}}
     */
    public static <T> Try<T> execute(final ThrowableSupplier<T> func) {
        try {
            return new Success<>(func.get());
        } catch (final Throwable throwable) {
            return new Failure<>(throwable);
        }
    }

    /**
     * Execute the supplied function with no arguments, expecting no return value but could possibly
     * fail.
     *
     * @param func the function to be executed
     * @return the result of the operation wrapped in {@code Try<Void>}
     */
    public static Try<Void> execute(final ThrowableRunnable func) {
        try {
            func.run();

            return new Success<>(null);
        } catch (final Throwable throwable) {
            return new Failure<>(throwable);
        }
    }

    /**
     * Execute the supplied function in a {@code Try} context.
     *
     * <pre>
     * {@code
     *  Try.execute((p) -> "The value of p should be 69").apply("69");
     * }
     * </pre>
     *
     * @param func the function to be executed inside a {@code Try}
     * @param <T> the parameter type of the function to be called.
     * @param <R> the return value of the function called.
     * @return a function which will return {@code Try<R>} when applied to a parameter
     */
    public static <T, R> Function<T, Try<R>> execute(final ThrowableFunction<T, R> func) {
        return p -> Try.execute(() -> func.apply(p));
    }

    /**
     * Return a successful {@code Try<T>}.
     *
     * @param value the successful value
     * @param <T> type of result
     * @return a successfully wrapped {@code Try<T>}
     */
    public static <T> Try<T> success(final T value) {
        return new Try.Success<>(value);
    }

    /**
     * Return a failed {@code Try<T>}.
     *
     * @param throwable the problem
     * @param <T> type of result
     * @return a failed wrapped {@code Try<T>}
     */
    public static <T> Try<T> failure(final Throwable throwable) {
        return new Try.Failure<>(throwable);
    }


    /**
     * Is this instance successful.
     *
     * @return {@code true} if execution was successful
     */
    public abstract boolean isSuccessful();

    /**
     * Retrieve the contained value.
     *
     * @return value of the execution
     * @throws IllegalStateException if execution was not successful. Use {@link #isSuccessful()} to check execution
     *                               status.
     */
    public abstract T getValue();

    /**
     * If exists, return the failure.
     *
     * @return exception which caused failure
     * @throws IllegalStateException if execution was successful. Use {@link #isSuccessful()} to check execution
     *                               status.
     */
    public abstract Throwable getFailure();

    /**
     * Executes supplied consumer in case of success.
     *
     * @param successHandler {@link Consumer} to receive execution result.
     * @return This instance of Try.
     */
    public Try<T> onSuccess(final Consumer<T> successHandler) {
        if (isSuccessful()) {
            successHandler.accept(getValue());
        }
        return this;
    }

    /**
     * Executes supplied consumer in case of failure.
     *
     * @param failHandler {@link Consumer} to receive failure in form of {@link Throwable}.
     * @return This instance of Try.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Try<T> onFailure(final Consumer<Throwable> failHandler) {
        if (!isSuccessful()) {
            failHandler.accept(getFailure());
        }
        return this;
    }

    /**
     * Allows to re-throw exception in case of failure.
     *
     * @param exceptionSupplier Supplier of an exception.
     * @param <X> Type of exception to throw.
     * @return This instance of Try.
     * @throws X Type of re-throwing exception.
     */
    public <X extends Throwable> Try<T> onFailureThrow(final Function<Throwable, ? extends X> exceptionSupplier)
            throws X {
        if (!isSuccessful()) {
            throw exceptionSupplier.apply(getFailure());
        }
        return this;
    }

    /**
     * Returns success execution result as a stream, otherwise an empty stream.
     *
     * @return A stream of success result.
     */
    public Stream<T> getSuccess() {
        if (isSuccessful()) {
            return Stream.of(getValue());
        } else {
            return Stream.empty();
        }
    }

    /**
     * Allows chaining of several operations depending on result of the previous one. Hides complexity of error-handling
     * and allows developer to focus on happy path.
     * Usage example:
     * <pre>
     * Try&lt;Integer&gt; result = Try.execute(() -&gt; deepThought.ask("What is the answer to the ultimate
     * question?")).then((x) -&gt; interpretAnswer(x));
     * </pre>
     * This code will execute method deepThough.ask() which accepts string as a parameter and then pass its result to
     * function interpretAnswer, which returns int.
     * If both of the functions execute successfully then result will be {@link Try.Success} containing an integer, if
     * any of the functions fail with exception then result will be {@link Try.Failure} containing exception. This code
     * will never crash.
     *
     * @param mapper function which accepts value from this object.
     * @param <S>    type of returning value of mapper function
     * @return result of execution of mapper function wrapped in Try
     */
    public <S> Try<S> then(final Function<T, S> mapper) {
        if (isSuccessful()) {
            return Try.execute(() -> mapper.apply(getValue()));
        } else {
            return new Failure<>(getFailure());
        }
    }

    /**
     * This method allows chaining of {@link Try} statements depending on result of the previous one. Hides complexity
     * of error-handling and allows developer to focus on happy path.
     *
     * @param mapper function which accepts value from this object.
     * @param <S>    type of returning value of mapper function.
     * @return result of execution of mapper function.
     */
    public <S> Try<S> thenTry(final Function<T, Try<S>> mapper) {
        if (isSuccessful()) {
            return mapper.apply(getValue());
        } else {
            return new Failure<>(getFailure());
        }
    }

    /**
     * Retrieves result of computation from Try wrapped in {@link Optional}. If execution was successful it will return
     * it, otherwise {@link Optional#empty()} will be returned.
     * Beware: there is no way to distinguish success with value {@code null}
     *
     * @return Optional result of computation.
     */
    public Optional<T> get() {
        if (isSuccessful()) {
            return Optional.ofNullable(getValue());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Execute an alternative {@link ThrowableSupplier} as a recovery step if the original execution failed.
     *
     * @param alternative the alternative operation
     * @return the result of the alternative operation; otherwise the original successful value
     */
    public Try<T> recover(final ThrowableSupplier<T> alternative) {
        if (isSuccessful()) {
            return this;
        }

        return Try.execute(alternative);
    }

    /**
     * If the execution was successful, return a sequential {@link Stream} containing the successful result.  Otherwise
     * return an empty stream.
     *
     * @return the successful value as a {@link Stream}
     */
    public Stream<T> stream() {
        return get().stream();
    }

    /**
     * Represents a successful execution.
     *
     * @param <T> type of wrapped result type
     */
    static final class Success<T> extends Try<T> {
        private final T value;

        Success(final T value) {
            super();
            this.value = value;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public Throwable getFailure() {
            throw new IllegalStateException("Success does not contain failure");
        }
    }

    /**
     * Represents a failed execution.
     *
     * @param <T> the expected type of result
     */
    static final class Failure<T> extends Try<T> {
        private final Throwable failure;

        Failure(final Throwable failure) {
            super();
            this.failure = failure;
        }

        @Override
        public Throwable getFailure() {
            return this.failure;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public T getValue() {
            throw new IllegalStateException("Failure does not contain result", this.failure);
        }
    }

}