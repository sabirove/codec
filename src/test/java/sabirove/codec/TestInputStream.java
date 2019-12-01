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

package sabirove.codec;

import java.io.ByteArrayInputStream;


import static org.junit.jupiter.api.Assertions.*;

public final class TestInputStream extends ByteArrayInputStream {
    private volatile boolean closed;

    public static TestInputStream from(TestOutputStream tos) {
        return new TestInputStream(tos.toByteArray());
    }

    public TestInputStream(byte[] buf) {
        super(buf);
    }

    public void assertClosed() {
        assertTrue(closed, "TestInputStream should be closed");
    }

    public void assertNotClosed() {
        assertFalse(closed, "TestInputStream should not be closed");
    }

    public void assertEOF() {
        assertEquals(-1, read(), "TestInputStream should be at EOF");
    }

    public void assertNotEOF() {
        assertTrue(available() > 0, "TestInputStream should not be at EOF");
    }

    public void assertRemaining(int count) {
        assertEquals(available(), count, "TestInputStream should have " + count + " bytes available.");
    }

    @Override
    public void close() {
        closed = true;
    }
}
