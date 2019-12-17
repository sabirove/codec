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

package com.github.sabirove.codec.function;

import java.io.*;
import java.nio.charset.Charset;

final class StringCodecFunction extends CodecFunction<String> {
    private final Charset charset;

    StringCodecFunction(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void write(String value, OutputStream out) throws IOException {
        byte[] bytes = value.getBytes(charset);
        CodecFunctions.binary().write(bytes, out);
    }

    @Override
    public String read(InputStream in) throws IOException {
        byte[] bytes = CodecFunctions.binary().read(in);
        return new String(bytes, charset);
    }
}