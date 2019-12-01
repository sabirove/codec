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
 * {@link InputStream} wrapper allowing to read decoded values from the underlying stream.
 */
public final class DecoderStream<T> implements AutoCloseable {
    private final InputStream is;
    private final CodecFunction<T> function;

    DecoderStream(InputStream is, CodecFunction<T> function) {
        this.is = is;
        this.function = function;
    }

    /**
     * Read and decode the value of the target type from the underlying stream.
     *
     * @return decoded value
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public T read() {
        try {
            return function.read(is);
        } catch (IOException e) {
            return CodecUtil.throwUnchecked(e);
        }
    }

    /**
     * Shortcut for {@link #read()} and then {@link #close()}.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public T readAndClose() {
        try (DecoderStream<T> ds = this) {
            return read();
        }
    }

    @Override
    public void close() {
        try {
            is.close();
        } catch (IOException e) {
            CodecUtil.throwUnchecked(e);
        }
    }
}
