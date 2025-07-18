package org.springframework.integration.sts;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(final String[] args) {

		final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/META-INF/spring/integration/jdbc-config.xml");

		context.registerShutdownHook();

		System.out.println("\n========================================================="
				+ "\n                               Welcome to Spring Integration!"
				+ "\n                                  ***JDBC DEMO***"
				+ "\n    This demo illustrates how to use Spring Integration's JDBC components."
				+ "\n"
				+ "\n    For more information please visit:"
				+ "\n    https://www.springsource.org/spring-integration"
				+ "\n=========================================================\n");

		System.out.println("Please enter some text and press <enter> to store it in database (or 'q' to quit).");

		@SuppressWarnings("resource")
		org.springframework.integration.samples.jdbc.console.ConsoleMessageSourceAdapter console = context.getBean(
				"console", org.springframework.integration.samples.jdbc.console.ConsoleMessageSourceAdapter.class);
		console.start();
	}

}
