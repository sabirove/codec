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

import com.github.sabirove.codec.test_util.FuncAndGen;
import com.github.sabirove.codec.test_util.RndCodec;
import com.github.sabirove.codec.test_util.TestUtil;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sabirove.codec.test_util.Rnd.rndInt;

@SuppressWarnings("resource")
class CodecFunctionTest {

    @RepeatedTest(500)
    <T> void testRandomCodecFunction() throws IOException {

        FuncAndGen<T> funcAndGen = RndCodec.rndCodecFunctionAndGen();

        List<T> inputs = Stream.generate(funcAndGen.gen)
                .limit(rndInt(1, 11))
                .collect(Collectors.toList());

        CodecFunction<T> function = funcAndGen.func;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (T input : inputs) {
            function.write(input, bos);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        for (T expected : inputs) {
            T actual = function.read(bis);
            TestUtil.assertEq(expected, actual);
        }
    }
}
