/*
 * Copyright 2019 Sabirov Evgenii (sabirov.e@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sabirove.codec.function;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;


/**
 * Bidirectional serialization function for doing IO operations
 * on target values against the provided sources.
 * <p>
 * Standard implementations can be obtained via {@link CodecFunctions} factory.
 *
 * @param <T> target value type
 * @implNote implementations are expected to throw {@link IOException} as is without any wrapping.
 */
public abstract class CodecFunction<T> {

    /**
     * @throws IOException when fails to write to the stream
     * @implNote shouldn't flush or close the underlying stream
     */
    public abstract void write(T value, OutputStream out) throws IOException;

    /**
     * @throws IOException  when fails to read from the stream
     * @throws EOFException when trying to read and the end of the input stream reached
     * @implNote shouldn't close the underlying stream
     */
    public abstract T read(InputStream in) throws IOException;

    /**
     * Adapt this function to another target type by using a pair of specified type converters.
     */
    public final <V> CodecFunction<V> adapt(Function<T, V> from, Function<V, T> to) {
        final CodecFunction<T> it = this;
        return new CodecFunction<V>() {
            @Override
            public void write(V value, OutputStream out) throws IOException {
                it.write(to.apply(value), out);
            }

            @Override
            public V read(InputStream in) throws IOException {
                return from.apply(it.read(in));
            }
        };
    }
}
