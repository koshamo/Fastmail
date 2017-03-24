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

package com.github.koshamo.fastmail.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * The FolderSynchronizerTask synchs the mails of a given Folder.
 * <p>
 * Used standalone in a thread, this task runs once to synch the given folder,
 * e.g. for local folders, when we know a change has been made and we
 * want to force an upadte.
 * <p>
 * The class can be used in a Scheduled Service, as it is implemented in
 * FolderSynchronizer, that uses this Task to poll the update of a given 
 * folder. Main use for polling the inbox to check, if any mails have arrived
 * or been deleted using another device / client.
 *  
 * @author jochen
 *
 */
public class FolderSynchronizerTask extends Task<Void> {

	private final Folder folder;
	private final ObservableList<EmailTableData> mailList;

	private boolean stop = false;
	
	/**
	 * The constructor builds the context of this task, which is the folder 
	 * on the server to check and the local list of mails to update.
	 * 
	 * @param folder	the folder on the server to check
	 * @param mailList	the local observable list of mails to update
	 */
	public FolderSynchronizerTask(final Folder folder, 
			final ObservableList<EmailTableData> mailList) {
		this.folder = folder;
		this.mailList = mailList;
	}
	
	/**
	 * stops the execution of the thread
	 * <p>
	 */
	public void stop() {
		stop = true;
	}

	/* (non-Javadoc)
	 * @see javafx.concurrent.Task#call()
	 */
	@Override
	protected Void call() throws Exception {
		try {
			// get mail count in local end
			int count = mailList.size();
			if (!folder.isOpen())
				folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();
			List<EmailTableData> serverList = new ArrayList<EmailTableData>();
			/* 
			 * first step: add mails, if there really are more mails
			 * on server than on the local end
			 */
			if (messages != null) {
				for (Message msg : messages) {
					if (stop) return null; // check if thread needs to be stopped
					// check, if message has been deleted meanwhile
					if (msg.isExpunged())
						continue;
					EmailTableData etd = new EmailTableData(msg);
					if (!mailList.contains(etd)) 
						mailList.add(etd);
					// prepare for step two
					if (count > 0)
						serverList.add(etd);
				}
			}
			/*
			 * if local end had no mails, we do not need to check, if any
			 * mails need to be deleted.
			 * This is always true, if we read a folder for the first time
			 */
			if (count > 0) {
				/*
				 * second step: if there are more mails on the local end,
				 * which should be true, whenever mails are added in the first
				 * step or mails have been deleted from another source,
				 * delete all excessive mails
				 */
				if (mailList.size() != serverList.size()) {
					// prevent ConcurrentModificationException
					Collection<EmailTableData> toBeRemoved = new ArrayList<EmailTableData>();
					for (EmailTableData etd : mailList) {
						if (!serverList.contains(etd))
							toBeRemoved.add(etd);
					}
					mailList.removeAll(toBeRemoved);
				}
			}
			// sort the list in the natural order
			mailList.sort(null);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
