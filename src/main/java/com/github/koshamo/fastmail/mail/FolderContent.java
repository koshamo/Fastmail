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

package com.github.koshamo.fastmail.mail;

import javax.mail.Folder;

import com.github.koshamo.fastmail.util.EmailTableData_NEW;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class FolderContent {
	
	private final Folder folder;
	private final FolderContentLister fcl;
	
	public FolderContent(final Folder folder) {
		this.folder = folder;
		fcl = new FolderContentLister(folder);
	}
	
	public void syncMailList() {
		new Thread(fcl).start();
	}
	
	public EmailTableData_NEW[] getMailList() {
		if (fcl.isDone())
			return fcl.getMailList();
		else
			return null;
	}
}
