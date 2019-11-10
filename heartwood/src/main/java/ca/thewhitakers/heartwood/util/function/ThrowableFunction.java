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

package ca.thewhitakers.heartwood.util.function;

/**
 * Represents a function that accepts one argument and produces a result; with a possibility of throwing
 * an error.
 *
 * @param <T> the type of the argument to the function
 * @param <R> the type of the return value from the function
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {
    /**
     * Apply this function to the specified argument.
     *
     * @param a the argument
     * @return the result
     * @throws Throwable if shit hits the fan.
     */
    R apply(T a) throws Throwable;
}
