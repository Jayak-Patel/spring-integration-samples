package org.springframework.integration.samples.mailattachments.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
					messages.addAll(transformBodyPartToMessage(bodyPart, message));
				}
			}
			if (includeEmail) {
				messages.add(MessageBuilder.fromMessage(message));
			}
		}
		catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Fail to transform message due to " + e.getClass().getSimpleName() +
						" : " + e.getMessage());
			}
			throw new IllegalStateException("Could not transform message", e);
		}
		return messages;
	}

	private static Collection<AbstractIntegrationMessageBuilder<?>> transformBodyPartToMessage(BodyPart bodyPart,
			Message<MimeMessage> mailMessage) throws MessagingException, IOException {
		String disposition = bodyPart.getDisposition();
		if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
			String filename = bodyPart.getFileName();
			if (filename == null) {
				filename = "attachment-" + System.currentTimeMillis();
			}
			AbstractIntegrationMessageBuilder<?> messageBuilder;
			if (bodyPart instanceof MimeBodyPart mimeBodyPart) {
				Path tempFile = Files.createTempFile("mailAttachments", null);
				try (InputStream inputStream = mimeBodyPart.getInputStream()) {
					File file = tempFile.toFile();
					try (OutputStream outputStream = new FileOutputStream(file)) {
						FileCopyUtils.copy(inputStream, outputStream);
					}
					messageBuilder = MessageBuilder.withPayload(file)
							.setHeader(FileHeaders.FILENAME, file.getName())
							.setHeader(FileHeaders.ORIGINAL_FILENAME, filename);
					//noinspection ConstantConditions,ConstantConditions
				}
				finally {
					// Do not log the filename as it's user-controlled data.
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Attachment processed");
					}
					Files.deleteIfExists(tempFile);
				}
			}
			else {
				messageBuilder = MessageBuilder.withPayload(bodyPart)
						.setHeader(FileHeaders.FILENAME, filename);
			}
			if (StringUtils.hasText(bodyPart.getContentID())) {
				messageBuilder.setHeader(MailHeaders.CONTENT_ID, bodyPart.getContentID());
			}

			@SuppressWarnings("unchecked")
			List<String> headerList = (List<String>) mailMessage.getHeaders().get(MailHeaders.HEADERS);
			if (headerList != null) {
				headerList.forEach(header -> {
					try {
						messageBuilder.setHeader(header, mailMessage.getPayload().getHeader(header));
					}
					catch (MessagingException e) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn(EXCEPTION_DETAILS + "Exception occurred while getting value for header " +
									header, e);
						}
					}
				});
			}

			// copy mail headers
			messageBuilder.copyHeaders(mailMessage.getHeaders());
			Set<AbstractIntegrationMessageBuilder<?>> result = new LinkedHashSet<>();
			result.add(messageBuilder);
			return result;
		}
		return new LinkedHashSet<>();
	}

}
