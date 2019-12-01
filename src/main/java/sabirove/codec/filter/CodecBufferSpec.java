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

package sabirove.codec.filter;


import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sabirove.codec.Codec;


import static sabirove.codec.util.CodecUtil.checkArgument;
import static sabirove.codec.util.CodecUtil.checkState;

/**
 * Specialized {@link CodecFilter} implementation that is used by the {@link Codec}
 * instance as the strategy to handle buffering on IO streams.
 *
 * @apiNote do not use when building the codec filter chains: this filter should only be used
 * to specify the buffering strategy within the {@link Codec.Builder} API.
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
    private static final CodecBufferSpec DEFAULT_BUFFER_SPEC =
            new CodecBufferSpec(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE);

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
    public static CodecBufferSpec withBuffers(int inputBufferSize, int outputBufferSize) {
        checkArgument(inputBufferSize >= 0 && outputBufferSize >= 0,
                "buffer size should be >= 0 ('0' means buffering is disabled)");
        return new CodecBufferSpec(inputBufferSize, outputBufferSize);
    }

    /**
     * Buffering spec with default input/output buffer sizes equal to {@link CodecBufferSpec#DEFAULT_BUFFER_SIZE}.
     */
    public static CodecBufferSpec withDefaultSize() {
        return DEFAULT_BUFFER_SPEC;
    }

    /**
     * Get this instance configured with the specified {@link OutputStream} types to be excluded from buffering.
     */
    public CodecBufferSpec withOutputStreamExclusions(Set<Class<? extends OutputStream>> outExclusions) {
        checkState(outBufferSize > 0,
                "output buffering is disabled, no point to specify exclusions");
        return new CodecBufferSpec(inBufferSize, outBufferSize, outExclusions, inExclusions);
    }

    /**
     * Get this instance configured with the specified {@link InputStream} types to be excluded from buffering.
     */
    public CodecBufferSpec withInputStreamExclusions(Set<Class<? extends InputStream>> inExclusions) {
        checkState(inBufferSize > 0,
                "input buffering is disabled, no point to specify exclusions");
        return new CodecBufferSpec(inBufferSize, outBufferSize, outExclusions, inExclusions);
    }
}
