package org.springframework.integration.sts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the TCP Client/Server sample.
 *
 * @author Gary Russell
 * @since 2.0
 */
public final class Main {

	private static final Log LOGGER = LogFactory.getLog(Main.class);

	private Main() { }

	/**
	 * Load the Spring Integration context and start the process.
	 *
	 * @param ignored command line arguments
	 */
	public static void main(final String... ignored) {

		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		LOGGER.info("Hit 'Enter' to terminate");

		try {
			System.in.read();
		}
		catch (Exception e) {
			LOGGER.error("Exception", e);
		}

		context.close();
	}

}
