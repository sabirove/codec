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

import com.github.sabirove.codec.test_util.CodecAndGen;
import com.github.sabirove.codec.test_util.RndCodec;
import com.github.sabirove.codec.test_util.TestUtil;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sabirove.codec.test_util.Rnd.rndInt;

class CodecTest {

    @RepeatedTest(1000)
    <T> void testRandomCodecAssemblyStream() {
        CodecAndGen<T> codecGen = RndCodec.rndCodec();

        List<T> expected = Stream.generate(codecGen.gen)
                .limit(rndInt(10))
                .collect(Collectors.toList());

        Codec<T> codec = codecGen.codec;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (EncoderStream<T> eos = codec.wrap(bos)) {
            expected.forEach(eos::write);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        try (DecoderStream<T> dos = codec.wrap(bis)) {
            for (T exp : expected) {
                T act = dos.read();
                TestUtil.assertEq(exp, act);
            }
        }
    }

    @RepeatedTest(1000)
    <T> void testRandomCodecAssemblySingle() {
        CodecAndGen<T> codecGen = RndCodec.rndCodec();
        T expected = codecGen.gen.get();
        Codec<T> codec = codecGen.codec;
        byte[] encoded = codec.encode(expected);
        T actual = codec.decode(encoded);
        TestUtil.assertEq(expected, actual);
    }
}
