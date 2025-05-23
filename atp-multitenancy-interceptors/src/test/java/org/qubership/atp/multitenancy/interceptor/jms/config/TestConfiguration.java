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

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.qubership.atp.multitenancy.interceptor.jms.MultiTenantJmsRequestInterceptor;
import org.qubership.atp.multitenancy.interceptor.jms.MultiTenantJmsTemplate;
import org.qubership.atp.multitenancy.interceptor.jms.MultiTenantJmsInterceptorTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableJms
@RequiredArgsConstructor
public class TestConfiguration {

    /**
     * Create queueJmsListenerContainerFactory bean.
     *
     * @param connectionFactory ConnectionFactory object
     * @return new DefaultJmsListenerContainerFactory object created and configured.
     */
    @Bean
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(
            final ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
                new MultiTenantJmsRequestInterceptor().initDefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setPubSubDomain(false);
        return factory;
    }

    /**
     * Create topicJmsListenerContainerFactory bean.
     *
     * @param topicConnectionFactory ConnectionFactory object
     * @return new DefaultJmsListenerContainerFactory object created and configured.
     */
    @Bean
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
            final ConnectionFactory topicConnectionFactory) {
        DefaultJmsListenerContainerFactory factory =
                new MultiTenantJmsRequestInterceptor().initDefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(topicConnectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setPubSubDomain(true);
        return factory;
    }

    /**
     * Create exceptionHolder bean.
     *
     * @return new empty ArrayList object.
     */
    @Bean
    public List<Exception> exceptionHolder() {
        return new ArrayList<>();
    }

    /**
     * Create connectionFactory bean.
     *
     * @return new ActiveMQConnectionFactory object created and configured.
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(MultiTenantJmsInterceptorTest.embeddedBroker.getVmURL());
    }

    /**
     * Create topicConnectionFactory bean.
     *
     * @return new ActiveMQConnectionFactory object created and configured.
     */
    @Bean
    public ConnectionFactory topicConnectionFactory() {
        return new ActiveMQConnectionFactory(MultiTenantJmsInterceptorTest.embeddedBroker.getVmURL());
    }

    /**
     * Create queueJmsTemplate bean.
     *
     * @param connectionFactory ConnectionFactory object
     * @return new MultiTenantJmsTemplate object created and configured.
     */
    @Bean
    public MultiTenantJmsTemplate queueJmsTemplate(final ConnectionFactory connectionFactory) {
        MultiTenantJmsTemplate jmsTemplate = new MultiTenantJmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setPubSubDomain(false);
        return jmsTemplate;
    }

    /**
     * Create topicJmsTemplate bean.
     *
     * @param topicConnectionFactory ConnectionFactory object
     * @return new MultiTenantJmsTemplate object created and configured.
     */
    @Bean
    public MultiTenantJmsTemplate topicJmsTemplate(final ConnectionFactory topicConnectionFactory) {
        MultiTenantJmsTemplate jmsTemplate = new MultiTenantJmsTemplate();
        jmsTemplate.setConnectionFactory(topicConnectionFactory);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
}
