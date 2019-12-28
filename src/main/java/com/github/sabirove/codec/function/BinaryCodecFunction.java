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

import com.github.sabirove.codec.util.Varint;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class BinaryCodecFunction extends CodecFunction<byte[]> {
    private static final byte[] EMPTY = new byte[0];

    @Override
    public void write(byte[] value, OutputStream out) throws IOException {
        Varint.writeUnsignedVarInt(value.length, out);
        out.write(value);
    }

    @Override
    public byte[] read(InputStream in) throws IOException {
        int len = Varint.readUnsignedVarInt(in);
        if (len == 0) {
            return EMPTY;
        }
        byte[] bytes = new byte[len];
        int read = in.read(bytes);
        if (read < len) {
            throw read == -1
                    ? new EOFException()
                    : new IOException(String.format("incomplete read: expected bytes=%s, actually read=%s", len, read));
        }
        return bytes;
    }
}
