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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

public class ConstructorFinder {
    @SuppressWarnings("unchecked")
    public <T extends java.lang.Record> Optional<ConstructorAndArguments<T>> findConstructor(
            Map<String, Value> keyToValue, Class<T> targetClass) {
        var keys = keyToValue.keySet();
        var keysCount = keys.size();
        var bestMatch = -1;
        Constructor<T> bestConstructor = null;
        var constructors = targetClass.getDeclaredConstructors();
        for (var i = 0; i < constructors.length; i++) {
            var constructor = constructors[i];
            var parameters = constructor.getParameters();
            var matchCount = match(keys, parameters);
            if (matchCount > bestMatch) {
                bestMatch = matchCount;
                bestConstructor = (Constructor<T>) constructor;
                if (bestMatch == keysCount) {
                    break;
                }
            }
        }
        if (bestConstructor == null) {
            return Optional.empty();
        }
        var arguments = Arrays.stream(bestConstructor.getParameters())
                .map(Parameter::getName)
                .map(name -> keyToValue.getOrDefault(name, Values.NULL))
                .toList();
        return Optional.of(new ConstructorAndArguments<>(bestConstructor, arguments));
    }

    // todo refactor
    private int match(Set<String> keys, Parameter[] parameters) {
        var matchCount = new AtomicInteger();
        Arrays.stream(parameters).map(Parameter::getName).forEach(name -> {
            if (keys.contains(name)) {
                matchCount.getAndIncrement();
            }
        });
        return matchCount.get();
    }
}
