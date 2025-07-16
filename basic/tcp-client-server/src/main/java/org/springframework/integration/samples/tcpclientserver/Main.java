package org.springframework.integration.samples.tcpclientserver;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the TCP Client/Server sample.
 *
 * @author Gary Russell
 * @since 2.0
 */
public final class Main {

	private Main() { }

	/**
	 * Load the Spring Integration context and start the process.
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		System.out.println("Hit 'Enter' to terminate");

		try {
			System.in.read();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		context.close();
	}

}
