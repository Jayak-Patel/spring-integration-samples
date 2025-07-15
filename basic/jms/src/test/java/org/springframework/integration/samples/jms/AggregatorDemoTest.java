/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.jms;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.backoff.FixedBackOff;

/**
 * @author Gunnar Hillert
 * @author Gary Russell
 * @author Artem Bilan
 */
class AggregatorDemoTest extends ActiveMQMultiContextTests {

	private static final String[] configFilesGatewayDemo = {
		"/META-INF/spring/integration/common.xml",
		"/META-INF/spring/integration/aggregation.xml"
	};

	@Test
	void testGatewayDemo() {

		System.setProperty("spring.profiles.active", "testCase");

		final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext(
				configFilesGatewayDemo);
		Map<String, DefaultMessageListenerContainer> containers = applicationContext
				.getBeansOfType(DefaultMessageListenerContainer.class);
		// wait for containers to subscribe before sending a message.
		containers.values().forEach(c -> {
			FixedBackOff backOff = new FixedBackOff(100, 100);
			int n = 0;
			while (n++ < 100 && !c.isRegisteredWithDestination()) {
				try {
					backOff.backOff();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			if (!c.isRegisteredWithDestination()) {
				throw new IllegalStateException("Container failed to subscribe to topic");
			}
		});

		final MessageChannel stdinToJmsOutChannel = applicationContext.getBean("stdinToJmsOutChannel", MessageChannel.class);

		stdinToJmsOutChannel.send(MessageBuilder.withPayload("jms test").build());

		final QueueChannel queueChannel = applicationContext.getBean("queueChannel", QueueChannel.class);

		@SuppressWarnings("unchecked")
		Message<List<String>> reply = (Message<List<String>>) queueChannel.receive(20_000);
		Assertions.assertNotNull(reply);
		List<String> out = reply.getPayload();

		Assertions.assertEquals("[JMS TEST, JMS TEST]", out.toString());

		applicationContext.close();
	}

}
