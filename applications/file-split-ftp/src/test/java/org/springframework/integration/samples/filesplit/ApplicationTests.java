package org.springframework.integration.sts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Artem Bilan
 * @author Christian Tzolov
 *
 * @since 5.0
 */
@SpringBootTest
@TestPropertySource(properties = {
		"target.directory=${java.io.tmpdir}/target",
		"output.directory=${java.io.tmpdir}/output",
		"ftp.local.directory=${java.io.tmpdir}/ftpLocalDir" })
public class ApplicationTests {

	private static File ftpLocalDir;

	@Autowired
	@Qualifier("sourceFileOutputChannel")
	private MessageChannel sourceFileOutputChannel;

	@Autowired
	@Qualifier("fileResults")
	private PollableChannel fileResults;

	@Autowired
	@Qualifier("fileSplitter")
	private FileSplitter fileSplitter;

	@BeforeAll
	static void setup() throws IOException {
		ftpLocalDir = new File(System.getProperty("ftp.local.directory"));
		deleteDirectory(ftpLocalDir.toPath());
		assertThat(ftpLocalDir.mkdirs()).isTrue();
	}

	@AfterAll
	static void tearDown() throws IOException {
		deleteDirectory(ftpLocalDir.toPath());
	}

	private static void deleteDirectory(Path path) throws IOException {
		if (Files.exists(path)) {
			Files.walk(path)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
	}

	@Test
	public void testSuccessfulFlow() throws Exception {
		File sourceFile = new File("src/test/resources/data/text-file.txt");
		assertThat(sourceFile).exists();

		this.fileSplitter.setFirstMessageAsHeader(true);
		this.fileSplitter.setMarkersJson(true);

		this.sourceFileOutputChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(sourceFile)
				.build());

		for (int i = 0; i < 3; i++) {
			Message<?> receive = this.fileResults.receive();
			assertThat(receive).isNotNull();
			assertThat(receive.getPayload()).isInstanceOf(File.class);

			File fileResult = (File) receive.getPayload();
			assertThat(fileResult)
					.as("File " + fileResult + " does not exists.")
					.exists();

			assertThat(fileResult.getName())
					.as("File " + fileResult + " has wrong name.")
					.startsWith("text-file.txt");
		}

		Message<?> receive = this.fileResults.receive(10000);
		assertThat(receive).isNull();
	}

}
