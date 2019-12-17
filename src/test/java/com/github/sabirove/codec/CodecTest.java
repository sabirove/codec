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

package com.github.sabirove.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.RepeatedTest;
import com.github.sabirove.codec.filter.CodecBufferSpec;
import com.github.sabirove.codec.filter.CodecFilters;
import com.github.sabirove.codec.function.CodecFunctions;


import static org.junit.jupiter.api.Assertions.assertEquals;

class CodecTest {

    private final Codec<State> codec =
            Codec.withFunction(CodecFunctions.binarySerializing(State::write, State::read))
                    .withBuffer(CodecBufferSpec.ofDefaultSize())
                    .withFilterChain(
                            CodecFilters.encryptWithAes(),
                            CodecFilters.encodeWithBase64Url(),
                            CodecFilters.compressWithDeflate()
                    ).build();

    @RepeatedTest(50)
    void testStreamEncodeDecode() {
        List<State> expected = Stream.generate(State::random)
                .limit(Rnd.rndInt(10))
                .collect(Collectors.toList());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (EncoderStream<State> eos = codec.wrap(bos)) {
            expected.forEach(eos::write);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        try (DecoderStream<State> dos = codec.wrap(bis)) {
            for (State exp : expected) {
                State act = dos.read();
                assertEquals(exp, act);
            }
        }
    }

    @RepeatedTest(100)
    void testBytesEncodeDecode() {
        State stat = State.random();
        byte[] encoded = codec.encode(stat);
        State decoded = codec.decode(encoded);
        assertEquals(stat, decoded);
    }

}