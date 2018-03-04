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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.util.MessageItem;
import com.github.koshamo.fastmail.util.MessageMarket;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

/**
 * FolderItem class populates the tree view containing accounts and folders.
 * <p>
 * The folder item holds references to its content, which can be a strong
 * reference to the INBOX folder or a soft reference to any other folder,
 * depending on its representing data.
 * <p>
 * This class adds adapter methods to delete messages, rename the folder, etc.
 * 
 * @author jochen
 *
 */
public class FolderItem implements MailTreeViewable {

	private Folder folder;

	private ObservableList<EmailTableData> inbox = null;
	private final boolean isInbox;
	private SoftReference<ObservableList<EmailTableData>> folderContent = null;
	private FolderSynchronizerTask fst;
	
	private final ResourceBundle i18n;

	public FolderItem(final Folder folder) {
		this.folder = folder;
		i18n = SerializeManager.getLocaleMessageBundle();
		
		if ("INBOX".equals(folder.getFullName())) { //$NON-NLS-1$
			isInbox = true;
			// get all the content of this accounts inbox and store it in the list
			inbox = FXCollections.observableList(new Vector<EmailTableData>());
			// synchronize folder on a regular basis
			FolderSynchronizer folderSynchronizer = new FolderSynchronizer(this.folder, inbox);
			folderSynchronizer.setPeriod(Duration.seconds(10));
			folderSynchronizer.start();
			// add a listener to the inbox
			InboxWatcher inboxWatcher = new InboxWatcher(this.folder, inbox);
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
		return folder.getName();
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolderContent()
	 */
	@Override
	public ObservableList<EmailTableData> getFolderContent() {
		if (isInbox)
			return inbox;
		if (folderContent == null) {
			ObservableList<EmailTableData> mails = 
					FXCollections.observableList(new Vector<EmailTableData>());
			folderContent = 
					new SoftReference<ObservableList<EmailTableData>>(mails);
		}
		fst = new FolderSynchronizerTask(this.folder, folderContent.get());
		Thread mlt = new Thread(fst);
		mlt.setDaemon(true);
		mlt.start();
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
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getName()
	 */
	@Override
	public String getName() {
		return folder.getName();
	}
	
	/**
	 * to rename the current folder this method needs a new locally 
	 * constructed folder object containing the new name. The new folder
	 * should not exist on the server.
	 * <p>
	 * This method is an adapter to the Folder class of Javamail.
	 * 
	 * @param f	the folder containing the new name
	 * @return	true, if renaming was successful, false otherwise
	 */
	public boolean renameTo(final Folder f) {
		try {
			if (folder.isOpen())
				folder.close(true);
			if (f.isOpen())
				f.close(true);
			return folder.renameTo(f);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
			return false;
		}
	}
	
	/**
	 * Removes a message from the email folder locally as well as on the server.
	 * 
	 * @param emailTableData the email message to remove
	 * @return true, if the mail could be removed, otherwise false
	 */
	public boolean deleteMessage(final EmailTableData emailTableData) {
		Folder f = emailTableData.getFolder();
		boolean deleted = false;
		try {
			if (!f.isOpen())
				f.open(Folder.READ_WRITE);
			emailTableData.setFlag(Flags.Flag.DELETED, true);
			deleted = true;
			f.close(true);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		
		if (deleted) {
			this.getFolderContent().removeAll(emailTableData);
		}
		return deleted;
	}

	/**
	 * Removes this Folder from the server. Any mails or subfolders 
	 * in this folder will be deleted recursively.
	 */
	public void removeFolder() {
		try {
			if (folder.isOpen())
				folder.close(true);
			folder.delete(true);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		// TODO: FolderSynchronizer still throws exception after folder deletion
		fst.stop();
		folderContent = null;
	}
	
	
	public void moveMessage(EmailTableData mail) {
		try {
			// copy message to new folder
			mail.getFolder().copyMessages(new Message[] {mail.getMessage()}, folder);
			// delete message in original folder
			mail.setFlag(Flag.DELETED, true);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolder()
	 */
	@Override
	public Folder getFolder() {
		return folder;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;
		FolderItem other = (FolderItem) obj;
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
