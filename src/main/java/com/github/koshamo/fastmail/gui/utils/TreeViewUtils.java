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

package com.github.koshamo.fastmail.gui.utils;

import com.github.koshamo.fastmail.util.MailTreeViewable;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class TreeViewUtils {

	private TreeViewUtils() {
		// prevent class from instantiation
	}
	
	/**
	 * The natural sort order of email folders is INBOX, Drafts, Sent, Trash,
	 * then followed by the user folders in their natural sort order (which 
	 * is alphabetically)
	 * @param list the observable list containing the email folders
	 */
	public static void sortFolders(final ObservableList<TreeItem<MailTreeViewable>> list) {
		list.sort((i1, i2) -> {
			if ("INBOX".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("INBOX".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Drafts".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Drafts".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Sent".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Sent".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			if ("Trash".equals(i1.getValue().getName())) //$NON-NLS-1$
				return -1;
			if ("Trash".equals(i2.getValue().getName())) //$NON-NLS-1$
				return 1;
			return i1.getValue().getName().compareTo(i2.getValue().getName());
		});		
	}
	

}
