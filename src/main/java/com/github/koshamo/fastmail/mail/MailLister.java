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

import javafx.collections.ObservableList;
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
 * new Thread(new Maillister(folder, oberservableEmailDataList)).start();
 *  
 * @author jochen
 *
 */
public final class MailLister extends Task<Boolean>{

	private Folder folder;
	private ObservableList<EmailTableData> mailList;
	private volatile boolean stop = false;
	
	/**
	 * builds the MailLister for a given folder 
	 * 
	 * @param folder	the folder to be listed
	 * @param mailList 	the observable list to be filled
	 */
	public MailLister(final Folder folder, final ObservableList<EmailTableData> mailList) {
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
		// iterate over the accounts to find the matching one
		if (stop) return new Boolean(false); // check if thread needs to be stopped

		try {
			folder.open(Folder.READ_WRITE);
			msg = folder.getMessages();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (msg != null) {
			// get messages and add them to the observable table view list
			for (Message message : msg) {
				if (stop) return null; // check if thread needs to be stopped
					mailList.add(new EmailTableData(message));
			}
			mailList.sort(null);
		}
		return new Boolean(true);
	}
	
}
