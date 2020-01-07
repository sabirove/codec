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

package com.github.sabirove.codec.test_util;

import com.github.sabirove.codec.Codec;
import com.github.sabirove.codec.filter.CodecBufferSpec;
import com.github.sabirove.codec.filter.CodecFilter;
import com.github.sabirove.codec.filter.CodecFilters;
import com.github.sabirove.codec.function.CodecFunctions;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.sabirove.codec.test_util.Rnd.*;

public class RndCodec {

    private static final CodecFilter[] FILTER_VARIETY = {
            CodecFilters.compressWithDeflate(),
            CodecFilters.compressWithGzip(),
            CodecFilters.encodeWithBase64(),
            CodecFilters.encodeWithBase64Mime(),
            CodecFilters.encodeWithBase64Url(),
            CodecFilters.encryptWithAes()
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<CodecBufferSpec>[] BUFFER_SPECS_VARIETY = new Supplier[]{
            () -> CodecBufferSpec.ofDefaultSize()
                    .withInputStreamExclusions()
                    .withOutputStreamExclusions(),
            () -> CodecBufferSpec.ofSize(rndInt(1, 8192), rndInt(1, 8192))
                    .withInputStreamExclusions()
                    .withOutputStreamExclusions(),
            () -> CodecBufferSpec.ofSize(0, 0),
            () -> CodecBufferSpec.ofSize(0, rndInt(1, 3894))
                    .withOutputStreamExclusions(),
            () -> CodecBufferSpec.ofSize(rndInt(1, 7331), 0)
                    .withInputStreamExclusions()
    };

    private static final FuncAndGen<?>[] FUNC_VS_GEN_VARIETY = new FuncAndGen[] {
            new FuncAndGen<>(CodecFunctions.binarySerializing(State::write, State::read), State::random),
            new FuncAndGen<>(CodecFunctions.javaSerializing(State.class), State::random),
            new FuncAndGen<>(CodecFunctions.binary(), () -> rndBytes(21321)),
            new FuncAndGen<>(CodecFunctions.binaryChunked(64, true), () -> rndBytes(64, 65)),
            new FuncAndGen<>(CodecFunctions.stringSerializing(), () -> rndString(14835, StandardCharsets.UTF_8))
    };

    public static <T> FuncAndGen<T> rndCodecFunctionAndGen() {
        //noinspection unchecked
        return (FuncAndGen<T>) rndElem(FUNC_VS_GEN_VARIETY);
    }

    public static CodecFilter rndFilterChain() {
        CodecFilter[] randomFilterChain = Stream.generate(() -> rndElem(FILTER_VARIETY))
                .limit(rndInt(10))
                .toArray(CodecFilter[]::new);
        return CodecFilter.chain(randomFilterChain);
    }

    public static <T> CodecAndGen<T> rndCodec() {
        FuncAndGen<T> funcAndGen = rndCodecFunctionAndGen();
        CodecBufferSpec rndBufferSpec = rndElem(BUFFER_SPECS_VARIETY).get();
        CodecFilter rndFilterChain = rndFilterChain();
        Codec<T> rndCodec = Codec.withFunction(funcAndGen.func)
                .withBuffer(rndBufferSpec)
                .withFilter(rndFilterChain)
                .build();
        return new CodecAndGen<>(rndCodec, funcAndGen.gen);
    }
}