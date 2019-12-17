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

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecFilterChainApplicationOrderTest {

    private CodecFilter probe(String name,
                              List<CodecFilter> filterWrite,
                              List<CodecFilter> filterRead,
                              List<CodecFilter> doWrite,
                              List<CodecFilter> doRead
    ) {
        return new CodecFilter() {
            @Override
            public OutputStream filter(OutputStream out) {
                CodecFilter f = this;
                filterWrite.add(f);
                return new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        doWrite.add(f);
                        out.write(b);
                    }
                };
            }

            @Override
            public InputStream filter(InputStream in) {
                CodecFilter f = this;
                filterRead.add(f);
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        doRead.add(f);
                        return in.read();
                    }
                };
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }


    @SuppressWarnings({"MisorderedAssertEqualsArguments", "resource"})
    @Test
    void chainTest() throws IOException {
        List<CodecFilter> filterWrite = new ArrayList<>();
        List<CodecFilter> filterRead = new ArrayList<>();
        List<CodecFilter> doWrite = new ArrayList<>();
        List<CodecFilter> doRead = new ArrayList<>();

        CodecFilter p1 = probe("p1", filterWrite, filterRead, doWrite, doRead);
        CodecFilter p2 = probe("p2", filterWrite, filterRead, doWrite, doRead);
        CodecFilter p3 = probe("p3", filterWrite, filterRead, doWrite, doRead);

        CodecFilter p1p2 = p1.chain(p2);
        CodecFilter p1p2p3 = CodecFilter.chain(p1p2, p3);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStream filterOut = p1p2p3.filter(b);
        int in = 1;
        filterOut.write(in);
        InputStream filterIn = p1p2p3.filter(new ByteArrayInputStream(b.toByteArray()));
        int out = filterIn.read();
        assertEquals(in, out);

        List<CodecFilter> expectedWrappingOrder = Arrays.asList(p3, p2, p1);
        List<CodecFilter> expectedOperationOrder = Arrays.asList(p1, p2, p3);

        assertEquals(expectedWrappingOrder, filterRead);
        assertEquals(expectedWrappingOrder, filterWrite);
        assertEquals(expectedOperationOrder, doWrite);
        assertEquals(expectedOperationOrder, doRead);
    }

}
