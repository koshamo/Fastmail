/*
 * Copyright (C) 2017  Dr. Jochen Raßler
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

package com.github.koshamo.fastmail;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * The class MailContentLister reads a given Mail from the server 
 * and stores all data in fields.
 * These fields can be read using the appropriate getter methods
 * 
 * @author jochen
 *
 */
public class MailContentLister {

	// internal fields...
	private Message[] msg = null;
	private Message message = null;
	private Folder folder = null;

	/**
	 * The constructor takes all fields necessary to read the mail from the server
	 * and stores the mail content to global fields for further processing.
	 * 
	 * @param accounts the list with local accounts
	 * @param currentAccount the current account, in which the mail is
	 * @param folderName the folder, in which the selected mail is
	 * @param messageID the ID of the message within the given folder
	 */
	public MailContentLister(MailAccount account, String folderName, int messageID) {

		// iterate over the accounts to find the matching one
		for (String f : account.getFolders()) {
			if (f.equals(folderName)) {
				folder = account.getFolder(folderName);
				if (folder == null) return;
				// get the messages from the folder
				try {
					folder.open(Folder.READ_WRITE);
					msg = folder.getMessages();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		if (msg != null) {
			// just write the wanted message to internal field for further processing
			message = msg[messageID-1];
		}
		// close folder after usage, but prevent INBOX to be closed, as we need 
		// it to check for new mails
//		if ((folder != null) && !folder.equals("INBOX")) {
//			try {
//				folder.close(true);
//			} catch (MessagingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	
	/* (non-Javadoc)
	 * 
	 * this method just closes the mail folder
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		if (folder != null) {
			try {
				folder.close(true);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The method getMessage returns the mail's content in plain text format
	 * <p>
	 * as Multipart messages have several body parts, currently the plain text
	 * body part is chosen. We need some further investigation into this, as
	 * we are able to display HTML text with webview. 
	 * See @com.github.koshamo.fastmail.MailContentLister#getBodyContent() where the body parts are read
	 * 
	 * @param messageID the message number within the folder
	 * @return the mail content as plain text
	 */
	public MailData getMessage(int messageID) {

		MailData md = null;
		
		String from = ""; //$NON-NLS-1$
		String fromName = ""; //$NON-NLS-1$
		String[] to = null;
		String[] toName = null;
		String[] cc = null;
		String[] ccName = null;
		String subject = ""; //$NON-NLS-1$
		String content = ""; //$NON-NLS-1$
		AttachmentData[] attachments = null;
		try {
			if (message.isMimeType("text/plain")) { //$NON-NLS-1$
				content = (String) message.getContent();
			} else if (message.isMimeType("multipart/*")) { //$NON-NLS-1$
				Multipart mp = (Multipart)message.getContent();
				content = getTextBodyContent(mp);
				if (message.isMimeType("multipart/mixed")) { //$NON-NLS-1$
					int cnt = mp.getCount();
					if (cnt > 1) {
						attachments = new AttachmentData[cnt-1];
						for (int i = 1; i < cnt; i++) {
							BodyPart bp = mp.getBodyPart(i); 
							if (bp.getContent() instanceof InputStream) {
								attachments[i-1] = new AttachmentData(
										bp.getFileName(), bp.getSize(), 
										(InputStream) bp.getContent());
							} else
								attachments[i-1] = getAttachmentBodyContent(bp);
						}
					}
				}
			} else if (message.isMimeType("message/rfc822")) { //$NON-NLS-1$
				// recursive reading
				md = getMessage(messageID);
			} else {
				System.out.println("this is an unknown message type");
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		try {
			from = ((InternetAddress[]) message.getFrom())[0].getAddress();
			fromName = ((InternetAddress[]) message.getFrom())[0].getPersonal();
			int toLength = ((InternetAddress[])message.getRecipients(RecipientType.TO)).length;
			to = new String[toLength];
			toName = new String[toLength];
			for (int i = 0; i < toLength; i++) {
				to[i] = ((InternetAddress[])message.getRecipients(RecipientType.TO))[i].getAddress();
				toName[i] = ((InternetAddress[])message.getRecipients(RecipientType.TO))[i].getPersonal();
			}
			if (message.getRecipients(RecipientType.CC) != null) {
				int ccLength = ((InternetAddress[])message.getRecipients(RecipientType.CC)).length;
				cc = new String[ccLength];
				ccName = new String[ccLength];
				for (int i = 0; i < ccLength; i++) {
					cc[i] = ((InternetAddress[])message.getRecipients(RecipientType.CC))[i].getAddress();
					ccName[i] = ((InternetAddress[])message.getRecipients(RecipientType.CC))[i].getPersonal();
				}
			}
			subject = message.getSubject();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (md == null)
			md = new MailData(from, fromName, to, toName, cc, ccName , subject, content, attachments);

		return md;

	}


	/**
	 * this method provides the content of Multipart messages
	 * <p>
	 * currently we read the plain text body part, which is the first body part
	 * provided. the second one seems to be the HTML body part, which currently
	 * is ignored.
	 * If the body part contains another body part, we dive recursively into it
	 * to get the real content - currently as plain text, see above
	 * 
	 * @param mp the Multipart message of the mail, which is the body part
	 * @return the mails content as plain text
	 */
	private String getTextBodyContent(Multipart mp) {
		String result = ""; //$NON-NLS-1$
		// seems that most Multipart messages have 2 Body parts
		// first body part: plain text
		// second body part: HTML message
		try {
			for (int i = 0; i < mp.getCount(); i++) {
				Object obj = mp.getBodyPart(i).getContent();
				if (obj instanceof String) {
					// TODO: we return the first body part, which is the plain text message
					// the second body part seems to be the HTML body, which will be
					// needed, as soon as we are able to display HTML text
					result = (String) mp.getBodyPart(i).getContent();
					break;
				} 
				else if (obj instanceof MimeMultipart) {
					result = getTextBodyContent((Multipart) obj);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * if BodyPart is recursively packed, this method is convenient 
	 * to get the attachments
	 * @param bp the BodyPart of a MimeMultipart message
	 * @return the AttachmentData object with the proper input stream
	 */
	// TODO: test this with a proper mail.... if not testable, remove!
	private AttachmentData getAttachmentBodyContent(BodyPart bp) {
		String fileName = null;
		int size = 0;
		InputStream is = null;
		try {
				Object obj = bp.getContent();
				System.out.println("Object " + obj.toString());
				if (obj instanceof String) {
					System.out.println("String....");
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
				}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new AttachmentData(fileName, size, is);
	}
}