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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.github.sabirove.codec.util.StateInputStream;
import com.github.sabirove.codec.util.StateOutputStream;

final class BinarySerializationCodecFunction<T> extends CodecFunction<T> {
    private final BiConsumer<StateOutputStream, T> writer;
    private final Function<StateInputStream, T> reader;

    BinarySerializationCodecFunction(BiConsumer<StateOutputStream, T> writer, Function<StateInputStream, T> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    public void write(T value, OutputStream out) {
        writer.accept(new StateOutputStream(out), value);
    }

    @Override
    public T read(InputStream in) {
        T result = reader.apply(new StateInputStream(in));
        if (result == null) {
            throw new NullPointerException("object read from the buffer can't be null");
        }
        return result;
    }

}
