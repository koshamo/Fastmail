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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

/**
 * This class is intended to synchronize the mails of a given folder on a 
 * regular basis.
 * <p>
 * The functionality is divided in FolderSynchronizerTask, which is the Task
 * doing the actual work and can be used standalone, and FolderSynchronizer,
 * which is a ScheduledService, that can be called once and polls the task.
 *   
 * @author jochen
 *
 */
public class FolderSynchronizer extends ScheduledService<Void> {

	private final Folder folder;
	private final ObservableList<EmailTableData> mailList;
	

	
	/**
	 * The constructor needs the folder on the server side and the data
	 * representation on the local side to do its work.
	 * 
	 * @param folder 	the folder on the server to be synched
	 * @param mailList	the ObservableList on the local side
	 */
	public FolderSynchronizer(final Folder folder, 
			final ObservableList<EmailTableData> mailList) {
		super();
		this.folder = folder;
		this.mailList = mailList;
	}



	/* (non-Javadoc)
	 * @see javafx.concurrent.Service#createTask()
	 */
	@Override
	protected Task<Void> createTask() {
		return new FolderSynchronizerTask(folder, mailList);
	}

}
