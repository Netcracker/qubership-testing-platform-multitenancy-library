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

import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.core.header.CustomHeader;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.lang.NonNull;

public class MultiTenantJmsRequestInterceptor {

    /**
     * Return DefaultJmsListenerContainerFactory instance with request interceptor, that receive jms message, set
     * x-project-id StringProperty value from javax.jms.Message to TenantContext (will use to getTenantInfo to get
     * data from necessary db\schema\cluster).
     *
     * @return {@link DefaultJmsListenerContainerFactory} with multi-tenant jms message interceptor.
     */
    public DefaultJmsListenerContainerFactory initDefaultJmsListenerContainerFactory() {
        return new DefaultJmsListenerContainerFactory() {
            @Override
            @NonNull
            protected DefaultMessageListenerContainer createContainerInstance() {
                return new DefaultMessageListenerContainer() {
                    @Override
                    protected Message receiveMessage(@NonNull final MessageConsumer consumer) throws JMSException {
                        Message message = super.receiveMessage(
                                Objects.requireNonNull(consumer, "Can't receive message - consumer is null"));
                        if (message != null) {
                            String tenantId = message.getStringProperty(CustomHeader.X_PROJECT_ID);
                            if (tenantId != null && !tenantId.isEmpty()) {
                                TenantContext.setTenantInfo(tenantId);
                            } else {
                                TenantContext.setDefaultTenantInfo();
                            }
                        }
                        return message;
                    }
                };
            }
        };
    }
}
