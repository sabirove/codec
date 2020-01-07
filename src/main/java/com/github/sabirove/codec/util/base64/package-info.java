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

/**
 * <p>This package contains classes adapted from the Apache commons-codec:commons-codec:1.13 library
 * to get working org.apache.commons.codec.binary.Base64InputStream implementation.
 *
 * <p>This is required until JDK-8222187 bug is fixed: https://bugs.openjdk.java.net/browse/JDK-8222187:
 * {@link java.util.Base64.Decoder#wrap(java.io.InputStream)} stream adds unexpected null bytes at the end
 * upon decoding. This bug trips the {@link javax.crypto.CipherInputStream} when it tries to read and validate
 * authentication tag at the end of the stream.
 * <br><br>
 * Modifications:
 * <ul>
 *  <li>removed interfaces/exception types</li>
 *  <li>replaced Charsets/Encoding related stuff (constants) with standard Java 1.7 spec constants</li>
 *  <li>removed some deprecated stuff</li>
 *  <li>all final, all package private except for Base64InputStream</li>
 * </ul>
 */
package com.github.sabirove.codec.util.base64;
