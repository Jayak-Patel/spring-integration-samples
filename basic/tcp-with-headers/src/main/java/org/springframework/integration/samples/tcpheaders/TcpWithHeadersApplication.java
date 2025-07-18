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

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the TCP Client/Server sample with Headers.
 *
 * @author Gary Russell
 * @since 3.0
 */
public final class TcpWithHeadersApplication {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String WHITESPACE = "                                                         \n";


	private TcpWithHeadersApplication() { }

	/**
	 * Load the Spring Integration context and start the process.
	 *
	 * @param ignored command line arguments
	 */
	public static void main(final String... ignored) {

		logWelcomeMessage();

		AbstractApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext(
					"META-INF/spring/integration/tcp-header-client-context.xml",
					"META-INF/spring/integration/tcp-header-server-context.xml");

			context.registerShutdownHook();
			terminateOnEnter();
			LOGGER.info("Exiting application. Shutting down.");

		}
		catch (Exception e) {
			LOGGER.error("Error during application execution: {}", e.getMessage(), e);
		} finally {
			closeContext(context);
		}


	}

	private static void logWelcomeMessage() {
		StringBuilder welcomeMessage = new StringBuilder();
		welcomeMessage.append("\n=========================================================\n")
				.append(WHITESPACE)
				.append("          Welcome to the TCP Headers Sample               \n")
				.append(WHITESPACE)
				.append("    This sample demonstrates message delivery using TCP   \n")
				.append("    and includes message headers. There are two main   