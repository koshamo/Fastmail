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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;

import com.github.koshamo.fastmail.util.EmailTableData_NEW;

/**
 * @author Dr. Jochen Raßler
 *
 */
/*private*/ class FolderContent {
	
	private final Folder folder;
	private final List<MailReference> mailRefs;
	private List<EmailTableData_NEW> mailData;
	private final MailListFetcher fetcher;
	
	public FolderContent(final Folder folder) {
		this.folder = folder;
		mailRefs = new ArrayList<>();
		mailData = new ArrayList<>();
		fetcher = new MailListFetcher(folder, mailRefs);
	}
	
	public void syncMailList() {
		fetcher.updateMailList();
		// TODO
	}
	
	public void fetchMails() {
		fetcher.updateMailList();
		// TODO
	}
	
	public EmailTableData_NEW[] getMailList() {
		return mailData.toArray(new EmailTableData_NEW[0]);
	}
}
