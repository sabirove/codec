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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.github.sabirove.codec.util.StateInputStream;
import com.github.sabirove.codec.util.StateOutputStream;

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
     * Standard java serialization codec function that works for any Serializable object.
     * Use for applications where serialization performance and footprint size is not of a major concern.
     * To lower the footprint apply compression or better yet use {@link #binarySerializing(BiConsumer, Function)}
     * function which yields much better performance and tiny footprint.
     */
    public static <T extends Serializable> CodecFunction<T> javaSerializing(Class<T> clazz) {
        return new JavaSerializationCodecFunction<>(clazz);
    }

    /**
     * Ad-hoc binary codec function with IO based on the pair of complementing
     * {@link StateOutputStream}/{@link StateInputStream} filters allowing to conveniently read/write most
     * of the standard java types.
     * Uses variable length encoding to write contiguous data length values as well as enum ordinals
     * allowing to yield small output footprint. <br>
     * <br> Limitations: <br>
     * <ol>
     *      <li>reading and writing of the fields should be carried out in the exact same order.</li>
     *      <li>writing null values is prohibited.</li>
     *      <li>no type information is stored and validated whatsoever.</li>
     * </ol>
     *
     * @param writer function to write the arbitrary {@code state} to the supplied input buffer
     * @param reader function to read the written {@code state} from the supplied output buffer
     */
    public static <T extends Serializable> CodecFunction<T> binarySerializing(
            BiConsumer<StateOutputStream, T> writer,
            Function<StateInputStream, T> reader) {
        return new BinarySerializationCodecFunction<>(writer, reader);
    }

    /**
     * Codec function reading and writing plain byte arrays.
     * Writes array lengths so that exactly the same arrays can be read back from the stream.
     *
     * @apiNote for a more specific use case of writing chunked byte arrays use {@link #binaryChunked(int, boolean)}.
     */
    public static CodecFunction<byte[]> binary() {
        return BINARY;
    }

    /**
     * {@link #binary()} analog that writes and reads against the stream in chunks of the specified size.
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
        return new ChunkedBinaryCodecFunction(chunkSize, strict);
    }

    /**
     * Codec function reading and writing the strings with the specified {@link Charset}.
     */
    public static CodecFunction<String> string(Charset charset) {
        switch (charset.name()) {
            case "UTF-8": return STRING_UTF8;
            case "UTF-16": return STRING_UTF16;
            case "US-ASCII": return STRING_ASCII;
            default: return new StringCodecFunction(charset);
        }
    }

}
