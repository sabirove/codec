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

import com.github.sabirove.codec.util.CodecUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Random values generator factory
 */
public enum Rnd {
    ;

    public static boolean rndBoolean() {
        return rnd().nextBoolean();
    }

    public static double rndDouble() {
        return rnd().nextDouble();
    }

    public static float rndFloat() {
        return rnd().nextFloat();
    }

    public static long rndLong() {
        return rnd().nextLong();
    }

    public static int rndInt() {
        return rnd().nextInt();
    }

    public static int rndInt(int fromInclusive, int toExclusive) {
        return rnd().nextInt(fromInclusive, toExclusive);
    }

    public static int rndInt(int toExclusive) {
        return rnd().nextInt(toExclusive);
    }

    public static byte rndByte() {
        return (byte) rnd().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1);
    }

    public static short rndShort() {
        return (short) rnd().nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);
    }

    public static char rndChar() {
        return (char) rnd().nextInt(Character.MIN_CODE_POINT, Character.MAX_CODE_POINT + 1);
    }

    public static String rndString(int bound) {
        return rndString(bound, StandardCharsets.UTF_8);
    }

    public static String rndString(int bound, Charset charset) {
        return new String(rndBytes(bound), charset);
    }

    public static boolean[] rndBooleans(int bound) {
        final boolean[] booleans = new boolean[rnd().nextInt(bound)];
        IntStream.range(0, booleans.length).forEach(i -> booleans[i] = rndBoolean());
        return booleans;
    }

    public static char[] rndChars(int bound) {
        final char[] chars = new char[rnd().nextInt(bound)];
        IntStream.range(0, chars.length).forEach(i -> chars[i] = rndChar());
        return chars;
    }

    public static double[] rndDoubles(int bound) {
        final double[] doubles = new double[rnd().nextInt(bound)];
        IntStream.range(0, doubles.length).forEach(i -> doubles[i] = rndDouble());
        return doubles;
    }

    public static float[] rndFloats(int bound) {
        final float[] floats = new float[rnd().nextInt(bound)];
        IntStream.range(0, floats.length).forEach(i -> floats[i] = rndFloat());
        return floats;
    }

    public static long[] rndLongs(int bound) {
        final long[] longs = new long[rnd().nextInt(bound)];
        IntStream.range(0, longs.length).forEach(i -> longs[i] = rndLong());
        return longs;
    }

    public static int[] rndInts(int bound) {
        final int[] ints = new int[rnd().nextInt(bound)];
        IntStream.range(0, ints.length).forEach(i -> ints[i] = rndInt());
        return ints;
    }

    public static short[] rndShorts(int bound) {
        final short[] shorts = new short[rnd().nextInt(bound)];
        IntStream.range(0, shorts.length).forEach(i -> shorts[i] = rndShort());
        return shorts;
    }

    public static BigInteger rndBigInteger() {
        return new BigInteger(rndBytes(1, 256));
    }

    public static LocalDate rndLocalDate() {
        return LocalDate.of(
                rnd().nextInt(Year.MIN_VALUE, Year.MAX_VALUE),
                rnd().nextInt(1, 12),
                rnd().nextInt(1, 28)
        );
    }

    public static LocalTime rndLocalTime() {
        return LocalTime.of(
                rnd().nextInt(0, 23),
                rnd().nextInt(0, 59),
                rnd().nextInt(0, 59),
                rnd().nextInt(0, 999_999_999)
        );
    }

    public static ZoneOffset rndZoneOffset() {
        return ZoneOffset.ofTotalSeconds(rnd().nextInt(-64800, 64800));
    }

    public static byte[] rndBytes(int bound) {
        return rndBytes(0, bound);
    }

    public static byte[] rndBytes(int from, int to) {
        final byte[] bytes = new byte[rnd().nextInt(from, to)];
        rnd().nextBytes(bytes);
        return bytes;
    }

    public static <E extends Enum<E>> E rndEnum(Class<E> type) {
        E[] enums = type.getEnumConstants();
        return enums[rnd().nextInt(0, enums.length)];
    }

    public static <T> List<T> rndList(int bound, Supplier<T> generator) {
        return Stream.generate(generator)
                .limit(rnd().nextInt(bound))
                .collect(Collectors.toList());
    }

    public static <T> T[] rndArray(int bound, Supplier<T> generator, IntFunction<T[]> constructor) {
        return Stream.generate(generator)
                .limit(rnd().nextInt(bound))
                .toArray(constructor);
    }

    public static <K, V> Map<K, V> rndMap(int bound, Supplier<K> kSupplier, Supplier<V> vSupplier) {
        return Stream.generate(() -> null)
                .limit(bound)
                .collect(Collectors.toMap(none -> kSupplier.get(), none -> vSupplier.get(), (v, v2) -> v));
    }

    public static BigDecimal rndBigDecimal() {
        return new BigDecimal(rndBigInteger(), rndInt());
    }

    public static UUID rndUUID() {
        return UUID.randomUUID();
    }

    public static Date rndDate() {
        return new Date(rnd().nextLong(0, Long.MAX_VALUE));
    }

    public static Instant rndInstant() {
        return Instant.ofEpochSecond(rnd().nextLong(0, 999_999_999));
    }

    public static ZonedDateTime rndZoneDateTime() {
        return ZonedDateTime.of(rndLocalDate(), rndLocalTime(), rndZoneOffset());
    }

    public static LocalDateTime rndLocalDateTime() {
        return LocalDateTime.of(rndLocalDate(), rndLocalTime());
    }

    public static ThreadLocalRandom rnd() {
        return ThreadLocalRandom.current();
    }

    public static <T> T rndElem(T[] array) {
        CodecUtil.checkArgument(array.length > 0, "empty array");
        return array[rndInt(array.length)];
    }
}
