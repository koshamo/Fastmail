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

package com.github.koshamo.fastmail.gui;

import java.text.MessageFormat;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.mail.FolderItem;
import com.github.koshamo.fastmail.mail.MailTools;
import com.github.koshamo.fastmail.mail.MailTreeViewable;
import com.github.koshamo.fastmail.util.MessageItem;
import com.github.koshamo.fastmail.util.MessageMarket;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The TreeCellFactory class is the editor class for account folders.
 * This class defines the context menu for the tree view and handles
 * the actions taken.
 * 
 * @author jochen
 *
 */
public class TreeCellFactory extends TreeCell<MailTreeViewable> {

	private TextField textField;
	private MailTreeViewable editItem;
	
	/**
	 * The class constructor builds the context menu
	 */
	public TreeCellFactory() {
	}
	
	/* 
	 * Begin editing: create text field and initialize it.
	 * 
	 * @see javafx.scene.control.TreeCell#startEdit()
	 */
	@Override
	public void startEdit() {
		// Account and special folders are not renameable!
		if (getItem().isAccount() || 
				"INBOX".equals(getItem().getName()) || //$NON-NLS-1$
				"Drafts".equals(getItem().getName()) ||  //$NON-NLS-1$
				"Sent".equals(getItem().getName()) || //$NON-NLS-1$
				"Trash".equals(getItem().getName())) { //$NON-NLS-1$
			return;
		}
		super.startEdit();
		
		if (textField == null) {
			createTextField();
		}
		setText(null);
		setGraphic(textField);
		textField.selectAll();
	}
	
	/* 
	 * Reset the text field, of editing has been cancelled
	 * 
	 * @see javafx.scene.control.TreeCell#cancelEdit()
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		if (getItem() == null)	// root item
			setText(null);
		else
			setText(getItem().getName());
		setGraphic(getTreeItem().getGraphic());
	}

	/* 
	 * After completion of editing, this method cares for update on tree view and server.
	 * The latter currently needs to be implemented.
	 *  
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(final MailTreeViewable item, final boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {	// here again: root item
			setText(null);
			setGraphic(null);
			return;
		}
		editItem = item;
		
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(getTreeItem().getGraphic());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javafx.scene.control.TreeCell#commitEdit(java.lang.Object)
	 */
	@Override 
	public void commitEdit(final MailTreeViewable newValue) {
		FolderItem folderItem = (FolderItem) editItem;
		folderItem.renameTo(((FolderItem)newValue).getFolder());
		super.commitEdit(newValue);
		// resort tree view after renaming
		TreeItem<MailTreeViewable> treeItem = getTreeItem();
		while (!treeItem.getValue().isAccount())
			treeItem = treeItem.getParent();
		MailTools.sortFolders(treeItem.getChildren());
	}
	
	/**
	 * Returns the String within the text field
	 * 
	 * @return the String the user entered in the text field
	 */
	private String getString() {
		return getItem().getName() == null ? "" : getItem().getName(); //$NON-NLS-1$
	}

	/**
	 * Creates the text field for folder manipulation
	 */
	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnKeyReleased((KeyEvent t) -> {
			if (t.getCode() == KeyCode.ESCAPE) 
				cancelEdit();
			if (t.getCode() == KeyCode.ENTER) {
				try {
					Folder newFolder = editItem.getFolder().getParent().getFolder(textField.getText());
					FolderItem newFolderItem = new FolderItem(newFolder);
					commitEdit(newFolderItem);
				} catch (MessagingException e) {
					MessageItem mItem = new MessageItem(
							MessageFormat.format(
									SerializeManager.getLocaleMessageBundle().getString("exception.mailaccess"),  //$NON-NLS-1$
									e.getMessage()),
							0.0, MessageItem.MessageType.EXCEPTION);
					MessageMarket.getInstance().produceMessage(mItem);
				}
			}
		});
	}
	
}
