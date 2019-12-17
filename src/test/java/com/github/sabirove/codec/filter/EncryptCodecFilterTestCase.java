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

package sabirove.codec.filter;

import org.junit.jupiter.api.Assertions;
import sabirove.codec.Rnd;


import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class EncryptCodecFilterTestCase extends CodecFilterTestCase {

    @Override
    protected final byte[] getInputBytes() {
        byte[] bytes = new byte[Rnd.rndInt(4096)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Rnd.rndInt('a', 'z');
        }
        return bytes;
    }

    @Override
    protected void testEncoded(byte[] input, byte[] encoded) {
        assertTrue(encoded.length > input.length);

        for (byte b : encoded) {
            if (b < 'a' || b > 'z') return;
        }

        Assertions.fail("no encrypted chars found");
    }
}