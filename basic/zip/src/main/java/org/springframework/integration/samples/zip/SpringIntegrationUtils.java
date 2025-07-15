/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 20 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package org.springframework.integration.samples.mailattachments.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.ParseException;

/**
 * Utility Class for parsing mail messages.
 *
 * @author Gunnar Hillert
 * @author Gary Russell
 * @author Artem Bilan
 *
 * @since 2.2
 *
 */
public final class EmailParserUtils {

	private static final Log LOGGER = LogFactory.getLog(EmailParserUtils.class);

	/** Prevent instantiation. */
	private EmailParserUtils() {
		throw new AssertionError();
	}

	/**
	 * Parses a mail message. The respective message can either be the root message
	 * or another message that is attached to another message.
	 *
	 * If the mail message is an instance of {@link String}, then a {@link EmailFragment}
	 * is being created using the email message's subject line as the file name,
	 * which will contain the mail message's content.
	 *
	 * If the mail message is an instance of {@link Multipart} then we delegate
	 * to {@link #handleMultipart(File, Multipart, List)}.
	 *
	 * @param directory The directory for storing the message. If null this is the root message.
	 * @param mailMessage The mail message to be parsed. Must not be null.
	 * @param emailFragments Must not be null.
	 */
	public static void handleMessage(final File directory,
			final jakarta.mail.Message mailMessage,
			final List<EmailFragment> emailFragments) {

		Assert.notNull(mailMessage, "The mail message to be parsed must not be null.");
		Assert.notNull(emailFragments, "The collection of email fragments must not be null.");

		try {
			Object content = mailMessage.getContent();
			String subject = mailMessage.getSubject();
			File directoryToUse = (directory == null) ? new File(subject) : new File(directory, subject);

			processContent(directoryToUse, content, mailMessage, emailFragments);
		}
		catch (MessagingException e) {
			LOGGER.error("Error while retrieving the email contents.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception details:", e);
			}
			throw new IllegalStateException("Error while retrieving the email contents.", e);
		}
	}

	private static void processContent(File directoryToUse, Object content, jakarta.mail.Message mailMessage, List<EmailFragment> emailFragments) throws MessagingException {
		try {
			if (content instanceof String) {
				// Do not log the content, as it might contain sensitive information.
				// Instead, log a message indicating that the content is being skipped for security reasons.
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Skipping logging of content for file '{}' due to potential security concerns.", mailMessage.getSubject());
				}
				emailFragments.add(new EmailFragment(new File(mailMessage.getSubject()), "message.txt", content));
			}
			else if (content instanceof Multipart) {
				handleMultipart(directoryToUse, (Multipart) content, emailFragments);
			}
			else {
				throw new IllegalStateException("This content type is not handled - " + content.getClass().getSimpleName());
			}
		}
		catch (IOException e) {
			LOGGER.error("Error while processing content.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception details:", e);
			}
			throw new IllegalStateException("Error while retrieving the email contents.", e);
		}
	}

	/**
	 * Parses any {@link Multipart} instances that contain text or Html attachments,
	 * {@link InputStream} instances, additional instances of {@link Multipart}
	 * or other attached instances of {@link jakarta.mail.Message}.
	 *
	 * Will create the respective {@link EmailFragment}s representing those attachments.
	 *
	 * Instances of {@link jakarta.mail.Message} are delegated to
	 * {@link #handleMessage(File, jakarta.mail.Message, List)}. Further instances
	 * of {@link Multipart} are delegated to
	 * {@link #handleMultipart(File, Multipart, List)}.
	 *
	 * @param directory Must not be null
	 * @param multipart Must not be null
	 * @param emailFragments Must not be null
	 */
	public static void handleMultipart(File directory, Multipart multipart, List<EmailFragment> emailFragments) {

		Assert.notNull(directory, "The directory must not be null.");
		Assert.notNull(multipart, "The multipart object to be parsed must not be null.");
		Assert.notNull(emailFragments, "The collection of email fragments must not be null.");

		try {
			int count = multipart.getCount();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Number of enclosed BodyPart objects: %s.", count));
			}

			for (int i = 0; i < count; i++) {
				BodyPart bp = multipart.getBodyPart(i);
				processBodyPart(directory, bp, emailFragments, i);
			}

		}
		catch (MessagingException e) {
			LOGGER.error("Error while retrieving the number of enclosed BodyPart objects: " + e.getMessage());
			throw new IllegalStateException("Error while retrieving the number of enclosed BodyPart objects.", e);
		}
	}

	private static void processBodyPart(File directory, BodyPart bp, List<EmailFragment> emailFragments, int i) throws MessagingException {
		try {
			String contentType = bp.getContentType();
			String filename = bp.getFileName();
			String disposition = bp.getDisposition();

			if (filename == null && bp instanceof MimeBodyPart mimeBodyPart) {
				filename = mimeBodyPart.getContentID();
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BodyPart - Content Type: {}, filename: {}, disposition: {}",
						contentType, filename, disposition);
			}

			if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
				LOGGER.info("Handling attachment '{}', type: '{}'", filename, contentType);
			}

			Object content = bp.getContent();

			handleContent(directory, content, filename, disposition, contentType, emailFragments, i);

		}
		catch (IOException e) {
			LOGGER.error("Error while processing body part.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception details:", e);
			}
			throw new IllegalStateException("Error while retrieving the email contents.", e);
		}
	}

	private static void handleContent(File directory, Object content, String filename, String disposition, String contentType, List<EmailFragment> emailFragments, int i) throws MessagingException {
		try {
			if (content instanceof String) {
				processStringContent(directory, content, filename, disposition, contentType, emailFragments, i);
			}
			else if (content instanceof InputStream) {
				processInputStreamContent(directory, (InputStream) content, filename, emailFragments);
			}
			else if (content instanceof jakarta.mail.Message) {
				handleMessage(directory, (jakarta.mail.Message) content, emailFragments);
			}
			else if (content instanceof Multipart) {
				handleMultipart(directory, (Multipart) content, emailFragments);
			}
			else {
				throw new IllegalStateException("Content type not handled: " + content.getClass().getSimpleName());
			}
		}
		catch (IOException e) {
			LOGGER.error("Error while handling content.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception details:", e);
			}
			throw new IllegalStateException("Error while retrieving the email contents.", e);
		}
	}

	private static void processStringContent(File directory, Object content, String filename, String disposition, String contentType, List<EmailFragment> emailFragments, int i) throws ParseException {
		if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
			emailFragments.add(new EmailFragment(directory, i + "-" + filename, content));
			LOGGER.info("Handling attachment '{}', type: '{}'", filename, contentType);
		}
		else {
			String textFilename = determineTextFilename(contentType);
			// Do not log the content, as it might contain sensitive information.
			// Instead, log a message indicating that the content is being skipped for security reasons.
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Skipping logging of content for file '{}' due to potential security concerns.", textFilename);
			}

			emailFragments.add(new EmailFragment(directory, textFilename, (Object) content));
		}
	}

	private static String determineTextFilename(String contentType) throws ParseException {
		ContentType ct = new ContentType(contentType);
		String baseType = ct.getBaseType();
		if ("text/plain".equalsIgnoreCase(baseType)) {
			return "message.txt";
		}
		else if ("text/html".equalsIgnoreCase(baseType)) {
			return "message.html";
		}
		else {
			return "message.other";
		}
	}

	private static void processInputStreamContent(File directory, InputStream contentAsInputStream, String filename, List<EmailFragment> emailFragments) {
		try {
			ByteArrayOutputStream bis = new ByteArrayOutputStream();

			IOUtils.copy(contentAsInputStream, bis);

			emailFragments.add(new EmailFragment(directory, filename, bis.toByteArray()));
		} catch (IOException e) {
			LOGGER.error("Error while processing input stream content.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception details:", e);
			}
			throw new IllegalStateException("Error while retrieving the email contents.", e);
		}
	}
}
