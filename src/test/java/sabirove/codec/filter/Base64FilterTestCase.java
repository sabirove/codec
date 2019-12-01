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


import java.util.Set;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class Base64FilterTestCase extends CodecFilterTestCase {

    @SuppressWarnings({"AbstractMethodCallInConstructor", "OverridableMethodCallDuringObjectConstruction"})
    private final Set<Integer> characters = getValidChars().chars().boxed().collect(Collectors.toSet());

    protected abstract String getValidChars();

    @Override
    protected final void testEncoded(byte[] input, byte[] encoded) {
        Set<Integer> cc = this.characters;
        for (byte b : encoded) {
            assertTrue(cc.contains((int) b));
        }
    }
}
