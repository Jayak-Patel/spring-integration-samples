/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.integration.samples.sftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.sshd.sftp.client.SftpClient;
import org.junit.jupiter.api.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates use of the outbound gateway to use ls, get and rm.
 * Creates a temporary directory with 2 files; retrieves and removes them.
 *
 * @author Gary Russell
 * @author Artem Bilan
 *
 * @since 2.1
 *
 */
public class SftpOutboundGatewaySampleTests {

	@Test
	public void testLsGetRm() {
		ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classpath:/META-INF/spring/integration/SftpOutboundGatewaySample-context.xml");
		ToSftpFlowGateway toFtpFlow = ctx.getBean(ToSftpFlowGateway.class);
		RemoteFileTemplate<SftpClient.DirEntry> template = null;
		String file1 = "1.ftptest";
		String file2 = "2.ftptest";
		Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

		try {
			// remove the previous output files if necessary
			deleteFileIfExists(tmpDir.resolve(file1));
			deleteFileIfExists(tmpDir.resolve(file2));

			@SuppressWarnings("unchecked")
			SessionFactory<SftpClient.DirEntry> sessionFactory = ctx.getBean(CachingSessionFactory.class);
			template = new RemoteFileTemplate<>(sessionFactory);
			SftpTestUtils.createTestFiles(template, file1, file2);

			// execute the flow (ls, get, rm, aggregate results)
			List<Boolean> rmResults = toFtpFlow.lsGetAndRmFiles("si.sftp.sample");

			// Check everything went as expected, and clean up
			assertThat(rmResults).hasSize(2);
			for (Boolean result : rmResults) {
				assertThat(result).isTrue();
			}

		}
		finally {
			SftpTestUtils.cleanUp(template, file1, file2);
			ctx.close();
			deleteFileIfExists(tmpDir.resolve(file1));
			deleteFileIfExists(tmpDir.resolve(file2));
		}
	}

	private static void deleteFileIfExists(Path path) {
		try {
			Files.deleteIfExists(path);
		}
		catch (IOException e) {
			// Handle the exception appropriately
		}
	}

}