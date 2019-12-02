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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;


import static java.nio.charset.StandardCharsets.UTF_8;
import static sabirove.codec.util.CodecUtil.throwUnchecked;

/**
 * {@link FilterOutputStream} implementation that can be used to write most of the common Java types
 * with binary serialization semantics.
 * Intended to be used in tandem with complementary {@link StateInputStream} that uses same encoding format
 * to deserialize the data.
 * <p>
 * This is the more robust analog of the {@link DataOutputStream} that can be used to implement binary
 * serialization of an arbitrary object ({@code state}) by sequentially writing the individual fields.
 *
 * @apiNote <ul>
 * <li>supports writing the LEB128 variable-length encoded signed/unsigned {@code int} and {@code long} values</li>
 * <li>underlying IOExceptions are rethrown as {@link UncheckedIOException} to keep the API clean</li>
 * <li>no nulls are allowed to be written</li>
 * </ul>
 * @see StateInputStream
 */
public final class StateOutputStream extends FilterOutputStream {

    public StateOutputStream(OutputStream out) {
        super(out);
    }

    public StateOutputStream putByte(byte value) {
        try {
            out.write(value);
            return this;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public StateOutputStream putBoolean(boolean value) {
        return putByte((byte) (value ? 1 : 0));
    }

    public StateOutputStream putShort(short value) {
        writeShort(value);
        return this;
    }

    public StateOutputStream putChar(char value) {
        writeShort((short) value);
        return this;
    }

    public StateOutputStream putInt(int value) {
        writeInt(value);
        return this;
    }

    public StateOutputStream putUnsignedVarInt(int value) {
        Varint.writeUnsignedVarInt(value, out);
        return this;
    }

    public StateOutputStream putSignedVarInt(int value) {
        Varint.writeSignedVarInt(value, out);
        return this;
    }

    public StateOutputStream putLong(long value) {
        writeLong(value);
        return this;
    }

    public StateOutputStream putUnsignedVarLong(long value) {
        Varint.writeUnsignedVarLong(value, out);
        return this;
    }

    public StateOutputStream putSignedVarLong(long value) {
        Varint.writeSignedVarLong(value, out);
        return this;
    }

    public StateOutputStream putFloat(float value) {
        int bits = Float.floatToIntBits(value);
        writeInt(bits);
        return this;
    }

    public StateOutputStream putDouble(double value) {
        long bits = Double.doubleToLongBits(value);
        writeLong(bits);
        return this;
    }

    public StateOutputStream putBytes(byte[] values) {
        try {
            Varint.writeUnsignedVarInt(values.length, out);
            out.write(values);
            return this;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public StateOutputStream putBooleans(boolean[] values) {
        try {
            Varint.writeUnsignedVarInt(values.length, out);
            for (boolean value : values) {
                out.write(value ? 1 : 0);
            }
            return this;
        } catch (IOException e) {
            return throwUnchecked(e);
        }
    }

    public StateOutputStream putShorts(short[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (short value : values) {
            writeShort(value);
        }
        return this;
    }

    public StateOutputStream putChars(char[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (char value : values) {
            writeShort((short) value);
        }
        return this;
    }

    public StateOutputStream putInts(int[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (int value : values) {
            writeInt(value);
        }
        return this;
    }

    public StateOutputStream putLongs(long[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (long value : values) {
            writeLong(value);
        }
        return this;
    }

    public StateOutputStream putFloats(float[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (float value : values) {
            writeInt(Float.floatToIntBits(value));
        }
        return this;
    }

    public StateOutputStream putDoubles(double[] values) {
        Varint.writeUnsignedVarInt(values.length, out);
        for (double value : values) {
            writeLong(Double.doubleToLongBits(value));
        }
        return this;
    }

    public StateOutputStream putString(String value) {
        byte[] bytes = value.getBytes(UTF_8);
        return putBytes(bytes);
    }

    public <T extends Enum<T>> StateOutputStream putEnum(T value) {
        Varint.writeUnsignedVarInt(value.ordinal(), out);
        return this;
    }

    public StateOutputStream putUUID(UUID value) {
        writeLong(value.getMostSignificantBits());
        writeLong(value.getLeastSignificantBits());
        return this;
    }

    public StateOutputStream putLocalTime(LocalTime value) {
        return putLong(value.toNanoOfDay());
    }

    public StateOutputStream putLocalDate(LocalDate value) {
        return putLong(value.toEpochDay());
    }

    public StateOutputStream putLocalDateTime(LocalDateTime value) {
        return putLocalDate(value.toLocalDate())
                .putLocalTime(value.toLocalTime());
    }

    public StateOutputStream putZonedDateTime(ZonedDateTime value) {
        return putLocalDateTime(value.toLocalDateTime())
                .putInt(value.getOffset().getTotalSeconds());
    }

    public StateOutputStream putZoneOffset(ZoneOffset value) {
        return putInt(value.getTotalSeconds());
    }

    public StateOutputStream putInstant(Instant value) {
        return putLong(value.getEpochSecond())
                .putInt(value.getNano());
    }

    public StateOutputStream putDate(Date value) {
        return putLong(value.getTime());
    }

    public StateOutputStream putBigInteger(BigInteger value) {
        return putBytes(value.toByteArray());
    }

    public StateOutputStream putBigDecimal(BigDecimal value) {
        return putBigInteger(value.unscaledValue())
                .putInt(value.scale());
    }

    public <T> StateOutputStream putCollection(Collection<T> collection,
                                               BiConsumer<StateOutputStream, T> elementWriter) {
        Varint.writeUnsignedVarInt(collection.size(), out);
        for (T e : collection) {
            elementWriter.accept(this, e);
        }
        return this;
    }

    public <T> StateOutputStream putArray(T[] array,
                                          BiConsumer<StateOutputStream, T> elementWriter) {
        Varint.writeUnsignedVarInt(array.length, out);
        for (T e : array) {
            elementWriter.accept(this, e);
        }
        return this;
    }

    public <K, V> StateOutputStream putMap(Map<K, V> map,
                                           BiConsumer<StateOutputStream, K> keyWriter,
                                           BiConsumer<StateOutputStream, V> valueWriter) {
        Varint.writeUnsignedVarInt(map.size(), out);
        for (Map.Entry<K, V> e : map.entrySet()) {
            keyWriter.accept(this, e.getKey());
            valueWriter.accept(this, e.getValue());
        }
        return this;
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void flush() {
        try {
            super.flush();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    //UTIL

    private void writeShort(short value) {
        try {
            out.write((value >>> 8) & 0xFF);
            out.write(value & 0xFF);
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    private void writeInt(int value) {
        OutputStream out = this.out;
        try {
            out.write((value >>> 24) & 0xFF);
            out.write((value >>> 16) & 0xFF);
            out.write((value >>> 8) & 0xFF);
            out.write(value & 0xFF);
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    private void writeLong(long value) {
        OutputStream out = this.out;
        try {
            out.write((byte) (value >>> 56));
            out.write((byte) (value >>> 48));
            out.write((byte) (value >>> 40));
            out.write((byte) (value >>> 32));
            out.write((byte) (value >>> 24));
            out.write((byte) (value >>> 16));
            out.write((byte) (value >>> 8));
            out.write((byte) value);
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }
}
