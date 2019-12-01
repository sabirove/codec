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

package sabirove.codec;

import java.io.*;

import sabirove.codec.function.CodecFunction;
import sabirove.codec.util.CodecUtil;

/**
 * {@link OutputStream} wrapper that allows to write the encoded values to the underlying stream.
 */
public final class EncoderStream<T> implements AutoCloseable, Flushable {
    private final OutputStream os;
    private final CodecFunction<T> function;

    EncoderStream(OutputStream os, CodecFunction<T> function) {
        this.os = os;
        this.function = function;
    }

    /**
     * Encode and write provided {@code value} to the underlying stream.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public void write(T value) {
        try {
            function.write(value, os);
        } catch (IOException e) {
            CodecUtil.throwUnchecked(e);
        }
    }

    /**
     * Shortcut for {@link #write(Object)} and then {@link #flush()}.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public void writeAndFlush(T value) {
        write(value);
        flush();
    }

    /**
     * Shortcut for {@link #write(Object)} and then {@link #close()}.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public void writeAndClose(T value) {
        try (EncoderStream<T> es = this) {
            write(value);
        }
    }

    @Override
    public void flush() {
        try {
            os.flush();
        } catch (IOException e) {
            CodecUtil.throwUnchecked(e);
        }
    }

    @Override
    public void close() {
        try (OutputStream os = this.os) {
            os.flush();
        } catch (IOException e) {
            CodecUtil.throwUnchecked(e);
        }
    }
}
