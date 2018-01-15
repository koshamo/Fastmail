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

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ResourceBundle;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import com.github.koshamo.fastmail.util.MessageItem;
import com.github.koshamo.fastmail.util.MessageMarket;
import com.github.koshamo.fastmail.util.SerializeManager;
import com.sun.mail.imap.IMAPMessage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * The class EmailTableData is the data object for the Table view
 * <p>
 * The class EmailTableData populates the observable list, holding all mails
 * within a folder and is shown in the table view. Some fields are read-only,
 * as they may never be changed, others are read-write, because they may be 
 * modified. Example: the From field may not be changed, but the status 
 * of read(seen) may be changed.
 * 
 * @author jochen
 *
 */
/**
 * @author jochen
 *
 */
public class EmailTableData implements Comparable<EmailTableData>{

	private SimpleStringProperty subject;
	private SimpleStringProperty fromName;
	private SimpleStringProperty fromAddress;
	// received date may be processed for functional folders (TODO)
	private Instant receivedDate;
	private Instant sentDate;
	private SimpleBooleanProperty attachment;
	private SimpleBooleanProperty read;
	private SimpleBooleanProperty marked;
	private MailData mailData;
	
	final ResourceBundle i18n;
	

	/**
	 * The constructor takes a Javamail Message and constructs a EmailTableData 
	 * object, which can be handled in the observable list
	 * 
	 * @param msg a message read from the mail account
	 */
	public EmailTableData (final Message msg) {
		i18n = SerializeManager.getLocaleMessages();
		// TODO: message can be expunged while running this method
		// this causes an exception.... but do we need to care for that message?
		
		// messages normally are marked as read, as soon as we get them from 
		// server. peek property should prevent this behavior, as long as we
		// have IMAP Messages, which should be true right now, as we only
		// work with IMAP accounts
		if (msg instanceof IMAPMessage) {
			IMAPMessage imapMsg = (IMAPMessage)msg;
			imapMsg.setPeek(true);
		}
		String adr;
		boolean attachment = false;
		try {
			adr = ((InternetAddress[]) msg.getFrom())[0].getPersonal();
			fromAddress = new SimpleStringProperty(((InternetAddress[]) msg.getFrom())[0].getAddress());
			if (adr == null || adr.isEmpty())
				adr = fromAddress.get();
			if (msg.isMimeType("multipart/mixed")) { //$NON-NLS-1$
				Multipart mp = (Multipart) msg.getContent();
				if (mp.getCount() > 1)
					attachment = true;
				else 
					// TODO: what, if image is the only content?
					// would it be better to check against not-TEXT?
					if (mp.getBodyPart(0).getContentType().contains("APPLICATION"))
						attachment = true;
			}
			this.subject = new SimpleStringProperty(msg.getSubject()); 
			if (this.subject.get() == null)
				this.subject.set(""); //$NON-NLS-1$
			this.fromName = new SimpleStringProperty(adr); 
			this.receivedDate = msg.getReceivedDate().toInstant();
			this.sentDate = msg.getSentDate().toInstant();
			this.read = new SimpleBooleanProperty(msg.isSet(Flag.SEEN));
			this.read.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
					try {
						msg.setFlag(Flag.SEEN, newVal.booleanValue());
					} catch (MessagingException e) {
						MessageItem mItem = new MessageItem(
								MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
										e.getMessage()),
								0.0, MessageItem.MessageType.EXCEPTION);
						MessageMarket.getInstance().produceMessage(mItem);
					}
				}
			});
			this.attachment = new SimpleBooleanProperty(attachment);
			this.marked = new SimpleBooleanProperty(msg.isSet(Flag.FLAGGED));
			this.marked.addListener(new ChangeListener<Boolean> () {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs, 
						Boolean oldVal, Boolean newVal) {
					try {
						msg.setFlag(Flag.FLAGGED, newVal.booleanValue());
					} catch (MessagingException e) {
						MessageItem mItem = new MessageItem(
								MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
										e.getMessage()),
								0.0, MessageItem.MessageType.EXCEPTION);
						MessageMarket.getInstance().produceMessage(mItem);
					}
				}
			});
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (IOException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailboxaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		mailData = new MailData(getFromAddress(), getFromName(), getSubject(),
				sentDate, receivedDate, attachment, msg);
	}
	
	
	/**
	 * This method is used by the PropertyValueFactory in the GUI to connect
	 * the object's editable value to the table view.
	 *  
	 * @return the property of the read / seen status
	 */
	public BooleanProperty readProperty() {
		return this.read;
	}
	

	/**
	 * This method is used by the PropertyValueFactory in the GUI to connect
	 * the object's editable value to the table view.

	 * @return the property of the marked status
	 */
	public BooleanProperty markedProperty() {
		return this.marked;
	}
	

	/**
	 * reads the message's subject
	 *  
	 * @return the subject as String
	 */
	public String getSubject() {
		return subject.get();
	}

	/**
	 * get the senders's address
	 * 
	 * @return the sender's displayed email name as String, if available, 
	 * otherwise the internet email address as String 
	 */
	public String getFromName() {
		return fromName.get();
	}
		
	/**
	 * get the sender's address as valid email address
	 * 
	 * @return the internet email address as string
	 */
	public String getFromAddress() {
		return fromAddress.get();
	}
	
	/**
	 * reads the email's received date. This may differ from the sent date
	 * @return the received date as Date object
	 */
	public String getReceivedDate() {
		return receivedDate.toString();
	}

	/**
	 * reads the email's sent date
	 * @return the sent date as Date object
	 */
	public String getSentDate() {
		return sentDate.toString();
	}

	/**
	 * This method is used by the PropertyValueFactory in the GUI to connect
	 * the object's editable value to the table view.

	 * @return the property of the attachment status
	 */
	public BooleanProperty attachmentProperty() {
		return this.attachment;
	}
	
	/**
	 * checks, if an attachment is available
	 * 
	 * @return true, if an attachment is present, false otherwise
	 */
	public boolean isAttachment() {
		return attachment.get();
	}

	
	/**
	 * this method returns the MailData object contained in the table data 
	 * object
	 * @return the MailData object representing the mail details
	 */
	public MailData getMailData() {
		return mailData;
	}

	/**
	 * this method returns the folder, in which this message resides
	 * @return	the folder of this message
	 */
	public Folder getFolder() {
		return mailData.getMessage().getFolder();
	}
	
	/**
	 * This method returns the message object, that contains the reference
	 * to the server side message
	 * @return	the message object of this mail
	 */
	public Message getMessage() {
		return mailData.getMessage();
	}
	
	/**
	 * this message provides the functionality to set a flag on the message. 
	 * This may be the SEEN or MARKED flag, amongst others.
	 * 
	 * @see javax.mail.Flags.Flag
	 * @param flag
	 * @param set
	 */
	public void setFlag(final Flags.Flag flag, final boolean set) {
		try {
			mailData.getMessage().setFlag(flag, set);
		} catch (MessagingException e) {
			MessageItem mItem = new MessageItem(
					MessageFormat.format(i18n.getString("exception.mailaccess"),  //$NON-NLS-1$
							e.getMessage()),
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
	}
	
	
	/* The natural order of Emails should be date based
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EmailTableData other) {
		/* 
		 * negate the Instant compareTo method, as we want to have the
		 * latest mails on top of the list
		 */
		return -this.sentDate.compareTo(other.sentDate);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass()) 
			return false;
		EmailTableData other = (EmailTableData) obj;
		// use negative test with shortcut
		if (!this.getSentDate().equals(other.getSentDate()) ||
				!this.getReceivedDate().equals(other.getReceivedDate()) ||
				!this.getFromName().equals(other.getFromName()) ||
				!this.getSubject().equals(other.getSubject()))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSentDate().hashCode() + getFromName().hashCode() 
				+ getSubject().hashCode();
	}

}
