/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.sts;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.samples.jpa.domain.Student;
import org.springframework.integration.samples.jpa.service.StudentService;

/**
 * Sample Application Context for demonstrating JPA Outbound Adapter
 *
 * @author Amol Nayak
 * @author Gary Russell
 * @author Michael Wiles
 * @since 2.2
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/jpa-config.xml");

		context.registerShutdownHook();

		StudentService studentService = context.getBean(StudentService.class);

		LOGGER.info("\n========================================================="
				+ "\n          Welcome to Spring Integration JPA Sample"
				+ "\n    For more information please visit:"
				+ "\n    https://www.springsource.org/spring-integration"
				+ "\n=========================================================");


		LOGGER.info("\nStarting the JPA Outbound Adapter Demo now, "
				+ "\nplease wait for a while to see the results .....\n");

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Lets persist a student");
		Student student = new Student();
		student.setFirstName("Integration");
		student.setLastName("Spring");
		student.setGender("Male");

		Student persisted = null;
		if (student != null) {
			persisted = studentService.persist(student);
		}

		if (persisted != null) {
			LOGGER.info("Successfully persisted the student with id '{}'",
					persisted.getStudentId());
		}

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets retrieve the student just persisted");
		Student retrieved = null;
		if (persisted != null) {
			retrieved = studentService.find(persisted.getStudentId());
		}

		if (retrieved != null) {
			LOGGER.info("Successfully retrieved student with id '{}' having first name '{}' and last name '{}'",
					retrieved.getStudentId(), retrieved.getFirstName(), retrieved.getLastName());
		}
		else {
			LOGGER.info("Student could not be retrieved");
		}

		//Give some time for the flow to complete processing and then retrieve the student
		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets find all students");
		List<Student> studentList = studentService.findAll();
		LOGGER.info("Total Students found {}", (studentList != null ? studentList.size() : 0));
		if (studentList != null && studentList.size() > 0) {
			LOGGER.info("Displaying all Students");
			for (Student s : studentList) {
				LOGGER.info("Student Id: {}, First Name: {}, Last Name: {}",
						s.getStudentId(), s.getFirstName(), s.getLastName());
			}
		}

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets find the student with first name Integration");
		List<Student> integrationList = studentService.findByFirstName("Integration");
		LOGGER.info("Total Students found with first name Integration {}", (integrationList != null ? integrationList.size() : 0));
		if (integrationList != null && integrationList.size() > 0) {
			LOGGER.info("Displaying all Students with first name Integration");
			for (Student s : integrationList) {
				LOGGER.info("Student Id: {}, First Name: {}, Last Name: {}",
						s.getStudentId(), s.getFirstName(), s.getLastName());
			}
		}

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets find all students in ascending order");
		List<Student> studentListAsc = studentService.findAllByOrderByFirstNameAsc();
		LOGGER.info("Total Students found {}", (studentListAsc != null ? studentListAsc.size() : 0));
		if (studentListAsc != null && studentListAsc.size() > 0) {
			LOGGER.info("Displaying all Students in ascending order");
			for (Student s : studentListAsc) {
				LOGGER.info("Student Id: {}, First Name: {}, Last Name: {}",
						s.getStudentId(), s.getFirstName(), s.getLastName());
			}
		}

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets update the student's last name to Integration");
		Student updated = null;
		if (retrieved != null) {
			retrieved.setLastName("Integration");
			updated = studentService.update(retrieved);
		}

		if (updated != null) {
			LOGGER.info("Successfully updated student with id '{}' having first name '{}' and last name '{}'",
					updated.getStudentId(), updated.getFirstName(), updated.getLastName());
		}
		else {
			LOGGER.info("Student could not be updated");
		}

		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		LOGGER.info("Now lets delete a student with id {}", (persisted != null ? persisted.getStudentId() : "null"));
		if (persisted != null) {
			if (studentService.delete(persisted.getStudentId())) {
				LOGGER.info("Successfully deleted student with id {}", persisted.getStudentId());
			}
			else {
				LOGGER.info("Student could not be deleted");
			}
		}

		context.close();

	}
}
