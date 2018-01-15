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

import com.github.koshamo.fastmail.mail.EmailTableData;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.paint.Color;

/**
 * This class is intended to be used for the main Mail TableView
 * to set formatting differences between read and unread mails.
 * Hence this class should be set for the "read" attribute 
 * TableColumn.
 * 
 * @author jochen
 *
 */
public class TableCellFactory extends CheckBoxTableCell<EmailTableData, Boolean> {
	
	/**
	 * Constructor for this class.
	 * 
	 * @param col	this constructor needs the mail's "read" column
	 * as parameter, because this is the argument the formatting is based
	 * on.
	 */
	public TableCellFactory(final TableColumn<EmailTableData, Boolean> col) {
		forTableColumn(col);
	}
	
	/* (non-Javadoc)
	 * 
	 * The actual formatting is done in this updateItem method.
	 * 
	 * @see javafx.scene.control.cell.CheckBoxTableCell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(final Boolean item, final boolean empty) {
		super.updateItem(item, empty);
		TableRow<EmailTableData> row = getTableRow();
		
		if (item == null || empty) {
			setText(null);
		} else {
			if (item.booleanValue()) {
				for (Node n : row.getChildrenUnmodifiable())
					if (n instanceof Labeled) {
						Labeled l = (Labeled) n;
						l.setUnderline(false);
						l.setTextFill(Color.BLACK);
					}
			} else {
				for (Node n : row.getChildrenUnmodifiable()) 
					if (n instanceof Labeled) {
						Labeled l = (Labeled) n;
						l.setUnderline(true);
						l.setTextFill(Color.LIMEGREEN);
					}
			}
		}
	}

}
