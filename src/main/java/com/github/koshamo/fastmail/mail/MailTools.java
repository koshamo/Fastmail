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

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * MailTools is a helper class that holds some static methods which are useful
 * throughout the application and do some basic stuff
 * 
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
	 * Just in time loading of message content. Returns an array, 
	 * containing the content. First entry is plain text, second
	 * entry, if available, holds HTML message
	 *  
	 * @param message	the message to process
	 * @return			the message content
	 */
	public static String[] getContent(final Message message) {
		try {
			String[] content = null;
			if (message.isMimeType("text/plain")) { //$NON-NLS-1$
				content = new String[1];
				content[0] = (String) message.getContent();
			} else if (message.isMimeType("text/html")) { //$NON-NLS-1$
				content = new String[1];
				content[0] = (String) message.getContent();
			} else if (message.isMimeType("multipart/*")) { //$NON-NLS-1$
				Multipart mp = (Multipart)message.getContent();
				content = getTextBodyContent(mp);
			} else if (message.isMimeType("message/rfc822")) { //$NON-NLS-1$
				// recursive reading
				System.out.println("come back to MailTools::getContent() to fix this");
				System.out.println("ContentType is " + message.getContentType());
//				MimeMultipart mm = (MimeMultipart) message.getContent();
//				md = getMessage();
			} else {
				System.out.println("this is an unknown message type, " + message.getContentType());
			}
			return content;
		} catch (IOException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailboxaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
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
	 * Just in time loading of message attachments
	 * @param message	the message to process
	 * @return			the attachment data objects as array
	 */
	public static AttachmentData[] getAttachments(final Message message) {
		try {
			if (message.isMimeType("multipart/mixed")) { //$NON-NLS-1$
				Multipart mp = (Multipart)message.getContent();
				int cnt = mp.getCount();
				if (cnt > 1) {
					AttachmentData[] attachments = new AttachmentData[cnt-1];
					for (int i = 1; i < cnt; i++) {
						BodyPart bp = mp.getBodyPart(i); 
						if (bp.getContent() instanceof InputStream) {
							attachments[i-1] = new AttachmentData(
									bp.getFileName(), bp.getSize(), 
									(InputStream) bp.getContent());
						} else
							attachments[i-1] = getAttachmentBodyContent(bp);
					}
					return attachments;
				}
			}
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (IOException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailboxaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}
	


	/**
	 * this method provides the content of Multipart messages
	 * <p>
	 * we read the plain text body part, which is the first body part
	 * provided. the second one seems to be the HTML body part.
	 * Both parts are stored in that order into the returning String array.
	 * <p>
	 * If the body part contains another body part, we dive recursively into it
	 * to get the real content - currently as plain text, see above
	 * 
	 * @param mp the Multipart message of the mail, which is the body part
	 * @return the mails content as String array
	 */
	private static String[] getTextBodyContent(final Multipart mp) {
		int cnt = 0;
		try {
			cnt = mp.getCount();
		} catch(MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		String[] simpleResult = new String[cnt];
		System.out.println("BodyPart Count: " + cnt);
		String[] complexResult = null;
		// seems that most Multipart messages have 2 Body parts
		// first body part: plain text
		// second body part: HTML message
		try {
			for (int i = 0; i < cnt; i++) {
				Object obj = mp.getBodyPart(i).getContent();
				System.out.println("BodyPart Type: " + obj.getClass());
				if (obj instanceof String) {
					// this is the Multipart containing the message BodyParts
					simpleResult[i] = (String) mp.getBodyPart(i).getContent();
				} 
				else if (obj instanceof MimeMultipart) {
					// this Multipart doesn't own the message BodyParts
					// on itself, so recursively dive into it
					complexResult = getTextBodyContent((Multipart) obj);
				}
			}
		} catch (IOException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailboxaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return complexResult == null ? simpleResult : complexResult;
	}
	
	/**
	 * if BodyPart is recursively packed, this method is convenient 
	 * to get the attachments
	 * @param bp the BodyPart of a MimeMultipart message
	 * @return the AttachmentData object with the proper input stream
	 */
	// TODO: test this with a proper mail.... if not testable, remove!
	/*
	 * SuppressWarning Reason: we do not open the stream, we just
	 * pull it out of the body part. Eclipse does not recognize this
	 * so the suppress warning annotation is for Eclipse only (?)
	 */
	@SuppressWarnings("resource")
	private static AttachmentData getAttachmentBodyContent(final BodyPart bp) {
		String fileName = null;
		int size = 0;
		InputStream is = null;
		try {
				Object obj = bp.getContent();
				if (obj instanceof String) {
						// do nothing with String attachment
				} 
				else if (obj instanceof MimeMultipart) {
					System.out.println("Multipart");
					getAttachmentBodyContent(((MimeMultipart) obj).getBodyPart(0));
					fileName = ((MimeMultipart) obj).getBodyPart(0).getFileName();
					System.out.println("Filename " + fileName);
					size = ((MimeMultipart) obj).getBodyPart(0).getSize();
					System.out.println("Size " + size);
				}
				else if (obj instanceof InputStream) {
					System.out.println("InputStream " + obj);
					is = (InputStream) obj;
				} else
					System.out.println("Object: " + obj.getClass());
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (IOException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(
							SerializeManager.getLocaleMessages().getString("exception.mailboxaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		// eclipse issue: is doesn't need to be closed
		return new AttachmentData(fileName, size, is);
	}


}
