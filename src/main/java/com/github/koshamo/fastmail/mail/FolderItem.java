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

import java.lang.ref.SoftReference;

import javax.mail.Folder;
import javax.mail.MessagingException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

/**
 * @author jochen
 *
 */
public class FolderItem implements MailTreeViewable {

	private Folder folder;

	private ObservableList<EmailTableData> inbox = null;
	private InboxWatcher inboxWatcher;
	private final boolean isInbox;
	private SoftReference<ObservableList<EmailTableData>> folderContent = null;

	public FolderItem(Folder folder) {
		this.folder = folder;
		
		if ("INBOX".equals(folder.getFullName())) {
			isInbox = true;
			// get all the content of this accounts inbox and store it in the list
			inbox = FXCollections.observableArrayList();
			MailLister ml = new MailLister(this.folder, inbox);
			Thread mlt = new Thread(ml);
			mlt.setDaemon(true);
			mlt.start();
			// add a listener to the inbox
			inboxWatcher = new InboxWatcher(this.folder, inbox);
			inboxWatcher.setPeriod(Duration.seconds(20));
			inboxWatcher.setDelay(Duration.seconds(30));
			inboxWatcher.start();
			inbox.sort(null);
		}
		else {
			isInbox = false;
		}

	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#isAccount()
	 */
	@Override
	public boolean isAccount() {
		return false;
	}

	@Override
	public String toString() {
		return folder.getFullName();
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolderContent()
	 */
	@Override
	public ObservableList<EmailTableData> getFolderContent() {
		if (isInbox)
			return inbox;
		if (folderContent == null) {
			ObservableList<EmailTableData> mails = FXCollections.observableArrayList();
			MailLister ml = new MailLister(this.folder, mails);
			Thread mlt = new Thread(ml);
			mlt.setDaemon(true);
			mlt.start();
			folderContent = new SoftReference<ObservableList<EmailTableData>>(mails);
		}
		return folderContent.get();
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getParentFolder()
	 */
	@Override
	public Folder getParentFolder() {
		try {
			return folder.getParent();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getName()
	 */
	@Override
	public String getName() {
		return folder.getFullName();
	}
	
	public boolean renameTo(Folder f) {
		try {
			return folder.renameTo(f);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolder()
	 */
	@Override
	public Folder getFolder() {
		return folder;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FolderItem))
			return false;
		FolderItem other = (FolderItem) obj;
		System.out.println(this.getName() + "--" + other.getName());
//		return this.folder.getStore().equals(other.folder.getStore())
//				&& this.getName().equals(other.getName());
		return this.getName().equals(other.getName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return folder.getFullName().hashCode() + folder.getStore().hashCode();
	}
}
