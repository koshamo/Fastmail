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

import java.io.InputStream;

import javax.mail.internet.MimeBodyPart;

/**
 * This class is a pure data class that stores attachment data
 * 
 * @author jochen
 *
 */
public final class AttachmentData {

	private final String fileName;
	private final int size;
	private final InputStream inputStream;
	
	/**
	 * @param fileName	the file name of the containing attachment
	 * @param size		the size in byte of the containing attachment
	 * @param inputStream	the attachment's input stream
	 */
	public AttachmentData(String fileName, int size, InputStream inputStream) {
		super();
		this.fileName = fileName;
		this.size = size;
		this.inputStream = inputStream;
	}
	
	/**	Get the file name of the containing attachment
	 * @return	the file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Get the size in byte of the containing attachemnt
	 * @return the file size
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Get the input stream of the attachment, which can be used to
	 * pipe a file output stream
	 * @return	the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	
	
}
