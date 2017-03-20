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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

/**
 * The InboxWatcher is used to check, if new mail has arrived to the mail box
 * and also notify us, when an email has been deleted from elsewhere
 * 
 * @author jochen
 *
 */
public class InboxWatcher extends ScheduledService<Void> {

	/*package private*/ Folder folder;
	/*package private*/ ObservableList<EmailTableData> mailList;
	
	/**
	 * the constructor of InboxWatcher adds Action Listeners to the INBOX folder
	 * <p>
	 * As POP3 accounts don't support action listeners at all and they did not
	 * work on my IMAP folders, we chose an implementation totally without
	 * folder action listeners
	 * 
	 * @param folder	the folder to watch
	 * @param mailList	the observable list to connect the server folder to the GUI
	 */
	public InboxWatcher(Folder folder, ObservableList<EmailTableData> mailList) {
		this.folder = folder;
		this.mailList = mailList;
	}

	
	/* (non-Javadoc)
	 * This thread checks the Inboxes on a regularly basis for new mails.
	 * If the action listeners worked properly, this would just be to trigger
	 * the IMAP server to stay connected and to send us events.
	 * 
	 * @see javafx.concurrent.Service#createTask()
	 */
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				int localCnt = mailList.size();
				int serverCnt = folder.getMessageCount();
				if (serverCnt != localCnt) {
					int newMails = serverCnt - localCnt;
					if (newMails > 0) {
						// as the number of mails has changed update the folder
						if (!folder.isOpen())
							folder.open(Folder.READ_WRITE);
						for (int i = localCnt; i < serverCnt; i++) {
							Message msg = folder.getMessage(i+1);
							if (msg != null) {
								EmailTableData etd = new EmailTableData(msg);
								if (!mailList.contains(etd))
									mailList.add(etd);
							}
						}
						mailList.sort(null);
						folder.close(true);
					}
				}
				return null;
			}
			
		};
	}

}
