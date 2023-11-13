/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.internal.value;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.neo4j.driver.types.Type;
import org.neo4j.driver.value.ValueMapper;

public class ValueMapperProvider {
    private static final List<? extends ValueMapper<?>> valueMappers;

    static {
        valueMappers = ServiceLoader.load(ValueMapper.class).stream()
                .map(provider -> (ValueMapper<?>) provider.get())
                .toList();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<? extends ValueMapper<T>> mapper(Type type, Class<T> targetClass) {
        return (Optional<? extends ValueMapper<T>>) valueMappers.stream()
                .filter(mapper -> mapper.supports(type, targetClass))
                .findFirst();
    }
}
