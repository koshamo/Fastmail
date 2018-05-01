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
import com.github.koshamo.fastmail.util.AccountWrapper;
import com.github.koshamo.fastmail.util.FolderWrapper;
import com.github.koshamo.fastmail.util.MailTreeViewable;
import com.github.koshamo.fastmail.util.UnbalancedTree;
import com.github.koshamo.fastmail.util.UnbalancedTreeUtils;

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
/*private*/ class AccountFolderWatcher implements Runnable {

	private MailAccount account;
	private boolean run = true;
	// Folder Tree to save the propagated folders
	private UnbalancedTree<MailTreeViewable> currentFolderTree = null;

	/**
	 * Basic constructor
	 * 
	 * @param account the current account to check for new folders
	 * @param accountTreeItem root item of the account
	 */
	/*private*/ AccountFolderWatcher(final MailAccount account) {
		this.account = Objects.requireNonNull(account, "account must not be null");
	}
	
	/*private*/ void stop() {
		run = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (run) {
			MailTreeViewable root = new AccountWrapper(account.getMailAccountData());
			final UnbalancedTree<MailTreeViewable> newFolderTree = 
					new UnbalancedTree<>(root);
			newFolderTree.addSubtree(
					getSubFolderTree(account.getDefaultFolder()), root);
			currentFolderTree = compareAndPropagate(currentFolderTree, newFolderTree);

			// we are done!
			propagateFolderTree(null);

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
	private UnbalancedTree<MailTreeViewable> compareAndPropagate(
			final UnbalancedTree<MailTreeViewable> currentFolderTree,
			final UnbalancedTree<MailTreeViewable> newFolderTree) {
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
	private void propagateFolderTree(final UnbalancedTree<MailTreeViewable> newFolderTree) {
		if (newFolderTree != null)
			account.postDataEvent(MailAccountOrders.FOLDER_NEW, newFolderTree);
		account.propagateFolderChanges(
				UnbalancedTreeUtils.unbalancedTreeToList(newFolderTree));
	}

	/**
	 * 
	 */
	/*private*/ void propagateRemovefolderTree() {
		account.postDataEvent(MailAccountOrders.FOLDER_REMOVE, currentFolderTree);
	}
	
	
	/**
	 * Build a UnbalancedTree of folders with their subfolders
	 * 
	 * @param folders	the given folders of a folder
	 *  
	 * @return	the tree of folders
	 */
	private UnbalancedTree<MailTreeViewable> getFolderTree(final Folder[] folders) {
		UnbalancedTree<MailTreeViewable> folderTree = new UnbalancedTree<>();
		for (int i = 0; i < folders.length; ++i) {
			FolderWrapper wrapper = new FolderWrapper(folders[i]);
			folderTree.add(wrapper);
			try {
				if ((folders[i].getType() & Folder.HOLDS_FOLDERS) != 0) {
					UnbalancedTree<MailTreeViewable> subFolderTree = getSubFolderTree(folders[i]);
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
	private UnbalancedTree<MailTreeViewable> getSubFolderTree(final Folder folder) {
		try {
			Folder[] subfolders = folder.list();
			if (subfolders.length > 0) {
				UnbalancedTree<MailTreeViewable> subFolderTree = getFolderTree(subfolders);
				return subFolderTree;
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
