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

import java.util.Map;
import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.Message;

import org.qubership.atp.multitenancy.core.header.CustomHeader;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 * This class overrides convertAndSend method that can hook jms message before sending and set properties to javax.jms.
 * Message that provided from input parameters.
 * <p>At this moment we set property to javax.jms.Message - x-project-id as StringProperty, if input properties isn't
 * contains x-project-id, will set "default" tenantId to Message</p>
 */
public class MultiTenantJmsTemplate extends JmsTemplate implements AtpJmsTemplate {

    /**
     * Convert and send message.
     *
     * @param destination String destination name
     * @param message Object message to be sent
     * @param properties Map of message properties.
     */
    public void convertAndSend(final String destination,
                               final Object message,
                               final Map<String, Object> properties) throws JmsException {
        super.send(destination, (session) -> {
            Message toMessage = Objects.requireNonNull(super.getMessageConverter(),
                            "MessageConverter wasn't configured for " + destination + "destination")
                    .toMessage(message, session);
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                setProperties(properties, toMessage, property);
            }
            return toMessage;
        });
    }

    private void setProperties(final Map<String, Object> properties,
                               final Message toMessage,
                               final Map.Entry<String, Object> property) throws JMSException {
        if (!CustomHeader.X_PROJECT_ID.equals(property.getKey())) {
            toMessage.setStringProperty(property.getKey(), String.valueOf(property.getValue()));
            return;
        }
        toMessage.setStringProperty(CustomHeader.X_PROJECT_ID,
                String.valueOf(properties.getOrDefault(property.getKey(), "default")));
    }
}
