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
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("resource")
class VarintTest {

    @Test
    void unsignedVarIntTest() {
        unsignedVarIntTest(0, 1);
        unsignedVarIntTest(1, 1);
        unsignedVarIntTest(-1, Integer.BYTES + 1);
        unsignedVarIntTest(63, 1);
        unsignedVarIntTest(-63, Integer.BYTES + 1);
        unsignedVarIntTest(64, 1);
        unsignedVarIntTest(-64, Integer.BYTES + 1);
        unsignedVarIntTest(127, 1);
        unsignedVarIntTest(-127, Integer.BYTES + 1);
        unsignedVarIntTest(128, 2);
        unsignedVarIntTest(-128, Integer.BYTES + 1);
        unsignedVarIntTest(Integer.MAX_VALUE, Integer.BYTES + 1);
        unsignedVarIntTest(Integer.MIN_VALUE, Integer.BYTES + 1);

    }

    @Test
    void signedVarIntTest() {
        signedVarIntTest(0, 1);
        signedVarIntTest(1, 1);
        signedVarIntTest(-1, 1);
        signedVarIntTest(63, 1);
        signedVarIntTest(-63, 1);
        signedVarIntTest(64, 2);
        signedVarIntTest(-64, 1);
        signedVarIntTest(-65, 2);
        signedVarIntTest(127, 2);
        signedVarIntTest(-127, 2);
        signedVarIntTest(128, 2);
        signedVarIntTest(-128, 2);
        signedVarIntTest(Integer.MAX_VALUE, Integer.BYTES + 1);
        signedVarIntTest(Integer.MIN_VALUE, Integer.BYTES + 1);
    }

    @Test
    void unsignedVarLongTest() {
        unsignedVarLongTest(0, 1);
        unsignedVarLongTest(1, 1);
        unsignedVarLongTest(-1, Long.BYTES + 2);
        unsignedVarLongTest(63, 1);
        unsignedVarLongTest(-63, Long.BYTES + 2);
        unsignedVarLongTest(64, 1);
        unsignedVarLongTest(-64, Long.BYTES + 2);
        unsignedVarLongTest(127, 1);
        unsignedVarLongTest(-127, Long.BYTES + 2);
        unsignedVarLongTest(128, 2);
        unsignedVarLongTest(-128, Long.BYTES + 2);
        unsignedVarLongTest(Long.MAX_VALUE, Long.BYTES + 1);
        unsignedVarLongTest(Long.MIN_VALUE, Long.BYTES + 2);

    }

    @Test
    void signedVarLongTest() {
        signedVarLongTest(0, 1);
        signedVarLongTest(1, 1);
        signedVarLongTest(-1, 1);
        signedVarLongTest(63, 1);
        signedVarLongTest(-63, 1);
        signedVarLongTest(64, 2);
        signedVarLongTest(-64, 1);
        signedVarLongTest(-65, 2);
        signedVarLongTest(127, 2);
        signedVarLongTest(-127, 2);
        signedVarLongTest(128, 2);
        signedVarLongTest(-128, 2);
        signedVarLongTest(Long.MAX_VALUE, Long.BYTES + 2);
        signedVarLongTest(Long.MIN_VALUE, Long.BYTES + 2);
    }

    private static void unsignedVarIntTest(int value, int expectedLength) {
        intTest(value, expectedLength, Varint::writeUnsignedVarInt, Varint::readUnsignedVarInt);
    }

    private static void signedVarIntTest(int value, int expectedLength) {
        intTest(value, expectedLength, Varint::writeSignedVarInt, Varint::readSignedVarInt);
    }

    private static void unsignedVarLongTest(long value, int expectedLength) {
        longTest(value, expectedLength, Varint::writeUnsignedVarLong, Varint::readUnsignedVarLong);
    }

    private static void signedVarLongTest(long value, int expectedLength) {
        longTest(value, expectedLength, Varint::writeSignedVarLong, Varint::readSignedVarLong);
    }

    private static void intTest(int value,
                                int expectedLength,
                                BiConsumer<Integer, OutputStream> writer,
                                Function<InputStream, Integer> reader) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.accept(value, out);
        byte[] bytes = out.toByteArray();
        assertEquals(expectedLength, bytes.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        assertEquals(value, (int) reader.apply(bis));
    }

    private static void longTest(long value,
                                 int expectedLength,
                                 BiConsumer<Long, OutputStream> writer,
                                 Function<InputStream, Long> reader) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.accept(value, out);
        byte[] bytes = out.toByteArray();
        assertEquals(expectedLength, bytes.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        assertEquals(value, (long) reader.apply(bis));
    }
}