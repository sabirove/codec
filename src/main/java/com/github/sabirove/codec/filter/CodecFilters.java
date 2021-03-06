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

import com.github.sabirove.codec.util.base64.Base64InputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
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
            //Base64.getDecoder()::wrap <- bugged: https://bugs.openjdk.java.net/browse/JDK-8222187
            Base64InputStream::new
    );
    private static final CodecFilter BASE64_URL = CodecFilter.of(
            Base64.getUrlEncoder().withoutPadding()::wrap,
            //Base64.getUrlDecoder()::wrap <- bugged: https://bugs.openjdk.java.net/browse/JDK-8222187
            Base64InputStream::new
    );
    private static final CodecFilter BASE64_MIME = CodecFilter.of(
            Base64.getMimeEncoder().withoutPadding()::wrap,
            //Base64.getMimeDecoder()::wrap <- bugged: https://bugs.openjdk.java.net/browse/JDK-8222187
            Base64InputStream::new
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
     * <p>Encryption filter based on a solid AES encryption method with data integrity validation and cryptographically
     * strong random number generators.</p>
     * <br>
     * <p>Specs:</p>
     * <ul>
     * <li>Galois/Counter transformation mode (AES/GCM/NoPadding): streaming AES with data integrity validation</li>
     * <li>128 bit secret key: optimal key length</li>
     * <li>96 bit initialization vector (salt): optimal length, random value is generated per each encryption</li>
     * <li>128 bit authentication tag</li>
     * </ul>
     *
     * Limitations: <br>
     * When using this filter authentication tag is computed on the whole stream and is flushed at the very end
     * when the underlying {@link CipherOutputStream} is closed.
     * Thus in order to validate the tag the whole encoded payload should be fully read back (decoded)
     * and then the input stream should be closed triggering the underlying {@link CipherInputStream} to read
     * and validate the tag from whatever leftover bytes in the stream.
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
