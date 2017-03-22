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

import com.github.koshamo.fastmail.util.MailTools;

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
	TreeItem<MailTreeViewable> accountTreeItem;
	ObservableList<TreeItem<MailTreeViewable>> localList;
	boolean stop = false;
	
	/**
	 * Basic constructor
	 * 
	 * @param accounts this is the list containing all mail accounts
	 * @param accountTreeItem root item of the account
	 */
	AccountFolderWatcher(final MailAccount account, 
			final TreeItem<MailTreeViewable> accountTreeItem) {
		this.account = account;
		this.accountTreeItem = accountTreeItem;
		localList = accountTreeItem.getChildren();
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
	 * adds all folders to the tree view that aren't already there.
	 * Excessive folders will be removed.
	 * <p>
	 * As we use an interface to produce the TreeItems, we would need
	 * an equals method within the interface, which is not implementable.
	 * Hence we need to manually check, if the items are there, the contains()
	 * method of the collections framework will not work for this case.
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

				// add folders, if they aren't already in the tree view
				Folder[] localFolders = new Folder[localList.size()];
				for (int i = 0; i < localList.size(); i++) 
					localFolders[i] = localList.get(i).getValue().getFolder();
				for (Folder sf : folders) {
					boolean contained = false;
					for (Folder lf : localFolders) {
						if (sf.getFullName().equals(lf.getFullName())) {
							contained = true;
							break;
						}
					}
					if (!contained) {
						FolderItem folderItem = new FolderItem(sf);
						TreeItem<MailTreeViewable> treeItem = new TreeItem<MailTreeViewable>(folderItem);
						localList.add(treeItem);
					}
				}
				
				
				if (stop) return null;	// thread needs to be stopped
				
				// remove items from tree view, that aren't on the server anymore
				// (e.g. because they are renamed)
				List<Folder> toRemove = new ArrayList<Folder>();
				for (Folder lf : localFolders) {
					boolean contained = false;
					for (Folder sf : folders) {
						if (lf.getFullName().equals(sf.getFullName())) {
							contained = true;
							break; 
						}
					}
					if (!contained) {
						toRemove.add(lf);
					}
				}
				if (!toRemove.isEmpty()) {
					for (Folder tr : toRemove) {
						for (TreeItem<MailTreeViewable> item : localList) {
							if (item.getValue().getFolder().getFullName().equals(tr.getFullName()))
								localList.remove(item);
						}
					}
				}
								
				MailTools.sortFolders(localList);
				return null;
			}
		};
	}

}
