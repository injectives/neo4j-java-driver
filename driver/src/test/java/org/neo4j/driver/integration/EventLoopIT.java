/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
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
package org.neo4j.driver.integration;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.testutil.DatabaseExtension;
import org.neo4j.driver.testutil.ParallelizableIT;

@ParallelizableIT
class EventLoopIT {
    @RegisterExtension
    static final DatabaseExtension neo4j = new DatabaseExtension();

    @Test
    void shouldBlock() {
        var config = Config.builder()
                .withEventLoopThreads(1)
                .withLogging(Logging.console(Level.FINE))
                .build();
        var driver = neo4j.customDriver(config);
        var blockingFuture = new CompletableFuture<Void>();

        var secondResultFuture = CompletableFuture.supplyAsync(() -> {
                    var session = driver.session(AsyncSession.class);
                    var future = new CompletableFuture<Void>();
                    session.runAsync("RETURN 1").thenAccept(ignored -> {
                        future.completeAsync(() -> null);
                        // block 1 eventloop thread
                        blockingFuture.join();
                    });
                    return future;
                })
                .thenCompose(Function.identity())
                .thenApply(ignored -> {
                    System.out.println(Thread.currentThread());
                    return driver.session().run("RETURN 1");
                });

        secondResultFuture.join();
        fail("Should be unreachable");
    }
}
