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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * @author Dr. Jochen Raßler
 *
 */
/*private*/ class MailListFetcher {

	private List<MailReference> localMails;
	private List<MailReference> toDelete;
	private List<MailReference> toAdd;
	private final Folder folder;
	
	/**
	 * 
	 */
	public MailListFetcher(final Folder folder) {
		this.folder = folder;
//		this.localMails = localMails;
		toDelete = new ArrayList<>();
		toAdd = new ArrayList<>();
	}
	
	/**
	 * @return the localMails
	 */
//	public List<MailReference> getLocalMails() {
//		return localMails;
//	}

	/**
	 * @return the toDelete
	 */
//	public List<MailReference> getToDelete() {
//		return toDelete;
//	}

	/**
	 * @return the toAdd
	 */
//	public List<MailReference> getToAdd() {
//		return toAdd;
//	}

	public void updateMailList() {
		List<Message> msgs = getMailsFromServer();
		checkForDeletedMailsOnServer(msgs);
		checkForNewMailsOnServer(msgs);
	}

	public List<MailReference> getMailRefs() {
		localMails = new ArrayList<>();
		List<Message> msgs = getMailsFromServer();
		for (Message msg : msgs)
			localMails.add(new MailReference(msg));
		return localMails;
	}
	
	/**
	 * @return
	 */
	private List<Message> getMailsFromServer() {
		List<Message> serverMails = new ArrayList<>();
		try {
			if (!folder.isOpen())
				folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();
			serverMails = Arrays.asList(messages);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				folder.close(true);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return serverMails;
	}

	/**
	 * @param msgs
	 */
	private void checkForDeletedMailsOnServer(List<Message> msgs) {
		toDelete = new ArrayList<>();
		for (MailReference ref : localMails) {
			if (!msgs.contains(ref.getMessage()))
				toDelete.add(ref);
		}
		// TODO: how to propagate?
	}

	/**
	 * @param msgs
	 */
	private void checkForNewMailsOnServer(List<Message> msgs) {
		toAdd = new ArrayList<>();
		
		List<Message> localRefs = new ArrayList<>();
		for (MailReference ref : localMails) {
			localRefs.add(ref.getMessage());
		}
		
		for (Message msg : msgs) {
			if (!localRefs.contains(msg))
				toAdd.add(new MailReference(msg));
		}
		
		// TODO: how to propagate new Mails?
		// TODO: how to create ETD and hash?
	}

}
