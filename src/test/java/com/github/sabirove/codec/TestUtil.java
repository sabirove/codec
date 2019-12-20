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

import java.io.IOException;
import java.io.UncheckedIOException;


import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestUtil {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    /**
     * assert throws {@code causeType} either wrapped to {@link UncheckedIOException} or as is
     */
    public static void assertThrowsIO(Class<? extends IOException> causeType, ThrowingRunnable op) {
        try {
            op.run();
        } catch (Throwable e) {
            Throwable target = e instanceof UncheckedIOException ? e.getCause() : e;
            assertTrue(target instanceof IOException, "IOException was expected, got e=" + e);
            assertSame(causeType, target.getClass());
        }
    }
}
