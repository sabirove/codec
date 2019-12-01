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

package sabirove.codec.util;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;


import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static sabirove.codec.util.CodecUtil.checkArgument;
import static sabirove.codec.util.CodecUtil.throwUnchecked;

/**
 * {@link FilterInputStream} implementation that can be used to read most of the common data types
 * that were written using the {@link StateOutputStream} or the corresponding encoding format.
 * This is the more robust analog of the {@link DataInputStream} which can be used to implement binary
 * deserialization of an arbitrary object ({@code state}) by sequentially reading the individual fields.
 *
 * @apiNote <ul>
 * <li>Supports reading the LEB128 variable-length encoded {@code int} and {@code long} types</li>
 * <li>underlying IOExceptions are rethrown as {@link UncheckedIOException} to keep the API clean</li>
 * <li>All {@code getXXX} methods will throw {@link EOFException} wrapped in {@link UncheckedIOException} if the
 * end of stream is reached while reading.
 * Use {@link #isEOF()} if the explicit check is required.</li>
 * <li>no nulls will ever be read</li>
 * </ul>
 * @see StateOutputStream
 */
public final class StateInputStream extends FilterInputStream {
    public StateInputStream(InputStream in) {
        super(in);
    }

    public boolean isEOF() {
        try {
            return in.read() == -1;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public byte getByte() {
        try {
            int b = in.read();
            if (b == -1) {
                throw new EOFException();
            }
            return (byte) b;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public boolean getBoolean() {
        byte b = getByte();
        boolean out = b == 1;
        checkArgument(out || b == 0, "illegal boolean byte value");
        return out;
    }

    public short getShort() {
        try {
            int b1 = in.read();
            int b2 = in.read();
            if (b2 == -1) {
                throw new EOFException();
            }
            return (short) ((b1 << 8) | (b2 & 0xff));
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public char getChar() {
        return (char) getShort();
    }

    public int getInt() {
        try {
            InputStream in = this.in;
            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            int b4 = in.read();
            if (b4 == -1) {
                throw new EOFException();
            }
            return ((b1 << 24) |
                    ((b2 & 0xff) << 16) |
                    ((b3 & 0xff) << 8) |
                    ((b4 & 0xff)));
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    /**
     * @see Varint#readUnsignedVarInt(InputStream)
     */
    public int getUnsignedVarInt() {
        return Varint.readUnsignedVarInt(in);
    }

    /**
     * @see Varint#readUnsignedVarInt(InputStream)
     */
    public int getSignedVarInt() {
        return Varint.readUnsignedVarInt(in);
    }

    public long getLong() {
        try {
            InputStream in = this.in;
            long val = 0;
            int b = -1;
            for (int offset = Long.SIZE - Byte.SIZE; offset >= 0; offset -= Byte.SIZE) {
                b = in.read();
                val |= ((long) b) << offset;
            }
            if (b == -1) {
                throw new EOFException();
            }
            return val;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    /**
     * @see Varint#readUnsignedVarLong(InputStream)
     */
    public long getUnsignedVarLong() {
        return Varint.readUnsignedVarLong(in);
    }

    /**
     * @see Varint#readSignedVarLong(InputStream)
     */
    public long getSignedVarLong() {
        return Varint.readSignedVarLong(in);
    }

    public float getFloat() {
        int bits = getInt();
        return Float.intBitsToFloat(bits);
    }

    public double getDouble() {
        long bits = getLong();
        return Double.longBitsToDouble(bits);
    }

    public byte[] getBytes() {
        try {
            int len = Varint.readUnsignedVarInt(in);
            byte[] bytes = new byte[len];
            int read = in.read(bytes, 0, len);
            if (read != len) {
                throw new EOFException();
            }
            return bytes;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public boolean[] getBooleans() {
        int size = Varint.readUnsignedVarInt(in);
        boolean[] values = new boolean[size];
        for (int i = 0; i < size; i++) {
            values[i] = getBoolean();
        }
        return values;
    }

    public short[] getShorts() {
        int size = Varint.readUnsignedVarInt(in);
        short[] values = new short[size];
        for (int i = 0; i < size; i++) {
            values[i] = getShort();
        }
        return values;
    }

    public char[] getChars() {
        int size = Varint.readUnsignedVarInt(in);
        char[] values = new char[size];
        for (int i = 0; i < size; i++) {
            values[i] = getChar();
        }
        return values;
    }

    public int[] getInts() {
        int size = Varint.readUnsignedVarInt(in);
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = getInt();
        }
        return values;
    }

    public long[] getLongs() {
        int size = Varint.readUnsignedVarInt(in);
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = getLong();
        }
        return values;
    }

    public float[] getFloats() {
        int size = Varint.readUnsignedVarInt(in);
        float[] values = new float[size];
        for (int i = 0; i < size; i++) {
            values[i] = getFloat();
        }
        return values;
    }

    public double[] getDoubles() {
        int size = Varint.readUnsignedVarInt(in);
        double[] values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = getDouble();
        }
        return values;
    }

    public String getString() {
        return new String(getBytes(), UTF_8);
    }

    public <T extends Enum<T>> T getEnum(Class<T> type) {
        int ordinal = Varint.readUnsignedVarInt(in);
        return type.getEnumConstants()[ordinal];
    }

    public UUID getUUID() {
        return new UUID(getLong(), getLong());
    }

    public LocalTime getLocalTime() {
        return LocalTime.ofNanoOfDay(getLong());
    }

    public LocalDate getLocalDate() {
        return LocalDate.ofEpochDay(getLong());
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(getLocalDate(), getLocalTime());
    }

    public ZonedDateTime getZonedDateTime() {
        LocalDateTime localDateTime = getLocalDateTime();
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(getInt());
        return ZonedDateTime.of(localDateTime, offset);
    }

    public ZoneOffset getZoneOffset() {
        return ZoneOffset.ofTotalSeconds(getInt());
    }

    public Instant getInstant() {
        return Instant.ofEpochSecond(getLong(), getInt());
    }

    public Date getDate() {
        return new Date(getLong());
    }

    public BigInteger getBigInteger() {
        return new BigInteger(getBytes());
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(getBigInteger(), getInt());
    }

    public <T> Collection<T> getCollection(Function<StateInputStream, T> elementReader) {
        return Stream.generate(() -> elementReader.apply(this))
                .limit(Varint.readUnsignedVarInt(in))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getArray(Class<T> elementType, Function<StateInputStream, T> elementReader) {
        return Stream.generate(() -> elementReader.apply(this))
                .limit(Varint.readUnsignedVarInt(in))
                .toArray(s -> (T[]) Array.newInstance(elementType, s));
    }

    public <K, V> Map<K, V> getMap(Function<StateInputStream, K> keyReader,
                                   Function<StateInputStream, V> valueReader) {
        return Stream.generate(() -> null)
                .limit(Varint.readUnsignedVarInt(in))
                .collect(toMap(
                        none -> keyReader.apply(this),
                        none -> valueReader.apply(this)));
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }
}
