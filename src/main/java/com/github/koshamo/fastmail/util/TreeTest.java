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

/**
 * @author Dr. Jochen Raßler
 *
 */
public class TreeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UnbalancedTree<String> tree = new UnbalancedTree<>();
		tree.add("eins");
		tree.add("zwei");
		tree.add("drei");
		tree.print();
		System.out.println();

		tree.add("zwei_eins", "zwei");
		tree.add("zwei_zwei", "zwei");
		tree.print();
		System.out.println();

		tree.add("vier");
		tree.print();
		System.out.println();
		tree.remove("drei");
		tree.print();
		System.out.println();
//		tree.remove("eins");
		tree.remove("zwei_eins");
		tree.print();
		System.out.println();

		tree.add("2.2.1", "zwei_zwei");
		tree.add("2.2.2", "zwei_zwei");
		tree.add("2.2.3", "zwei_zwei");
		tree.print();
		System.out.println();

		tree.remove("2.2.1");
		tree.print();
		System.out.println();
	}

}
