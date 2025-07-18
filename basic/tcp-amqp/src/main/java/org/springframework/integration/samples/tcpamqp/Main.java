/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.springsource.org/spring-integration
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.sts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

public final class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private Main() {
		super();
	}

	public static void main(final String... ignored) {

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/jms-context.xml");

		context.registerShutdownHook();

		ConnectionFactory cf = context.getBean(ConnectionFactory.class);

		if (cf instanceof ActiveMQConnectionFactory) {
			((ActiveMQConnectionFactory) cf).setTrustAllPackages(true);
		}

		JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

		JmsSendingMessageHandler gateway = new JmsSendingMessageHandler(jmsTemplate);
		gateway.setDefaultDestinationName("queue");

		LOGGER.info("""

				=========================================================
				                                                         
				          Welcome to Spring Integration JMS Sample     
				                                                         
				    This sample requires a JMS broker. Running the     
				    sample with the default configuration will attempt 
				    to connect to a broker running on localhost:61616. 
				    See the readme file for instructions on starting    
				    a broker.                                           
				                                                         
				=========================================================
				""");

		LOGGER.info("Press 'Enter' to exit.");

		try {
			System.in.read();
		}
		catch (final Exception e) {
			LOGGER.error("Exception", e);
		}

		context.close();
	}

}
