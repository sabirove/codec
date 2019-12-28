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

import com.github.sabirove.codec.test_util.Rnd;

final class AesEncryptCodecWithFixedKeyFilterTest extends EncryptCodecFilterTestCase {
    private final byte[] key = Rnd.rndBytes(16, 17);

    @Override
    protected CodecFilter getFilter() {
        return CodecFilters.encryptWithAes(key);
    }

}
