/*
 * Copyright 2016-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.dsl.kafka;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Gary Russell
 * @author Artem Bilan
 *
 * @since 4.3
 */
@SpringBootApplication
@EnableConfigurationProperties(KafkaAppProperties.class)
public class Application {

	private static final Log LOGGER = LogFactory.getLog(Application.class);

	private final KafkaAppProperties properties;

	private final IntegrationFlowContext flowContext;

	private final KafkaProperties kafkaProperties;

	private final KafkaGateway kafkaGateway;

	@Autowired
	public Application(KafkaAppProperties properties, IntegrationFlowContext flowContext,
			KafkaProperties kafkaProperties) {
		this.properties = properties;
		this.flowContext = flowContext;
		this.kafkaProperties = kafkaProperties;
		this.kafkaGateway = createKafkaGateway();
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context =
				new SpringApplicationBuilder(Application.class)
						.web(WebApplicationType.NONE)
						.run(args);
		Application app = context.getBean(Application.class);
		app.runDemo(context);
		context.close();
	}

	private void runDemo(ConfigurableApplicationContext context) {
		LOGGER.info("Sending 10 messages...");
		for (int i = 0; i < 10; i++) {
			String message = "foo" + i;
			LOGGER.info("Send to Kafka: " + message);
			kafkaGateway.sendToKafka(message, properties.getTopic());
		}

		for (int i = 0; i < 10; i++) {
			Message<?> received = kafkaGateway.receiveFromKafka();
			LOGGER.info("Received: " + received);
		}
		LOGGER.info("Adding an adapter for a second topic and sending 10 messages...");
		addAnotherListenerForTopics(properties.getNewTopic());
		for (int i = 0; i < 10; i++) {
			String message = "bar" + i;
			LOGGER.info("Send to Kafka: " + message);
			kafkaGateway.sendToKafka(message, properties.getNewTopic());
		}
		for (int i = 0; i < 10; i++) {
			Message<?> received = kafkaGateway.receiveFromKafka();
			LOGGER.info("Received: " + received);
		}
		context.close();
	}

	@Component
	@MessagingGateway
	public interface KafkaGateway {

		@Gateway(requestChannel = "toKafka.input")
		void sendToKafka(String payload, @Header(KafkaHeaders.TOPIC) String topic);

		@Gateway(replyChannel = "fromKafka", replyTimeout = 10000)
		Message<?> receiveFromKafka();

	}

	@Bean
	public KafkaGateway createKafkaGateway() {
		return new KafkaGateway() {
			@Override
			public void sendToKafka(String payload, @Header(KafkaHeaders.TOPIC) String topic) {
				// Implementation will be provided by Spring Integration at runtime
			}

			@Override
			public Message<?> receiveFromKafka() {
				// Implementation will be provided by Spring Integration at runtime
				return null;
			}
		};
	}

	@Bean
	public IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
		return f -> f
				.handle(Kafka.outboundChannelAdapter(kafkaTemplate)
						.messageKey(properties.getMessageKey()));
	}

	@Bean
	public IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
		return IntegrationFlow
				.from(Kafka.messageDrivenChannelAdapter(consumerFactory, properties.getTopic()))
				.channel(c -> c.queue("fromKafka"))
				.get();
	}

	/*
	 * Boot's autoconfigured KafkaAdmin will provision the topics.
	 */

	@Bean
	public NewTopic topic(KafkaAppProperties properties) {
		return new NewTopic(properties.getTopic(), 1, (short) 1);
	}

	@Bean
	public NewTopic newTopic(KafkaAppProperties properties) {
		return new NewTopic(properties.getNewTopic(), 1, (short) 1);
	}

	public void addAnotherListenerForTopics(String... topics) {
		Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties(null);
		// change the group id, so we don't revoke the other partitions.
		consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,
				consumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG) + "x");
		IntegrationFlow flow =
				IntegrationFlow
						.from(Kafka.messageDrivenChannelAdapter(
								new DefaultKafkaConsumerFactory<String, String>(consumerProperties), topics))
						.channel("fromKafka")
						.get();
		this.flowContext.registration(flow).register();
	}

}
