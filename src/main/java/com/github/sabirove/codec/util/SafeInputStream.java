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

package com.github.sabirove.codec.util;

import java.io.*;

/**
 * <p>{@link InputStream} wrapper that helps to avoid possible incomplete reads when using bulk read API
 * ({@link InputStream#read(byte[])}) against "framed" {@link InputStream} implementations.
 *
 * <p> Implements bulk read in the loop until the target buffer is properly filled
 * or EOF is reached (as per {@link InputStream#read()} returning {@code -1}) in which case
 * returns whatever amount of bytes were read (-1 if none).
 *
 * <p>This is a more optimal approach to shield against "framed" input sources compared to the common practice
 * of wrapping the source with {@link BufferedInputStream} when no actual buffering is required.
 */
public final class SafeInputStream extends FilterInputStream {
    public SafeInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off + len > b.length) {
            throw new IllegalArgumentException(
                    String.format("illegal range: array=[0, %s), range=[%s, %s]", b.length, off, off + len)
            );
        }

        int read = 0;

        while (read < len) {
            int next = in.read(b, off + read, len - read);
            if (next == -1) {
                return read > 0 ? read : -1;
            }
            read += next;
        }

        return read;
    }

    /**
     * Wrap the provided {@link InputStream} excluding some {@link InputStream}
     * implementations known to be "safe" as is.
     */
    public static InputStream wrap(InputStream in) {
        return in instanceof SafeInputStream ||
                in instanceof ByteArrayInputStream ||
                in instanceof BufferedInputStream
                ? in : new SafeInputStream(in);
    }
}
