/*
 * Copyright (C) 2017  Dr. Jochen Raﬂler
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.github.koshamo.fastmail.mail;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import com.github.koshamo.fastmail.util.MessageItem;
import com.github.koshamo.fastmail.util.MessageMarket;
import com.github.koshamo.fastmail.util.SerializeManager;
import com.sun.mail.util.BASE64DecoderStream;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * MailTools is a helper class that holds some static methods which are useful
 * throughout the application and do some basic stuff
 * 
 * @author jochen
 *
 */
/**
 * @author jochen
 *
 */
public class MailTools {

	/**
	 * The default constructor is private to prevent users to
	 * instantiate this class
	 */
	private MailTools() {
		// prevent instantiating
	}
	
	
	/**
	 * checks if a semicolon separated String of email addresses holds
	 * possibly valid addresses.
	 * <p>
	 * This method only checks the structure of a mail address of type
	 * <code>name@domain.toplevel</code>
	 * <p>
	 * no existence checks will be done!
	 * 
	 * @param addressLine a string with (semicolon separated) email addresses
	 * @return	true if valid, false otherwise
	 */
	public static boolean isValid(final String addressLine) {
		String[] addresses = addressLine.split(";"); //$NON-NLS-1$
		for (String adr : addresses){
			int at = adr.indexOf("@"); //$NON-NLS-1$
			int tld = adr.lastIndexOf("."); //$NON-NLS-1$
			if (at < 0 || tld < 0 || tld < at)
				return false;
		}
		return true;
	}
	
	/**
	 * this method parses a given String that holds several email addresses 
	 * semicolon separated and transforms them to an array of internet email
	 * addresses.
	 * 
	 * @param addressLine a semicolon separated list of email addresses as string
	 * @return an array of internet email Addresses
	 */
	public static InternetAddress[] parseAddresses(final String addressLine) {
		String[] adr = addressLine.split(";"); //$NON-NLS-1$
		InternetAddress[] inetAdr = new InternetAddress[adr.length];
		for (int i = 0; i < adr.length; i++) {
			try {
				inetAdr[i] = new InternetAddress(adr[i]);
			} catch (@SuppressWarnings("unused") AddressException e) {
				MessageItem mItem = new MessageItem(
						SerializeManager.getLocaleMessages().getString("error.mailaddress"), //$NON-NLS-1$
						0.0, MessageItem.MessageType.ERROR);
				MessageMarket.getInstance().produceMessage(mItem);
			}
		}
		return inetAdr;
		
	}
	
	/**
	 * prepends a subject line with Re: if not yet present
	 * 
	 * @param subject the current subject line
	 * @return the subject line starting with Re: 
	 */
	public static String makeSubject(final String subject) {
		if (subject.startsWith("Re:") || subject.startsWith("RE:")) //$NON-NLS-1$ //$NON-NLS-2$
			return subject;
		return new StringBuilder("Re: ").append(subject).toString();  //$NON-NLS-1$
	}
	
	/**
	 * Decorates reply messages with the widely used > to identify cited text
	 * 
	 * @param text the email text, that will be replied
	 * @return the decorated email text
	 */
	public static String decorateMailText(final String text) {
		String[] lines = text.split("\n"); //$NON-NLS-1$
		StringBuilder reply = new StringBuilder();
		for (String line : lines)
			reply.append("> ").append(line).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return reply.toString();
	}
	
	/**
	 * checks if the server string holds a potentially valid address
	 * 
	 * @param 	addressLine the server address to check
	 * @return 	true, if potentially valid, false otherwise
	 */
	public static boolean isServerValid(final String addressLine) {
		if (addressLine.contains("@")) //$NON-NLS-1$
			return false;
		String[] str = addressLine.split("\\."); //$NON-NLS-1$
		if (str.length < 3)
			return false;
		return true;
	}
	
	/**
	 * The natural sort order of email folders is INBOX, Drafts, Sent, Trash,
	 * then followed by the user folders in their natural sort order (which 
	 * is alphabetically)
	 * @param list the observable list containing the email folders
	 */
	public static void sortFolders(final ObservableList<TreeItem<MailTreeViewable>> list) {
		list.sort((i1, i2) -> {
			if ("INBOX".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("INBOX".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Drafts".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Drafts".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Sent".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Sent".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Trash".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Trash".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			return i1.getValue().getName().compareTo(i2.getValue().getName());
		});		
	}
	
	/**
	 * getSubFolders lists the subfolders of a given Folder.
	 * This method is intended to encapsulate the try..catch surrounding
	 * the the Folder.list() method to keep GUI-code clean.
	 * 
	 * @param root the folder, from which the subfolders need to be read
	 * @return	the array of subfolders
	 */
	public static Folder[] getSubFolders(final Folder root) {
		Folder[] subFolders = null;
		try {
			subFolders = root.list();
		} catch (@SuppressWarnings("unused") MessagingException e) {
			MessageItem mItem = new MessageItem(
					SerializeManager.getLocaleMessages().getString("exception.subfolders"), //$NON-NLS-1$
					0.0, MessageItem.MessageType.ERROR);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return subFolders;
	}
	
	/**
	 * Just in time loading of to addresses of the given message
	 * @param message	the message to process
	 * @return			the list of to addresses as array
	 */
	public static String[] getToAddresses(final Message message) {
		try {
			int toLength = ((InternetAddress[])message.getRecipients(RecipientType.TO)).length;
			String[] to = new String[toLength];
			for (int i = 0; i < toLength; i++) {
				to[i] = ((InternetAddress[])message.getRecipients(RecipientType.TO))[i].getAddress();
			}
			return to;
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}
	

	/**
	 * Just in time loading of to display names of the given message
	 * @param message	the message to process
	 * @return			the list of to display names as array
	 */
	public static String[] getToNames(final Message message) {
		try {
			int toLength = ((InternetAddress[])message.getRecipients(RecipientType.TO)).length;
			String[] toName = new String[toLength];
			for (int i = 0; i < toLength; i++) {
				toName[i] = ((InternetAddress[])message.getRecipients(RecipientType.TO))[i].getPersonal();
			}
			return toName;
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}
	
	
	/**
	 * Just in time loading of cc addresses of the given message
	 * @param message	the message to process
	 * @return			the list of cc addresses as array
	 */
	public static String[] getCcAddresses(final Message message) {
		try {
			if (message.getRecipients(RecipientType.CC) != null) {
				int ccLength = ((InternetAddress[])message.getRecipients(RecipientType.CC)).length;
				String[] cc = new String[ccLength];
				for (int i = 0; i < ccLength; i++) {
					cc[i] = ((InternetAddress[])message.getRecipients(RecipientType.CC))[i].getAddress();
				}
				return cc;
			}
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}
	
	
	/**
	 * Just in time loading of cc display names of the given message
	 * @param message	the message to process
	 * @return			the list of cc display names as array
	 */
	public static String[] getCcNames(final Message message) {
		try {
			if (message.getRecipients(RecipientType.CC) != null) {
				int ccLength = ((InternetAddress[])message.getRecipients(RecipientType.CC)).length;
				String[] ccName = new String[ccLength];
				for (int i = 0; i < ccLength; i++) {
					ccName[i] = ((InternetAddress[])message.getRecipients(RecipientType.CC))[i].getPersonal();
				}
				return ccName;
			}
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}
	

	
	/**
	 * Just in time loading of Message content, such as Text and HTML
	 * content and attachments
	 * 
	 * @param msg	the message to be read 
	 * @return		the MailContent object containing text, and HTML 
	 * content as well as the attachments
	 */
	public static MailContent parseMailContent(final Message msg) {
		MailContent content = null;
		
		try {
			String type = msg.getContentType();
			if (type.toLowerCase().contains("text")) { //$NON-NLS-1$
				content = new MailContent();
				// content type TEXT/PLAIN 
				if (type.toLowerCase().contains("plain")) { //$NON-NLS-1$
					if (!(msg.getContent() instanceof String))
						System.out.println("Content is no String?!");
					content.setTextContent((String)msg.getContent());
				}
				// content type TEXT/HTML
				if (type.toLowerCase().contains("html")) { //$NON-NLS-1$
					if (!(msg.getContent() instanceof String))
						System.out.println("Content is no String?!");
					content.setHtmlContent((String)msg.getContent());
				}
				return content;
			}
			// content type MULTIPART
			if (type.toLowerCase().contains("multipart")) //$NON-NLS-1$
				content = parseMultipartContent((Multipart) msg.getContent());
			return content;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: what to return, if an exception occurred?
		return null;
	}
	
	/**
	 * A Mail either contains only a simple text content (plain or HTML), 
	 * or is a Multipart message, possibly with attachments.
	 * If the mail is Multipart message, this method diggs into it.
	 * 
	 * @param part	the Multipart object in the mail
	 * @return		the MailContent object containing text, and HTML 
	 * content as well as the attachments
	 * @throws MessagingException 	if the message is expunged or similar
	 * @throws IOException 			if the internet connection is lost
	 */
	private static MailContent parseMultipartContent(final Multipart part) 
			throws MessagingException, IOException {
		MailContent content = new MailContent();
			for (int i = 0; i < part.getCount(); i++) 
				parseBodyPart(part.getBodyPart(i), content);
		return content;
	}
	
	
	/**
	 * The actual content of a Multipart message object is within a
	 * Bodypart object. So this method gets the content and stores it to
	 * the content object.
	 * 
	 * Note: this method has side effects to the parameter content!
	 * 
	 * @param bodyPart	the Bodypart object to process
	 * @param conent	the MailContent object to store the date
	 * @throws MessagingException 	if the message is expunged or similar
	 * @throws IOException 			if the internet connection is lost
	 */
	private static void parseBodyPart(final BodyPart bodyPart, 
			final MailContent content) 
			throws IOException, MessagingException {
		Object obj = bodyPart.getContent();
		if (bodyPart.getContentType().toLowerCase().contains("text")) {
			if (bodyPart.getContentType().toLowerCase().contains("plain"))
				content.setTextContent((String) obj);
			if (bodyPart.getContentType().toLowerCase().contains("html"))
				content.setHtmlContent((String) obj);
		}
		if (obj instanceof Multipart) {
			MailContent inline = parseMultipartContent((Multipart) obj);
			if (inline.getTextContent() != null)
				content.setTextContent(inline.getTextContent());
			if (inline.getHtmlContent() != null)
				content.setHtmlContent(inline.getHtmlContent());
			if (inline.getAttachments() != null)
				content.addAttachment(inline.getAttachments());
		}
		if (obj instanceof DataHandler)
			System.out.println("Content is DataHandler - is it the same as a BASE64DecoderStream?");
		if (obj instanceof BodyPart)
			System.out.println("A BodyPart should contain Multiparts, no Bodyparts!");
		if (obj instanceof BASE64DecoderStream)
			content.addAttachment(bodyPart.getFileName(), bodyPart.getSize(), 
					bodyPart.getContentType(), (InputStream) obj);
		
	}

	// TODO: delete at given time
	// ALL ANALYZE METHODS BELOW
	// are just for testing purpose and may be deleted, 
	// if proper working of parse* methods is proven

	/**
	 * This method is to analyze the mail messages, as the
	 * javamail documentation and the FAQ does not cover all
	 * possible mail configurations and thus some mails can't get
	 * read correctly.
	 * 
	 * @param msg
	 */
	public static void analyzeContent(Message msg) {
		try {
			System.out.println("===== Message Content =====?");
			System.out.println("=== " + msg.getSubject() + " ===");
			System.out.println("Content Type: " + msg.getContentType());
			// content type can be Text, multipart or unknown
			if (msg.getContentType().contains("multipart"))
				analyzeMultipart((Multipart)msg.getContent(), 0);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param part	possibly Multipart?
	 * @param lvl	for recursion
	 */
	public static void analyzeMultipart(Multipart part, int lvl) {
		System.out.println("MULTIPART Level: " + lvl + " Class: " + part.getClass());
		
		try {
			System.out.println("Multipart Content Type: " + part.getContentType());
			System.out.println("Body Count: " + part.getCount());
			for (int i = 0; i < part.getCount(); i++) 
				analyzeBodyPart(part.getBodyPart(i), lvl);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void analyzeBodyPart(BodyPart bp, int lvl) {
		try {
			System.out.println("BODYPART Level: " + lvl + " Class: " + bp.getClass());
			System.out.println("Content Type: " + bp.getContentType());
			Object obj = bp.getContent();
			System.out.println("Class of Content: " + obj.getClass());
			if (obj instanceof Multipart)
				analyzeMultipart((Multipart)obj, ++lvl);
			if (obj instanceof DataHandler)
				System.out.println("Content is DataHandler");
			if (obj instanceof BodyPart)
				analyzeBodyPart((BodyPart) obj, ++lvl);
			if (obj instanceof BASE64DecoderStream)
				System.out.println("ATTACHMENT");
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
