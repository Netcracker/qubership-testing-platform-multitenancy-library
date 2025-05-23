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

import javax.jms.ConnectionFactory;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.lang.Nullable;

public interface AtpJmsTemplate extends JmsOperations {

    /**
     * Convert and send message.
     *
     * @param destination String Jms Destination
     * @param message Object message to be sent
     * @param properties Map of properties
     * @throws JmsException in case JMS errors occurred.
     */
    void convertAndSend(final String destination,
                        final Object message,
                        final Map<String, Object> properties) throws JmsException;

    /**
     * Setter for messageConverter.
     *
     * @param messageConverter MessageConverter object.
     */
    void setMessageConverter(@Nullable final MessageConverter messageConverter);

    /**
     *  Setter for connectionFactory.
     *
     * @param connectionFactory ConnectionFactory object.
     */
    void setConnectionFactory(@Nullable final ConnectionFactory connectionFactory);

    /**
     * Setter for pubSubDomain.
     *
     * @param pubSubDomain true/false.
     */
    void setPubSubDomain(final boolean pubSubDomain);
}
