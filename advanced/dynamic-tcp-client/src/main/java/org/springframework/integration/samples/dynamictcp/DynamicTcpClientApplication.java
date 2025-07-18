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

package org.springframework.integration.samples.dynamictcp;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.integration.samples.dynamictcp.domain.Student;
import org.springframework.integration.samples.dynamictcp.service.StudentService;
import org.springframework.util.ErrorHandler;

/**
 * Sample Application Context for demonstrating JPA Outbound Adapter
 *
 * @author Amol Nayak
 * @author Gary Russell
 * @author Michael Wiles
 * @since 2.2
 */
public class Main {

	private static final Log LOGGER = LogFactory.getLog(Main.class);

	private static StudentService studentService;

	private static final String INTEGRATION = "Integration";
	private static final String SPRING = "Spring";
	private static final String MALE = "Male";
	private static final String STUDENT_INFO_FORMAT = "Student Id: {}, First Name: {}, Last Name: {}";

	public static void main(String[] args) {

		AbstractApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/jpa-config.xml");
			context.registerShutdownHook();
			studentService = context.getBean(StudentService.class);

			printWelcomeMessage();
			demoJpaOperations();

		} catch (BeanCreationException bce) {
			LOGGER.error("Failed to create one or more beans, application may not function correctly.", bce);
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception during JPA operations", dae);
		} catch (Exception e) {
			LOGGER.error("Exception during JPA demo operations", e);
		} finally {
			LOGGER.info("JPA Outbound Adapter Demo completed.");
			if (context != null) {
				try {
					context.close();
				} catch (Exception closeException) {
					LOGGER.error("Exception during context closing", closeException);
				}
			}
		}

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
		Student student = createStudent(INTEGRATION, SPRING, MALE);
		Student persisted = null;
		try {
			persisted = studentService.persist(student);
			if (persisted == null) {
				LOGGER.warn("Failed to persist student.");
			} else {
				LOGGER.info("Successfully persisted the student with id '{}'", persisted.getStudentId());
			}
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception while persisting student", dae);
		} catch (Exception e) {
			LOGGER.error("Error persisting student", e);
		}
		return persisted;
	}

	private static Student createStudent(String firstName, String lastName, String gender) {
		Student student = new Student();
		student.setFirstName(firstName);
		student.setLastName(lastName);
		student.setGender(gender);
		return student;
	}

	private static Student retrieveStudent(Student persistedStudent) {
		LOGGER.info("Now lets retrieve the student just persisted");
		if (persistedStudent == null) {
			LOGGER.warn("persistedStudent is null, cannot retrieve.");
			return null;
		}

		Student retrieved = null;
		try {
			retrieved = studentService.find(persistedStudent.getStudentId());
			if (retrieved == null) {
				LOGGER.warn("Could not retrieve student with id '{}'", persistedStudent.getStudentId());
				return null;
			}

			logStudentInfo(retrieved);
			return retrieved;
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception while retrieving student", dae);
			return null;
		} catch (Exception e) {
			LOGGER.error("Error retrieving student", e);
			return null;
		}
	}


	private static void findAllStudents() {
		LOGGER.info("Now lets find all students");
		List<Student> studentList = null;
		try {
			studentList = studentService.findAll();
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception while finding all students", dae);
			return;
		} catch (Exception e) {
			LOGGER.error("Error finding all students", e);
			return;
		}
		printStudentList("All", studentList);
	}

	private static void findStudentsByFirstName(String firstName) {
		LOGGER.info("Now lets find the student with first name {}", firstName);
		List<Student> integrationList = null;
		try {
			integrationList = studentService.findByFirstName(firstName);
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception while finding students by first name", dae);
			return;
		} catch (Exception e) {
			LOGGER.error("Error finding students by first name", e);
			return;
		}
		printStudentList("with first name {0}", integrationList, firstName);
	}

	private static void findAllStudentsAscendingOrder() {
		LOGGER.info("Now lets find all students in ascending order");
		List<Student> studentListAsc = null;
		try {
			studentListAsc = studentService.findAllByOrderByFirstNameAsc();
		} catch (DataAccessException dae) {
			LOGGER.error("Data access exception while finding all students in ascending order", dae);
			return;
		} catch (Exception e) {
			LOGGER.error("Error finding all students in ascending order", e);
			return;
		}
		printStudentList("in ascending order", studentListAsc);
	}

	private static void printStudentList(String description, List<Student> studentList, Object... params) {
		LOGGER.info("Total Students found {} {}", description, (studentList != null ? studentList.size() : 0));
		if (studentList != null && !studentList.isEmpty()) {
			LOGGER.info("Displaying all Students {}", description);
			for (Student s : studentList) {
				logStudentInfo(s);
			}
		} else {
			LOGGER.info("No students found to display.");
		}
	}

	private static void updateStudentLastName(Student retrievedStudent) {
		LOGGER.info("Now lets update the student's last name to {}", INTEGRATION);
		if (retrievedStudent == null) {
			LOGGER.warn("No student to update as retrievedStudent is null");
			return;
		}

		try {
			Student updated = updateStudentLastNameInternal(retrievedStudent, INTEGRATION);

			if (updated == null) {
				LOGGER.warn("Student could not be updated");
			} else {
				logStudentInfo(updated);
			}
		} catch (DataAccessException e) {
			LOGGER.error("Data access exception while updating student's last name", e);
		} catch (Exception e) {
			LOGGER.error("Error updating student", e);
		}
	}

	private static Student updateStudentLastNameInternal(Student student, String lastName) {
		student.setLastName(lastName);
		try {
			return studentService.update(student);
		} catch (DataAccessException e) {
			LOGGER.error("Data access exception during student update", e);
			return null;
		}
		catch (Exception e) {
			LOGGER.error("Error updating student: ", e);
			return null;
		}

	}

	private static void deleteStudent(Student persistedStudent) {
		LOGGER.info("Now lets delete a student with id {}",
				(persistedStudent != null) ? persistedStudent.getStudentId() : "null");

		if (persistedStudent == null) {
			LOGGER.warn("No student to delete, persistedStudent is null");
			return;
		}

		try {
			boolean deleted = studentService.delete(persistedStudent.getStudentId());
			if (deleted) {
				LOGGER.info("Successfully deleted student with id {}", persistedStudent.getStudentId());
			}
			else {
				LOGGER.info("Student could not be deleted");
			}
		} catch (DataAccessException e) {
			LOGGER.error("Data access exception while deleting student", e);
		} catch (Exception e) {
			LOGGER.error("Error deleting student", e);
		}
	}

	private static void logStudentInfo(Student student) {
		if (student == null) {
			LOGGER.warn("Student object is null, cannot log information.");
			return;
		}
		LOGGER.info(STUDENT_INFO_FORMAT,
					student.getStudentId(), student.getFirstName(), student.getLastName());
	}

	private static void demoJpaOperations() {
		LOGGER.info("Starting the JPA Outbound Adapter Demo now...");

		Student persistedStudent = persistStudent();
		Student retrievedStudent = retrieveStudent(persistedStudent);
		findAllStudents();
		findStudentsByFirstName(INTEGRATION);
		findAllStudentsAscendingOrder();
		updateStudentLastName(retrievedStudent);
		deleteStudent(persistedStudent);

	}
}
