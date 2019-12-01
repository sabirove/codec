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

package sabirove.codec.filter;

import java.io.*;
import java.util.stream.Stream;

import javax.annotation.WillNotClose;

import sabirove.codec.Codec;

/**
 * Bidirectional encoding/decoding filter providing extra post/pre- processing of the raw data
 * that's being exchanged against the underlying source.
 * You can think of it as a pair of countering {@link FilterOutputStream}/{@link FilterInputStream}
 * wrappers in a single package.
 * <p>
 * Standard implementations can be obtained via {@link CodecFilters} factory.
 * <p>
 * Most implementations can be easily build inline with {@link #of(Wrapper, Wrapper)} factory
 * by providing a pair of corresponding {@link OutputStream} and {@link InputStream} wrappers.
 * <p>
 * Filters can be chained (composed) with each other by means of {@link #chain(CodecFilter)}
 * or {@link #chain(CodecFilter...)} with the order of composition corresponding to the order of filter application.
 * For example, a usage like {@code filter1.chain(filter2)} or equivalent {@code chain(filter1, filter2)} will yield
 * the following filtering schema: <br>
 * WRITE: {@code bytes -> filter1 -> filter2 -> OUTPUT} <br>
 * READ: {@code bytes <- filter1 <- filter2 <- INPUT} <br>
 *
 * @implNote
 * <ul>
 * <li>do not use {@link BufferedOutputStream}/{@link BufferedInputStream} based filters:
 * IO stream buffering is explicitly addressed within the {@link Codec} implementation.</li>
 * <li>implementations are expected to throw {@link IOException} as is without any wrapping.</li>
 * </ul>
 */
public abstract class CodecFilter {

    public abstract OutputStream filter(@WillNotClose OutputStream out) throws IOException;

    public abstract InputStream filter(@WillNotClose InputStream in) throws IOException;

    @SuppressWarnings("ObjectEquality")
    public final CodecFilter chain(CodecFilter next) {
        if (this == CodecFilters.noOp()) {
            return next;
        } else if (next == CodecFilters.noOp()) {
            return this;
        }
        return of(out -> filter(next.filter(out)), in -> filter(next.filter(in)));
    }

    public static CodecFilter chain(CodecFilter... filters) {
        return Stream.of(filters)
                .reduce((f1, f2) -> f1.chain(f2))
                .orElse(CodecFilters.noOp());
    }

    public static CodecFilter of(Wrapper<OutputStream> outWrapper, Wrapper<InputStream> inWrapper) {
        return new CodecFilter() {
            @Override
            public OutputStream filter(OutputStream out) throws IOException {
                return outWrapper.wrap(out);
            }

            @Override
            public InputStream filter(InputStream in) throws IOException {
                return inWrapper.wrap(in);
            }
        };
    }

    @FunctionalInterface
    public interface Wrapper<T> {
        T wrap(T input) throws IOException;
    }
}
