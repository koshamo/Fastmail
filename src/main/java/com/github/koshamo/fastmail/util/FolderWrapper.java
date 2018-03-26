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

import javax.mail.Folder;

/**
 * The FolderAdapter is a adapter class to get javax.mail-free
 * representation of a mail folder in the GUI module
 * 
 * @author Dr. Jochen Raßler
 *
 */
public class FolderWrapper {
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
	public String getName() {
		return folder.getName();
	}
	
	/**
	 * Get the full qualified name of the folder
	 * 
	 * @return the folder's full name
	 */
	public String getFullName() {
		return folder.getFullName();
	}
}
