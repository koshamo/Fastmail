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

/**
 * @author Dr. Jochen Raßler
 *
 */
public class EmailTableData_NEW {

	private final String from;
	private final String fromName;
	private final String subject;
	private final Instant sentDate;
	private final boolean attached;
	private boolean read;
	private boolean marked;
	private final byte[] md5;

	/**
	 * @param from
	 * @param fromName
	 * @param subject
	 * @param sentDate
	 * @param attached
	 * @param read
	 * @param marked
	 */
	public EmailTableData_NEW(String from, String fromName, String subject, Instant sentDate, boolean attached,
			boolean read, boolean marked, byte[] md5) {
		this.from = from;
		this.fromName = fromName;
		this.subject = subject;
		this.sentDate = sentDate;
		this.attached = attached;
		this.read = read;
		this.marked = marked;
		this.md5 = md5;
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
	public boolean isAttached() {
		return attached;
	}

	/**
	 * @return the read
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * @return the marked
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
	 * @return the md5
	 */
	public byte[] getMd5() {
		return md5;
	}
	
}
