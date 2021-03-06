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

import java.io.ByteArrayOutputStream;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestOutputStream extends ByteArrayOutputStream {
    private volatile boolean flushed;
    private volatile boolean closed;

    public TestOutputStream() { }

    public TestOutputStream(int size) {
        super(size);
    }

    public int getCount() {
        return count;
    }

    public void assertFlushed() {
        assertTrue(flushed, "TestOutputStream should be flushed");
    }

    public void assertNotFlushed() {
        assertFalse(flushed, "TestOutputStream should not be flushed");
    }

    public void assertClosed() {
        assertTrue(closed, "TestOutputStream should be closed");
    }

    public void assertNotClosed() {
        assertFalse(closed, "TestOutputStream should not be closed");
    }

    @Override
    public synchronized void flush() {
        assertNotClosed();
        flushed = true;
    }

    @Override
    public void close() {
        closed = true;
    }
}
