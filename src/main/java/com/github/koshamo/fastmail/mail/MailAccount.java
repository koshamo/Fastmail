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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.AuthenticationFailedException;
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
import com.github.koshamo.fastmail.events.MailAccountMeta;
import com.github.koshamo.fastmail.events.MailAccountOrders;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.concurrent.Task;

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
// TODO:
public class MailAccount /*implements MailTreeViewable*/{
	// some fields needed to create connection
	private final MailAccountData mailAccountData;
	private final MailModule mailModule;
	private Properties props;
	private Session session;
	private Store store;
	
	// stores the parent folder within the mail account
	private Folder parentFolder; 

	private AccountFolderWatcher accountFolderWatcher;
	
//	private TreeItem<MailTreeViewable> accountTreeItem;
	
	private static ResourceBundle i18n;
	

	/**
	 * In this constructor we initialize the account.
	 * 
	 * @param data 	a MailAccountData object containing all relevant data for 
	 * server connection
	 * @param messageBus	the MessageBus, to which error messages can be 
	 * delivered
	 */
	public MailAccount(final MailAccountData data, final MailModule mailModule) {
		this.mailAccountData = data;
		this.mailModule = mailModule;
		this.store = null;
		i18n = SerializeManager.getLocaleMessageBundle();
		props = createSessionProperties();
	}
	
	/**
	 * this method does the actual work for the constructor
	 */
	public void connect() {
		if (store == null || !store.isConnected()) {
			session = Session.getInstance(props);
			try {
				store = session.getStore(mailAccountData.getInboxType().toLowerCase());
				store.connect(mailAccountData.getInboxHost(), mailAccountData.getUsername(), mailAccountData.getPassword());
			} catch (@SuppressWarnings("unused") NoSuchProviderException e) {
				postMessage("Provider Unknown");
			} catch (AuthenticationFailedException e) {
				postMessage("Authentication Failed: " +e.getMessage());
			} catch (MessagingException e) {
				postMessage("Something weird happened connecting to mail server");
			}
		}
		addFolderWatcher();
	}

	/**
	 * 
	 */
	private Properties createSessionProperties() {
		Properties props = new Properties();
		if ("IMAP".equals(mailAccountData.getInboxType())) { //$NON-NLS-1$
			props.setProperty("mail.imap.ssl.enable", new Boolean(mailAccountData.isSsl()).toString()); //$NON-NLS-1$
		}
		props.put("mail.smtp.host", mailAccountData.getSmtpHost()); //$NON-NLS-1$
		props.setProperty("mail.smtp.starttls.enable", new Boolean(mailAccountData.isTls()).toString()); //$NON-NLS-1$
		return props;
	}
	
	
	/**
	 * This method has to be called to add a folder watcher to this account. 
	 * The folder watcher loads all folders stored on the server and adds them
	 * to the tree view. If any folders change, the folder watcher will
	 * detect this and add or delete the folders to / from the tree view.
	 *  
	 * @param accountTreeItem
	 */
	public void addFolderWatcher() {
		accountFolderWatcher = new AccountFolderWatcher(this);
		new Thread(accountFolderWatcher).start();;
	}

	/**
	 * Cares for a clean shutdown process
	 */
	public void shutdown() {
		accountFolderWatcher.stop();
	}
	
	/**
	 * Propagates the Data Events to the message bus
	 * 
	 * @param order	the order for the meta data
	 * @param data	the actual data
	 */
	/* private */ <T> void postDataEvent(MailAccountOrders order, T data) {
		mailModule.postEvent(
				new MailAccountMeta(mailAccountData.getUsername(), order), data);
	}
	/**
	 * To delete this account from the tree view, use this method, as it also
	 * stops the folder watcher.
	 */
//	public void remove() {
//		accountFolderWatcher.cancel();
//		accountTreeItem.getParent().getChildren().remove(accountTreeItem);
//	}
	
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
		if (i18n == null)
			i18n = SerializeManager.getLocaleMessageBundle();
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
		} catch (@SuppressWarnings("unused") NoSuchProviderException e) {
			return i18n.getString("error.URL"); //$NON-NLS-1$
		} catch (@SuppressWarnings("unused") AuthenticationFailedException e) {
			return i18n.getString("error.username"); //$NON-NLS-1$
		} catch (@SuppressWarnings("unused") IllegalStateException e) {
			return i18n.getString("error.connected"); //$NON-NLS-1$
		} catch (MessagingException e) {
			return i18n.getString("error.connected") + e.getMessage(); //$NON-NLS-1$
		}
		return i18n.getString("info.settingsOK"); //$NON-NLS-1$
	}		
		
	/*private*/ void postMessage(String message) {
		mailModule.postMessage(message);
	}
	/**
	 * supplies a unique String of the account
	 * @return the username
	 */
	public String getAccountName() {
		return mailAccountData.getUsername();
	}
	
	
	
	/**
	 * Get all folders of this mail account
	 * @return	all folders as array
	 */
//	public Folder[] getFolders() {
//		try {
//			parentFolder = store.getDefaultFolder();
//			return parentFolder.list();
//		} catch(MessagingException e) {
//			mailModule.postMessage(i18n.getString("exception.mailaccess"));
//		}
//		return null;
//	}

	/**
	 * Get the default folder of the account, which is the root of the account
	 * 
	 * @return	the root folder of the account
	 */
	public Folder getDefaultFolder() {
		try {
			Folder folder = store.getDefaultFolder();
			return folder;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * A new Folder will be added to this account with a default folder name.
	 */
	public void addFolder() {
		try {
			Folder folder = parentFolder.getFolder(i18n.getString("entry.newfolder")); //$NON-NLS-1$
			if (!folder.exists())
				folder.create(Folder.HOLDS_MESSAGES);
			// trigger the addition of the new folder to the tree view
//			forceFolderUpdate();
		} catch (MessagingException e) {
			mailModule.postMessage(i18n.getString("exception.mailaccess"));
		}
	}
	
	/**
	 * Triggers the execution of a new AccountFolderWatcher task to force the
	 * update of the tree view after changing folder items
	 */
//	public void forceFolderUpdate() {
//		new Thread(accountFolderWatcher.createTask()).start();
//	}
	
	
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
			mailModule.postMessage(i18n.getString("exception.mailaccess"));
		}
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
		mailModule.postMessage(i18n.getString("entry.sendmail"));
		
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
			InternetAddress ia = new InternetAddress(
					mailAccountData.getUsername(), mailAccountData.getDisplayName(), 
					java.nio.charset.StandardCharsets.ISO_8859_1.toString());
			msg.setFrom(ia);
			msg.setRecipients(RecipientType.TO, MailTools.parseAddresses(to));
			if (cc != null && !cc.isEmpty())
				msg.setRecipients(RecipientType.CC, MailTools.parseAddresses(cc));
			msg.setSubject(subject, java.nio.charset.StandardCharsets.ISO_8859_1.toString());
			msg.setSentDate(Date.from(Instant.now()));
			msg.setText(text, java.nio.charset.StandardCharsets.ISO_8859_1.toString());
			msg.setHeader("X-mailer", FastMailGenerals.getNameVersion()); //$NON-NLS-1$
			// attachments
			if (attachments != null && !attachments.isEmpty()) {
				MimeMultipart mmp = new MimeMultipart("mixed"); //$NON-NLS-1$
				MimeBodyPart content = new MimeBodyPart();
				content.setContent(text, "text/plain"); //$NON-NLS-1$
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
					Transport.send(msg, mailAccountData.getUsername(), mailAccountData.getPassword());
					mailModule.postMessage("Mail sent");
					return null;
				}
			});
			t.start();
		} catch (MessagingException e) {
			mailModule.postMessage(i18n.getString("exception.mailaccess"));
		} catch (IOException e) {
			mailModule.postMessage(i18n.getString("exception.mailboxaccess"));
		}
	}

	/**
	 * Get the settings data of this mail account as a summarized object
	 * @return the settings data object for this account
	 */
	public MailAccountData getMailAccountData() {
		return mailAccountData;
	}

	// TODO: delete this method -> if settings have changed, create a new
	// MailAccount
	/**
	 * Set the settings data of this mail account
	 * 
	 * @param data a MailAccountData object
	 */
//	public void setMailAccountData(final MailAccountData data) {
//		this.data = data;
//	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#isAccount()
	 */
//	@Override
//	public boolean isAccount() {
//		return true;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
//	@Override
//	public String toString() {
//		return data.getUsername();
//	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolderContent()
	 */
//	@Override
//	public ObservableList<EmailTableData> getFolderContent() {
//		return FXCollections.observableArrayList();
//	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getParentFolder()
	 */
//	@Override
//	public Folder getParentFolder() {
//		return parentFolder;
//	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getName()
	 */
//	@Override
//	public String getName() {
//		return getAccountName();
//	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolder()
	 */
//	@Override
//	public Folder getFolder() {
//		return parentFolder;
//	}
	
}
