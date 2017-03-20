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
import java.util.Arrays;
import java.util.List;

import javax.mail.Folder;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

/**
 * The class AccountFolderWatcher does its work in seperate threads.
 * It builds the tree item view at startup and checks dynamically the
 * mail account, if any folder has been added or removed. Server side renamed
 * folders will be added as new folders and the old (named) folder will be deleted.
 * 
 * @author jochen
 *
 */
public class AccountFolderWatcher extends ScheduledService<Void> {

	MailAccount account;
	TreeItem<MailTreeViewable> rootItem;
	boolean stop = false;
	
	/**
	 * Basic constructor
	 * 
	 * @param accounts this is the list containing all mail accounts
	 * @param root root item of the account
	 */
	AccountFolderWatcher(final MailAccount account, 
			final TreeItem<MailTreeViewable> rootItem) {
		this.account = account;
		this.rootItem = rootItem;
	}
	
	/**
	 * Overrides superclass' cancel method to add functionality:
	 * this account has to be removed from the treeview
	 * 
	 * @see javafx.concurrent.ScheduledService#cancel()
	 */
	@Override 
	public boolean cancel() {
		boolean ret = super.cancel();
		stop = true;
		return ret;
	}

	/* 
	 * This task iterates over all folders within a given account and
	 * adds all folder to the tree view that aren't already there.
	 * Excessive folders will be removed.
	 *   
	 * @see javafx.concurrent.Service#createTask()
	 */
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() {
				if (stop) return null;	// thread needs to be stopped
				
				Folder[] folders = account.getFolders();
				if (folders == null)
					return null;
				
				if (stop) return null;	// thread needs to be stopped

				ObservableList<TreeItem<MailTreeViewable>> localList = rootItem.getChildren();
				// add folders, if they aren't already in the tree view
				for (Folder f : folders) {
					FolderItem folderItem = new FolderItem(f);
					TreeItem<MailTreeViewable> item = new TreeItem<MailTreeViewable>(folderItem);
					if (!localList.contains(item)) 
						localList.add(item);
				}
				
				if (stop) return null;	// thread needs to be stopped

				// remove items from tree view, that aren't on the server anymore
				// (e.g. because they are renamed)
				List<Folder> serverList = new ArrayList<Folder>(folders.length);
				for (Folder f : folders)
					serverList.add(f);
				for (int i = 0; i < localList.size(); i++) {
					if (!serverList.contains(localList.get(i).getValue().getFolder()))
						localList.remove(i);
				}
				
				localList.sort((i1, i2) -> {
					if ("INBOX".equals(i1.getValue().getName()))
						return -1;
					if ("INBOX".equals(i2.getValue().getName()))
						return 1;
					if ("Drafts".equals(i1.getValue().getName()))
						return -1;
					if ("Drafts".equals(i2.getValue().getName()))
						return 1;
					if ("Sent".equals(i1.getValue().getName()))
						return -1;
					if ("Sent".equals(i2.getValue().getName()))
						return 1;
					if ("Trash".equals(i1.getValue().getName()))
						return -1;
					if ("Trash".equals(i2.getValue().getName()))
						return 1;
					return i1.getValue().getName().compareTo(i2.getValue().getName());
				});
				return null;
			}
		};
	}

}
