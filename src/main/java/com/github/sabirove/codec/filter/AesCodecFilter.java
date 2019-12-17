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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.sabirove.codec.util.CodecUtil;


import static com.github.sabirove.codec.util.CodecUtil.checkArgument;

/*
 * - Galois/Counter transformation mode (AES/GCM/NoPadding) - streaming AES with data integrity validation
 * - 128 bit secret key - optimal/recommended key length. We either use random keys per instance or configure the custom one.
 * - 96 bit initialization vector (recommended/optimal) - random value is generated per each encryption
 * - 128 bit authentication tag
 */
final class AesCodecFilter extends CodecFilter {
    private static final String TRANSFORMATION_MODE = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 16; //128 bit key length (recommended/optimal)
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128; //128 bit authentication tag
    private static final int INITIALIZATION_VECTOR_LENGTH_BYTES = 12; //96 bit IV (recommended/optimal)
    private final SecureRandom random = new SecureRandom();
    private final SecretKey key;

    private AesCodecFilter(SecretKey key) {
        this.key = key;
    }

    static AesCodecFilter of() {
        byte[] key = new byte[KEY_LENGTH_BYTES];
        new SecureRandom().nextBytes(key);
        return of(key);
    }

    static AesCodecFilter of(byte[] key) {
        checkArgument(key.length == KEY_LENGTH_BYTES, "invalid key length");
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return new AesCodecFilter(secretKey);
    }

    @Override
    public OutputStream filter(OutputStream out) {
        try {
            byte[] iv = new byte[INITIALIZATION_VECTOR_LENGTH_BYTES];
            random.nextBytes(iv);
            Cipher enc = Cipher.getInstance(TRANSFORMATION_MODE);
            GCMParameterSpec spec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv);
            enc.init(Cipher.ENCRYPT_MODE, key, spec);
            out.write(iv);
            return new CipherOutputStream(out, enc);
        } catch (Exception e) {
            throw new IllegalStateException("failed encrypt", e);
        }
    }

    @Override
    public InputStream filter(InputStream in) {
        try {
            byte[] iv = new byte[INITIALIZATION_VECTOR_LENGTH_BYTES];
            CodecUtil.checkState(in.read(iv) == INITIALIZATION_VECTOR_LENGTH_BYTES, "malformed input data");
            Cipher dec = Cipher.getInstance(TRANSFORMATION_MODE);
            GCMParameterSpec spec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv);
            dec.init(Cipher.DECRYPT_MODE, key, spec);
            return new CipherInputStream(in, dec);
        } catch (Exception e) {
            throw new IllegalStateException("failed decrypt", e);
        }
    }
}
