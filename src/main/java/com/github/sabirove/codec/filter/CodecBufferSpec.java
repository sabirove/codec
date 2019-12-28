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


import com.github.sabirove.codec.Codec;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sabirove.codec.util.CodecUtil.checkArgument;
import static com.github.sabirove.codec.util.CodecUtil.checkState;

/**
 * Specialized {@link CodecFilter} implementation used by a {@link Codec}
 * instance as the strategy to apply buffering on IO streams.
 *
 * @apiNote intended to be used only as part of the {@link Codec.Builder} API.
 */
public final class CodecBufferSpec extends CodecFilter {
    private static final Set<Class<? extends OutputStream>> DEFAULT_EXCLUDED_OUTPUT_TYPES = Stream.of(
            BufferedOutputStream.class,
            ByteArrayOutputStream.class
    ).collect(Collectors.toSet());

    private static final Set<Class<? extends InputStream>> DEFAULT_EXCLUDED_INPUT_TYPES = Stream.of(
            BufferedInputStream.class,
            ByteArrayInputStream.class
    ).collect(Collectors.toSet());

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final CodecBufferSpec DEFAULT = new CodecBufferSpec(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
    private static final CodecBufferSpec EMPTY = new CodecBufferSpec(0, 0);

    private final int inBufferSize;
    private final int outBufferSize;
    private final Set<Class<? extends OutputStream>> outExclusions;
    private final Set<Class<? extends InputStream>> inExclusions;

    private CodecBufferSpec(int inBufferSize, int outBufferSize) {
        this(inBufferSize, outBufferSize, DEFAULT_EXCLUDED_OUTPUT_TYPES, DEFAULT_EXCLUDED_INPUT_TYPES);
    }

    private CodecBufferSpec(int inBufferSize,
                            int outBufferSize,
                            Set<Class<? extends OutputStream>> outExclusions,
                            Set<Class<? extends InputStream>> inExclusions) {
        this.inBufferSize = inBufferSize;
        this.outBufferSize = outBufferSize;
        this.outExclusions = outExclusions;
        this.inExclusions = inExclusions;
    }

    @Override
    public OutputStream filter(OutputStream out) {
        return outBufferSize == 0 || outExclusions.contains(out.getClass())
                ? out
                : new BufferedOutputStream(out, outBufferSize);
    }

    @Override
    public InputStream filter(InputStream in) {
        return inBufferSize == 0 || inExclusions.contains(in.getClass())
                ? in
                : new BufferedInputStream(in, inBufferSize);
    }

    //FACTORY

    /**
     * Buffering spec with specified buffer sizes.
     *
     * @param inputBufferSize  input buffer size, in bytes
     * @param outputBufferSize output buffer size, in bytes
     */
    public static CodecBufferSpec ofSize(int inputBufferSize, int outputBufferSize) {
        checkArgument(inputBufferSize >= 0 && outputBufferSize >= 0,
                "buffer sizes should be >= 0 ('0' means buffering is disabled)");
        return inputBufferSize == 0 && outputBufferSize == 0
                ? EMPTY
                : new CodecBufferSpec(inputBufferSize, outputBufferSize);
    }

    /**
     * Buffering spec with default buffer sizes of {@link CodecBufferSpec#DEFAULT_BUFFER_SIZE}.
     */
    public static CodecBufferSpec ofDefaultSize() {
        return DEFAULT;
    }

    /**
     * Buffering spec implying no buffering should be applied.
     */
    public static CodecBufferSpec noBuffer() {
        return EMPTY;
    }

    /**
     * Get the copy of this instance configured with the specified {@link OutputStream} types to be excluded from buffering.
     */
    public CodecBufferSpec withOutputStreamExclusions(Collection<Class<? extends OutputStream>> outExclusions) {
        checkState(outBufferSize > 0,
                "output buffering is disabled, no point to specify exclusions");
        return new CodecBufferSpec(inBufferSize, outBufferSize, new HashSet<>(outExclusions), inExclusions);
    }

    /**
     * Get the copy of this instance configured with the specified {@link InputStream} types to be excluded from buffering.
     */
    public CodecBufferSpec withInputStreamExclusions(Collection<Class<? extends InputStream>> inExclusions) {
        checkState(inBufferSize > 0,
                "input buffering is disabled, no point to specify exclusions");
        return new CodecBufferSpec(inBufferSize, outBufferSize, outExclusions, new HashSet<>(inExclusions));
    }

    /**
     * Get the copy of this instance configured with the specified {@link OutputStream} types added to the set
     * of types to be excluded from buffering.
     */
    public CodecBufferSpec addOutputStreamExclusions(Collection<Class<? extends OutputStream>> outExclusions) {
        checkState(outBufferSize > 0,
                "output buffering is disabled, no point to specify exclusions");
        Set<Class<? extends OutputStream>> all = new HashSet<>(this.outExclusions.size() + outExclusions.size());
        all.addAll(this.outExclusions);
        all.addAll(outExclusions);
        return new CodecBufferSpec(inBufferSize, outBufferSize, all, inExclusions);
    }

    /**
     * Get the copy of this instance configured with the specified {@link InputStream} types added to the set
     * of types to be excluded from buffering.
     */
    public CodecBufferSpec addInputStreamExclusions(Collection<Class<? extends InputStream>> inExclusions) {
        checkState(inBufferSize > 0,
                "input buffering is disabled, no point to specify exclusions");
        Set<Class<? extends InputStream>> all = new HashSet<>(this.inExclusions.size() + inExclusions.size());
        all.addAll(this.inExclusions);
        all.addAll(inExclusions);
        return new CodecBufferSpec(inBufferSize, outBufferSize, outExclusions, all);
    }
}
