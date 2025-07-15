/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.dynamictcp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.Assert;

@SpringBootApplication
@EnableMessageHistory
public class DynamicTcpClientApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTcpClientApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DynamicTcpClientApplication.class, args);
		ToTCP toTcp = context.getBean(ToTCP.class);
		toTcp.send("foo", "localhost", 1234);
		toTcp.send("foo", "localhost", 5678);
		QueueChannel outputChannel = context.getBean("outputChannel", QueueChannel.class);
		LOGGER.info(outputChannel.receive(10000).toString());
		LOGGER.info(outputChannel.receive(10000).toString());
		context.close();
	}

	// Client side

	@MessagingGateway(defaultRequestChannel = "toTcp.input")
	public interface ToTCP {

		void send(String data, @Header("host") String host, @Header("port") int port);

	}

	@Bean
	public IntegrationFlow toTcp() {
		return f -> f.route(new TcpRouter());
	}

	// Two servers

	@Bean
	public TcpNetServerConnectionFactory cfOne() {
		return new TcpNetServerConnectionFactory(1234);
	}

	@Bean
	public TcpReceivingChannelAdapter inOne(TcpNetServerConnectionFactory cfOne) {
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(cfOne);
		adapter.setOutputChannel(outputChannel());
		return adapter;
	}

	@Bean
	public TcpNetServerConnectionFactory cfTwo() {
		return new TcpNetServerConnectionFactory(5678);
	}

	@Bean
	public TcpReceivingChannelAdapter inTwo(TcpNetServerConnectionFactory cfTwo) {
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(cfTwo);
		adapter.setOutputChannel(outputChannel());
		return adapter;
	}

	@Bean
	public QueueChannel outputChannel() {
		return new QueueChannel();
	}

	public static class TcpRouter extends AbstractMessageRouter {

		private static final Logger LOGGER = LoggerFactory.getLogger(TcpRouter.class);

		private static final int MAX_CACHED = 10; // When this is exceeded, we remove the LRU.

		@SuppressWarnings("serial")
		private final LinkedHashMap<String, MessageChannel> subFlows =
				new LinkedHashMap<String, MessageChannel>(MAX_CACHED, .75f, true) {

					@Override
					protected boolean removeEldestEntry(Entry<String, MessageChannel> eldest) {
						if (size() > MAX_CACHED) {
							removeSubFlow(eldest);
							return true;
						}
						else {
							return false;
						}
					}

				};

		@Autowired
		private IntegrationFlowContext flowContext;

		@Override
		protected synchronized Collection<MessageChannel> determineTargetChannels(Message<?> message) {
			String host = message.getHeaders().get("host", String.class);
			Integer port = message.getHeaders().get("port", Integer.class);
			String hostPort = host + port;

			MessageChannel channel = this.subFlows.get(hostPort);

			if (channel == null) {
				channel = createNewSubflow(message);
			}

			return Collections.singletonList(channel);
		}

		private MessageChannel createNewSubflow(Message<?> message) {
			String host = (String) message.getHeaders().get("host");
			Integer port = (Integer) message.getHeaders().get("port");
			Assert.state(host != null && port != null, "host and/or port header missing");
			String hostPort = host + port;

			TcpNetClientConnectionFactory cf = new TcpNetClientConnectionFactory(host, port);
			TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
			handler.setConnectionFactory(cf);
			// Check if the connection factory is already started before starting it.
			if (!cf.isRunning()) {
				cf.start();
			}

			IntegrationFlow flow = f -> f.handle(handler);

			IntegrationFlowContext.IntegrationFlowRegistration flowRegistration =
					this.flowContext.registration(flow)
							.addBean(cf)
							.id(String.format("%s.flow", hostPort))
							.register();
			MessageChannel inputChannel = flowRegistration.getInputChannel();
			this.subFlows.put(hostPort, inputChannel);
			LOGGER.info("Created new subflow for hostPort: {}", hostPort);
			return inputChannel;
		}

		private void removeSubFlow(Entry<String, MessageChannel> eldest) {
			String hostPort = eldest.getKey();
			this.flowContext.remove(String.format("%s.flow", hostPort));
			LOGGER.info("Removed subflow for hostPort: {}", hostPort);
		}

	}

}
