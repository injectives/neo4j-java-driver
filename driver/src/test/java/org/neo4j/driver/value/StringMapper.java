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
package org.neo4j.driver.value;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.Type;
import org.neo4j.driver.types.TypeSystem;

public class StringMapper implements ValueMapper<String> {
    private static final TypeSystem TS = TypeSystem.getDefault();

    @Override
    public boolean supports(Type valueType, Class<?> targetClass) {
        return TS.FLOAT().equals(valueType) && targetClass.equals(String.class);
    }

    @Override
    public String map(Value value, Class<String> targetClass) {
        return "" + value.asDouble();
    }
}
