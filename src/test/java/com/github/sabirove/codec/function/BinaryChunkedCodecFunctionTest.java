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

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.github.sabirove.codec.test_util.Rnd;
import com.github.sabirove.codec.test_util.TestInputStream;
import com.github.sabirove.codec.test_util.TestOutputStream;
import com.github.sabirove.codec.test_util.TestUtil;
import org.junit.jupiter.api.RepeatedTest;


import static org.junit.jupiter.api.Assertions.*;

final class BinaryChunkedCodecFunctionTest extends CodecFunctionTestCase<byte[]> {

    private static final int CHUNK_SIZE = 128;

    @Override
    CodecFunction<byte[]> getFunction() {
        return CodecFunctions.binaryChunked(CHUNK_SIZE, true);
    }

    @Override
    byte[] generateInput() {
        byte[] bytes = new byte[CHUNK_SIZE];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    @RepeatedTest(100)
    void testStrict() throws IOException {
        CodecFunction<byte[]> function = CodecFunctions.binaryChunked(CHUNK_SIZE, true);
        TestOutputStream tos = new TestOutputStream();
        assertThrows(IllegalArgumentException.class, () -> function.write(Rnd.rndBytes(CHUNK_SIZE - 1), tos));
        TestUtil.assertThrowsIO(EOFException.class, () -> function.read(new TestInputStream(new byte[0])));

        byte[] i1 = generateInput();
        byte[] i2 = generateInput();
        function.write(i1, tos);
        function.write(i2, tos);

        TestInputStream tis = TestInputStream.from(tos);

        assertArrayEquals(i1, function.read(tis));
        assertArrayEquals(i2, function.read(tis));
        TestUtil.assertThrowsIO(EOFException.class, () -> function.read(tis));
    }

    @RepeatedTest(100)
    void testNonStrict() throws IOException {
        CodecFunction<byte[]> function = CodecFunctions.binaryChunked(CHUNK_SIZE, false);
        TestOutputStream tos = new TestOutputStream();
        TestUtil.assertThrowsIO(EOFException.class, () -> function.read(new TestInputStream(new byte[0])));

        byte[] i1 = generateInput();

        byte[] i2 = generateInput();

        byte[] i3 = generateInput();
        byte[] i4 = generateInput();
        byte[] i3i4 = new byte[CHUNK_SIZE * 2];
        System.arraycopy(i3, 0, i3i4, 0, i3.length);
        System.arraycopy(i4, 0, i3i4, i3.length, i4.length);
        byte[] i5 = Rnd.rndBytes(10, 20);

        function.write(i1, tos);
        function.write(i2, tos);
        function.write(i3i4, tos);
        function.write(i5, tos);

        TestInputStream tis = TestInputStream.from(tos);

        assertArrayEquals(i1, function.read(tis));
        assertArrayEquals(i2, function.read(tis));
        assertArrayEquals(i3, function.read(tis));
        assertArrayEquals(i4, function.read(tis));
        assertArrayEquals(i5, function.read(tis));
        TestUtil.assertThrowsIO(EOFException.class, () -> function.read(tis));

        TestOutputStream tos2 = new TestOutputStream();
        tos2.write(34);
        assertEquals(34, function.read(TestInputStream.from(tos2))[0]);
    }

}
