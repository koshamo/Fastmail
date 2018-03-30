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

import java.util.Objects;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.FastmailGlobals;
import com.github.koshamo.fastmail.events.MailAccountOrders;
import com.github.koshamo.fastmail.util.FolderWrapper;
import com.github.koshamo.fastmail.util.UnbalancedTree;

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
public class AccountFolderWatcher implements Runnable {

	MailAccount account;
	boolean run = true;
	
	/**
	 * Basic constructor
	 * 
	 * @param account the current account to check for new folders
	 * @param accountTreeItem root item of the account
	 */
	public AccountFolderWatcher(final MailAccount account) {
		this.account = Objects.requireNonNull(account, "account must not be null");
	}
	
	public void stop() {
		run = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Folder Tree to save the propagated folders
		UnbalancedTree<FolderWrapper> currentFolderTree = null;
		while (run) {
			final UnbalancedTree<FolderWrapper> newFolderTree = 
					getSubFolderTree(account.getDefaultFolder());
			currentFolderTree = compareAndPropagate(currentFolderTree, newFolderTree);

			try {
				Thread.sleep(FastmailGlobals.FOLDER_REFRESH_MS);
			} catch (InterruptedException e) {
				account.postMessage("Thread to update folders for account "
						+ account.getAccountName()
						+ " was interrupted while sleeping");
			}
		}
	}

	/**
	 * @param currentFolderTree
	 * @param newFolderTree
	 * @return
	 */
	private UnbalancedTree<FolderWrapper> compareAndPropagate(
			final UnbalancedTree<FolderWrapper> currentFolderTree,
			final UnbalancedTree<FolderWrapper> newFolderTree) {
		if (currentFolderTree == null || 
				!currentFolderTree.equals(newFolderTree)) {
			propagateFolderTree(newFolderTree);
			return newFolderTree;
		}
		return currentFolderTree;
	}

	/**
	 * @param newFolderTree
	 */
	private void propagateFolderTree(final UnbalancedTree<FolderWrapper> newFolderTree) {
		account.postDataEvent(MailAccountOrders.FOLDERS, newFolderTree);
	}

	/**
	 * Build a UnbalancedTree of folders with their subfolders
	 * 
	 * @param folders	the given folders of a folder
	 *  
	 * @return	the tree of folders
	 */
	private UnbalancedTree<FolderWrapper> getFolderTree(final Folder[] folders) {
		UnbalancedTree<FolderWrapper> folderTree = new UnbalancedTree<>();
		for (int i = 0; i < folders.length; ++i) {
			FolderWrapper wrapper = new FolderWrapper(folders[i]);
			folderTree.add(wrapper);
			try {
				if ((folders[i].getType() & Folder.HOLDS_FOLDERS) != 0) {
					UnbalancedTree<FolderWrapper> subFolderTree = getSubFolderTree(folders[i]);
					if (subFolderTree != null)
						folderTree.addSubtree(subFolderTree, wrapper);
				}
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return folderTree;
	}

	/**
	 * Checks if a folder has subfolders and builds a UnbalancedTree of 
	 * all subfolders
	 * 
	 * @param folder	the current folder
	 * @return	the tree of subfolders
	 */
	private UnbalancedTree<FolderWrapper> getSubFolderTree(final Folder folder) {
		try {
			Folder[] subfolders = folder.list();
			if (subfolders.length > 0) {
				UnbalancedTree<FolderWrapper> subFolderTree = getFolderTree(subfolders);
				return subFolderTree;
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
/*	void buildFolderTree(final Folder[] folders) {
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
		
		
		if (run) return;	// thread needs to be stopped
		
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
		
	}*/
	
}
