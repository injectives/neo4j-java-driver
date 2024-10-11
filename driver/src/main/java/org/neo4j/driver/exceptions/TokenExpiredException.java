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
package org.neo4j.driver.exceptions;

import java.io.Serial;
import java.util.Map;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.GqlStatusError;
import org.neo4j.driver.util.Preview;

/**
 * The provided token has expired.
 * <p>
 * The current driver instance is considered invalid. It should not be used anymore. The client must create a new driver instance with a valid token.
 * <p>
 * Error code: Neo.ClientError.Security.TokenExpired
 */
public class TokenExpiredException extends SecurityException {
    @Serial
    private static final long serialVersionUID = -5593851859769531234L;

    /**
     * Creates a new instance.
     * @param code the code
     * @param message the message
     */
    // for testing only
    public TokenExpiredException(String code, String message) {
        this(
                GqlStatusError.UNKNOWN.getStatus(),
                GqlStatusError.UNKNOWN.getStatusDescription(message),
                code,
                message,
                GqlStatusError.DIAGNOSTIC_RECORD,
                null);
    }

    /**
     * Creates a new instance.
     * @param gqlStatus the GQLSTATUS as defined by the GQL standard
     * @param statusDescription the status description
     * @param code the code
     * @param message the message
     * @param diagnosticRecord the diagnostic record
     * @param cause the cause
     * @since 5.26.0
     */
    @Preview(name = "GQL-error")
    public TokenExpiredException(
            String gqlStatus,
            String statusDescription,
            String code,
            String message,
            Map<String, Value> diagnosticRecord,
            Throwable cause) {
        super(gqlStatus, statusDescription, code, message, diagnosticRecord, cause);
    }
}
