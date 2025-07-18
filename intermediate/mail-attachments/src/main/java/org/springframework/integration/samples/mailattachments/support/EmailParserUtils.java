package org.springframework.integration.sts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

/**
 * Utility to parse an email and extract attachments.
 *
 * @author Marius Bogoevici
 * @author Gary Russell
 * @since 3.0
 */
public final class EmailParserUtils {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final String EXCEPTION_DETAILS = "Exception details: ";
	private static final String ATTACHMENT_PROCESSED_MESSAGE = "Attachment processed - temporary file created.";
	private static final String HEADER_RETRIVAL_EXCEPTION = "Exception occurred while getting value for header - check debug level for the header name.";

	private EmailParserUtils() {
		super();
	}

	/**
	 * Parse mail message and create a message per attachment, optionally including the mail message itself.
	 * @param message the message.
	 * @param includeEmail whether to include the email message.
	 * @return the message builder.
	 */
	public static Collection<AbstractIntegrationMessageBuilder<?>> transformEmailMessageToAttachmentMessage(
			Message<MimeMessage> message, boolean includeEmail) {
		Assert.notNull(message, "'message' cannot be null");
		Set<AbstractIntegrationMessageBuilder<?>> messages = new LinkedHashSet<>();
		try {
			MimeMessage mailMessage = message.getPayload();
			Object content = mailMessage.getContent();
			if (content instanceof Multipart multipart) {
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					messages.addAll(transformBodyPartToMessage(bodyPart));
				}
			}
			if (includeEmail) {
				messages.add(MessageBuilder.fromMessage(message));
			}
		}
		catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Fail to transform message due to {} : {}", e.getClass().getSimpleName(),
						e.getMessage());
			}
			throw new IllegalStateException("Could not transform message", e);
		}
		return messages;
	}

	private static Collection<AbstractIntegrationMessageBuilder<?>> transformBodyPartToMessage(BodyPart bodyPart) throws MessagingException {
		String disposition = bodyPart.getDisposition();
		String filename = bodyPart.getFileName();
		if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT)) {

			if (filename == null) {
				filename = "attachment-" + System.currentTimeMillis();
			}

			AbstractIntegrationMessageBuilder<?> messageBuilder;
			Path tempFile = null;
			OutputStream outputStream = null;
			if (bodyPart instanceof MimeBodyPart mimeBodyPart) {
				try (InputStream inputStream = mimeBodyPart.getInputStream()) {
					tempFile = Files.createTempFile("mailAttachments", null);
					File file = tempFile.toFile();
					outputStream = new FileOutputStream(file);
					FileCopyUtils.copy(inputStream, outputStream);
					messageBuilder = MessageBuilder.withPayload(file)
							.setHeader(FileHeaders.FILENAME, file.getName())
							.setHeader(FileHeaders.ORIGINAL_FILENAME, filename);
				}
				catch (IOException e) {
					throw new IllegalStateException("Failed to process attachment", e);
				}
				finally {
					//Do not log the filename as it's user-controlled data.
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(ATTACHMENT_PROCESSED_MESSAGE);
					}
					if (outputStream != null) {
						closeStream(outputStream);
					}

					if (tempFile != null) {
						deleteFile(tempFile);
					}
				}
			}
			else {
				List<String> headerList = null;
				try {
					headerList = (List<String>) ((MimeMessage) (bodyPart.getParent())).getHeaders().get(MailHeaders.HEADERS);
				} catch (MessagingException e) {
					LOGGER.warn(EXCEPTION_DETAILS + "Failed to retrieve headers from MimeMessage", e);
				}
				messageBuilder = MessageBuilder.withPayload(bodyPart)
						.setHeader(FileHeaders.FILENAME, filename);

				if (headerList != null) {
					for (String header : headerList) {
						try {
							//messageBuilder.setHeader(header, (Object) ((MimeMessage) (bodyPart.getParent())).getHeader(header));
							if(LOGGER.isDebugEnabled()){
								LOGGER.debug(HEADER_RETRIVAL_EXCEPTION + " Header Key: "+ header);
							}
						} catch (Exception e) {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn(EXCEPTION_DETAILS + "Exception occurred while getting value for header {}", e.getMessage());
							}
						}
					}
				}


			}
			if (StringUtils.hasText(bodyPart.getContentID())) {
				messageBuilder.setHeader(MailHeaders.CONTENT_ID, bodyPart.getContentID());
			}

			Set<AbstractIntegrationMessageBuilder<?>> result = new LinkedHashSet<>();
			result.add(messageBuilder);
			return result;
		}
		return new LinkedHashSet<>();
	}

	private static void closeStream(OutputStream outputStream) {
		try {
			outputStream.close();
		}
		catch (IOException e) {
			// ignore
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Exception during output stream close: {}", e.getMessage());
			}
		}
	}

	private static void deleteFile(Path tempFile) {
		try {
			Files.deleteIfExists(tempFile);
		}
		catch (IOException e) {
			//ignore
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Exception during temp file deletion: {}", e.getMessage());
			}
		}
	}

}
