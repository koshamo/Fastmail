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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.github.koshamo.fastmail.FastMailGenerals;
import com.github.koshamo.fastmail.util.MailTools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;

/**
 * MailAccount is the class that contains a mail account information.
 * <p>
 * In the classes constructor we build up a connection to the mail provider
 * all other methods work with the existing connection.
 * <p>
 * The MailAccount implements MailTreeViewable to be shown in the tree view
 *  
 * @author jochen
 *
 */
public class MailAccount implements MailTreeViewable{
	// some fields needed to create connection
	private MailAccountData data;
	private Properties props;
	private Session session;
	private Store store;
	private boolean connected = false;
	private boolean imap = false;
	
	// stores the folders within the mail account
	// TODO: think about fetching these dynamically, but maybe this will be done by
	// the to be implemented method to watch the account...
	private Folder[] folders;
	private Folder parentFolder; 

	private AccountFolderWatcher accountFolderWatcher;
	
	private TreeItem<MailTreeViewable> accountTreeItem;
	
	/* InboxWatcher detects new Messages at startup and adds mails to inbox redundant.
	 * This can be avoided, if InboxWatcher checks, if inbox has been initialized.
	 */
	private boolean setup;
	

	/**
	 * In this constructor we initialize the account.
	 * 
	 * @param data a MailAccountData object containing all relevant data for 
	 * server connection
	 */
	public MailAccount(final MailAccountData data) {
		this.data = data;
		buildConnection();
	}
	
	/**
	 * this method does the actual work for the constructor
	 */
	private void buildConnection() {
		setup = false;
		props = new Properties();
		if ("IMAP".equals(data.getInboxType())) { //$NON-NLS-1$
			props.setProperty("mail.imap.ssl.enable", new Boolean(data.isSsl()).toString()); //$NON-NLS-1$
			// currently we only work with IMAP...
			imap = true;
		}
		props.put("mail.smtp.host", data.getSmtpHost()); //$NON-NLS-1$
		props.setProperty("mail.smtp.starttls.enable", new Boolean(data.isTls()).toString()); //$NON-NLS-1$
		session = Session.getInstance(props);
		store = null;
		folders = null;
		try {
			store = session.getStore(data.getInboxType().toLowerCase());
			store.connect(data.getInboxHost(), data.getUsername(), data.getPassword());
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connected = true;
	}
	
	
	/**
	 * This method has to be called to add a folder watcher to this account. 
	 * The folder watcher loads all folders stored on the server and adds them
	 * to the tree view. If any folders change, the folder watcher will
	 * detect this and add or delete the folders to / from the tree view.
	 *  
	 * @param accountTreeItem
	 */
	public void addFolderWatcher(final TreeItem<MailTreeViewable> accountTreeItem) {
		this.accountTreeItem = accountTreeItem;
		accountFolderWatcher = new AccountFolderWatcher(this, accountTreeItem);
		accountFolderWatcher.setPeriod(Duration.seconds(60));
		accountFolderWatcher.start();
	}

	/**
	 * To delete this account from the tree view, use this method, as it also
	 * stops the folder watcher.
	 */
	public void remove() {
		accountFolderWatcher.cancel();
		accountTreeItem.getParent().getChildren().remove(accountTreeItem);
	}
	
	/**
	 * This method is to test a new account configuration and returns a message 
	 * with a failure description or a success message.
	 * <p>
	 * This Method does the same as the constructor, but does not return a 
	 * Account object, but helps to setup a working configuration.
	 * 
	 * @param data	a MailAccountData object containing the account settings
	 * @return		a String with a success or failure description
	 */
	public static String testConnection(final MailAccountData data) {
		Properties props = new Properties();
		if ("IMAP".equals(data.getInboxType())) { //$NON-NLS-1$
			props.setProperty("mail.imap.ssl.enable", new Boolean(data.isSsl()).toString()); //$NON-NLS-1$
		}
	    props.put("mail.smtp.host", data.getSmtpHost()); //$NON-NLS-1$
	    props.setProperty("mail.smtp.starttls.enable", new Boolean(data.isTls()).toString()); //$NON-NLS-1$
		Session session = Session.getInstance(props);
		Store store = null;
		try {
			store = session.getStore(data.getInboxType().toLowerCase());
			store.connect(data.getInboxHost(), data.getUsername(), data.getPassword());
		} catch (NoSuchProviderException e) {
			return "Failed to get connection to server. Please check server URLs and Protocol type.";
		} catch (AuthenticationFailedException e) {
			return "Failed to authenticate to the server. Please check username and password.";
		} catch (IllegalStateException e) {
			return "Failed to cennect to the server. This account already seems to be connected.";
		} catch (MessagingException e) {
			return "Failed to connect to server. Reason: " + e.getMessage();
		}
		return "Test successfull! Now push the Add Account button!";
	}		
		
	
	/**
	 * supplies a unique String of the account
	 * @return the username
	 */
	public String getAccountName() {
		return data.getUsername();
	}
	
	
	
	/**
	 * Get all folders of this mail account
	 * @return	all folders as array
	 */
	public Folder[] getFolders() {
		try {
			parentFolder = store.getDefaultFolder();
			folders = parentFolder.list();
			return folders;
		} catch(MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
		
	/* (non-Javadoc)
	 * closes the connection to the server
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			store.close();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * This Method is for InboxWatcher to check, if mailbox has been set up.
	 * <p>
	 * If the mailbox has not been set up, new mails are probably old mails
	 * still in the inbox.
	 * TODO: what happens, if old mails are in inbox and new mails have come
	 * since last startup? How to differ between new and old mails?
	 * This may most probably only recognized using local storage of mails.
	 * @return
	 */
	public boolean isSetup() {
		return setup;
	}
	
	/**
	 * this method sends a mail from this account.
	 * 
	 * @param to		the recipients as a semicolon separated list of email
	 * addresses stored in a string
	 * @param cc		the cc recipients as a semicolon separated list of email
	 * addresses stored in a string
	 * @param subject	the email subject
	 * @param text		the email text
	 * @param message	the message object to reply to
	 */
	public void sendMail(final String to, final String cc, 
			final String subject, final String text, 
			final List<File> attachments, final Message message) {
		MimeMessage msg;
		Message m;
		try {
			if (message != null) {
				if (message instanceof MimeMessage) 
					m = ((MimeMessage) message).reply(true, true);
				else
					m = message.reply(true);
				msg = (MimeMessage) m;
			}
			else
				msg = new MimeMessage(session);
			InternetAddress ia = new InternetAddress(data.getUsername(), data.getDisplayName());
			msg.setFrom(ia);
			msg.setRecipients(RecipientType.TO, MailTools.parseAddresses(to));
			if (cc != null && !cc.isEmpty())
				msg.setRecipients(RecipientType.CC, MailTools.parseAddresses(cc));
			msg.setSubject(subject);
			msg.setSentDate(Date.from(Instant.now()));
			msg.setText(text);
			msg.setHeader("X-mailer", FastMailGenerals.getNameVersion()); //$NON-NLS-1$
			// attachments
			if (attachments != null && !attachments.isEmpty()) {
				MimeMultipart mmp = new MimeMultipart("mixed");
				MimeBodyPart content = new MimeBodyPart();
				content.setContent(text, "text/plain");
				mmp.addBodyPart(content);
				for (File file : attachments) {
					MimeBodyPart mbp = new MimeBodyPart();
					mbp.attachFile(file);
					mmp.addBodyPart(mbp);
				}
				msg.setContent(mmp);
			}
			// send with inline Thread
			Thread t = new Thread(new Task<Void>(){
				@Override
				protected Void call() throws Exception {
					Transport.send(msg, data.getUsername(), data.getPassword());
					return null;
				}
			});
			t.start();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get the settings data of this mail account as a summarized object
	 * @return the settings data object for this account
	 */
	public MailAccountData getMailAccountData() {
		return data;
	}

	/**
	 * Set the settings data of this mail account
	 * 
	 * @param data a MailAccountData object
	 */
	public void setMailAccountData(final MailAccountData data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#isAccount()
	 */
	@Override
	public boolean isAccount() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return data.getUsername();
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolderContent()
	 */
	@Override
	public ObservableList<EmailTableData> getFolderContent() {
		return FXCollections.observableArrayList();
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getParentFolder()
	 */
	@Override
	public Folder getParentFolder() {
		return parentFolder;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getName()
	 */
	@Override
	public String getName() {
		return getAccountName();
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolder()
	 */
	@Override
	public Folder getFolder() {
		return parentFolder;
	}
	
}
