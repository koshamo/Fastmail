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

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

/**
 * The InboxWatcher is used to check, if new mails have arrived to the mail box
 * and also notifies us, when an email has been deleted from elsewhere.
 * <p>
 * InboxWatcher and FolderSynchronizer are very similar, but currently have
 * different purposes and thus specializations:
 * <p>
 * InboxWatcher only checks for new mails and adds them to the local 
 * mail list and notifies the user of newly arrived mails, whereas 
 * FolderSynchronizer checks the folder and the local mail list, if any
 * differences exists and updates the local list.
 * <p>
 * Currently we use both tasks, as we can define different periods for both 
 * tasks to check the server folder. Maybe it would be a great idea to 
 * integrate InboxWatcher into FolderSynchronizer and leave this class. 
 * 
 * @author jochen
 *
 */
public class InboxWatcher extends ScheduledService<Void> {

	/*package private*/ Folder folder;
	/*package private*/ ObservableList<EmailTableData> mailList;
	
	/**
	 * The constructor is aimed to can watch any folder, although most
	 * probably it will only be used for the inbox. 
	 * <p>
	 * 
	 * @param folder	the folder to watch
	 * @param mailList	the observable list to connect the server folder to the GUI
	 */
	public InboxWatcher(final Folder folder, 
			final ObservableList<EmailTableData> mailList) {
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
					}
				}
				return null;
			}
			
		};
	}

}
