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
package org.neo4j.driver;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.value.NodeValue;

class ValueTests {
    @Test
    void test() {
        var node = new InternalNode(
                0L, Set.of("Product"), Map.of("name", Values.value("Apple"), "value", Values.value(100)));
        var nodeValue = new NodeValue(node);

        var product = nodeValue.as(Product.class);
        System.out.println(product);
    }

    public record Product(String name, Long value) {}
}
