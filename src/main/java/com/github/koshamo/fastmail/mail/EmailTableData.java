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
import java.time.Instant;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import com.github.koshamo.fastmail.util.MailTools;

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
 * as they may never be changed, others are read-write, because they may be modified.
 * Example: the From field may not be changed, but the status of read(seen) may be
 * changed.
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
	private SimpleStringProperty from;
	private SimpleStringProperty fromAddress;
	private Instant receivedDate;
	private Instant sentDate;
	private SimpleBooleanProperty attachment;
	private SimpleBooleanProperty read;
	private SimpleBooleanProperty marked;
	private MailData mailData;
	

	/**
	 * The constructor takes a javamail Message an constructs a EmailTableData object,
	 * which can be handled in the observable list
	 * 
	 * @param msg a message read from the mail account
	 */
	public EmailTableData (Message msg) {
		String adr;
		try {
			adr = ((InternetAddress[]) msg.getFrom())[0].getPersonal();
			fromAddress = new SimpleStringProperty(((InternetAddress[]) msg.getFrom())[0].getAddress());
			if (adr == null || adr.isEmpty())
				adr = fromAddress.get();
			boolean attachment = false;
			if (msg.isMimeType("multipart/mixed")) { //$NON-NLS-1$
				Multipart mp = (Multipart) msg.getContent();
				if (mp.getCount() > 1)
					attachment = true;
			}
			this.subject = new SimpleStringProperty(msg.getSubject()); 
			if (this.subject.get() == null)
				this.subject.set(""); //$NON-NLS-1$
			this.from = new SimpleStringProperty(adr); 
			this.receivedDate = msg.getReceivedDate().toInstant();
			this.sentDate = msg.getSentDate().toInstant();
			this.read = new SimpleBooleanProperty(msg.isSet(Flag.SEEN));
			this.read.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
					try {
						msg.setFlag(Flag.SEEN, newVal.booleanValue());
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mailData = MailTools.getMessage(msg);
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
	public String getFrom() {
		return from.get();
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

	public MailData getMailData() {
		return mailData;
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
	public boolean equals(Object other) {
		EmailTableData extern;
		if (other instanceof EmailTableData) 
			extern = (EmailTableData) other;
		else return false;
		// use negative test with shortcut
		if (!this.getSentDate().equals(extern.getSentDate()) ||
				!this.getReceivedDate().equals(extern.getReceivedDate()) ||
				!this.getFrom().equals(extern.getFrom()) ||
				!this.getSubject().equals(extern.getSubject()))
				// TODO: test, if ID can be removed, as the ID changes, as 
				// a mail with a lower ID gets deleted
//				!(this.getId() == extern.getId()))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSentDate().hashCode() + getFrom().hashCode() 
				+ getSubject().hashCode();
	}

}
