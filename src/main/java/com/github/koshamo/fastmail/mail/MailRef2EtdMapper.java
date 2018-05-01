/*
 * Copyright (C) 2018  Dr. Jochen Raßler
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import com.github.koshamo.fastmail.util.EmailTableData;
import com.github.koshamo.fastmail.util.HashUtils;
import com.sun.mail.imap.IMAPMessage;

/**
 * @author Dr. Jochen Raßler
 *
 */
/*private*/ class MailRef2EtdMapper implements Runnable {

	private List<EmailTableData> mailList;
	private final List<MailReference> messages;
	private boolean stop = false;
	private boolean done = false;
	
	/**
	 * @param folder
	 */
	public MailRef2EtdMapper(final List<MailReference> messages, List<EmailTableData> mailList) {
		this.messages = messages;
		if (mailList != null)
			this.mailList = mailList;
	}

	public void stop() {
		stop = true;
	}

	public boolean isDone() {
		return done;
	}
	
	public List<EmailTableData> getMailList() {
		return mailList;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (mailList == null)
			mailList = new ArrayList<>();
		if (messages.size() == 0)
			return;
		Folder folder = messages.get(0).getMessage().getFolder();
		try {
			if (!folder.isOpen())
				folder.open(Folder.READ_ONLY);
			for (MailReference ref : messages) {
				if (stop) return;
				
				if (ref.getMessage() instanceof IMAPMessage) 
					((IMAPMessage) ref.getMessage()).setPeek(true);
				EmailTableData etd = getEmailTableData(ref.getMessage());
				ref.setUniqueId(etd.getUniqueID());
				mailList.add(etd);
			}
			done = true;
			if (folder.isOpen())
				folder.close(false);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static EmailTableData getEmailTableData(Message msg) 
			throws MessagingException, IOException {
		String from = ((InternetAddress[]) msg.getFrom())[0].getAddress();
		String fromName = ((InternetAddress[]) msg.getFrom())[0].getPersonal();
		String subject = msg.getSubject();
		if (subject == null)
			subject = "";
		Instant sentDate = msg.getSentDate().toInstant();
		boolean attached = false;
		if (msg.isMimeType("multipart/mixed")) { //$NON-NLS-1$
			Multipart mp = (Multipart) msg.getContent();
			if (mp.getCount() > 1)
				attached = true;
			else 
				// TODO: what, if image is the only content?
				// would it be better to check against not-TEXT?
				if (mp.getBodyPart(0).getContentType().contains("APPLICATION"))
					attached = true;
		}
		boolean read = msg.isSet(Flag.SEEN);
		boolean marked = msg.isSet(Flag.FLAGGED);
		String hashText = from + fromName + subject + sentDate.toString();
		String uniqueID = HashUtils.calcMD5Hash(hashText);

		return new EmailTableData(from, fromName, subject, sentDate, attached, read, marked, uniqueID);
	}
}
