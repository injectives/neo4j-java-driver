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
package org.neo4j.driver.internal.value.mapper;

import java.util.Set;
import java.util.function.Function;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.value.Uncoercible;
import org.neo4j.driver.types.Type;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.driver.value.ValueMapper;

public class JavaRecordMapper implements ValueMapper<Record> {
    private static final TypeSystem TS = TypeSystem.getDefault();

    private static final Set<Type> KEYED_TYPES = Set.of(TS.MAP(), TS.NODE(), TS.RELATIONSHIP());

    private static final ConstructorFinder constructorFinder = new ConstructorFinder();

    private static final ObjectConstructor objectConstructor = new ObjectConstructor();

    @Override
    public boolean supports(Type valueType, Class<?> targetClass) {
        return KEYED_TYPES.contains(valueType) && Record.class.isAssignableFrom(targetClass);
    }

    @Override
    public Record map(Value value, Class<Record> targetClass) {
        var keyToValue = value.asMap(Function.identity());
        return constructorFinder
                .findConstructor(keyToValue, targetClass)
                .map(objectConstructor::mapAndConstruct)
                .orElseThrow(() -> new Uncoercible(value.type().name(), targetClass.getCanonicalName()));
    }
}
