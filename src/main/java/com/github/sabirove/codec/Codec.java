/*
 * Copyright 2020 Sabirov Evgenii (sabirov.e@gmail.com)
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

package com.github.sabirove.codec;

import com.github.sabirove.codec.function.CodecFunction;

import java.io.*;

/**
 * Bidirectional IO function suitable to encode/decode single values and streams of values
 * of the specific type operating on top of the {@link java.io} streams.
 *
 * @param <T> target value type
 * @apiNote use {@link CodecBuilder#withFunction(CodecFunction)} builder to build an instance.
 */
public interface Codec<T> {

    /**
     * Wrap the provided {@link OutputStream} to write the stream of values encoded by this codec.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when wrap operation fails
     * @implNote shouldn't close the underlying stream
     */
    EncoderStream<T> wrap(OutputStream os);

    /**
     * Wrap the provided {@link InputStream} to decode the stream of values previously encoded by this codec.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when wrap operation fails
     * @implNote shouldn't close the underlying stream
     */
    DecoderStream<T> wrap(InputStream is);

    /**
     * Encode single value to bytes.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    default byte[] encode(T value) {
        @SuppressWarnings("resource")
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wrap(bos).writeAndClose(value);
        return bos.toByteArray();
    }

    /**
     * Decode single value from bytes.
     *
     * @param in bytes containing the value previously encoded by this codec
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    default T decode(byte[] in) {
        @SuppressWarnings("resource")
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        return wrap(bais).readAndClose();
    }
}
