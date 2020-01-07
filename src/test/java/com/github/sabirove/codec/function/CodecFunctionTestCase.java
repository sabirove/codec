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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sabirove.codec.test_util.Rnd;
import com.github.sabirove.codec.test_util.TestInputStream;
import com.github.sabirove.codec.test_util.TestOutputStream;
import com.github.sabirove.codec.test_util.TestUtil;
import org.junit.jupiter.api.RepeatedTest;


import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class CodecFunctionTestCase<T> {

    abstract CodecFunction<T> getFunction();

    abstract T generateInput();

    @RepeatedTest(100)
    final void runTest() throws IOException {
        CodecFunction<T> function = getFunction();
        List<T> inputs = Stream.generate(this::generateInput)
                .limit(Rnd.rndInt(100))
                .collect(Collectors.toList());
        TestOutputStream tos = new TestOutputStream();
        for (T input : inputs) {
            function.write(input, tos);
            tos.assertNotFlushed();
            tos.assertNotClosed();
        }
        TestInputStream tis = TestInputStream.from(tos);

        for (T expected : inputs) {
            T actual = function.read(tis);
            tis.assertNotClosed();
            TestUtil.assertEq(expected, actual);
        }
        tis.assertEOF();

        TestUtil.assertThrowsIO(EOFException.class, () -> function.read(tis));
    }
}
