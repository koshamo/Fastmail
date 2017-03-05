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
 * @author jochen
 *
 */
public final class MailData {

	private final String from;
	private final String[] to;
	private final String[] cc;
	private final String subject;
	private final String content;
//	private final whatever[] attachments;

	
	/**
	 * @param from
	 * @param to
	 * @param cc
	 * @param subject
	 * @param content
	 */
	public MailData(String from, String[] to, String[] cc, String subject, String content) {
		super();
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.subject = subject;
		this.content = content;
	}


	public String getFrom() {
		return from;
	}


	public String[] getTo() {
		return to;
	}


	public String[] getCc() {
		return cc;
	}


	public String getSubject() {
		return subject;
	}


	public String getContent() {
		return content;
	}
	
	
	
}
