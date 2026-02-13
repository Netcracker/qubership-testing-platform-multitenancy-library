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

package org.qubership.atp.multitenancy.interceptor.jms.config;

import java.util.List;

import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.interceptor.jms.TestConstant;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TopicMessageListener {

    /**
     * List of exceptions.
     */
    private final List<Exception> exceptionHolder;

    /**
     * Method to collect exceptions.
     *
     * @param message TextMessage object.
     */
    @JmsListener(destination = "topic-1", containerFactory = "topicJmsListenerContainerFactory")
    public void topicJmsListenerMethod(final TextMessage message) {
        exceptionHolder.clear();
        if (!TestConstant.TEST_TENANT_ID.equals(TenantContext.getTenantInfo())) {
            exceptionHolder.add(new RuntimeException("TenantContext contains not correct tenantId"));
        }
    }
}
