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

package com.github.koshamo.fastmail;

import java.util.ArrayList;
import java.util.Arrays;

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
	TreeItem<String> rootItem;
	
	/**
	 * Basic constructor
	 * 
	 * @param accounts this is the list containing all mail accounts
	 * @param root root item of the tree view
	 */
	AccountFolderWatcher(MailAccount account, TreeItem<String> rootItem) {
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
		removeFromTree();
		return ret;
	}
	
	private void removeFromTree() {
		TreeItem<String> toRemove = null;
		for (TreeItem<String> acc : rootItem.getChildren())
			if (acc.getValue().equals(account.getAccountName()))
				toRemove = acc;
		if (toRemove != null)
			rootItem.getChildren().removeAll(Arrays.asList(toRemove));
	}
		
	/* 
	 * This method iterates over all mail accounts.
	 * It adds all mail accounts to the tree view and cares for the folders.
	 * New folders will be added, old folders will be removed
	 * 
	 * @see javafx.concurrent.Service#createTask()
	 */
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() {
				// iterate over all Mail accounts
				TreeItem<String> accountItem = new TreeItem<String>(account.getAccountName());
				boolean found = false;
				
				for (TreeItem<String> accNode : rootItem.getChildren()) {
					// make sure account hasn't been added yet
					if (accNode.getValue().contentEquals(accountItem.getValue())) {
						found = true;
						// account already exists, check folders
						String[] serverFolders = account.getFolders();
						ArrayList<TreeItem<String>> clientFolders = new ArrayList<>(accNode.getChildren());
						// if new folders exist, add them
						for (String folder : serverFolders) {
							boolean itemfound = false;
							for (TreeItem<String> clientItem : clientFolders) {
								if (folder.equals(clientItem.getValue()))
									itemfound = true;
							}
							if (!itemfound){
								TreeItem<String> leaf = new TreeItem<>(folder);
								accNode.getChildren().add(leaf);
								clientFolders.add(leaf);
							}

						}
						// if folder has been deleted, remove it, should be rare
						if (clientFolders.size() > serverFolders.length) {
							ArrayList<String> sF = new ArrayList<>(serverFolders.length);
							for (String folder : serverFolders)
								sF.add(folder);
							for (TreeItem<String> cF : clientFolders) {
								if (!sF.contains(cF.getValue())) {
									accNode.getChildren().remove(cF);
								}
							}
						}
						break;
					}
				}
				// account is new, so add it and build the folders
				if (!found) {
					for (String folder : account.getFolders()) {
						TreeItem<String> leaf = new TreeItem<>(folder);
						accountItem.getChildren().add(leaf);
					}
					accountItem.setExpanded(true);
					rootItem.getChildren().add(accountItem);
				}
				return null;
			}
		};
	}

}
