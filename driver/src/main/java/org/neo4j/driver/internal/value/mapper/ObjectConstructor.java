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

import java.lang.reflect.InvocationTargetException;

public class ObjectConstructor {

    public <T extends Record> T mapAndConstruct(ConstructorAndArguments<T> constructorAndArguments) {
        var constructor = constructorAndArguments.constructor();
        var parameters = constructor.getParameters();
        var arguments = constructorAndArguments.argumentValues();
        var constructorArgs = new Object[arguments.size()];
        for (var i = 0; i < parameters.length; i++) {
            var type = parameters[i].getType();
            constructorArgs[i] = arguments.get(i).as(type);
        }
        try {
            return constructor.newInstance(constructorArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
