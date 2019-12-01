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

package sabirove.codec;

import java.io.*;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import sabirove.codec.filter.CodecBufferSpec;
import sabirove.codec.filter.CodecFilters;
import sabirove.codec.function.CodecFunctions;


import static org.junit.jupiter.api.Assertions.assertEquals;

class ShowcaseTest {

    static final class Person implements Serializable {
        final String name;
        final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age &&
                    name.equals(person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }


    @Test
    void testCodec() {
        final Codec<Person> personCodec =
                Codec.withFunction(CodecFunctions.javaSerializing(Person.class))
                        .withBuffer(CodecBufferSpec.withDefaultSize())
                        .withFilterChain(
                                CodecFilters.compressWithGzip(),
                                CodecFilters.encodeWithBase64Url()
                        ).build();

        Person person1 = new Person("Jack", 24);
        Person person2 = new Person("Sarah", 30);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (EncoderStream<Person> encoder = personCodec.wrap(bos)) {
            encoder.write(person1);
            encoder.write(person2);
        }

        byte[] bytes = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (DecoderStream<Person> decoder = personCodec.wrap(bis)) {
            assertEquals(person1, decoder.read());
            assertEquals(person2, decoder.read());
        }
    }
}
