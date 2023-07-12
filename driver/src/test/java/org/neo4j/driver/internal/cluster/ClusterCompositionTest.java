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
package org.neo4j.driver.internal.cluster;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.driver.Values.value;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.A;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.B;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.C;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.D;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.E;
import static org.neo4j.driver.internal.util.ClusterCompositionUtil.F;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.BoltServerAddress;
import org.neo4j.driver.internal.InternalRecord;

class ClusterCompositionTest {
    @Test
    void hasWritersReturnsFalseWhenNoWriters() {
        var composition = newComposition(1, addresses(A, B), addresses(), addresses(C, D));

        assertFalse(composition.hasWriters());
    }

    @Test
    void hasWritersReturnsTrueWhenSomeWriters() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses(E, F));

        assertTrue(composition.hasWriters());
    }

    @Test
    void hasRoutersAndReadersReturnsFalseWhenNoRouters() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses());

        assertFalse(composition.hasRoutersAndReaders());
    }

    @Test
    void hasRoutersAndReadersReturnsFalseWhenNoReaders() {
        var composition = newComposition(1, addresses(), addresses(A, B), addresses(C, D));

        assertFalse(composition.hasRoutersAndReaders());
    }

    @Test
    void hasRoutersAndReadersWhenSomeReadersAndRouters() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses(E, F));

        assertTrue(composition.hasRoutersAndReaders());
    }

    @Test
    void readersWhenEmpty() {
        var composition = newComposition(1, addresses(), addresses(A, B), addresses(C, D));

        assertEquals(0, composition.readers().size());
    }

    @Test
    void writersWhenEmpty() {
        var composition = newComposition(1, addresses(A, B), addresses(), addresses(C, D));

        assertEquals(0, composition.writers().size());
    }

    @Test
    void routersWhenEmpty() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses());

        assertEquals(0, composition.routers().size());
    }

    @Test
    void readersWhenNonEmpty() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses(E, F));

        assertEquals(addresses(A, B), composition.readers());
    }

    @Test
    void writersWhenNonEmpty() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses(E, F));

        assertEquals(addresses(C, D), composition.writers());
    }

    @Test
    void routersWhenNonEmpty() {
        var composition = newComposition(1, addresses(A, B), addresses(C, D), addresses(E, F));

        assertEquals(addresses(E, F), composition.routers());
    }

    @Test
    void expirationTimestamp() {
        var composition = newComposition(42, addresses(A, B), addresses(C, D), addresses(E, F));

        assertEquals(42, composition.expirationTimestamp());
    }

    @Test
    void parseCorrectRecord() {
        var values = new Value[] {
            value(42L),
            value(asList(serversEntry("READ", A, B), serversEntry("WRITE", C, D), serversEntry("ROUTE", E, F)))
        };
        Record record = new InternalRecord(asList("ttl", "servers"), values);

        var composition = ClusterComposition.parse(record, 0);

        // TTL is received in seconds and is converted to millis
        assertEquals(42_000, composition.expirationTimestamp());

        assertEquals(addresses(A, B), composition.readers());
        assertEquals(addresses(C, D), composition.writers());
        assertEquals(addresses(E, F), composition.routers());
    }

    @Test
    void parsePreservesOrderOfReaders() {
        var values = new Value[] {
            value(42L),
            value(asList(serversEntry("READ", A, C, E, B, F, D), serversEntry("WRITE"), serversEntry("ROUTE")))
        };
        Record record = new InternalRecord(asList("ttl", "servers"), values);

        var composition = ClusterComposition.parse(record, 0);

        assertThat(composition.readers(), contains(A, C, E, B, F, D));
        assertEquals(0, composition.writers().size());
        assertEquals(0, composition.routers().size());
    }

    @Test
    void parsePreservesOrderOfWriters() {
        var values = new Value[] {
            value(42L),
            value(asList(serversEntry("READ"), serversEntry("WRITE", C, F, D, A, B, E), serversEntry("ROUTE")))
        };
        Record record = new InternalRecord(asList("ttl", "servers"), values);

        var composition = ClusterComposition.parse(record, 0);

        assertEquals(0, composition.readers().size());
        assertThat(composition.writers(), contains(C, F, D, A, B, E));
        assertEquals(0, composition.routers().size());
    }

    @Test
    void parsePreservesOrderOfRouters() {
        var values = new Value[] {
            value(42L),
            value(asList(serversEntry("READ"), serversEntry("WRITE"), serversEntry("ROUTE", F, D, A, B, C, E)))
        };
        Record record = new InternalRecord(asList("ttl", "servers"), values);

        var composition = ClusterComposition.parse(record, 0);

        assertEquals(0, composition.readers().size());
        assertEquals(0, composition.writers().size());
        assertThat(composition.routers(), contains(F, D, A, B, C, E));
    }

    private static ClusterComposition newComposition(
            long expirationTimestamp,
            Set<BoltServerAddress> readers,
            Set<BoltServerAddress> writers,
            Set<BoltServerAddress> routers) {
        return new ClusterComposition(expirationTimestamp, readers, writers, routers, null);
    }

    private static Set<BoltServerAddress> addresses(BoltServerAddress... elements) {
        return new LinkedHashSet<>(asList(elements));
    }

    private static Map<String, Object> serversEntry(String role, BoltServerAddress... addresses) {
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        var addressStrings =
                Arrays.stream(addresses).map(BoltServerAddress::toString).collect(Collectors.toList());
        map.put("addresses", addressStrings);
        return map;
    }
}
