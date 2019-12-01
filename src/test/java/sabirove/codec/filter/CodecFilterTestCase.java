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

import java.io.*;
import java.security.SecureRandom;

import org.junit.jupiter.api.RepeatedTest;
import sabirove.codec.*;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;

abstract class CodecFilterTestCase {
    private final SecureRandom rnd = new SecureRandom();

    protected abstract CodecFilter getFilter();

    protected void testEncoded(byte[] input, byte[] encoded) { }

    protected byte[] getInputBytes() {
        byte[] input = new byte[Rnd.rndInt(8192)];
        rnd.nextBytes(input);
        return input;
    }

    @RepeatedTest(100)
    final void testFilter() throws IOException {
        CodecFilter filter = getFilter();
        byte[] input = getInputBytes();

        TestOutputStream tos = new TestOutputStream();
        OutputStream fOut = filter.filter(tos);
        tos.assertNotFlushed();
        tos.assertNotClosed();

        fOut.write(input);
        //fOut.flush();
        //tos.assertFlushed();
        fOut.close();
        tos.assertClosed();

        byte[] encoded = tos.toByteArray();
        testEncoded(input, encoded);

        TestInputStream tis = new TestInputStream(encoded);
        InputStream fIn = filter.filter(tis);
        tis.assertNotClosed();

        byte[] decoded = new byte[input.length];

        //we explicitly read like that, b.c. there's InflateInputStream
        //that will fail to read all data otherwise, e.g. with plain fIn.read(byte[])
        int b;
        int i = 0;
        while ((b = fIn.read()) != -1) {
            decoded[i++] = (byte) b;
        }

        assertArrayEquals(input, decoded);

        fIn.close();
        tis.assertClosed();
    }
}
