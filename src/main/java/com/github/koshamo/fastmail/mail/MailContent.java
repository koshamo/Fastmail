/*
 * Copyright (C) 2017  Dr. Jochen Raﬂler
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a data class only. It is intended to be a return value 
 * from the util method to parse the mail content. 
 * @see MailTools.parseMailContent() 
 * Actually the data is stored and used from the mail data model class.
 * @see MailData
 * 
 * @author jochen
 *
 */
public final class MailContent {
	private String textContent;
	private String htmlContent;
	private List<AttachmentData> attachments;
	

	public MailContent() {
		attachments = new ArrayList<>();
	}

	/**
	 * @return the textContent
	 */
	public String getTextContent() {
		return textContent;
	}

	/**
	 * @param textContent the textContent to set
	 */
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	/**
	 * @return the htmlContent
	 */
	public String getHtmlContent() {
		return htmlContent;
	}

	/**
	 * @param htmlContent the htmlContent to set
	 */
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	/**
	 * @return the attachments
	 */
	public List<AttachmentData> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void addAttachment(String name, int size, String type, InputStream stream) {
		attachments.add(new AttachmentData(name, size, type, stream));
	}
	
	public void addAttachment(List<AttachmentData> attachments) {
		this.attachments.addAll(attachments);
	}
	
	
}
