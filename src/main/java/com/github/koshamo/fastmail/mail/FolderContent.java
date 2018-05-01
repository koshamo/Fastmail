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

import com.github.koshamo.fastmail.util.EmailTableData;

/**
 * @author Dr. Jochen Raßler
 *
 */
/*private*/ class FolderContent {
	
	// TODO: make FolderContent sortable for number of mails in it
	
	private final Folder folder;
	private List<MailReference> mailRefs;
	private List<EmailTableData> mailData;
	private final MailListFetcher fetcher;
	
	public FolderContent(final Folder folder) {
		this.folder = folder;
//		mailRefs = new ArrayList<>();
//		mailData = new ArrayList<>();
		fetcher = new MailListFetcher(folder);
	}
	
	public void generateMailList() {
		MailRef2EtdMapper etdMapper = generateMail2EtdRunner();
		new Thread(etdMapper).start();
	}
	
	public MailRef2EtdMapper generateMail2EtdRunner() {
		mailRefs = fetcher.getMailRefs();
		mailData = new ArrayList<>();
		return new MailRef2EtdMapper(mailRefs, mailData);
	}
	
//	public void fetchMails() {
//		fetcher.updateMailList();
//	}
	
	public EmailTableData[] getMailList() {
		if (mailData == null)
			return null;
		return mailData.toArray(new EmailTableData[0]);
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
		if (!(obj instanceof FolderContent))
			return false;
		
		FolderContent other = (FolderContent) obj;
		return this.folder.equals(other.folder);
	}
	
	public String getFolderName() {
		return folder.getFullName();
	}
}
