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

import java.util.Objects;

public final class Left<L, R> extends Either<L, R> {
    private final L value;

    public Left(final L value) {
        super();

        this.value = value;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public L getLeft() {
        return value;
    }

    @Override
    public R getRight() {
        throw new IllegalStateException("No right value");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Left)) {
            return false;
        }

        final Left<?, ?> left = (Left<?, ?>)obj;

        return Objects.equals(this.value, left.value);
    }

    @Override
    public String toString() {
        return String.format("Left {%s}", this.value);
    }
}
