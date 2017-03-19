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

package com.github.koshamo.fastmail.util;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.mail.FolderItem;
import com.github.koshamo.fastmail.mail.MailTreeViewable;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
	private final ContextMenu contextMenu = new ContextMenu();
	private MailTreeViewable editItem;
	
	/**
	 * The class constructor builds the context menu
	 */
	public TreeCellFactory() {
		if (getTreeItem().getValue().isAccount()) {
			MenuItem editAccountMenu = new MenuItem("Edit Account");
			// TODO: add action listener
			MenuItem deleteAccountMenu = new MenuItem("Delete Account");
			// TODO: add action listener
			contextMenu.getItems().addAll(editAccountMenu, deleteAccountMenu);
		}
		else {
			MenuItem addSubFolderMenu = new MenuItem("Add Sub Folder");
			// TODO: add action listener
			contextMenu.getItems().add(addSubFolderMenu);
		}
		MenuItem addFolderMenu = new MenuItem("Add Folder");
		contextMenu.getItems().add(addFolderMenu);
		addFolderMenu.setOnAction((ActionEvent t) -> {
			// TODO: the Folder class should not be used here. 
			// TODO: Add interface for folder creation in the FolderItem class
			Folder parent = getTreeItem().getValue().getParentFolder();
			Folder newFolder = null;
			try {
				newFolder = parent.getFolder("new Folder");
				if (!newFolder.exists())
					newFolder.create(Folder.HOLDS_MESSAGES);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			FolderItem newFolderItem = new FolderItem(newFolder);
			if (!getTreeItem().getChildren().contains(newFolderItem))
				getTreeItem().getChildren().add(new TreeItem<MailTreeViewable>(newFolderItem));
		});
	}
	
	/* 
	 * Begin editing: create text field and initialize it.
	 * 
	 * @see javafx.scene.control.TreeCell#startEdit()
	 */
	@Override
	public void startEdit() {
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
	public void updateItem(MailTreeViewable item, boolean empty) {
		// Account and special folders are not renameable!
		if (item.isAccount() || 
				"INBOX".equals(item.getName()) ||
				"Drafts".equals(item.getName()) || 
				"Sent".equals(item.getName()) ||
				"Trash".equals(item.getName())) {
			cancelEdit();
			return;
		}
		super.updateItem(item, empty);
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
				setContextMenu(contextMenu);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javafx.scene.control.TreeCell#commitEdit(java.lang.Object)
	 */
	// TODO: test and fix this method
	/*
	 * This method currently doesn't work as wished. But this may be
	 * because of overriding from account folder watcher.
	 * Need further investigation of this topic
	 */
	@Override 
	public void commitEdit(MailTreeViewable newValue) {
		super.commitEdit(newValue);
		FolderItem folderItem = (FolderItem) editItem;
		folderItem.renameTo(((FolderItem)newValue).getFolder());
	}
	
	/**
	 * Returns the String within the text field
	 * 
	 * @return the String the user entered in the text field
	 */
	private String getString() {
		return getItem().getName() == null ? "" : getItem().getName();
	}

	/**
	 * Creates the text field for folder manipulation
	 */
	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnKeyReleased((KeyEvent t) -> {
			if (t.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});
	}
	
}
