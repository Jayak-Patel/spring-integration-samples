/*
 * Copyright 2018-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.tcpbroadcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.dsl.TcpNetServerConnectionFactorySpec;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnectionServerListeningEvent;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TcpBroadcastApplication {

	private static final Log LOGGER = LogFactory.getLog(TcpBroadcastApplication.class);
	private static final int PORT = 1234;
	private static final String DEFAULT_MESSAGE = "Default Broadcast Message";

	@Configuration
	public static class Config {

		private final CountDownLatch listenLatch = new CountDownLatch(1);

		/*
		 * Server connection factory.
		 */
		@Bean
		public TcpNetServerConnectionFactorySpec serverFactory() {
			return Tcp.netServer(PORT);
		}

		/*
		 * Inbound adapter - sends "connected!".
		 */
		@Bean
		public IntegrationFlow tcpServer(AbstractServerConnectionFactory serverFactory) {
			return IntegrationFlow.from(Tcp.inboundAdapter(serverFactory))
					.transform(p -> "connected!")
					.channel("toTcp.input")
					.get();
		}

		/*
		 * Gateway flow for controller.
		 */
		@Bean
		public IntegrationFlow gateway() {
			return IntegrationFlow.from(Sender.class)
					.channel("toTcp.input")
					.get();
		}

		/*
		 * Outbound channel adapter flow.
		 */
		@Bean
		public IntegrationFlow toTcp(AbstractServerConnectionFactory serverFactory) {
			return f -> f.handle(Tcp.outboundAdapter(serverFactory));
		}

		/*
		 * Executor for clients.
		 */
		@Bean
		public ThreadPoolTaskExecutor exec() {
			ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
			exec.setCorePoolSize(5);
			return exec;
		}

		/*
		 * Wait for server to start listening and start 5 clients.
		 */
		@Bean
		public ApplicationRunner runner(TaskExecutor exec, Broadcaster caster) {
			return args -> {
				if (!this.listenLatch.await(10, TimeUnit.SECONDS)) {
					throw new IllegalStateException("Failed to start listening");
				}
				IntStream.range(1, 6).forEach(i -> exec.execute(new Client()));
			};
		}

		@EventListener
		public void serverStarted(TcpConnectionServerListeningEvent event) {
			this.listenLatch.countDown();
		}

	}

	/*
	 * Sender gateway sets the connection id header.
	 */
	public interface Sender {

		void send(String payload, @Header(IpHeaders.CONNECTION_ID) String connectionId);

	}

	@RestController
	public static class Controller {

		private final Broadcaster broadcaster;
		private final ConfigurableApplicationContext applicationContext;

		public Controller(Broadcaster broadcaster, ConfigurableApplicationContext applicationContext) {
			this.broadcaster = broadcaster;
			this.applicationContext = applicationContext;
		}

		@PostMapping("/broadcast")
		public String broadcast() {
			broadcaster.send(DEFAULT_MESSAGE);
			return "sent: " + DEFAULT_MESSAGE;
		}

		@GetMapping("/shutdown")
		public void shutDown() {
			applicationContext.close();
		}

	}

	@Component
	@DependsOn("gateway") // Needed to ensure the gateway flow bean is created first
	public static class Broadcaster {

		private final Sender sender;
		private final AbstractServerConnectionFactory server;

		public Broadcaster(Sender sender, AbstractServerConnectionFactory server) {
			this.sender = sender;
			this.server = server;
		}

		/**
		 * Sends the broadcast message to all connected clients.
		 * @param what The broadcast message to send.
		 */
		public void send(String what) {
			server.getOpenConnectionIds().forEach(cid -> sender.send(what, cid));
		}

	}

	public static class Client implements Runnable {

		private static final ByteArrayCrLfSerializer deserializer = new ByteArrayCrLfSerializer();

		private static int next;

		private final int instance;

		public Client() {
			this.instance = ++next;
		}

		@Override
		public void run() {
			Socket socket = null;
			try {
				socket = SocketFactory.getDefault().createSocket("localhost", PORT);
				socket.getOutputStream().write("hello\r\n".getBytes());
				InputStream is = socket.getInputStream();
				readMessages(is, instance);
			}
			catch (IOException e) {
				LOGGER.error("Error occurred while creating or writing to socket", e);
			}
			finally {
				if (socket != null) {
					try {
						socket.close();
					}
					catch (IOException e) {
						LOGGER.error("Error while closing socket", e);
					}
				}
			}
		}

		private void readMessages(InputStream is, int instance) {
			boolean running = true;
			while (running) {
				try {
					byte[] data = deserializer.deserialize(is);
					if (data != null) {
						String receivedMessage = new String(data);
						LOGGER.info(receivedMessage + " from client# " + instance);
					} else {
						LOGGER.warn("Received null data from the server.");
						running = false;
					}
				} catch (IOException e) {
					LOGGER.error("Error while reading or deserializing message from socket", e);
					running = false;
				}
			}
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(TcpBroadcastApplication.class, args);
	}

}
