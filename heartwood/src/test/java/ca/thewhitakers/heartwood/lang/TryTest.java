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

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TryTest {
    @Test
    public void successfulExecutionShouldReturnSuccess() {
        final Try<Boolean> actual = Try.execute(() -> true);

        assertThat(actual, is(instanceOf(Try.Success.class)));
        assertThat(actual.isSuccessful(), is(true));
    }

    @Test
    public void shouldGetActualResultFromSuccess() {
        final Try<Integer> actual = Try.execute(() -> 42);

        assertThat(actual.getValue(), is(42));
    }

    @Test
    public void shouldGetActualThrowableFromFailure() {
        final Try<Integer> actual = Try.execute(() -> {
            throw new NumberFormatException("blah");
        });

        assertThat(actual.getFailure(), is(instanceOf(NumberFormatException.class)));
    }

    @Test
    public void unsuccessfulExecutionShouldReturnFailure() {
        final Try<Boolean> actual = Try.execute(() -> {
            throw new Exception("blah");
        });

        assertThat(actual, is(instanceOf(Try.Failure.class)));
        assertThat(actual.isSuccessful(), is(false));
    }

    @Test
    public void shouldRunOnSuccessHandler() {
        final AtomicInteger actual = new AtomicInteger(0);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Try.execute(() -> 42).onSuccess(actual::set).onFailure(ex -> failed.set(true));

        assertThat(actual.get(), is(42));
        assertThat(failed.get(), is(false));
    }

    @Test
    public void shouldRunOnFailureHandler() {
        final AtomicInteger actual = new AtomicInteger(0);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Try.execute(() -> {
            throw new Exception("blah");
        }).onSuccess(val -> actual.set(42)).onFailure(ex -> failed.set(true));

        assertThat(failed.get(), is(true));
        assertThat(actual.get(), is(0));
    }

    @Test
    public void shouldExecuteFunctionWithParameter() {
        final Try<Integer> actual = Try.<Integer, Integer>execute(x -> 42 + x).apply(42);

        assertThat(actual.getValue(), is(84));
    }

    @Test
    public void mapShouldMapValues() {
        final Try<String> actual = Try.execute(() -> 42).then(String::valueOf);

        assertThat(actual.getValue(), is("42"));
    }

    @Test
    public void mapShouldPropagateErrors() {
        final Try<String> actual = Try.execute(() -> {
            throw new NumberFormatException("blah");
        }).then(String::valueOf);

        assertThat(actual.getFailure(), is(instanceOf(NumberFormatException.class)));
    }

    @Test
    public void mapShouldMapTry() {
        final Try<String> actual = Try.execute(() -> 42).thenTry(Try.execute(String::valueOf));

        assertThat(actual.getValue(), is("42"));
    }

    @Test

    public void mapShouldPropagateTryErrors() {
        final Try<String> actual = Try.execute(() -> 42).thenTry(i -> Try.failure(new NumberFormatException("blah")));

        assertThat(actual.getFailure(), is(instanceOf(NumberFormatException.class)));
    }

    @Test
    public void shouldReturnAbsentOptionWhenFailed() {
        final Try<Integer> actual = Try.execute(() -> {
            throw new IllegalStateException("blah");
        });

        assertThat(actual.get().isPresent(), is(false));
    }

    @Test
    public void shouldReturnOptionalResult() {
        final Try<Integer> actual = Try.execute(() -> 42);

        assertThat(actual.get(), is(Optional.of(42)));
    }


    @Test
    public void shouldReturnEmptyStreamWhenFailed() {
        final Try<Integer> actual = Try.execute(() -> {
            throw new IllegalStateException("blah");
        });

        assertThat(actual.stream().count(), is(0L));
    }

    @Test
    public void shouldReturnResultWrappedInStream() {
        final Try<Integer> actual = Try.execute(() -> 42);

        assertThat(actual.stream().findFirst(), is(Optional.of(42)));
    }


    @Test
    public void shouldThrowExceptionWhenGettingFailureFromSuccess() {
        assertThrows(IllegalStateException.class, () -> {
            final Try<Integer> actual = Try.execute(() -> 42);

            actual.getFailure();
        });
    }

    @Test
    public void shouldThrowExceptionWhenGettingValueFromFailure() {
        assertThrows(IllegalStateException.class, () -> {
            final Try<Integer> actual = Try.execute(() -> {
                throw new IllegalAccessException("blah");
            });

            actual.getValue();
        });
    }

    @Test
    public void shouldCreateSuccess() {
        final Try<Integer> actual = Try.success(42);

        assertThat(actual.isSuccessful(), is(true));
        assertThat(actual.get(), is(Optional.of(42)));
    }

    @Test
    public void shouldCreateFailure() {
        final Try actual = Try.failure(new IllegalStateException("blah"));

        assertThat(actual.isSuccessful(), is(false));
        assertThat(actual.getFailure(), is(instanceOf(IllegalStateException.class)));
    }
}