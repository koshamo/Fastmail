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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class TreeTest extends Application{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		StackPane pane = new StackPane();
		
//		test_Tree();
		
		TreeItem<String> item = 
				UnbalancedTreeUtils.unbalancedTreeToJfxTreeItems(createTree());
		TreeView<String> treeView = new TreeView<>(item);
		
		pane.getChildren().add(treeView);
		
		primaryStage.setScene(new Scene(pane));
		primaryStage.sizeToScene();
		primaryStage.show();
	}
	
	UnbalancedTree<String> createTree() {
		UnbalancedTree<String> tree = new UnbalancedTree<>();
		tree.add("eins");
		tree.add("zwei");
		tree.add("drei");
		System.out.println(tree);
		System.out.println();

		tree.add("zwei_eins", "zwei");
		tree.add("zwei_zwei", "zwei");
		System.out.println(tree);
		System.out.println();

		tree.add("vier");
		System.out.println(tree);
		System.out.println();
		tree.remove("drei");
		System.out.println(tree);
		System.out.println();
//		tree.remove("eins");
		tree.remove("zwei_eins");
		System.out.println(tree);
		System.out.println();

		tree.add("2.2.1", "zwei_zwei");
		tree.add("2.2.2", "zwei_zwei");
		tree.add("2.2.3", "zwei_zwei");
		System.out.println(tree);
		System.out.println();
		return tree;
	}
	
	void test_Tree() {
		UnbalancedTree<String> tree = new UnbalancedTree<>();
		tree.add("eins");
		tree.add("zwei");
		tree.add("drei");
		System.out.println(tree);
		System.out.println();

		tree.add("zwei_eins", "zwei");
		tree.add("zwei_zwei", "zwei");
		System.out.println(tree);
		System.out.println();

		tree.add("vier");
		System.out.println(tree);
		System.out.println();
		tree.remove("drei");
		System.out.println(tree);
		System.out.println();
//		tree.remove("eins");
		tree.remove("zwei_eins");
		System.out.println(tree);
		System.out.println();

		tree.add("2.2.1", "zwei_zwei");
		tree.add("2.2.2", "zwei_zwei");
		tree.add("2.2.3", "zwei_zwei");
		System.out.println(tree);
		System.out.println();

		tree.remove("2.2.1");
		System.out.println(tree);
		System.out.println();
		
	}

}
