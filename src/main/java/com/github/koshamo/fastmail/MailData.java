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

/**
 * The MailData class is a pure data class to get pure data into
 * the GUI without dealing with JavaMail API in the GUI
 * 
 * @author jochen
 *
 */
public final class MailData {

	private final String from;
	private final String[] to;
	private final String[] cc;
	private final String subject;
	private final String content;
	private final AttachmentData[] attachments;

	
	/**
	 * @param from	the from address of the mail
	 * @param to	the to addresses of the mail as array
	 * @param cc	the cc addresses of the mail as array
	 * @param subject	the mail's subject
	 * @param content	the mail's actual message content
	 * @param attachments	all attachments assigned to this mail
	 */
	public MailData(String from, String[] to, String[] cc, String subject, 
			String content, AttachmentData[] attachments) {
		super();
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.subject = subject;
		this.content = content;
		this.attachments = attachments;
	}


	/**
	 * Get the senders address
	 * @return	the senders address
	 */
	public String getFrom() {
		return from;
	}


	/**
	 * Get all directly addressed recipients of this mail
	 * @return	the recipients as array
	 */
	public String[] getTo() {
		return to;
	}


	/**
	 * Get all indirectly addressed recipients of this mail
	 * @return	the cc recipients as array
	 */
	public String[] getCc() {
		return cc;
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
		return content;
	}


	/**
	 * Get the contained attachments
	 * @return	the attachment data object
	 */
	public AttachmentData[] getAttachments() {
		return attachments;
	}
	
}
