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

import javax.annotation.WillNotClose;

import sabirove.codec.filter.*;
import sabirove.codec.function.CodecFunction;


import static sabirove.codec.util.CodecUtil.throwUnchecked;

/**
 * Bidirectional IO function suitable to encode/decode single values and streams of values
 * of the specific type operating on top of the {@link java.io} streams.
 *
 * @param <T> target value type
 * @apiNote use {@link #withFunction(CodecFunction)} builder to build an instance.
 */
@SuppressWarnings("resource")
public final class Codec<T> {
    private final CodecFunction<T> function;
    private final CodecFilter filter;

    private Codec(CodecFunction<T> function, CodecFilter filter) {
        this.function = function;
        this.filter = filter;
    }

    /*
     * Single value oriented API
     */

    /**
     * Encode single value to bytes.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public byte[] encode(T value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wrap(bos).write(value);
        return bos.toByteArray();
    }

    /**
     * Decode single value from bytes.
     *
     * @param in bytes containing the value previously encoded by this codec
     * @throws UncheckedIOException wrapping the original {@link IOException} when IO operation fails
     */
    public T decode(byte[] in) {
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        return wrap(bais).read();
    }

    /*
     * Stream oriented API
     */

    /**
     * Wrap the provided {@link OutputStream} to write the stream of values encoded by this codec.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when wrap operation fails
     */
    public EncoderStream<T> wrap(@WillNotClose OutputStream os) {
        try {
            return new EncoderStream<>(filter.filter(os), function);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    /**
     * Wrap the provided {@link InputStream} to decode the stream of values previously encoded by this codec.
     *
     * @throws UncheckedIOException wrapping the original {@link IOException} when wrap operation fails
     */
    public DecoderStream<T> wrap(@WillNotClose InputStream is) {
        try {
            return new DecoderStream<>(filter.filter(is), function);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    /*
     * Builder API
     */

    /**
     * Build the codec instance starting with the provided {@link CodecFunction}.
     */
    public static <T> Builder<T> withFunction(CodecFunction<T> function) {
        return new Builder<>(function);
    }

    public static final class Builder<T> {
        private final CodecFunction<T> function;
        private CodecFilter filter = CodecFilters.noOp();
        private CodecFilter bufferSpec = CodecBufferSpec.ofDefaultSize();

        private Builder(CodecFunction<T> function) {
            this.function = function;
        }

        /**
         * Specify the codec filter to apply when doing IO with this codec.
         *
         * @apiNote <ul>
         * <li>do not use {@link CodecBufferSpec} as plain codec filter</li>
         * <li>use {@link #withFilterChain(CodecFilter...)} to add more than one filter. Alternatively,
         * a filter composed with {@link CodecFilter#chain(CodecFilter...)} could be provided.</li>
         * </ul>
         */
        public Builder<T> withFilter(CodecFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Specify the codec filter chain (filters applied consequently) to apply
         * when doing IO with this codec.
         *
         * @apiNote do not use {@link CodecBufferSpec} as part of the filter chain.
         */
        public Builder<T> withFilterChain(CodecFilter... chain) {
            return withFilter(CodecFilter.chain(chain));
        }

        /**
         * Specify the buffering strategy to apply when doing IO with this codec.
         */
        public Builder<T> withBuffer(CodecBufferSpec bufferSpec) {
            this.bufferSpec = bufferSpec;
            return this;
        }

        /**
         * Do not apply buffering when doing IO with this codec.
         */
        public Builder<T> withNoBuffer() {
            this.bufferSpec = CodecFilters.noOp();
            return this;
        }

        public Codec<T> build() {
            CodecFilter chain = filter.chain(bufferSpec);
            return new Codec<>(function, chain);
        }
    }
}
