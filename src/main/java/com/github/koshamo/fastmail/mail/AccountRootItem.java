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

package com.github.koshamo.fastmail.mail;

import javax.mail.Folder;

import com.github.koshamo.fastmail.util.MailTreeViewable;

import javafx.collections.ObservableList;

/**
 * This class represents the root of the mail account folder tree.
 * <p>
 * The tree view is build using the interface MailTreeViewable. To
 * keep the GUI class clean, we explicitly define this class here, so
 * we need no anonymous class in the GUI class.
 * <p>
 * Actually this class does nothing except being the root for the mail accounts.
 *  
 * @author jochen
 *
 */
public class AccountRootItem implements MailTreeViewable {

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#isAccount()
	 */
	@Override
	public boolean isAccount() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolderContent()
	 */
	@Override
	public ObservableList<EmailTableData> getFolderContent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getParentFolder()
	 */
	@Override
	public Folder getParentFolder() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getFolder()
	 */
	@Override
	public Folder getFolder() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.mail.MailTreeViewable#getName()
	 */
	@Override
	public String getName() {
		return null;
	}

}
