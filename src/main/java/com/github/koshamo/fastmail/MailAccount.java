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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * MailAccount is the class that contains a mail accounts information.
 * <p>
 * In the classes constructor we build up a connection to the mail provider
 * all other methods work with the existing connection.
 * <p>
 * The inbox is always kept in memory for fast access. All other folders will be
 * loader as requested and then kept in memory using a SoftReference. Thus they
 * may be removed from memory, if memory gets short. This way we can speed up the
 * program, but make sure the available memory won't be oversized.
 * 
 * @author jochen
 *
 */
public class MailAccount {
	// some fields needed to create connection
	MailAccountData data;
	Properties props;
	Session session;
	Store store;
	boolean connected = false;
	boolean imap = false;
	
	// stores the folders within the mail account
	// TODO: think about fetching these dynamically, but maybe this will be done by
	// the to be implemented method to watch the account...
	Folder[] folders;

	private ObservableList<EmailTableData> inbox;
	private HashMap<String, SoftReference<ObservableList<EmailTableData>>> folderContentMap;
	
	private AccountFolderWatcher accountFolderWatcher;
	private InboxWatcher inboxWatcher;
	
	/* InboxWatcher detects new Messages at startup and adds mails to inbox redundant.
	 * This can be avoided, if InboxWatcher checks, if inbox has been initialized.
	 */
	private boolean setup;
	
	/**
	 * In this constructor we initialize the account.
	 * <p>
	 * The constructor also loads the inbox's content and stores it locally for
	 * faster access
	 * 
	 * @param username		the username for this account as fullly qualified 
	 * email address
	 * @param password		the account's password
	 * @param displayName	the display name for most email clients
	 * @param inboxType		type of inbox, currently only IMAP is supported
	 * @param inboxHost		host URL for retrieving (e.g. imap.gmail.com)
	 * @param smtpHost		host URL for sending (e.g. smtp.gmail.com)
	 * @param ssl			use SSL connection
	 * @param tls			use TLS authentification
	 */
	public MailAccount(String username, String password, String displayName,
			String inboxType, String inboxHost, String smtpHost,
			boolean ssl, boolean tls) {
		data = new MailAccountData(username, password, displayName,
				inboxType, inboxHost, smtpHost, ssl, tls);
		buildConnection();
	}

	/**
	 * In this constructor we initialize the account.
	 * <p>
	 * The constructor also loads the inbox's content and stores it locally for
	 * faster access

	 * @param data a MailAccountData object containing all relevant data for 
	 * server connection
	 */
	public MailAccount(MailAccountData data) {
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
		// get all the content of this accounts inbox and store it in the list
		inbox = FXCollections.observableArrayList();
		MailLister ml = new MailLister(this, "INBOX");
		ml.setOnSucceeded(ev -> {setup = ml.getValue().booleanValue();});
		Thread mlt = new Thread(ml);
		mlt.start();
		inbox.sort(null);
		folderContentMap = new HashMap<String, SoftReference<ObservableList<EmailTableData>>>();
	}
	
	/**
	 * This method is to check a new account configuration and returns a message 
	 * with a failure description or a success message.
	 * <p>
	 * This Method does the same as the constructor, but does not return a Account 
	 * object, but helps to setup a working configuration.
	 * 
	 * @param username		the username for this account as fullly qualified 
	 * email address
	 * @param password		the account's password
	 * @param displayName	the display name for most email clients
	 * @param inboxType		type of inbox, currently only IMAP is supported
	 * @param inboxHost		host URL for retrieving (e.g. imap.gmail.com)
	 * @param smtpHost		host URL for sending (e.g. smtp.gmail.com)
	 * @param ssl			use SSL connection
	 * @param tls			use TLS authentification
	 * @return				a String with a success or failure description
	 */
	public static String testConnection(String username, String password, String displayName,
	String inboxType, String inboxHost, String smtpHost,
	boolean ssl, boolean tls) {
		Properties props = new Properties();
		if ("IMAP".equals(inboxType)) { //$NON-NLS-1$
			props.setProperty("mail.imap.ssl.enable", new Boolean(ssl).toString()); //$NON-NLS-1$
		}
	    props.put("mail.smtp.host", smtpHost); //$NON-NLS-1$
	    props.setProperty("mail.smtp.starttls.enable", new Boolean(tls).toString()); //$NON-NLS-1$
		Session session = Session.getInstance(props);
		Store store = null;
		try {
			store = session.getStore(inboxType.toLowerCase());
			store.connect(inboxHost, username, password);
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
	 * Checks if the account already has connection to the server
	 * 
	 * @return true, if connected, otherwise false
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Checks if the account is a IMAP account
	 * 
	 * @return true, if IMAP account
	 */
	public boolean isIMAP() {
		return imap;
	}
	
	/**
	 * method reads the folders of the mail account and stores it in a field.
	 * folders are INBOX, Trash,... and all user generated folders to archive 
	 * and sort emails.
	 * <p>
	 * current limitations: we expect all folders to be in the default folder. 
	 * Right now I haven't ever seen a mailbox with several base folders / namespaces.
	 * Another limitation is, that we don't support hierarchical folder structures.
	 * This is at least used in googlemail account, where the default folder
	 * conatins a folder [googlemail], where additional folders exist. This most 
	 * probably will be supported soon, as there is my gmails spam box, that sometimes
	 * filters useful mails 
	 * 
	 * @return all folders within the mail account as a String array
	 */
	public String[] getFolders() {
		try {
			Folder rf = store.getDefaultFolder();
			folders = rf.list();
		} catch(MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> foldersAsString = new ArrayList<String>(folders.length);
		// TODO: convert this to lambda
		for (int i = 0; i < folders.length; ++i)
			foldersAsString.add(folders[i].getFullName());
		// resort Arraylist
		foldersAsString.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if ("INBOX".equals(o1))
					return -1;
				if ("INBOX".equals(o2))
					return 1;
				if ("Drafts".equals(o1))
					return -1;
				if ("Drafts".equals(o2))
					return 1;
				if ("Sent".equals(o1))
					return -1;
				if ("Sent".equals(o2))
					return 1;
				if ("Trash".equals(o1))
					return -1;
				if ("Trash".equals(o2))
					return 1;
				
				return o1.compareTo(o2);
			}
		});
		String[] ret = new String[folders.length];
		ret = foldersAsString.toArray(ret); 
		return ret;
	}
	
	/**
	 * this method providers the Folder class of a given String representation
	 * of this folder
	 * 
	 * @param folderName the String representation of the folder
	 * @return the appropriate Folder or null, if not found
	 */
	public Folder getFolder(String folderName) {
		if (folders == null)
			getFolders();
		for (Folder f : folders) {
			if (f.getFullName().equals(folderName))
				return f;
		}
		return null;
	}
	
	/**
	 * read all the messages within a given mail folder.
	 * <p>
	 * If the folder already has been read, the content can be returned directly.
	 * If the folder hasn't been read yet or be removed by the Garbage Collector,
	 * load the content and return it.
	 * 
	 * @param folder the folder, which mails will be read
	 * 
	 * @return the emails read as an ObservableList for direct usage in a TableView
	 */
	public ObservableList<EmailTableData> getMessages(String folder) {
		if ("INBOX".equals(folder))
			return inbox;
		if (!folderContentMap.isEmpty() && folderContentMap.containsKey(folder)) {
			ObservableList<EmailTableData> data = folderContentMap.get(folder).get();
			if (data != null)
				return data;
			// the SoftReference has been removed from Garbage Collector, so remove entry
			folderContentMap.remove(folder);
		}
		ObservableList<EmailTableData> data = FXCollections.observableArrayList();
		for (Folder f : folders) {
			if (folder.equals(f.getFullName())) {
				try {
					f.open(Folder.READ_WRITE);
					MailLister lister = new MailLister(this, folder);
					Thread t = new Thread(lister);
					t.start();
					folderContentMap.put(
							folder, new SoftReference<ObservableList<EmailTableData>>(data));
					f.close(true);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		data.sort(null);
		return data;
	}
	
	
	/**
	 * this method informs about the number of mails in the inbox
	 * 
	 * @return number of messages in the inbox
	 */
	public int getInboxCount() {
		return inbox.size();
	}
		
	/**
	 * Direct access to the INBOX
	 * 
	 * @return the ObservableList of the account's inbox
	 */
	public ObservableList<EmailTableData> getInbox() {
		return inbox;
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
	 * Removes a message from the email folder locally as well as on the server.
	 * 
	 * @param msg the email message to remove
	 * @param currentFolder the folder, where the email is in
	 * @return true, if the mail could be removed, otherwise false
	 */
	public boolean deleteMessage(EmailTableData msg, String currentFolder) {
		Folder f = getFolder(currentFolder);
		boolean deleted = false;
		try {
			if (!f.isOpen())
				f.open(Folder.READ_WRITE);
			Message[] messages = f.getMessages();
			for (Message m : messages) {
				if (msg.equals(new EmailTableData(m))) {
					f.setFlags(new Message[] {m}, new Flags(Flags.Flag.DELETED), true);
					deleted = true;
					break; // message found, so no further processing needed
				}
			}
			f.close(true);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (deleted)
			inbox.remove(msg);
		return deleted;
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
	public void sendMail(String to, String cc, String subject, String text, 
			List<File> attachments, Message message) {
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
			// send
			Transport.send(msg, data.getUsername(), data.getPassword());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get the settings data of this mail account as a sumarized object
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
	public void setMailAccountData(MailAccountData data) {
		this.data = data;
	}
	
	/**
	 * Get the Account Folder Watcher for this account
	 * @return account's AccountFolderWatcher
	 */
	public AccountFolderWatcher getAccountFolderWatcher() {
		return accountFolderWatcher;
	}

	/** 
	 * Set the Account Folder Watcher for this account
	 * @param accountFolderWatcher this account's AccountFolderWatcher
	 */
	public void setAccountFolderWatcher(AccountFolderWatcher accountFolderWatcher) {
		this.accountFolderWatcher = accountFolderWatcher;
	}

	/**
	 * Get the Inbox Watcher for this account
	 * @return account's InboxWatcher
	 */
	public InboxWatcher getInboxWatcher() {
		return inboxWatcher;
	}

	/** 
	 * Set the Inbox Watcher for this account
	 * @param inboxWatcher this account's InboxWatcher
	 */
	public void setInboxWatcher(InboxWatcher inboxWatcher) {
		this.inboxWatcher = inboxWatcher;
	}
	
	
}
