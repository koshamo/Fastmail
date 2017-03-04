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
public class TreeCellFactory extends TreeCell<String> {

	private TextField textField;
	private final ContextMenu contextMenu = new ContextMenu();
	
	/**
	 * The class constructor builds the context menu
	 */
	public TreeCellFactory() {
		MenuItem addMenuItem = new MenuItem("Add Folder");
		contextMenu.getItems().add(addMenuItem);
		addMenuItem.setOnAction((ActionEvent t) -> {
			TreeItem<String> newFolder = new TreeItem<>("New Folder");
			getTreeItem().getChildren().add(newFolder);
		});
	}
	
	/* 
	 * Beginn editing: create text field and initialize it.
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
		
		setText(getItem());
		setGraphic(getTreeItem().getGraphic());
	}

	/* 
	 * After completion of editing, this method cares for update on tree view and server.
	 * The latter currently needs to be implemented.
	 *  
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		
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
				if (!getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
					setContextMenu(contextMenu);
				}
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
	public void commitEdit(String newValue) {
		// account item should not be modified here
		if (getTreeItem().getParent() != null && 
				getTreeItem().getParent().getParent() == null) {
			setText(getItem());
			setGraphic(getTreeItem().getGraphic());
			return;
		}
		// special folders also should not be renamed
		if (getText().equals("INBOX") || getText().equals("Drafts")
				|| getText().equals("Sent") || getText().equals("Trash")) {
			setText(getItem());
			setGraphic(getTreeItem().getGraphic());
			return;
		}
		
		// TODO: add updating on server

		setText(getString());
		setGraphic(getTreeItem().getGraphic());
		super.commitEdit(newValue);
	}
	
	/**
	 * Returns the String within the text field
	 * 
	 * @return the String the user entered in the text field
	 */
	private String getString() {
		return getItem() == null ? "" : getItem().toString();
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
