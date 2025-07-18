package org.springframework.integration.sts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 *
 * @author Gunnar Hillert
 * @since 2.2
 *
 */
public final class Main {

	private static final Logger LOGGER = LogManager.getLogger();

	private Main() { }

	/**
	 * Load the Spring Integration context and start the process.
	 */
	public static void main(final String... args) {

		LOGGER.info("\n========================================================="
				+ "\n                                                         "
				+ "\n          Welcome to the Stored Procedure Sample!           "
				+ "\n                                                         "
				+ "\n    This sample demonstrates how to call four different    "
				+ "\n    stored procedures:                                      "
				+ "\n                                                         "
				+ "\n        1. A Simple Stored Procedure Call                 "
				+ "\n        2. A Stored Procedure Output Parameter              "
				+ "\n        3. A Stored Procedure Returning a ResultSet         "
				+ "\n        4. A Stored Procedure with a Poller               "
				+ "\n                                                         "
				+ "\n========================================================="
		);

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();
		context.getBean("storedProcedureTest");

		LOGGER.info("Hit 'Enter' to terminate");

		try {
			System.in.read();
		}
		catch (final IOException e) {
			LOGGER.error("Exception details:" ,e);
		}

		LOGGER.info("Exiting application. Shutting down context.");
		context.close();

	}

}
