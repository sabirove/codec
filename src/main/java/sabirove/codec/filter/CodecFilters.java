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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.*;

/**
 * Standard {@link CodecFilters} implementations.
 *
 * @see CodecFilter
 */
@SuppressWarnings("resource")
public final class CodecFilters {
    private static final CodecFilter NO_OP = CodecFilter.of(
            o -> o,
            i -> i
    );
    private static final CodecFilter BASE64 = CodecFilter.of(
            Base64.getEncoder().withoutPadding()::wrap,
            Base64.getDecoder()::wrap
    );
    private static final CodecFilter BASE64_URL = CodecFilter.of(
            Base64.getUrlEncoder().withoutPadding()::wrap,
            Base64.getUrlDecoder()::wrap
    );
    private static final CodecFilter BASE64_MIME = CodecFilter.of(
            Base64.getMimeEncoder().withoutPadding()::wrap,
            Base64.getMimeDecoder()::wrap
    );
    private static final CodecFilter COMPRESS_DEFLATE = CodecFilter.of(
            os -> new DeflaterOutputStream(os, true),
            InflaterInputStream::new
    );
    private static final CodecFilter COMPRESS_GZIP = CodecFilter.of(
            os -> new GZIPOutputStream(os, true),
            GZIPInputStream::new
    );

    private CodecFilters() { }

    /**
     * No-op codec filter implementation which doesn't apply filtering on top of the underlying source.
     */
    public static CodecFilter noOp() {
        return NO_OP;
    }

    /**
     * Compression codec filter applying 'Deflate' compression with default parameters.
     *
     * @implNote remember that decompressing stream implementations (like {@link InflaterInputStream})
     * contain checksum bytes and require special care to be properly read: reading explicitly until {@code -1} byte
     * is hit is generally required to fully read such streams when used without any wrappers
     * (like {@link BufferedInputStream}), otherwise calls like {@link InputStream#read(byte[])} might
     * not read the stream fully (see: https://bugs.openjdk.java.net/browse/JDK-4529325).
     */
    public static CodecFilter compressWithDeflate() {
        return COMPRESS_DEFLATE;
    }

    /**
     * Compression codec filter applying 'GZIP' compression with default parameters.
     *
     * @implNote remember that decompressing stream implementations (like {@link InflaterInputStream})
     * contain checksum bytes and require special care to be properly read: reading explicitly until {@code -1} byte
     * is hit is generally required to fully read such streams when used without any wrappers
     * (like {@link BufferedInputStream}), otherwise calls like {@link InputStream#read(byte[])} might
     * not read the stream fully (see: https://bugs.openjdk.java.net/browse/JDK-4529325).
     */
    public static CodecFilter compressWithGzip() {
        return COMPRESS_GZIP;
    }

    /**
     * Standard Base64 codec filter
     */
    public static CodecFilter encodeWithBase64() {
        return BASE64;
    }

    /**
     * URL-compliant Base64 codec filter.
     */
    public static CodecFilter encodeWithBase64Url() {
        return BASE64_URL;
    }

    /**
     * MIME-compliant Base64 codec.
     */
    public static CodecFilter encodeWithBase64Mime() {
        return BASE64_MIME;
    }

    /**
     * AES-based encryption codec filter with the following specs: <br>
     * - Galois/Counter transformation mode (AES/GCM/NoPadding) <br>
     * - 128 bit secret key <br>
     * - 96 bit initialization vector <br>
     * - 128 bit authentication tag <br>
     *
     * @param key 128 bit encryption key (array must be 16 bytes long)
     */
    public static CodecFilter encryptWithAes(byte[] key) {
        return AesCodecFilter.of(key);
    }

    /**
     * Same as {@link #encryptWithAes(byte[])} but with random secret key generated with
     * cryptographically strong random number generator.
     */
    public static CodecFilter encryptWithAes() {
        return AesCodecFilter.of();
    }
}
