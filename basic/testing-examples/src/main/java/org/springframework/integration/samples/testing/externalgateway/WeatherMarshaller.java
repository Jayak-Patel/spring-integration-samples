/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.tcpheaders;

import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.MessageConvertingTcpMessageMapper;
import org.springframework.integration.ip.tcp.serializer.MapJsonSerializer;
import org.springframework.integration.support.converter.MapMessageConverter;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class TcpWithHeadersApplication {

	private static final Log LOGGER = LogFactory.getLog(TcpWithHeadersApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TcpWithHeadersApplication.class, args);
	}

	// Client side

	interface TcpExchanger {

		String exchange(String data, @Header("type") String type);

	}

	@Bean
	IntegrationFlow client(@Value("${tcp.port:1234}") int port,
							@Value("${tcp.host:localhost}") String host,
							@Value("${tcp.client.connection.factory.name:tcpClientCF}") String clientConnectionFactoryName) {

		return IntegrationFlow.from(TcpExchanger.class)
				.handle(Tcp.outboundGateway(Tcp.netClient(host, port)
						.id(clientConnectionFactoryName)
						.deserializer(jsonMapping())
						.serializer(jsonMapping())
						.mapper(mapper())))
				.get();
	}

	// Server side

	@Bean
	IntegrationFlow server(@Value("${tcp.port:1234}") int port) {
		return IntegrationFlow.from(Tcp.inboundGateway(Tcp.netServer(port)
						.deserializer(jsonMapping())
						.serializer(jsonMapping())
						.mapper(mapper())))
				.log(Level.INFO, "exampleLogger", "'Received type header:' + headers['type']")
				.route("headers['type']", r -> r
						.subFlowMapping("upper",
								subFlow -> subFlow.transform(String.class, String::toUpperCase))
						.subFlowMapping("lower",
								subFlow -> subFlow.transform(String.class, String::toLowerCase)))
				.get();
	}

	// Common

	@Bean
	MessageConvertingTcpMessageMapper mapper() {
		MapMessageConverter converter = new MapMessageConverter();
		converter.setHeaderNames("type");
		return new MessageConvertingTcpMessageMapper(converter);
	}

	@Bean
	MapJsonSerializer jsonMapping() {
		return new MapJsonSerializer();
	}

	// Console

	@Bean
	@DependsOn("client")
	ApplicationRunner runner(TcpExchanger exchanger,
			ConfigurableApplicationContext context) {

		return args -> {
			LOGGER.info("""
					Enter some text; if it starts with a lower case character,
					it will be upper-cased by the server; otherwise it will be lower-cased;
					enter 'quit' to end""");
			processInputAndCloseContext(exchanger, context);
		};
	}

	private void processInputAndCloseContext(TcpExchanger exchanger, ConfigurableApplicationContext context) {
		try (Scanner scanner = new Scanner(System.in)) {
			processInput(exchanger, scanner);
		}
		finally {
			if (context != null) {
				context.close();
			}
		}
	}

	private void processInput(TcpExchanger exchanger, Scanner scanner) {
		String request = getNextRequest(scanner);
		while (!"quit".equalsIgnoreCase(request)) {
			if (StringUtils.hasText(request)) {
				String result = exchanger.exchange(request,
						Character.isLowerCase(request.charAt(0)) ? "upper" : "lower");
				LOGGER.info("Result from server: " + result);
			}
			request = getNextRequest(scanner);
		}
	}

	private String getNextRequest(Scanner scanner) {
		if (scanner.hasNextLine()) {
			return scanner.nextLine();
		}
		else {
			return "quit";
		}
	}

}
