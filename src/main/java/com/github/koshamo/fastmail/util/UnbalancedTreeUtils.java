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

package com.github.koshamo.fastmail.util;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TreeItem;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class UnbalancedTreeUtils {

	private UnbalancedTreeUtils() {
		// prevent this class from instantiation
	}
	
	public static <T> TreeItem<T> unbalancedTreeToJfxTreeItems(
			final UnbalancedTree<T> tree) {
		if (tree == null)
			return null;

		final TreeItem<T> root = new TreeItem<>();
		
		T elem = tree.getRootItem();
		TreeItem<T> treeItem = new TreeItem<>(elem);
		
		boolean more = true;
		do {
			if (tree.hasSubtree(elem)) {
				TreeItem<T> subtree = unbalancedTreeToJfxTreeItems(tree.getSubtree(elem));
				treeItem.getChildren().addAll(subtree.getChildren());
			}
			root.getChildren().add(treeItem);
			if (tree.hasNext(elem)) {
				elem = tree.next(elem);
				treeItem = new TreeItem<>(elem);
			} else
				more = false;
		} while (more);
		
		return root;
	}
	
	public static <T> List<T> unbalancedTreeToList(final UnbalancedTree<T> tree) {
		if (tree == null)
			return null;
		
		List<T> list = new ArrayList<>();
		T elem = tree.getRootItem();
		list.add(elem);
		
		boolean more = true;
		do {
			if (tree.hasSubtree(elem)) {
				List<T> subtree = unbalancedTreeToList(tree.getSubtree(elem));
				list.addAll(subtree);
			}
			if (tree.hasNext(elem)) {
				elem = tree.next(elem);
				list.add(elem);
			} else
				more = false;
		} while (more);
		
		return list;
	}
}
