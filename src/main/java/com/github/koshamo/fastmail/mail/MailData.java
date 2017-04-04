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

import java.time.Instant;

import javax.mail.Message;

import com.github.koshamo.fastmail.util.MailTools;

/**
 * The MailData class is a pure data class to get pure data into
 * the GUI without dealing with JavaMail API in the GUI
 * 
 * @author jochen
 *
 */
public final class MailData {

	/*
	 *  fields that must be available at message construction to be
	 *  shown in table view
	 */
	private final String from;
	private final String fromName;
	private final String subject;
	private final Instant sentDate;
	private final boolean attached;
	private final Message message;
	/*
	 * fields that can be loaded lazily, which means they only need to be
	 * fetched from the server, if they are needed by the user
	 */
	private String[] to;
	private String[] toName;
	private String[] cc;
	private String[] ccName;
	private String content;
	private AttachmentData[] attachments;
	// the received date can change, e.g. if the message is moved to another folder
	private Instant receivedDate;	

	

	/**
	 * Build a new Mail object using lazy loading. Only fill needed fields
	 * for displaying in table view.
	 * 
	 * @param from			the sender's internet address
	 * @param fromName		the sender's display name
	 * @param subject		the message's subject
	 * @param sentDate		the message's sent date 
	 * @param receivedDate	the message's received date
	 * @param attached		set, if an attachment is available
	 * @param message		the message reference
	 */
	public MailData (final String from, final String fromName, 
			final String subject, final Instant sentDate, 
			final Instant receivedDate, final boolean attached,
			final Message message) {
		this.from = from;
		this.fromName = fromName;
		this.subject = subject;
		this.sentDate = sentDate;
		this.receivedDate = receivedDate;
		this.attached = attached;
		this.message = message;
	}
	
	
	/**
	 * Get the senders address
	 * @return	the senders address
	 */
	public String getFrom() {
		return from;
	}


	/**
	 * Get the senders Name
	 * @return the sender name
	 */
	public String getFromName() {
		return fromName;
	}


	/**
	 * Get all directly addressed recipients of this mail
	 * @return	the recipients as array
	 */
	public String[] getTo() {
		if (to == null) {
			to = MailTools.getToAddresses(message);
		}
		return to;
	}
	
	/**
	 * Get all directly addressed recipients of this mail
	 * @return	the recipients as string
	 */
	public String getToAsLine() {
		if (to == null)
			to = MailTools.getToAddresses(message);
		StringBuilder sb = new StringBuilder();
		for (String str: to)
			sb.append(str).append(";"); //$NON-NLS-1$
		return sb.toString();
	}


	/** 
	 * Get all directly addressed recipients of this mail as name
	 * @return	get the recipients names as array
	 */
	public String[] getToName() {
		if (toName == null)
			toName = MailTools.getToNames(message);
		return toName;
	}


	/**
	 * Get all indirectly addressed recipients of this mail
	 * @return	the cc recipients as array
	 */
	public String[] getCc() {
		if (cc == null)
			cc = MailTools.getCcAddresses(message);
		return cc;
	}


	/**
	 * Get all indirectly addressed recipients of this mail
	 * @return	the cc recipients as string
	 */
	public String getCcAsLine() {
		if (cc == null)
			cc = MailTools.getCcAddresses(message);
		if (cc == null)
			return ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (String str : cc)
			sb.append(str).append(";"); //$NON-NLS-1$
		return sb.toString();
	}

	
	/**
	 * Get all indirectly addressed recipients of this mail as name
	 * @return	get the cc recipients names as array
	 */
	public String[] getCcName() {
		if (ccName == null) 
			MailTools.getCcNames(message);
		return ccName;
	}


	/**
	 * Get the mail's subject
	 * @return	the subject
	 */
	public String getSubject() {
		return subject;
	}


	/**
	 * Get the mail's text content
	 * @return	the content as string
	 */
	public String getContent() {
		if (content == null)
			content = MailTools.getContent(message);
		return content;
	}


	/**
	 * Get the contained attachments
	 * @return	the attachment data object
	 */
	public AttachmentData[] getAttachments() {
		if (attachments == null)
			attachments = MailTools.getAttachments(message);
		return attachments;
	}


	/**
	 * Get the original Message object
	 * @return	the containing message object
	 */
	public Message getMessage() {
		return message;
	}
	
	
}
