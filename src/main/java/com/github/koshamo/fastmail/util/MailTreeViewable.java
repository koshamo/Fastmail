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

package com.github.koshamo.fastmail.util;

/**
 * This interface is intended to be used for the TreeView.
 * <p>
 * This interface keeps the necessary methods for operations on the tree
 * view that allow processing of folders as well as accounts.
 * 
 * @author jochen
 *
 */
public interface MailTreeViewable {

	/**
	 * If this MailTreeViewable represents a MailAccount, this method should
	 * return true, in all other cases false
	 * 
	 * @return true if it is a account, false otherwise
	 */
//	boolean isAccount();
	
	/**
	 * As every folder should be aware of its content, this method is convenient 
	 * to get the folder's content
	 * 
	 * @return the ObservableList of EmailTableData or null for accounts, root,
	 * and possibly folders containing only folders
	 */
//	ObservableList<EmailTableData> getFolderContent();
	
	/**
	 * Returns the parent folder for the mail folders. In accounts this is
	 * the default folder or the namespace folders, if supported. For mail
	 * folders it is the parent folder, which again is the default folder.
	 * 
	 * @return the parent of the mail folders
	 */
//	Folder getParentFolder();
	
	/**
	 * Get the current working folder. This is simple for email folders.
	 * Accounts should return getParentFolder()
	 * 
	 * @return the current working folder
	 */
//	Folder getFolder();
	
	/**
	 * The name of item represented by this MailTreeViewable.
	 * This method is intended to be used to edit folder names and the like,
	 * while the toString() method of the implementing objects can return 
	 * different Strings, as e.g. message counters for email folders
	 * 
	 * @return the name of the implementing objects data
	 */
	String getName();

}
