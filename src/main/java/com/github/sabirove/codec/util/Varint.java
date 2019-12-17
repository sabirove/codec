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


import static com.github.sabirove.codec.util.CodecUtil.throwUnchecked;

/*
 * Adapted from: com.clearspring.analytics:stream:2.9.5
 * file: com.clearspring.analytics.util.Varint
 * github: https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java
 *
 * Modifications:
 * 1. APIs adapted to accommodate InputStream/OutputStream parameters
 * 2. Explicit EOF checks added for 'read' methods
 * 3. Irrelevant API methods removed
 * 4. IOException handling added
 * 4. Javadoc adjusted accordingly
 */

/**
 * LEB128 variable-length encoding/decoding utility.
 */
public final class Varint {
    private Varint() { }

    /**
     * @see #writeUnsignedVarLong(long, OutputStream)
     */
    public static void writeUnsignedVarInt(int value, OutputStream out) {
        try {
            while ((value & 0xFFFFFF80) != 0L) {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write(value & 0x7F);
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    /**
     * Read unsigned {@code int} written using the LEB128 variable-length encoding.
     *
     * @throws IllegalArgumentException if variable-length value does not terminate after 5 bytes have been read
     * @throws UncheckedIOException     wrapping the {@link IOException} when operation fails with {@link IOException}
     * @throws UncheckedIOException     wrapping the {@link EOFException} when the end of stream reached while reading
     * @see #readUnsignedVarLong(InputStream)
     */
    public static int readUnsignedVarInt(InputStream in) {
        try {
            int value = 0;
            int i = 0;
            int b;
            while (((b = in.read()) & 0x80) != 0) {
                if (b == -1) {
                    throw new EOFException();
                }
                value |= (b & 0x7F) << i;
                i += 7;
                if (i > 35) {
                    throw new IllegalArgumentException("Variable length quantity is too long");
                }
            }
            return value | (b << i);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }


    /**
     * @see #writeSignedVarLong(long, OutputStream)
     */
    public static void writeSignedVarInt(int value, OutputStream out) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarInt((value << 1) ^ (value >> 31), out);
    }

    /**
     * Read signed {@code int} written using the "zig-zag" LEB128 variable-length encoding.
     *
     * @throws IllegalArgumentException if variable-length value does not terminate after 5 bytes have been read
     * @throws UncheckedIOException     wrapping the {@link IOException} when operation fails with {@link IOException}
     * @throws UncheckedIOException     wrapping the {@link EOFException} when the end of stream reached while reading
     * @see #readSignedVarLong(InputStream)
     */
    public static int readSignedVarInt(InputStream in) {
        int raw = readUnsignedVarInt(in);
        // This undoes the trick in writeSignedVarInt()
        int temp = (((raw << 31) >> 31) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values.
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1 << 31));
    }

    /**
     * Write unsigned {@code long} using the LEB128 variable-length encoding.
     * Input must not be negative! Negative input will be treated like a large unsigned value.
     * To write potentially negative values use {@link #writeSignedVarLong(long, OutputStream)} instead.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws UncheckedIOException wrapping the {@link IOException} when operation fails with {@link IOException}
     */
    public static void writeUnsignedVarLong(long value, OutputStream out) {
        try {
            while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
                out.write(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write((int) value & 0x7F);
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    /**
     * Read signed {@code long} written in the "zig-zag" LEB128 variable-length encoding.
     *
     * @param in to read bytes from
     * @return decoded value
     * @throws UncheckedIOException     wrapping the {@link IOException} when operation fails with {@link IOException}
     * @throws UncheckedIOException     wrapping the {@link EOFException} when the end of stream reached while reading
     * @throws IllegalArgumentException if variable-length value does not terminate after 9 bytes have been read
     * @see #writeUnsignedVarLong(long, OutputStream)
     */
    public static long readUnsignedVarLong(InputStream in) {
        try {
            long value = 0L;
            int i = 0;
            long b;
            while (((b = in.read()) & 0x80L) != 0) {
                if (b == -1) {
                    throw new EOFException();
                }
                value |= (b & 0x7F) << i;
                i += 7;
                if (i > 63) {
                    throw new IllegalArgumentException("Variable length quantity is too long");
                }
            }
            return value | (b << i);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    /**
     * Write signed {@code long} using the "zig-zag" LEB128 variable-length encoding.
     * If values are known to be potentially non-negative, {@link #writeUnsignedVarLong(long, OutputStream)}
     * should be used instead.
     *
     * @param value value to encode
     * @param out   to write bytes to
     * @throws UncheckedIOException wrapping the {@link IOException} when operation fails with {@link IOException}
     */
    public static void writeSignedVarLong(long value, OutputStream out) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarLong((value << 1) ^ (value >> 63), out);
    }

    /**
     * Read unsigned {@code long} written in the LEB128 variable-length encoding.
     *
     * @param in to read bytes from
     * @return decoded value
     * @throws UncheckedIOException     wrapping the {@link IOException} when operation fails with {@link IOException}
     * @throws UncheckedIOException     wrapping the {@link EOFException} when the end of stream reached while reading
     * @throws IllegalArgumentException if variable-length value does not terminate after 9 bytes have been read
     * @see #writeSignedVarLong(long, OutputStream)
     */
    public static long readSignedVarLong(InputStream in) {
        long raw = readUnsignedVarLong(in);
        // This undoes the trick in writeSignedVarLong()
        long temp = (((raw << 63) >> 63) ^ raw) >> 1;
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values
        // Must re-flip the top bit if the original read value had it set.
        return temp ^ (raw & (1L << 63));
    }
}
