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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.qubership.atp.multitenancy.core.header.CustomHeader;
import org.qubership.atp.multitenancy.interceptor.jms.config.QueueMessageListener;
import org.qubership.atp.multitenancy.interceptor.jms.config.TestConfiguration;
import org.qubership.atp.multitenancy.interceptor.jms.config.TopicMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@Slf4j
@ContextConfiguration(classes = {TestConfiguration.class, QueueMessageListener.class, TopicMessageListener.class})
public class MultiTenantJmsInterceptorTest {

    @ClassRule
    public static EmbeddedActiveMQBroker embeddedBroker = new EmbeddedActiveMQBroker();

    @Autowired
    List<Exception> exceptionHolder;
    @Autowired
    MultiTenantJmsTemplate queueJmsTemplate;
    @Autowired
    MultiTenantJmsTemplate topicJmsTemplate;
    @SpyBean
    QueueMessageListener queueMessageListenerSpy;
    @SpyBean
    TopicMessageListener topicMessageListenerSpy;

    @Test
    public void testInitDefaultJmsListenerContainerFactoryShouldReturnEqualTenantIdFromTenantContextAndXProjectIdHeaderWhenJmsMessageReceived() throws Exception {
        Map<String, Object> prop = new HashMap<>();
        prop.put(CustomHeader.X_PROJECT_ID, TestConstant.TEST_TENANT_ID);
        embeddedBroker.pushMessageWithProperties(TestConstant.QUEUE_NAME, TestConstant.MESSAGE_TEXT, prop);
        assertEquals(1, embeddedBroker.getMessageCount(TestConstant.QUEUE_NAME)); // check that message in queue
        Thread.sleep(100); //wait for consumer receive message
        assertEquals(0, embeddedBroker.getMessageCount(TestConstant.QUEUE_NAME)); // check that message consumed
        if (!exceptionHolder.isEmpty()) {
            fail();
        }
    }

    @Test
    public void testInitDefaultJmsListenerContainerFactoryTopicShouldReturnEqualTenantIdFromTenantContextAndXProjectIdHeaderWhenJmsMessageReceived() throws Exception {
        Map<String, Object> prop = new HashMap<>();
        prop.put(CustomHeader.X_PROJECT_ID, TestConstant.TEST_TENANT_ID);
        embeddedBroker.pushMessageWithProperties(TestConstant.TOPIC_NAME_WITH_DESTINATION_TYPE, TestConstant.MESSAGE_TEXT, prop);
        Thread.sleep(100); //wait for consumer receive message
        if (!exceptionHolder.isEmpty()) {
            fail();
        }
    }

    @Test
    public void testConvertAndSendShouldReturnEqualsXProjectIdInReceivedMessageFromQueueByJmsListenerAfterXProjectIdWasSentByMultiTenantJmsTemplate() throws JMSException {
        Map<String, Object> prop = new HashMap<>();
        prop.put(CustomHeader.X_PROJECT_ID, TestConstant.TEST_TENANT_ID);
        queueJmsTemplate.convertAndSend(TestConstant.QUEUE_NAME, TestConstant.MESSAGE_TEXT, prop);
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        Mockito.verify(queueMessageListenerSpy, Mockito.timeout(1000))
                .queueJmsListenerMethod(messageCaptor.capture());
        TextMessage receivedMessage = messageCaptor.getValue();
        assertEquals(TestConstant.TEST_TENANT_ID, receivedMessage.getStringProperty(CustomHeader.X_PROJECT_ID));
    }

    @Test
    public void testConvertAndSendShouldReturnEqualsXProjectIdInReceivedMessageFromTopicByJmsListenerAfterXProjectIdWasSentByMultiTenantJmsTemplate() throws JMSException {
        Map<String, Object> prop = new HashMap<>();
        prop.put(CustomHeader.X_PROJECT_ID, TestConstant.TEST_TENANT_ID);
        topicJmsTemplate.convertAndSend(TestConstant.TOPIC_NAME, TestConstant.MESSAGE_TEXT, prop);
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        Mockito.verify(topicMessageListenerSpy, Mockito.timeout(100))
                .topicJmsListenerMethod(messageCaptor.capture());
        TextMessage receivedMessage = messageCaptor.getValue();
        assertEquals(TestConstant.TEST_TENANT_ID, receivedMessage.getStringProperty(CustomHeader.X_PROJECT_ID));
    }

    @Test
    public void testReceiveMessageByJmsRequestInterceptorShouldAddExceptionIntoExceptionHolderIfGetTenantInfoIsDefaultAfterXProjectIdWasSentAsNull() {
        Map<String, Object> prop = new HashMap<>();
        prop.put(CustomHeader.X_PROJECT_ID, null);
        topicJmsTemplate.convertAndSend(TestConstant.TOPIC_NAME, TestConstant.MESSAGE_TEXT, prop);
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        Mockito.verify(topicMessageListenerSpy, Mockito.timeout(200))
                .topicJmsListenerMethod(messageCaptor.capture());
        if (exceptionHolder.isEmpty()) {
            fail();
        }
    }

    private void fail() {
        Exception exception = exceptionHolder.get(0);
        exceptionHolder.clear();
        if (exception != null) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
