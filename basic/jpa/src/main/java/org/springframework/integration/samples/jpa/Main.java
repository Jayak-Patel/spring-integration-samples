/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.jpa;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.samples.jpa.domain.Person;
import org.springframework.integration.samples.jpa.service.PersonService;
import org.springframework.util.StringUtils;

/**
 * Starts the Spring Context and will initialize the Spring Integration routes.
 *
 * @author Gunnar Hillert
 * @author Amol Nayak
 * @author Gary Russell
 * @author Artem Bilan
 * @version 1.0
 *
 */
@SpringBootApplication(exclude = JpaRepositoriesAutoConfiguration.class)
@ImportResource("spring-integration-context.xml")
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		final Scanner scanner = new Scanner(System.in);

		LOGGER.info("""

				=========================================================
				                                                         
				    Welcome to the Spring Integration JPA Sample!        
				                                                         
				    For more information please visit:                   
				    https://www.springsource.org/spring-integration/                    
				                                                         
				=========================================================
				""");

		ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class)
				.web(WebApplicationType.NONE)
				.run(args);

		final PersonService personService = context.getBean(PersonService.class);

		LOGGER.info("Please enter a choice and press <enter>: ");
		LOGGER.info("\t1. List all people");
		LOGGER.info("\t2. Create a new person");
		LOGGER.info("\tq. Quit the application");
		LOGGER.info("Enter you choice: ");

		while (true) {
			final String input = scanner.nextLine();

			if ("1".equals(input.trim())) {
				findPeople(personService);
			}
			else if ("2".equals(input.trim())) {
				createPersonDetails(scanner, personService);
			}
			else if ("q".equals(input.trim())) {
				break;
			}
			else {
				LOGGER.info("Invalid choice\n\n");
			}

			LOGGER.info("Please enter a choice and press <enter>: ");
			LOGGER.info("\t1. List all people");
			LOGGER.info("\t2. Create a new person");
			LOGGER.info("\tq. Quit the application");
			LOGGER.info("Enter you choice: ");
		}

		LOGGER.info("Exiting application...bye.");
		context.close();
		System.exit(0);

	}

	private static void createPersonDetails(final Scanner scanner, PersonService service) {
		while (true) {
			LOGGER.info("\nEnter the Person's name:");
			String name = null;

			while (true) {

				name = scanner.nextLine();

				if (StringUtils.hasText(name)) {
					break;
				}

				LOGGER.info("No text entered....Please enter a name:");

			}

			Person person = new Person();
			person.setName(name);
			person = service.createPerson(person);
			LOGGER.info("Created person record with id: " + person.getId());
			LOGGER.info("Do you want to create another person? (y/n)");
			String choice = scanner.nextLine();

			if (!"y".equalsIgnoreCase(choice)) {
				break;
			}
		}
	}

	private static void findPeople(final PersonService service) {

		LOGGER.info("ID            NAME         CREATED");
		LOGGER.info("==================================");

		final List<Person> people = service.findPeople();

		if (people != null && !people.isEmpty()) {
			for (Person person : people) {
				LOGGER.info(String.format("%d, %s, ", person.getId(), person.getName()));
				LOGGER.info(DATE_FORMAT.format(person.getCreatedDateTime()));//NOSONAR
			}
		}
		else {
			LOGGER.info(
					String.format("No Person record found."));
		}

		LOGGER.info("==================================\n\n");
	}

}
