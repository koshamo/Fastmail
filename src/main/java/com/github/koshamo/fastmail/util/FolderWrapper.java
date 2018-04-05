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

import java.util.Objects;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.mail.EmailTableData;

/**
 * The FolderAdapter is a adapter class to get javax.mail-free
 * representation of a mail folder in the GUI module
 * 
 * @author Dr. Jochen Raßler
 *
 */
public class FolderWrapper implements MailTreeViewable{
	private final Folder folder;
	
	/**
	 * Constructor needs a non-null folder
	 * 
	 * @param folder	the mail folder
	 */
	public FolderWrapper(final Folder folder) {
		this.folder = Objects.requireNonNull(folder, "folder must not be null");
	}
	
	/**
	 * Get the name of the folder
	 * 
	 * @return	the folder's name
	 */
	@Override
	public String getName() {
		return folder.getName();
	}
	
	/**
	 * Get the full qualified name of the folder
	 * 
	 * @return the folder's full name
	 */
	@Override
	public String getFullName() {
		return folder.getFullName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return folder.getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof FolderWrapper))
			return false;
		
		FolderWrapper other = (FolderWrapper) obj;
		
		return this.getFullName().equals(other.getFullName());
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.util.MailTreeViewable#isAccount()
	 */
	@Override
	public boolean isAccount() {
		return false;
	}
	
	public FolderWrapper createFolder(String name) {
		// TODO: this currently is old code, move it to mail module
		try {
			Folder f = folder.getParent().getFolder(name);
			return new FolderWrapper(f);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void moveMessage(EmailTableData mail) {
		// TODO: this currently is old code, move it to mail module
		try {
			// copy message to new folder
			mail.getFolder().copyMessages(new Message[] {mail.getMessage()}, folder);
			// delete message in original folder
			mail.setFlag(Flag.DELETED, true);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
