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

    @Bean
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
                new MultiTenantJmsRequestInterceptor().initDefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setPubSubDomain(false);
        return factory;
    }

    @Bean
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory topicConnectionFactory) {
        DefaultJmsListenerContainerFactory factory =
                new MultiTenantJmsRequestInterceptor().initDefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(topicConnectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    public List<Exception> exceptionHolder() {
        return new ArrayList<>();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(MultiTenantJmsInterceptorTest.embeddedBroker.getVmURL());
    }

    @Bean
    public ConnectionFactory topicConnectionFactory() {
        return new ActiveMQConnectionFactory(MultiTenantJmsInterceptorTest.embeddedBroker.getVmURL());
    }

    @Bean
    public MultiTenantJmsTemplate queueJmsTemplate(ConnectionFactory connectionFactory) {
        MultiTenantJmsTemplate jmsTemplate = new MultiTenantJmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setPubSubDomain(false);
        return jmsTemplate;
    }

    @Bean
    public MultiTenantJmsTemplate topicJmsTemplate(ConnectionFactory topicConnectionFactory) {
        MultiTenantJmsTemplate jmsTemplate = new MultiTenantJmsTemplate();
        jmsTemplate.setConnectionFactory(topicConnectionFactory);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
}
