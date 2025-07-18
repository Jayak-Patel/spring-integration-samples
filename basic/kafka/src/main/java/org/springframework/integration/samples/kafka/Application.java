/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.integration.sts;

import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.sts.domain.Student;
import org.springframework.integration.sts.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample Application Context for demonstrating JPA Outbound Adapter
 *
 * @author Amol Nayak
 * @author Gary Russell
 * @author Michael Wiles
 * @since 2.2
 */
public final class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static StudentService studentService;

	private static final String INTEGRATION = "Integration";
	private static final String SPRING = "Spring";
	private static final String MALE = "Male";
	private static final String STUDENT_INFO_FORMAT = "Student Id: {}, First Name: {}, Last Name: {}";

	private Main() {
		// Prevent instantiation
	}

	public static void main(String... ignored) {

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/jpa-config.xml");

		context.registerShutdownHook();

		studentService = context.getBean(StudentService.class);

		printWelcomeMessage();

		LOGGER.info("Starting the JPA Outbound Adapter Demo now");

		Student persistedStudent = persistStudent();
		Student retrievedStudent = retrieveStudent(persistedStudent);
		findAllStudents();
		findStudentsByFirstName(INTEGRATION);
		findAllStudentsAscendingOrder();
		Student updatedStudent = updateStudentLastName(retrievedStudent); // Assign the updated student
		//TODO Make sure to use updatedStudent in the following steps to verify the last name was updated successfully.
		if (updatedStudent != null) {
			deleteStudent(updatedStudent);
		}
		else {
			LOGGER.warn("Skipping delete operation because updatedStudent is null");
		}


		context.close();

	}

	private static void printWelcomeMessage() {
		LOGGER.info("""

				=========================================================
				          Welcome to Spring Integration JPA Sample
				    For more information please visit:
				    https://www.springsource.org/spring-integration
				=========================================================
				""");
	}

	private static Student persistStudent() {
		LOGGER.info("Lets persist a student");
		Student student = createStudent();
		Student persisted = studentService.persist(student);

		if (persisted != null) {
			LOGGER.info("Successfully persisted the student with id '{}'", persisted.getStudentId());
		}
		return persisted;
	}

	private static Student createStudent() {
		Student student = new Student();
		student.setFirstName(INTEGRATION);
		student.setLastName(SPRING);
		student.setGender(MALE);
		return student;
	}

	private static Student retrieveStudent(Student persistedStudent) {
		LOGGER.info("Now lets retrieve the student just persisted");
		Student retrieved = null;
		if (persistedStudent != null) {
			retrieved = studentService.find(persistedStudent.getStudentId());
		}

		if (retrieved != null) {
			logStudentInfo("Successfully retrieved student", retrieved);
		} else {
			LOGGER.info("Student could not be retrieved");
		}
		return retrieved;
	}


	private static void findAllStudents() {
		LOGGER.info("Now lets find all students");
		List<Student> studentList = studentService.findAll();
		printStudentList(studentList);
	}

	private static void findStudentsByFirstName(String firstName) {
		LOGGER.info("Now lets find the student with first name {}", firstName);
		List<Student> integrationList = studentService.findByFirstName(firstName);
		printStudentList(integrationList, firstName);
	}

	private static void findAllStudentsAscendingOrder() {
		LOGGER.info("Now lets find all students in ascending order");
		List<Student> studentListAsc = studentService.findAllByOrderByFirstNameAsc();
		printStudentList("in ascending order", studentListAsc);
	}

	private static Student updateStudentLastName(Student retrievedStudent) {
		LOGGER.info("Now lets update the student's last name to {}", INTEGRATION);
		Student updated = null;
		if (retrievedStudent != null) {
			updated = updateStudentLastNameInternal(retrievedStudent);

		} else {
			LOGGER.warn("No student to update as retrievedStudent is null");
			return null;
		}

		if (updated != null) {
			logStudentInfo("Successfully updated student", updated);
		} else {
			LOGGER.info("Student could not be updated");
		}
		return updated;
	}

	private static Student updateStudentLastNameInternal(Student student) {
		student.setLastName(INTEGRATION);
		return studentService.update(student);
	}

	private static void deleteStudent(Student updatedStudent) {
		if(updatedStudent == null){
			LOGGER.warn("No student to delete, updatedStudent is null");
			return;
		}
		LOGGER.info("Now lets delete a student with id {}", updatedStudent.getStudentId());
		boolean deleted = studentService.delete(updatedStudent.getStudentId());
		if (deleted) {
			LOGGER.info("Successfully deleted student with id {}", updatedStudent.getStudentId());
		} else {
			LOGGER.info("Student could not be deleted");
		}
	}

	private static void printStudentList(List<Student> studentList) {
		printStudentList("All", studentList);
	}

	private static void printStudentList(String description, List<Student> studentList, Object... params) {
		LOGGER.info("Total Students found for {}: {}", description, (studentList != null ? studentList.size() : 0));
		if (studentList != null && !studentList.isEmpty()) {
			LOGGER.info("Displaying all {}", description);
			for (Student s : studentList) {
				logStudentInfo(s);
			}
		} else {
			LOGGER.info("No students found to display.");
		}
	}

	private static void logStudentInfo(Student student) {
		if (student != null) {
			LOGGER.info(STUDENT_INFO_FORMAT,
					student.getStudentId(), student.getFirstName(), student.getLastName());
		} else {
			LOGGER.warn("Student object is null, cannot log information.");
		}
	}
}
