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

import com.github.sabirove.codec.util.StateInputStream;
import com.github.sabirove.codec.util.StateOutputStream;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Standard {@link CodecFunction} implementations.
 */
public final class CodecFunctions {
    private static final BinaryCodecFunction BINARY = new BinaryCodecFunction();
    private static final StringCodecFunction STRING_UTF8 = new StringCodecFunction(StandardCharsets.UTF_8);
    private static final StringCodecFunction STRING_UTF16 = new StringCodecFunction(StandardCharsets.UTF_16);
    private static final StringCodecFunction STRING_ASCII = new StringCodecFunction(StandardCharsets.US_ASCII);

    private CodecFunctions() { }

    /**
     * Standard java serialization function that works for any Serializable object.
     * Use for applications where serialization performance and footprint size is not of a major concern.
     * To lower the footprint apply compression or better yet use binary serialization function which yields much
     * better performance and smallest possible footprint.
     */
    public static <T extends Serializable> CodecFunction<T> javaSerializing(Class<T> clazz) {
        return new JavaSerializationCodecFunction<>(clazz);
    }

    /**
     * Ad-hoc binary serialization function based on a pair of custom {@code java.io} stream wrappers
     * {@link StateInputStream}/{@link StateOutputStream} providing convenient API to read and write
     * most of the standard Java types including collections, maps, arrays, strings, enums and then some.
     * <p>Supports IO with LEB128 variable-length encoded `int` and `long` values: unsigned variable-length
     * ints are used to serialize enum ordinals and length values for contiguous data types (e.g. collections, arrays)
     * helping to yield tiny serialization footprint.</p>
     * <br>
     * <p>Limitations:
     * <ol>
     *   <li>writing and reading of the fields should be carried out in the exact same order</li>
     *   <li>null values are not supported</li>
     *   <li>no type information is stored and validated whatsoever</li>
     * </ol>
     * @param writer function to write the arbitrary {@code state} to the supplied input buffer
     * @param reader function to read the written {@code state} from the supplied output buffer
     */
    public static <T extends Serializable> CodecFunction<T> binarySerializing(
            BiConsumer<StateOutputStream, T> writer,
            Function<StateInputStream, T> reader) {
        return new BinarySerializationCodecFunction<>(writer, reader);
    }

    /**
     * Codec function for IO with plain byte arrays.
     * Writes array lengths so that exactly the same arrays can be read back from the stream.
     *
     * @apiNote for a more specific use case of writing chunked byte arrays use {@link #binaryChunked(int, boolean)}.
     */
    public static CodecFunction<byte[]> binary() {
        return BINARY;
    }

    /**
     * {@link #binary()} analog that writes and reads bytes in chunks of specified size.
     * <ul>
     *      <li>If {@code strict} mode is on only the chunks of the exact {@code chinkSize} can be written or read.</li>
     *      <li>If {@code strict} mode is off chunks of any size can be written, while reads are is still in chunks
     *      of specified size except for the last chunk in the stream which could be less (remainder). </li>
     * </ul>
     *
     * @param chunkSize target chunk size in bytes
     * @param strict    "strict mode" feature option flag
     * @apiNote for a more generic use case of writing arbitrary length byte arrays use {@link #binary()} (int, boolean)}.
     */
    public static CodecFunction<byte[]> binaryChunked(int chunkSize, boolean strict) {
        return new BinaryChunkedCodecFunction(chunkSize, strict);
    }

    /**
     * Codec function for IO with strings using specified encoding.
     */
    public static CodecFunction<String> stringSerializing(Charset charset) {
        switch (charset.name()) {
            case "UTF-8": return STRING_UTF8;
            case "UTF-16": return STRING_UTF16;
            case "US-ASCII": return STRING_ASCII;
            default: return new StringCodecFunction(charset);
        }
    }

    /**
     * Codec function for IO with strings using UTF-8 encoding.
     */
    public static CodecFunction<String> stringSerializing() {
        return STRING_UTF8;
    }
}
