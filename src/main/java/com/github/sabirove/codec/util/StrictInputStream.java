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
 * throws {@link EOFException} to signal a failure.
 *
 * <p>This is rather specific but more optimal approach for the target use case compared to common
 * practice of wrapping the input with {@link BufferedInputStream} when no actual buffering is required.
 */
public final class StrictInputStream extends FilterInputStream {
    public StrictInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        CodecUtil.checkArgument(off >= 0 && off + len <= b.length, "illegal range");

        if (len == 0) {
            return 0;
        }

        int read = in.read(b, off, len);

        if (read == -1) {
            throw new EOFException();
        }

        while (read < len) {
            int next = in.read(b, off + read, len - read);
            if (next == -1) {
                throw new EOFException();
            }
            read += next;
        }

        return read;
    }

    public static InputStream wrap(InputStream in) {
        return in instanceof StrictInputStream ||
                in instanceof BufferedInputStream ||
                in instanceof ByteArrayInputStream
                ? in : new StrictInputStream(in);
    }
}
