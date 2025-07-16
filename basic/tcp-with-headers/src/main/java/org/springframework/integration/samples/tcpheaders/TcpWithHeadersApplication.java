package org.springframework.integration.sts;

import java.io.IOException;

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

	private TcpWithHeadersApplication() { }

	/**
	 * Load the Spring Integration context and start the process.
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		logWelcomeMessage();

		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"META-INF/spring/integration/tcp-header-client-context.xml",
				"META-INF/spring/integration/tcp-header-server-context.xml");

		context.registerShutdownHook();

		terminateOnEnter(context);

		LOGGER.info("Exiting application. Shutting down.");
		context.close();

	}

	private static void logWelcomeMessage() {
		StringBuilder welcomeMessage = new StringBuilder();
		welcomeMessage.append("\n=========================================================\n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("          Welcome to the TCP Headers Sample               \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("    This sample demonstrates message delivery using TCP   \n");
		welcomeMessage.append("    and includes message headers. There are two main     \n");
		welcomeMessage.append("    components; a client and server.                     \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("    The Client sends a message to the server which echos \n");
		welcomeMessage.append("    the message back to the client.                      \n");
		welcomeMessage.append("                                                         \n");
		welcomeMessage.append("=========================================================\n");

		LOGGER.info(welcomeMessage.toString());
	}

	private static void terminateOnEnter(AbstractApplicationContext context) {
		try {
			LOGGER.info("Hit 'Enter' to terminate");
			System.in.read();
		}
		catch (Exception e) {
			LOGGER.error("Error during termination: {}", e.getMessage(), e);
		}
	}

}
