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

package com.github.sabirove.codec.filter;

import com.github.sabirove.codec.test_util.RndCodec;
import com.github.sabirove.codec.util.SafeInputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.RepeatedTest;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sabirove.codec.test_util.Rnd.rndBytes;
import static com.github.sabirove.codec.test_util.Rnd.rndInt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressFBWarnings("OS_OPEN_STREAM")
class CodecFilterChainTest {

    @RepeatedTest(500)
    void testRandomCodecFilterChain() throws IOException {

        List<byte[]> inputs = Stream.generate(() -> rndBytes(1, 10000))
                .limit(rndInt(1, 8))
                .collect(Collectors.toList());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        CodecFilter chain = RndCodec.rndFilterChain();

        try (OutputStream filtered = chain.filter(bos)) {
            for (byte[] input : inputs) {
                filtered.write(input);
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        try (InputStream filtered = chain.filter(bis)) {
            for (byte[] expected : inputs) {
                byte[] actual = new byte[expected.length];
                //wrap to StrictInputStream, b.c. there're some "framed" InputStreams used (like InflateInputStream)
                //that will may fail to read all data at once with plain read(byte[]) call
                int read = new SafeInputStream(filtered).read(actual);
                assertEquals(expected.length, read);
                assertArrayEquals(expected, actual);
            }
        }
    }
}
