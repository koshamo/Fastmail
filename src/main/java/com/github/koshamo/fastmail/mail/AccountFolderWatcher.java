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
import java.util.List;

import javax.mail.Folder;

import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

/**
 * The class AccountFolderWatcher does its work in a separate thread.
 * It builds the tree item view at startup and checks dynamically the
 * mail account, if any folder has been added or removed. Server side renamed
 * folders will be added as new folders and the old (named) folder will be 
 * deleted.
 * 
 * @author jochen
 *
 */
public class AccountFolderWatcher extends ScheduledService<Void> {

	MailAccount account;
	TreeItem<MailTreeViewable> accountTreeItem;
	boolean stop = false;
	
	/**
	 * Basic constructor
	 * 
	 * @param account the current account to check for new folders
	 * @param accountTreeItem root item of the account
	 */
	public AccountFolderWatcher(final MailAccount account, 
			final TreeItem<MailTreeViewable> accountTreeItem) {
		this.account = account;
		this.accountTreeItem = accountTreeItem;
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
	 * This task iterates over all folders within this account and
	 * adds all folders to the tree view that aren't already there.
	 * Excessive folders will be removed.
	 * <p>
	 * As we use an interface to produce the TreeItems, we would need
	 * an equals method within the interface, which is not implementable.
	 * Hence we need to manually check, if the items are there. The contains()
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
				
				final Folder[] folders = account.getFolders();
				if (folders == null)
					return null;
				
				if (stop) return null;	// thread needs to be stopped
				buildFolderTree(folders, accountTreeItem);

				return null;
			}
		};
	}

	/**
	 * buildFolderTree is the actual working method for the task above.
	 * It gets all the folders and subfolders of a mail account and
	 * creates TreeItems for the TreeView in a recursive way and adds
	 * them to the current TreeItem
	 * 
	 * @param folders the folders to be examined
	 * @param root the current TreeItem, where subfolders can be added
	 */
	/*package private*/ 
	void buildFolderTree(final Folder[] folders, final TreeItem<MailTreeViewable> root) {
		final ObservableList<TreeItem<MailTreeViewable>> localList =
				root.getChildren();
		// add folders, if they aren't already in the tree view
		final Folder[] localFolders = new Folder[localList.size()];
		for (int i = 0; i < localList.size(); i++) 
			localFolders[i] = localList.get(i).getValue().getFolder();
		for (Folder sf : folders) {
			boolean contained = false;
			TreeItem<MailTreeViewable> treeItem = null;
			for (Folder lf : localFolders) {
				if (sf.getFullName().equals(lf.getFullName())) {
					contained = true;
					treeItem = localList.get(
							localList.indexOf(new TreeItem<MailTreeViewable>(
									new FolderItem(lf)))); 
					break;
				}
			}
			if (!contained) {
				treeItem = new TreeItem<>(new FolderItem(sf));
				localList.add(treeItem);
			}
			final Folder[] subFolders = MailTools.getSubFolders(sf);
			buildFolderTree(subFolders, treeItem);
		}
		
		
		if (stop) return;	// thread needs to be stopped
		
		// remove items from tree view, that aren't on the server anymore
		// (e.g. because they are renamed)
		final List<Folder> toRemove = new ArrayList<>();
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
						
		// sort the folder to represent items in a natural way
		MailTools.sortFolders(localList);
		
	}
}
