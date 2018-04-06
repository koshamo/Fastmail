/*
 * Copyright (C) 2018  Dr. Jochen Raßler
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

import java.util.Objects;

import com.github.koshamo.fastmail.util.MailTreeViewable;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;

/**
 * @author Dr. Jochen Raßler
 *
 */
/*private*/ class TreeViewChangeListener implements ChangeListener<TreeItem<MailTreeViewable>> {

	private final FastGui gui;
	
	/*private*/ TreeViewChangeListener(FastGui gui) {
		this.gui = Objects.requireNonNull(gui, "gui must not be null");
	}
	
	@Override
	public void changed(
			ObservableValue<? extends TreeItem<MailTreeViewable>> observedItem, 
					TreeItem<MailTreeViewable> oldVal,
					TreeItem<MailTreeViewable> newVal) {
		// this should only be the case if a account has been removed
		if (newVal == null) {
			return;	
		}
		// context Menu
		gui.getTreeContextMenuItems().clear();
		gui.getTreeContextMenuItems().add(gui.addAddFolderItem());
		if (!newVal.getValue().isAccount() && 
				!newVal.getValue().getName().equals("INBOX") && //$NON-NLS-1$
				!newVal.getValue().getName().equals("Drafts") && //$NON-NLS-1$
				!newVal.getValue().getName().equals("Sent") && //$NON-NLS-1$
				!newVal.getValue().getName().equals("Trash")) //$NON-NLS-1$
			gui.getTreeContextMenuItems().add(gui.addDeleteFolderItem());
		if (newVal.getValue().isAccount()) {
			gui.getTreeContextMenuItems().add(new SeparatorMenuItem());
			gui.getTreeContextMenuItems().add(gui.addEditAccountItem());
		}
		// buttons and table view
		gui.setComponentsForNoMailSelected();
//		folderMailTable.setItems(newVal.getValue().getFolderContent().sorted());
	}
}
