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

import javafx.concurrent.Task;

/**
 * This class reads a folder and fills the table view with the 
 * contents of a mail folder.
 * <p>
 * This class is designed to be created just for a thread execution.
 * Thus it should be used inline or in local space and not be reused.
 * To guarantee this, no getter and setter methods exist and the 
 * fields to configure runtime are final.
 * <p>
 * Usage: 
 * new Thread(new Maillister(accountList, observableList, account, foler)).start();
 *  
 * @author jochen
 *
 */
public final class MailLister extends Task<Boolean>{
	private final MailAccount account;
	private final String folderName;
	private volatile boolean stop = false;
	
	/**
	 * Constructor needs the accounts 
	 * 
	 * @param accounts2 the mail accounts to process
	 * @param emailList the observable list, that holds the table items 
	 * to be filled dynamically
	 */
	public MailLister(MailAccount account, String folderName) {
		this.account = account;
		this.folderName = folderName;
	}

	/**
	 * stops the execution of the thread
	 * <p>
	 * if a new folder is selected, but the current thread still is reading data 
	 * from the mail server to populate the table view, the thread needs to be 
	 * stopped, as we currently use only one observable list to display the folders
	 * content. This is due to the current design of holding only data, which is
	 * actually needed 
	 */
	public void setStop() {
		stop = true;
	}

	/* 
	 * this task will be used in a thread and reads the folder of the given 
	 * account and puts all emails in the observable list, which will populate
	 * the table view.
	 * 
	 * @see javafx.concurrent.Task#call()
	 */
	@Override
	protected Boolean call() throws Exception {
		Message[] msg = null;
		Folder folder = null;
		// iterate over the accounts to find the matching one
		if (stop) return new Boolean(false); // check if thread needs to be stopped
		// find the folder to be processed
		for (String f : account.getFolders()) {
			if (stop) return new Boolean(false); // check if thread needs to be stopped
			if (f.equals(folderName)) {
				// folder found! read the items and populate the list
				folder = account.getFolder(folderName);
				if (folder == null) return new Boolean(false);
				if (stop) return new Boolean(false); // check if thread needs to be stopped
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
			// get messages and add them to the observable table view list
			for (Message message : msg) {
				if (stop) return null; // check if thread needs to be stopped
				account.getMessages(folderName).add(new EmailTableData(message));
//				System.out.println(message.getMessageNumber());
			}
			account.getMessages(folderName).sort(null);
		}
		return new Boolean(true);
	}
	
}
