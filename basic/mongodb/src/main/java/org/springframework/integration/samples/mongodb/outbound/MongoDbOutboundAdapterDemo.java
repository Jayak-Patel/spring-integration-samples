/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.samples.mongodb.outbound;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class.
 *
 * @author Gary Russell
 * @author Artem Bilan
 * @since 4.2
 *
 */
public final class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private Main() {
		super();
	}

	public static void main(final String... ignored) {
		LOGGER.info("Starting application");

		final ConfigurableApplicationContext context =
				new ClassPathXmlApplicationContext(
						"META-INF/spring/integration/spring-integration-context.xml");

		context.registerShutdownHook();

		try {
			LOGGER.info("""
					=========================================================
					                                                         
					          Press 'Enter' to terminate the application.
					                                                         
					=========================================================
					""");

			new Scanner(System.in).nextLine();
		}
		catch (final IOException e) {
			LOGGER.error("Exception details: {}", e.getMessage(), e);
		}

		LOGGER.info("Exiting application. Shutting down context.");
		context.close();
	}

}
