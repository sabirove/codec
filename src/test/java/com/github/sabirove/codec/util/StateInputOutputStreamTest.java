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

package com.github.sabirove.codec.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

import com.github.sabirove.codec.test_util.Rnd;
import com.github.sabirove.codec.test_util.State;
import com.github.sabirove.codec.test_util.TestUtil;
import org.junit.jupiter.api.RepeatedTest;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"resource", "rawtypes"})
class StateInputOutputStreamTest {

    @RepeatedTest(500)
    void testStateSerialization() {

        List<State> states = Stream.generate(State::random)
                .limit(Rnd.rndInt(100))
                .collect(Collectors.toList());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StateOutputStream sos = new StateOutputStream(bos);

        for (State state : states) {
            State.write(sos, state);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        StateInputStream sis = new StateInputStream(bis);

        for (State s : states) {
            assertEquals(s, State.read(sis));
        }

        TestUtil.assertThrowsIO(EOFException.class, sis::getByte);
    }


    @SuppressWarnings("unchecked")
    @RepeatedTest(500)
    void testRandomObjectsSerialization() {
        //random list of specs
        List<Spec> specs = IntStream.generate(() -> Rnd.rndInt(0, this.specs.size()))
                .limit(Rnd.rndInt(50))
                .mapToObj(this.specs::get)
                .collect(Collectors.toList());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StateOutputStream sos = new StateOutputStream(bos);
        List<Object> objects = new ArrayList<>(specs.size());

        //generate objects and write
        for (Spec spec : specs) {
            Object o = spec.generator.get();
            spec.writer.apply(sos, o);
            objects.add(o);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        StateInputStream sis = new StateInputStream(bis);

        //read and assert
        for (int i = 0; i < objects.size(); i++) {
            Object actual = specs.get(i).reader.apply(sis);
            Object expected = objects.get(i);
            if (actual instanceof char[]) {
                assertArrayEquals((char[]) expected, (char[]) actual);
            } else if (actual instanceof byte[]) {
                assertArrayEquals((byte[]) expected, (byte[]) actual);
            } else {
                assertEquals(expected, actual);
            }
        }
    }

    private static final class Spec<T> {
        final Supplier<T> generator;
        final BiFunction<StateOutputStream, T, StateOutputStream> writer;
        final Function<StateInputStream, T> reader;

        Spec(Supplier<T> generator,
             BiFunction<StateOutputStream, T, StateOutputStream> writer,
             Function<StateInputStream, T> reader) {
            this.generator = generator;
            this.writer = writer;
            this.reader = reader;
        }
    }

    private final List<Spec> specs = Arrays.asList(
            new Spec<>(
                    Rnd::rndBigDecimal,
                    StateOutputStream::putBigDecimal,
                    StateInputStream::getBigDecimal
            ),
            new Spec<>(
                    () -> Rnd.rndMap(20, () -> Rnd.rndString(1024), Rnd::rndBoolean),
                    (s, m) -> s.putMap(m, StateOutputStream::putString, StateOutputStream::putBoolean),
                    s -> s.getMap(StateInputStream::getString, StateInputStream::getBoolean)
            ),
            new Spec<>(
                    Rnd::rndLong,
                    StateOutputStream::putSignedVarLong,
                    StateInputStream::getSignedVarLong
            ),
            new Spec<>(
                    () -> Rnd.rndInt(0, Integer.MAX_VALUE),
                    StateOutputStream::putUnsignedVarInt,
                    StateInputStream::getUnsignedVarInt
            ),
            new Spec<>(
                    () -> Rnd.rndBytes(9999),
                    StateOutputStream::putBytes,
                    StateInputStream::getBytes
            ),
            new Spec<>(
                    () -> Rnd.rndString(3467),
                    StateOutputStream::putString,
                    StateInputStream::getString
            ),
            new Spec<>(
                    () -> Rnd.rndChars(744),
                    StateOutputStream::putChars,
                    StateInputStream::getChars
            ),
            new Spec<>(
                    Rnd::rndUUID,
                    StateOutputStream::putUUID,
                    StateInputStream::getUUID
            ),
            new Spec<>(
                    Rnd::rndDouble,
                    StateOutputStream::putDouble,
                    StateInputStream::getDouble
            ),
            new Spec<>(
                    Rnd::rndLong,
                    StateOutputStream::putLong,
                    StateInputStream::getLong
            ),
            new Spec<>(
                    Rnd::rndShort,
                    StateOutputStream::putShort,
                    StateInputStream::getShort
            ),
            new Spec<>(
                    Rnd::rndLocalDateTime,
                    StateOutputStream::putLocalDateTime,
                    StateInputStream::getLocalDateTime
            ),
            new Spec<>(
                    () -> Rnd.rndEnum(TimeUnit.class),
                    StateOutputStream::putEnum,
                    (s) -> s.getEnum(TimeUnit.class)
            ),
            new Spec<>(
                    () -> Rnd.rndList(175, Rnd::rndFloat),
                    (s, l) -> s.putCollection(l, StateOutputStream::putFloat),
                    s -> s.getCollection(StateInputStream::getFloat)
            ));

}
