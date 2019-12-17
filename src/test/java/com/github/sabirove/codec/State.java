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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.sabirove.codec.util.StateInputStream;
import com.github.sabirove.codec.util.StateOutputStream;

/**
 * arbitrary State object with all kinds of field types
 */
public final class State implements Serializable {
    final String stringValue;
    final TimeUnit enumValue;
    final byte byteValue;
    final short shortValue;
    final int intValue;
    final long longValue;
    final float floatValue;
    final double doubleValue;
    final char charValue;
    final byte[] bytes;
    final boolean booleanValue;
    final short[] shorts;
    final int[] ints;
    final long[] longs;
    final float[] floats;
    final double[] doubles;
    final char[] chars;
    final boolean[] booleans;
    final LocalTime localTime;
    final LocalDate localDate;
    final LocalDateTime localDateTime;
    final ZonedDateTime zonedDateTime;
    final ZoneOffset zoneOffset;
    final Instant instant;
    final Date date;
    final UUID uuid;
    final BigInteger bigInteger;
    final BigDecimal bigDecimal;
    final Collection<String> collection;
    final Integer[] array;
    final Map<String, Long> map;

    private State(String stringValue,
          TimeUnit enumValue,
          byte byteValue,
          short shortValue,
          int intValue,
          long longValue,
          float floatValue,
          double doubleValue,
          char charValue,
          boolean booleanValue,
          byte[] bytes,
          short[] shorts,
          int[] ints,
          long[] longs,
          float[] floats,
          double[] doubles,
          char[] chars,
          boolean[] booleans,
          LocalTime localTime,
          LocalDate localDate,
          LocalDateTime localDateTime,
          ZonedDateTime zonedDateTime,
          ZoneOffset zoneOffset, Instant instant,
          Date date,
          UUID uuid,
          BigInteger bigInteger,
          BigDecimal bigDecimal,
          Collection<String> collection,
          Integer[] array,
          Map<String, Long> map) {
        this.stringValue = stringValue;
        this.enumValue = enumValue;
        this.byteValue = byteValue;
        this.shortValue = shortValue;
        this.intValue = intValue;
        this.longValue = longValue;
        this.floatValue = floatValue;
        this.doubleValue = doubleValue;
        this.charValue = charValue;
        this.booleanValue = booleanValue;
        this.bytes = bytes;
        this.shorts = shorts;
        this.ints = ints;
        this.longs = longs;
        this.floats = floats;
        this.doubles = doubles;
        this.chars = chars;
        this.booleans = booleans;
        this.zoneOffset = zoneOffset;
        this.instant = instant;
        this.localTime = localTime;
        this.localDate = localDate;
        this.localDateTime = localDateTime;
        this.zonedDateTime = zonedDateTime;
        this.date = date;
        this.uuid = uuid;
        this.bigDecimal = bigDecimal;
        this.bigInteger = bigInteger;
        this.collection = collection;
        this.array = array;
        this.map = map;
    }

    public static State random() {
        int arraysBound = 128;
        int bytesBound = 1024;
        return new State(
                Rnd.rndString(bytesBound),
                Rnd.rndEnum(TimeUnit.class),
                Rnd.rndByte(),
                Rnd.rndShort(),
                Rnd.rndInt(),
                Rnd.rndLong(),
                Rnd.rndFloat(),
                Rnd.rndDouble(),
                Rnd.rndChar(),
                Rnd.rndBoolean(),
                Rnd.rndBytes(bytesBound),
                Rnd.rndShorts(arraysBound),
                Rnd.rndInts(arraysBound),
                Rnd.rndLongs(arraysBound),
                Rnd.rndFloats(arraysBound),
                Rnd.rndDoubles(arraysBound),
                Rnd.rndChars(arraysBound),
                Rnd.rndBooleans(arraysBound),
                Rnd.rndLocalTime(),
                Rnd.rndLocalDate(),
                Rnd.rndLocalDateTime(),
                Rnd.rndZoneDateTime(),
                Rnd.rndZoneOffset(),
                Rnd.rndInstant(),
                Rnd.rndDate(),
                Rnd.rndUUID(),
                Rnd.rndBigInteger(),
                Rnd.rndBigDecimal(),
                Rnd.rndList(20, () -> Rnd.rndUUID().toString()),
                Rnd.rndArray(30, Rnd::rndInt, Integer[]::new),
                Rnd.rndMap(15, () -> Rnd.rndUUID().toString(), Rnd::rndLong));
    }

    //IO

    public static void write(StateOutputStream buffer, State s) {
        buffer.putString(s.stringValue)
                .putEnum(s.enumValue)
                .putByte(s.byteValue)
                .putShort(s.shortValue)
                .putInt(s.intValue)
                .putLong(s.longValue)
                .putFloat(s.floatValue)
                .putDouble(s.doubleValue)
                .putChar(s.charValue)
                .putBoolean(s.booleanValue)
                .putBytes(s.bytes)
                .putShorts(s.shorts)
                .putInts(s.ints)
                .putLongs(s.longs)
                .putFloats(s.floats)
                .putDoubles(s.doubles)
                .putChars(s.chars)
                .putBooleans(s.booleans)
                .putLocalTime(s.localTime)
                .putLocalDate(s.localDate)
                .putLocalDateTime(s.localDateTime)
                .putZonedDateTime(s.zonedDateTime)
                .putZoneOffset(s.zoneOffset)
                .putInstant(s.instant)
                .putDate(s.date)
                .putUUID(s.uuid)
                .putBigInteger(s.bigInteger)
                .putBigDecimal(s.bigDecimal)
                .putCollection(s.collection, StateOutputStream::putString)
                .putArray(s.array, StateOutputStream::putInt)
                .putMap(s.map, StateOutputStream::putString, StateOutputStream::putLong);
    }

    public static State read(StateInputStream buffer) {
        return new State(
                buffer.getString(),
                buffer.getEnum(TimeUnit.class),
                buffer.getByte(),
                buffer.getShort(),
                buffer.getInt(),
                buffer.getLong(),
                buffer.getFloat(),
                buffer.getDouble(),
                buffer.getChar(),
                buffer.getBoolean(),
                buffer.getBytes(),
                buffer.getShorts(),
                buffer.getInts(),
                buffer.getLongs(),
                buffer.getFloats(),
                buffer.getDoubles(),
                buffer.getChars(),
                buffer.getBooleans(),
                buffer.getLocalTime(),
                buffer.getLocalDate(),
                buffer.getLocalDateTime(),
                buffer.getZonedDateTime(),
                buffer.getZoneOffset(),
                buffer.getInstant(),
                buffer.getDate(),
                buffer.getUUID(),
                buffer.getBigInteger(),
                buffer.getBigDecimal(),
                buffer.getCollection(StateInputStream::getString),
                buffer.getArray(Integer.class, StateInputStream::getInt),
                buffer.getMap(StateInputStream::getString, StateInputStream::getLong));
    }

    //EQUALS


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return byteValue == state.byteValue &&
                shortValue == state.shortValue &&
                intValue == state.intValue &&
                longValue == state.longValue &&
                Float.compare(state.floatValue, floatValue) == 0 &&
                Double.compare(state.doubleValue, doubleValue) == 0 &&
                charValue == state.charValue &&
                booleanValue == state.booleanValue &&
                stringValue.equals(state.stringValue) &&
                enumValue == state.enumValue &&
                Arrays.equals(bytes, state.bytes) &&
                Arrays.equals(shorts, state.shorts) &&
                Arrays.equals(ints, state.ints) &&
                Arrays.equals(longs, state.longs) &&
                Arrays.equals(floats, state.floats) &&
                Arrays.equals(doubles, state.doubles) &&
                Arrays.equals(chars, state.chars) &&
                Arrays.equals(booleans, state.booleans) &&
                localTime.equals(state.localTime) &&
                localDate.equals(state.localDate) &&
                localDateTime.equals(state.localDateTime) &&
                zonedDateTime.equals(state.zonedDateTime) &&
                zoneOffset.equals(state.zoneOffset) &&
                instant.equals(state.instant) &&
                date.equals(state.date) &&
                uuid.equals(state.uuid) &&
                bigInteger.equals(state.bigInteger) &&
                bigDecimal.equals(state.bigDecimal) &&
                collection.equals(state.collection) &&
                Arrays.equals(array, state.array) &&
                map.equals(state.map);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                stringValue,
                enumValue,
                byteValue,
                shortValue,
                intValue,
                longValue,
                floatValue,
                doubleValue,
                charValue,
                booleanValue,
                localTime,
                localDate,
                localDateTime,
                zonedDateTime,
                zoneOffset,
                instant,
                date,
                uuid,
                bigInteger,
                bigDecimal,
                collection,
                map);
        result = 31 * result + Arrays.hashCode(bytes);
        result = 31 * result + Arrays.hashCode(shorts);
        result = 31 * result + Arrays.hashCode(ints);
        result = 31 * result + Arrays.hashCode(longs);
        result = 31 * result + Arrays.hashCode(floats);
        result = 31 * result + Arrays.hashCode(doubles);
        result = 31 * result + Arrays.hashCode(chars);
        result = 31 * result + Arrays.hashCode(booleans);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }
}
