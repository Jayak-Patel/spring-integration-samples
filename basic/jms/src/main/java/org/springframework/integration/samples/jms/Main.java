/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.jms;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A simple bootstrap main() method for starting a pair of JMS Channel
 * Adapters. Text entered in the console will go through an outbound
 * JMS Channel Adapter from which it is sent to a JMS Destination.
 * An inbound JMS Channel Adapter is listening to that same JMS
 * Destination and will echo the result in the console.
 * <p>
 * See the configuration in the three XML files that are referenced below.
 *
 * @author Mark Fisher
 * @author Gunnar Hillert
 * @author Gary Russell
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private final static String[] configFilesGatewayDemo = {
		"/META-INF/spring/integration/common.xml",
		"/META-INF/spring/integration/inboundGateway.xml",
		"/META-INF/spring/integration/outboundGateway.xml"
	};

	private final static String[] configFilesChannelAdapterDemo = {
		"/META-INF/spring/integration/common.xml",
		"/META-INF/spring/integration/inboundChannelAdapter.xml",
		"/META-INF/spring/integration/outboundChannelAdapter.xml"
	};

	private final static String[] configFilesAggregationDemo = {
		"/META-INF/spring/integration/common.xml",
		"/META-INF/spring/integration/aggregation.xml"
	};

	public static void main(String[] args) {

		final Scanner scanner = new Scanner(System.in);

		LOGGER.info("""

				=========================================================
				                                                         
				    Welcome to the Spring Integration JMS Sample!        
				                                                         
				    For more information please visit:                   
				    https://www.springsource.org/spring-integration/                    
				                                                         
				=========================================================
				""");

		ActiveMqTestUtils.prepare();

		LOGGER.info("\n    Which Demo would you like to run? <enter>:\n");
		LOGGER.info("\t1. Channel Adapter Demo");
		LOGGER.info("\t2. Gateway Demo");
		LOGGER.info("\t3. Aggregation Demo");

		while (true) {
			final String input = scanner.nextLine();

			if("1".equals(input.trim())) {
				LOGGER.info("    Loading Channel Adapter Demo...");
				new ClassPathXmlApplicationContext(configFilesChannelAdapterDemo, Main.class);
				break;
			}
			else if("2".equals(input.trim())) {
				LOGGER.info("    Loading Gateway Demo...");
				new ClassPathXmlApplicationContext(configFilesGatewayDemo, Main.class);
				break;
			}
			else if("3".equals(input.trim())) {
				LOGGER.info("    Loading Aggregation Demo...");
				new ClassPathXmlApplicationContext(configFilesAggregationDemo, Main.class);
				break;
			}
			else {
				LOGGER.info("Invalid choice\n\n");
				LOGGER.info("Enter you choice: ");
			}

		}

		LOGGER.info("    Please type something and hit <enter>\n");
		scanner.close();

	}

}
