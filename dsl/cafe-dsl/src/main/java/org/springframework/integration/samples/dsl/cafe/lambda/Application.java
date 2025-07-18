package org.springframework.integration.samples.dsl.cafe.lambda;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
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
import org.springframework.util.Assert;
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
	private static final String ATTACHMENT_PROCESSED = "Attachment processed";
	private static final String ATTACHMENT = "attachment-";
	private static final String FAIL_TO_TRANSFORM_MESSAGE_DUE_TO = "Fail to transform message due to {} : {}";

	private EmailParserUtils() {
		super();
	}

	/**
	 * Parse mail message and create a message per attachment, optionally including the mail message itself.
	 * @param message the message.
	 * @param includeEmail whether to include the email message.
	 * @return the message builder.
	 */
	public static Collection<AbstractIntegrationMessageBuilder<File>> transformEmailMessageToAttachmentMessage(
			Message<MimeMessage> message, boolean includeEmail) {
		Assert.notNull(message, "'message' cannot be null");
		Set<AbstractIntegrationMessageBuilder<File>> messages = new LinkedHashSet<>();
		try {
			MimeMessage mailMessage = message.getPayload();
			Object content = mailMessage.getContent();
			if (content instanceof Multipart multipart) {
				processMultipart(multipart, message, messages);
			}
			if (includeEmail) {
				messages.add(MessageBuilder.fromMessage(message));
			}
		}
		catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(FAIL_TO_TRANSFORM_MESSAGE_DUE_TO, e.getClass().getSimpleName(), e.getMessage());
			}
			throw new IllegalStateException("Could not transform message", e);
		}
		return messages;
	}

	private static void processMultipart(Multipart multipart, Message<MimeMessage> message,
			Set<AbstractIntegrationMessageBuilder<File>> messages) throws MessagingException, IOException {
		for (int i = 0; i < multipart.getCount(); i++) {
			BodyPart bodyPart = multipart.getBodyPart(i);
			messages.addAll(transformBodyPartToMessage(bodyPart, message));
		}
	}

	private static Collection<AbstractIntegrationMessageBuilder<File>> transformBodyPartToMessage(BodyPart bodyPart,
			Message<MimeMessage> mailMessage) throws MessagingException, IOException {
		String disposition = bodyPart.getDisposition();
		if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
			return handleAttachment(bodyPart, mailMessage);
		}
		return new LinkedHashSet<>();
	}

	private static Collection<AbstractIntegrationMessageBuilder<File>> handleAttachment(BodyPart bodyPart,
			Message<MimeMessage> mailMessage) throws MessagingException, IOException {
		String filename = bodyPart.getFileName();
		if (filename == null) {
			filename = ATTACHMENT + System.currentTimeMillis();
		}

		AbstractIntegrationMessageBuilder<File> messageBuilder = createMessageBuilder(bodyPart, filename);

		if (messageBuilder == null) {
			return new LinkedHashSet<>();
		}

		if (StringUtils.hasText(bodyPart.getContentID())) {
			messageBuilder.setHeader(MailHeaders.CONTENT_ID, bodyPart.getContentID());
		}

		copyHeaders(mailMessage, messageBuilder);

		Set<AbstractIntegrationMessageBuilder<File>> result = new LinkedHashSet<>();
		result.add(messageBuilder);
		return result;
	}

	private static AbstractIntegrationMessageBuilder<File> createMessageBuilder(BodyPart bodyPart, String filename)
			throws IOException, MessagingException {
		if (bodyPart instanceof MimeBodyPart mimeBodyPart) {
			Path tempFile = Files.createTempFile("mailAttachments", null);
			try (InputStream inputStream = mimeBodyPart.getInputStream()) {
				File file = tempFile.toFile();
				try (OutputStream outputStream = new FileOutputStream(file)) {
					FileCopyUtils.copy(inputStream, outputStream);
				}
				AbstractIntegrationMessageBuilder<File> messageBuilder = MessageBuilder.withPayload(file)
						.setHeader(FileHeaders.FILENAME, file.getName())
						.setHeader(FileHeaders.ORIGINAL_FILENAME, filename);
				return messageBuilder;
			}
			finally {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(ATTACHMENT_PROCESSED);
				}
				Files.deleteIfExists(tempFile);
			}
		}
		else {
			return MessageBuilder.withPayload((BodyPart) bodyPart)
					.setHeader(FileHeaders.FILENAME, filename);
		}
	}

	private static void copyHeaders(Message<MimeMessage> mailMessage,
			AbstractIntegrationMessageBuilder<File> messageBuilder) {
		@SuppressWarnings("unchecked")
		List<String> headerList = (List<String>) mailMessage.getHeaders().get(MailHeaders.HEADERS);
		if (headerList != null) {
			for (String header : headerList) {
				try {
					messageBuilder.setHeader(header, mailMessage.getPayload().getHeader(header));
				}
				catch (MessagingException e) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("{}Exception occurred while getting value for header {}", EXCEPTION_DETAILS,
								header, e);
					}
				}
			}
		}

		// copy mail headers
		messageBuilder.copyHeaders(mailMessage.getHeaders());
	}

}
