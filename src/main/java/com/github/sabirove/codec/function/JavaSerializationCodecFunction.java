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


@SuppressWarnings("resource")
final class JavaSerializationCodecFunction<T extends Serializable> extends CodecFunction<T> {
    private final Class<T> type;

    JavaSerializationCodecFunction(Class<T> type) {
        this.type = type;
    }

    @Override
    public void write(T value, OutputStream out) throws IOException {
        new ObjectOutputStream(out).writeObject(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(InputStream in) throws IOException {
        try {
            return (T) new ObjectInputStream(in).readObject();
        } catch (ClassCastException cce) {
            throw new IllegalStateException("java deserialization failed: invalid target type! expected=" + type, cce);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("java deserialization failed", e);
        }
    }

}
