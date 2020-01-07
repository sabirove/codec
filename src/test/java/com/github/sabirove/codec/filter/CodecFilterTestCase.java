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

import com.github.sabirove.codec.test_util.Rnd;
import com.github.sabirove.codec.test_util.TestInputStream;
import com.github.sabirove.codec.test_util.TestOutputStream;
import com.github.sabirove.codec.util.SafeInputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressFBWarnings("OS_OPEN_STREAM")
abstract class CodecFilterTestCase {

    protected abstract CodecFilter getFilter();

    protected byte[] getInputBytes() {
        return Rnd.rndBytes(9000);
    }

    protected void testEncoded(byte[] input, byte[] encoded) { }

    @SuppressWarnings("resource")
    @RepeatedTest(100)
    final void testFilter() throws IOException {
        CodecFilter filter = getFilter();
        byte[] input = getInputBytes();

        TestOutputStream tos = new TestOutputStream();
        OutputStream filteredTos = filter.filter(tos);
        tos.assertNotFlushed();
        tos.assertNotClosed();

        filteredTos.write(input);
        filteredTos.flush();
        tos.assertFlushed();
        filteredTos.close();
        tos.assertClosed();

        byte[] encoded = tos.toByteArray();
        testEncoded(input, encoded);

        TestInputStream tis = new TestInputStream(encoded);
        InputStream filteredTis = filter.filter(tis);
        tis.assertNotClosed();

        byte[] decoded = new byte[input.length];

        //wrap to StrictInputStream, b.c. there're some "framed" InputStreams used (like InflateInputStream)
        //that will may fail to read all data at once with plain read(byte[]) call
        int read = new SafeInputStream(filteredTis).read(decoded);
        assertEquals(input.length, read);
        assertArrayEquals(input, decoded);

        filteredTis.close();
        tis.assertClosed();
    }
}
