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

import com.github.sabirove.codec.filter.CodecFilter;
import com.github.sabirove.codec.filter.CodecFilters;
import com.github.sabirove.codec.function.CodecFunctions;
import com.github.sabirove.codec.util.base64.Base64InputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

import java.io.InputStream;
import java.util.Base64;

import static com.github.sabirove.codec.test_util.Rnd.rndBytes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * JDK-8222187 bug showcase: https://bugs.openjdk.java.net/browse/JDK-8222187
 * {@link Base64.DecInputStream} against reference commons-codec:commons-codec:1.13 Base64InputStream implementation.
 */
@SuppressWarnings({"IgnoredJUnitTest", "NewClassNamingConvention", "resource"})
@Disabled("JDK-8222187 bug showcase")
class _java_util_Base64_DecInputStream__BugTest {

    private static final Codec<byte[]> B64_CODEC_JAVA = assembleCodec(Base64.getDecoder()::wrap);
    private static final Codec<byte[]> B64_CODEC_APACHE = assembleCodec(Base64InputStream::new);

    @RepeatedTest(10000)
    void testB64CodecJava() {
        test(B64_CODEC_JAVA);
    }

    @RepeatedTest(10000)
    void testB64CodecApache() {
        test(B64_CODEC_APACHE);
    }

    private static void test(Codec<byte[]> codec) {
        byte[] bs = rndBytes(14321);
        byte[] encoded = codec.encode(bs);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals(bs, decoded);
    }

    private static Codec<byte[]> assembleCodec(CodecFilter.Wrapper<InputStream> base64Decoder) {
        final CodecFilter base64Filter = CodecFilter.of(
                Base64.getEncoder()::wrap,
                base64Decoder
        );
        return CodecBuilder.withFunction(CodecFunctions.binary())
                .withNoBuffer()
                .withFilterChain(
                        CodecFilters.encryptWithAes(), //trips with javax.crypto.AEADBadTagException if different array is read
                        base64Filter
                ).build();
    }
}
