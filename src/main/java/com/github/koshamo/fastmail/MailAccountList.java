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

import java.util.List;
import java.util.Vector;

import javafx.scene.control.TreeItem;
import javafx.util.Duration;

/**
 * The class MailAccountList holds all used mail accounts and provides
 * functionality to process the accounts, as e.g. the AccountFolderWatcher
 * 
 * @author jochen
 *
 */
public class MailAccountList {

	private List<MailAccount> accounts = new Vector<MailAccount>();
	private TreeItem<String> rootItem;
	
	// currently shown mails and folders will be stored globally
	private String currentAccount;
	private String currentFolder;
	private int currentMessage;
	
	/**
	 * the constructor needs some GUI components. These components will be distributed
	 * to aggregated subclasses, which add data to the GUI.
	 * <p>
	 * The folder watcher may add new folders to the account tree.
	 * The inbox watcher checks for new mails and adds them to the table view.
	 * 
	 * @param rootItem
	 */
	public MailAccountList(TreeItem<String> rootItem) {
		this.rootItem = rootItem;
	}
	
	
	/**
	 * This method adds a mail account to the internal list
	 * 
	 * @param account a new MailAccount
	 */
	public void add(MailAccount account) {
		if (account == null) {
			System.out.println("Account not yet ready");
			return;
		}
		accounts.add(account);

		if (!account.isConnected())
			System.out.println("not yet connected!");
		
		AccountFolderWatcher accountFolderWatcher = 
				new AccountFolderWatcher(account, rootItem);
		accountFolderWatcher.setPeriod(Duration.seconds(60));
		accountFolderWatcher.start();
		
		InboxWatcher inboxWatcher = new InboxWatcher(account);
		inboxWatcher.setPeriod(Duration.seconds(20));
		inboxWatcher.start();

	}
	
	/**
	 * This method returns the mail accounts as a list to iterate through it
	 * 
	 * @return the MailAccounts as List
	 */
	public  List<MailAccount> getList() {
		return accounts;
	}
	
	/**
	 * This methods looks for the given MailAccount representation of this argument 
	 * and returns the appropriate MailAccount
	 * 
	 * @param name the mail account's string representation
	 * @return the MailAccount with name, else null
	 */
	public MailAccount getAccount(String name) {
		for (MailAccount ma : accounts)
			if (ma.getAccountName().equals(name))
				return ma;
		return null;
	}
	
	/**
	 * Sets the account chosen in the GUI
	 * 
	 * @param account the currently chosen account
	 */
	public void setCurrentAccount(String account) {
		currentAccount = account;
	}
	
	/**
	 * Gives the currently chosen account
	 * 
	 * @return the currently chosen account 
	 */
	public String getCurrentAccount() {
		return currentAccount;
	}

	/**
	 * Sets the folder chosen in the GUI
	 * 
	 * @param folder the active folder
	 */
	public void setCurrentFolder(String folder) {
		currentFolder = folder;
	}
	
	/**
	 * Gives the chosen folder
	 * 
	 * @return the active folder
	 */
	public String getCurrentFolder() {
		return currentFolder;
	}

	/**
	 * Sets the chosen email in the GUI
	 * 
	 * @param mailId the active email
	 */
	public void setCurrentMail(int mailId) {
		currentMessage = mailId;
	}
	
	/**
	 * Gives the chosen mail in the GUI
	 * 
	 * @return the ID of the active mail, zero if non is chosen
	 */
	public int getCurrentMessage() {
		return currentMessage;
	}

}
