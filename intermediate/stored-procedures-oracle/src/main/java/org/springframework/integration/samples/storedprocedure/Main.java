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
public final class MongoDbOutboundAdapterDemo {

	private static final Logger LOGGER = LogManager.getLogger();

	private MongoDbOutboundAdapterDemo() { }

	/**
	 * Load the Spring Integration context and start the process.
	 */
	public static void main(final String... args) {

		LOGGER.info("\n========================================================="
				+ "\n                                                         "
				+ "\n          Welcome to the MongoDB Demo!                     "
				+ "\n                                                         "
				+ "\n    This sample demonstrates how to use the               "
				+ "\n    MongoDB Outbound Adapter.                                "
				+ "\n                                                         "
				+ "\n========================================================="
		);

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		/*final SimpleJdbcTemplate simpleJdbcTemplate = (SimpleJdbcTemplate) context.getBean("simpleJdbcTemplate");

		System.out.println("\nPopulating test table ...");

		simpleJdbcTemplate.update("DELETE FROM EMPLOYEES");

		simpleJdbcTemplate.update("INSERT INTO EMPLOYEES (id, name, salary) VALUES (1, 'John Doe', 1000)");
		simpleJdbcTemplate.update("INSERT INTO EMPLOYEES (id, name, salary) VALUES (2, 'Jane Doe', 2000)");
		simpleJdbcTemplate.update("INSERT INTO EMPLOYEES (id, name, salary) VALUES (3, 'Joe Bloggs', 3000)");

		System.out.println("\nCreated " +
				simpleJdbcTemplate.queryForInt("SELECT COUNT(*) FROM EMPLOYEES") + " records.");*/

		try {
			System.in.read();
		}
		catch (final IOException e) {
			System.err.println(e);
		}

		LOGGER.info("Exiting application. Shutting down context.");
		context.close();

	}

}
