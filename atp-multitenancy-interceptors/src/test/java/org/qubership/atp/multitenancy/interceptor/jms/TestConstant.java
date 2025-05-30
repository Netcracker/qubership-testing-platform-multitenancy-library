/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.multitenancy.interceptor.jms;

public class TestConstant {

    /**
     * Tenant ID for tests.
     */
    public static final String TEST_TENANT_ID = "3d6a138d-057b-4e35-8348-17aee2f2b0f8";

    /**
     * Queue name.
     */
    public static final String QUEUE_NAME = "queue-1";

    /**
     * Text of message.
     */
    public static final String MESSAGE_TEXT = "Test message";

    /**
     * Topic name.
     */
    public static final String TOPIC_NAME = "topic-1";

    /**
     * Topic name with destination type.
     */
    public static final String TOPIC_NAME_WITH_DESTINATION_TYPE = "topic://topic-1";
}
