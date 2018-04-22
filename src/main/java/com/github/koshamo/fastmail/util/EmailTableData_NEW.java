/*
 * Copyright (C) 2018  Dr. Jochen Raßler
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

package com.github.koshamo.fastmail.util;

import java.time.Instant;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class EmailTableData_NEW implements Comparable<EmailTableData_NEW>{

	private final String from;
	private final String fromName;
	private final String subject;
	private final Instant sentDate;
//	private final boolean attached;
//	private boolean read;
//	private boolean marked;
	private final SimpleBooleanProperty attachment;
	private SimpleBooleanProperty read;
	private SimpleBooleanProperty marked;
	private final byte[] uniqueID;

	/**
	 * @param from
	 * @param fromName
	 * @param subject
	 * @param sentDate
	 * @param attached
	 * @param read
	 * @param marked
	 * @param uniqueID
	 */
	public EmailTableData_NEW(String from, String fromName, String subject, Instant sentDate, boolean attached,
			boolean read, boolean marked, byte[] uniqueID) {
		this.from = from;
		this.fromName = fromName;
		this.subject = subject;
		this.sentDate = sentDate;
		this.attachment = new SimpleBooleanProperty(attached);
		this.read = new SimpleBooleanProperty(read);
		this.marked = new SimpleBooleanProperty(marked);
		this.uniqueID = uniqueID;
	}

	/* The natural order of Emails should be date based
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EmailTableData_NEW other) {
		/* 
		 * negate the Instant compareTo method, as we want to have the
		 * latest mails on top of the list
		 */
		return -this.sentDate.compareTo(other.sentDate);
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the fromName
	 */
	public String getFromName() {
		return fromName;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return the sentDate
	 */
	public Instant getSentDateAsInstant() {
		return sentDate;
	}

	/**
	 * @return the sentDate
	 */
	public String getSentDate() {
		return sentDate.toString();
	}

	/**
	 * @return the attached
	 */
	public boolean isAttachment() {
		return attachment.get();
	}

	/**
	 * @return the read
	 */
	public boolean isRead() {
		return read.get();
	}

	/**
	 * @return the marked
	 */
	public boolean isMarked() {
		return marked.get();
	}

	/**
	 * @return the read
	 */
	public SimpleBooleanProperty readProperty() {
		return read;
	}

	/**
	 * @return the marked
	 */
	public SimpleBooleanProperty markedProperty() {
		return marked;
	}

	/**
	 * @return the attached
	 */
	public SimpleBooleanProperty attachmentProperty() {
		return attachment;
	}

	/**
	 * @return the uniqueID
	 */
	public byte[] getUniqueID() {
		return uniqueID;
	}
	
}
