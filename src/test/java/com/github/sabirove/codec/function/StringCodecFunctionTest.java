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

import java.nio.charset.StandardCharsets;

import com.github.sabirove.codec.test_util.Rnd;

final class StringCodecFunctionTest extends CodecFunctionTestCase<String> {

    @Override
    CodecFunction<String> getFunction() {
        return CodecFunctions.stringSerializing(StandardCharsets.UTF_8);
    }

    @Override
    String generateInput() {
        return Rnd.rndString(4096, StandardCharsets.UTF_8);
    }

}
