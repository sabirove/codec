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

import com.github.sabirove.codec.function.CodecFunction;

import java.util.function.Supplier;

public final class FuncAndGen<T> {
    public final CodecFunction<T> func;
    public final Supplier<T> gen;

    FuncAndGen(CodecFunction<T> func, Supplier<T> gen) {
        this.func = func;
        this.gen = gen;
    }
}
