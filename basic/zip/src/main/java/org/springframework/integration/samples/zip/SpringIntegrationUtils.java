package org.springframework.integration.sts;

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
		try {
			return doTransformEmailMessageToAttachmentMessage(message, includeEmail);
		}
		catch (Exception e) {
			LOGGER.error("Fail to transform message due to " + e.getClass().getSimpleName() + " : " + e.getMessage(), e);
			throw new IllegalStateException("Could not transform message", e);
		}
	}

	private static Collection<AbstractIntegrationMessageBuilder<?>> doTransformEmailMessageToAttachmentMessage(
			Message<MimeMessage> message, boolean includeEmail) throws MessagingException, IOException {

		Set<AbstractIntegrationMessageBuilder<?>> messages = new LinkedHashSet<>();
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
		return messages;
	}


	private static Collection<AbstractIntegrationMessageBuilder<?>> transformBodyPartToMessage(BodyPart bodyPart,
			Message<MimeMessage> mailMessage) throws MessagingException, IOException {

		String disposition = bodyPart.getDisposition();
		if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
			return createAttachmentMessage(bodyPart, mailMessage);
		}
		return Collections.emptySet();
	}

	private static Collection<AbstractIntegrationMessageBuilder<?>> createAttachmentMessage(BodyPart bodyPart,
			Message<MimeMessage> mailMessage) throws MessagingException, IOException {
		String filename = bodyPart.getFileName();
		if (filename == null) {
			filename = "attachment-" + System.currentTimeMillis();
		}

		AbstractIntegrationMessageBuilder<?> messageBuilder = createMessageBuilder(bodyPart, filename);
		addContentIdHeader(bodyPart, messageBuilder);
		copyMailHeaders(mailMessage, messageBuilder);
		return Collections.singleton(messageBuilder);
	}

	private static AbstractIntegrationMessageBuilder<?> createMessageBuilder(BodyPart bodyPart, String filename)
			throws MessagingException, IOException {

		AbstractIntegrationMessageBuilder<?> messageBuilder;

		if (bodyPart instanceof MimeBodyPart mimeBodyPart) {
			Path tempFile = createTemporaryAttachmentFile(mimeBodyPart);
			File file = tempFile.toFile();
			messageBuilder = MessageBuilder.withPayload(file)
					.setHeader(FileHeaders.FILENAME, file.getName())
					.setHeader(FileHeaders.ORIGINAL_FILENAME, filename);
		}
		else {
			messageBuilder = MessageBuilder.withPayload(bodyPart)
					.setHeader(FileHeaders.FILENAME, filename);
		}
		return messageBuilder;
	}

	private static Path createTemporaryAttachmentFile(MimeBodyPart mimeBodyPart) throws IOException {
		Path tempFile = Files.createTempFile("mailAttachments", null);
		try (InputStream inputStream = mimeBodyPart.getInputStream();
			 OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
			FileCopyUtils.copy(inputStream, outputStream);
		} finally {
			logAttachmentProcessed();
		}
		return tempFile;
	}

	private static void logAttachmentProcessed() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attachment processed");
		}
	}

	private static void addContentIdHeader(BodyPart bodyPart, AbstractIntegrationMessageBuilder<?> messageBuilder) throws MessagingException {
		if (StringUtils.hasText(bodyPart.getContentID())) {
			messageBuilder.setHeader(MailHeaders.CONTENT_ID, bodyPart.getContentID());
		}
	}

	private static void copyMailHeaders(Message<MimeMessage> mailMessage, AbstractIntegrationMessageBuilder<?> messageBuilder) throws MessagingException {
		@SuppressWarnings("unchecked")
		List<String> headerList = (List<String>) mailMessage.getHeaders().get(MailHeaders.HEADERS);
		if (headerList != null) {
			for (String header : headerList) {
				try {
					messageBuilder.setHeader(header, mailMessage.getPayload().getHeader(header));
				} catch (MessagingException e) {
					LOGGER.warn("Exception occurred while getting value for header " + header, e);
				}
			}
		}

		messageBuilder.copyHeaders(mailMessage.getHeaders());
	}

}
