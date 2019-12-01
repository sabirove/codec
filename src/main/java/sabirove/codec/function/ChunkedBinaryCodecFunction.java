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

package sabirove.codec.function;

import java.io.*;
import java.util.Arrays;

import sabirove.codec.util.CodecUtil;

final class ChunkedBinaryCodecFunction extends CodecFunction<byte[]> {
    private final int chunkSize;
    private final boolean strict;

    ChunkedBinaryCodecFunction(int chunkSize, boolean strict) {
        CodecUtil.checkArgument(chunkSize > 0, "chunkSize should be positive");
        this.chunkSize = chunkSize;
        this.strict = strict;
    }

    @Override
    public void write(byte[] value, OutputStream out) throws IOException {
        if (strict) {
            CodecUtil.checkArgument(value.length == chunkSize,
                    "output array length should be strictly equal to the chunkSize=%s", chunkSize);
        }
        out.write(value);
    }

    @Override
    public byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[chunkSize];
        int read = in.read(buf);
        if (read == -1 || strict && read < chunkSize) {
            throw new EOFException();
        }
        return read == chunkSize ? buf : Arrays.copyOf(buf, read);
    }
}
