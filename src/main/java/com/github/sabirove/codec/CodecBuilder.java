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

package com.github.sabirove.codec;

import com.github.sabirove.codec.filter.CodecBufferSpec;
import com.github.sabirove.codec.filter.CodecFilter;
import com.github.sabirove.codec.filter.CodecFilters;
import com.github.sabirove.codec.function.CodecFunction;

import static com.github.sabirove.codec.util.CodecUtil.checkNotNull;

public final class CodecBuilder<T> {
    private final CodecFunction<T> function;
    private CodecFilter filter = CodecFilters.noOp();
    private CodecBufferSpec bufferSpec = CodecBufferSpec.ofDefaultSize();

    private CodecBuilder(CodecFunction<T> function) {
        this.function = checkNotNull(function);
    }

    /**
     * Build codec instance starting with the provided {@link CodecFunction}.
     */
    public static <T> CodecBuilder<T> withFunction(CodecFunction<T> function) {
        return new CodecBuilder<>(function);
    }

    /**
     * Specify the codec filter to apply when doing IO with this codec.
     *
     * @apiNote <ul>
     * <li>do not use {@link CodecBufferSpec} as plain codec filter</li>
     * <li>use {@link #withFilterChain(CodecFilter...)} to add more than one filter. Alternatively,
     * a filter composed with {@link CodecFilter#chain(CodecFilter...)} could be provided.</li>
     * </ul>
     */
    public CodecBuilder<T> withFilter(CodecFilter filter) {
        this.filter = checkNotNull(filter);
        return this;
    }

    /**
     * Specify the codec filter chain (filters applied consequently) to apply
     * when doing IO with this codec.
     *
     * @apiNote do not use {@link CodecBufferSpec} as part of the filter chain.
     */
    public CodecBuilder<T> withFilterChain(CodecFilter... chain) {
        for (CodecFilter codecFilter : checkNotNull(chain)) {
            checkNotNull(codecFilter);
        }
        return withFilter(CodecFilter.chain(chain));
    }

    /**
     * Specify the buffering strategy to apply when doing IO with this codec.
     */
    public CodecBuilder<T> withBuffer(CodecBufferSpec bufferSpec) {
        this.bufferSpec = checkNotNull(bufferSpec);
        return this;
    }

    /**
     * Do not apply buffering when doing IO with this codec.
     */
    public CodecBuilder<T> withNoBuffer() {
        this.bufferSpec = CodecBufferSpec.noBuffer();
        return this;
    }

    public Codec<T> build() {
        CodecFilter chain = bufferSpec == CodecBufferSpec.noBuffer() ? filter : filter.chain(bufferSpec);
        return new CodecImpl<>(function, chain);
    }
}
