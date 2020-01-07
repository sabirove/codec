/*
 * Copyright 2020 Sabirov Evgenii (sabirov.e@gmail.com)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED", justification = "assertThrows")
class CodecBufferSpecTest {

    @Test
    void filterIn() {
        CodecBufferSpec cbs = CodecBufferSpec.ofDefaultSize().withInputStreamExclusions();

        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(bais);

        assertTrue(cbs.filter(bais) instanceof BufferedInputStream);
        assertTrue(cbs.filter(inflaterInputStream) instanceof BufferedInputStream);

        CodecBufferSpec withExclusion = cbs.withInputStreamExclusions(InflaterInputStream.class);
        assertNotEquals(cbs, withExclusion);
        assertEquals(inflaterInputStream, withExclusion.filter(inflaterInputStream));

        CodecBufferSpec withInputBufferingDisabled = CodecBufferSpec.ofSize(0, 8192);
        assertEquals(inflaterInputStream, withInputBufferingDisabled.filter(inflaterInputStream));
    }

    @Test
    void filterOut() {
        CodecBufferSpec cbs = CodecBufferSpec.ofDefaultSize().withOutputStreamExclusions();

        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(bais);

        assertTrue(cbs.filter(bais) instanceof BufferedOutputStream);
        assertTrue(cbs.filter(inflaterOutputStream) instanceof BufferedOutputStream);

        CodecBufferSpec withExclusion = cbs.withOutputStreamExclusions(InflaterOutputStream.class);
        assertNotEquals(cbs, withExclusion);
        assertEquals(inflaterOutputStream, withExclusion.filter(inflaterOutputStream));

        CodecBufferSpec withOutputBufferingDisabled = CodecBufferSpec.ofSize(8192, 0);
        assertEquals(inflaterOutputStream, withOutputBufferingDisabled.filter(inflaterOutputStream));
    }

    @Test
    void noBuffer() {
        CodecBufferSpec cbs = CodecBufferSpec.noBuffer();
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        assertEquals(bais, cbs.filter(bais));
        InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(bais);
        assertEquals(inflaterOutputStream, cbs.filter(inflaterOutputStream));
        assertThrows(IllegalStateException.class, () -> cbs.withInputStreamExclusions(BufferedInputStream.class));
        assertThrows(IllegalStateException.class, () -> cbs.withOutputStreamExclusions(BufferedOutputStream.class));
    }
}
