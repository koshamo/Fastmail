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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

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

	MailAccount account;
	/**
	 * the constructorof InboxWatcher adds Action Listeners to the INBOX folder
	 * <p>
	 * It seems that the action listeners don't work as expected.
	 * 
	 * @param account the object storing all available mail accounts
	 * @param emailList the observable list of mails, to add newly arrived mails
	 */
	// TODO: check the usage and/or usability of the action listeners, as they 
	// don't work currently
	public InboxWatcher(MailAccount account) {
		this.account = account;
		Folder inbox = null;
		try {
			inbox = account.getFolder("INBOX");
			if (inbox == null || !inbox.exists())
				return;
			if (!inbox.isOpen())
				inbox.open(Folder.READ_WRITE);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// POP3 accounts don't allow message handler, thus we only use IMAP accounts
		// TODO: my IMAP accounts don't respond to the handlers
		if (account.isIMAP()) {
			inbox.addMessageCountListener(new MessageCountAdapter() {
				@Override
				public void messagesAdded(MessageCountEvent mce) {
					Message[] msgs = mce.getMessages();
					System.out.println("****");
					System.out.println("Account: " + account.getAccountName() 
					+ " hat " + msgs.length + " neue Nachrichten");
					System.out.println("****");
					for (Message msg : msgs) {
						account.getInbox().add(new EmailTableData(msg));
					}
				}
				@Override
				public void messagesRemoved(MessageCountEvent mce) {
					// TODO: synchronize the mailbox
					System.out.println("Message Removed Event");
				}
			});
		}
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
				if (!account.isSetup())
					return null;
				Folder serverInbox = account.getFolder("INBOX");
				if (serverInbox == null || !serverInbox.exists())
					return null;
				int cnt = serverInbox.getMessageCount();
				if (cnt != account.getInboxCount()) {
					int newMails = cnt - account.getInboxCount();
					if (newMails > 0) {
						// TODO: add this message to the GUI, however this will be done
						// (using status line, using popup,...)
						System.out.println(account.getAccountName() + " has " + newMails + " new Mails!");
						// as the number of mails has changed update the folder
						if (!serverInbox.isOpen())
							serverInbox.open(Folder.READ_WRITE);
						for (int i = account.getInboxCount(); i < cnt; i++) {
							Message msg = serverInbox.getMessage(i+1);
							if (msg != null)
								account.getInbox().add(new EmailTableData(msg));
						}
						account.getInbox().sort(null);
						serverInbox.close(true);
					}
				}
				return null;
			}
			
		};
	}

}
