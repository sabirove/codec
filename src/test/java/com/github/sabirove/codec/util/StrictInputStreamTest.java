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

import com.github.sabirove.codec.test_util.Rnd;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"resource", "ResultOfMethodCallIgnored", "ZeroLengthArrayAllocation"})
@SuppressFBWarnings({"OS_OPEN_STREAM", "RR_NOT_CHECKED"})
class StrictInputStreamTest {

    @RepeatedTest(50)
    void testFramedRead() throws IOException {
        FramedInputStream fis = new FramedInputStream();

        List<byte[]> bytes = Stream.generate(() -> Rnd.rndBytes(128))
                .limit(100)
                .peek(fis::offer)
                .collect(Collectors.toList());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024 * 20);
        bytes.forEach(b -> buffer.write(b, 0 , b.length));
        byte[] expected = buffer.toByteArray();

        StrictInputStream sis = new StrictInputStream(fis);
        byte[] actual = new byte[expected.length];
        assertEquals(expected.length, sis.read(actual));
        assertArrayEquals(expected, actual);
        assertEquals(-1, sis.read());
    }

    @Test
    void testEmptyRead() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        StrictInputStream sis = new StrictInputStream(bis);
        assertEquals(0, sis.read(new byte[10], 2, 0));
        assertThrows(EOFException.class, () -> sis.read(new byte[10], 2, 1));
    }

    @Test
    void testStrictness() {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[10]);
        StrictInputStream sis = new StrictInputStream(bis);
        assertThrows(EOFException.class, () -> sis.read(new byte[11]));
    }

    @Test
    void testRangeChecks() {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[10]);
        StrictInputStream sis = new StrictInputStream(bis);
        assertThrows(IllegalArgumentException.class, () -> sis.read(new byte[10], -1, 10));
        assertThrows(IllegalArgumentException.class, () -> sis.read(new byte[10], 0, 11));
    }

    private static final class FramedInputStream extends InputStream {
        private final Queue<ByteArrayInputStream> frames = new ArrayDeque<>();

        void offer(byte[] bytes) {
            frames.offer(new ByteArrayInputStream(bytes));
        }

        @Override
        public int read() {
            return read(ByteArrayInputStream::read);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return read(bis -> bis.read(b, off, len));
        }

        private int read(Function<ByteArrayInputStream, Integer> rdr) {
            while (!frames.isEmpty()) {
                ByteArrayInputStream frame = frames.peek();
                if (frame.available() == 0) {
                    frames.poll();
                } else {
                    return rdr.apply(frame);
                }
            }
            return -1;
        }
    }
}
