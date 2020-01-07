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

import com.github.sabirove.codec.filter.CodecFilter;
import com.github.sabirove.codec.function.CodecFunction;
import com.github.sabirove.codec.util.SafeInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.github.sabirove.codec.util.CodecUtil.throwUnchecked;

final class CodecImpl<T> implements Codec<T> {
    private final CodecFunction<T> function;
    private final CodecFilter filter;

    CodecImpl(CodecFunction<T> function, CodecFilter filter) {
        this.function = function;
        this.filter = filter;
    }

    @Override
    public EncoderStream<T> wrap(OutputStream os) {
        try {
            OutputStream filtered = filter.filter(os);
            return new EncoderStream<>(filtered, function);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    @Override
    public DecoderStream<T> wrap(InputStream is) {
        try {
            InputStream filtered = filter.filter(is);
            InputStream filteredSafe = SafeInputStream.wrap(filtered);
            return new DecoderStream<>(filteredSafe, function);
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }
}
