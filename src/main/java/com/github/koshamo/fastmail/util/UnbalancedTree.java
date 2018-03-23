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

import java.util.NoSuchElementException;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class UnbalancedTree<T> {
	private Knot<T> root;
	
	/**
	 * Create a new empty Unbalanced Tree
	 */
	public UnbalancedTree() {
		root = null;
	}
	
	/**
	 * Create a new Unbalaced Tree with the given element
	 * 
	 * @param elem	the element to be stored in the Tree
	 */
	public UnbalancedTree(T elem) {
		root = new Knot<>(elem);
	}
	
	/**
	 * Add an element to the main tree
	 * 
	 * @param elem	the element to be added
	 */
	public void add(T elem) {
		if (root == null) {
			root = new Knot<>(elem);
		}
		else {
			Knot<T> cur = root;
			while (cur.hasNext())
				cur = cur.next();
			cur.add(elem);
		}
	}
	
	/**
	 * Add an element as a child to the element parent
	 *  
	 * @param elem	the element to be added
	 * @param parent	the elements parent
	 */
	public void add(T elem, T parent) {
		if (root == null)
			throw new NoSuchElementException("No Element " + parent + " in Tree");
		
		Knot<T> cur = getKnot(parent);
		if (cur != null)
			cur.addSubtree(elem);
		else
			throw new NoSuchElementException("No Element " + parent + " in Tree");
	}

	/**
	 * Add an Unbalanced Tree as child tree to the element parent
	 * 
	 * @param tree	the Tree to be added
	 * @param parent	the elements parent
	 */
	public void addSubtree(UnbalancedTree<T> tree, T parent) {
		if (root == null)
			throw new NoSuchElementException("No Element " + parent + " in Tree");
		
		Knot<T> cur = getKnot(parent);
		if (cur != null)
			cur.addSubtree(tree);
		else
			throw new NoSuchElementException("No Element " + parent + " in Tree");
	}
	
	/**
	 * Remove the given element from the Unbalanced Tree.
	 * 
	 * Note: if the element to be removed has children, they will be
	 * removed as well.
	 * 
	 * @param elem	the element to be removed
	 */
	public void remove(T elem) {
		Knot<T> cur = getKnot(elem);
		if (cur == null)
			throw new NoSuchElementException("No Element " + elem + " in Tree");
		
		if (cur == root)
			root = cur.next();
		else {
			Knot<T> prev = findPrev(cur);
			if (prev != null)
				prev.setNext(cur.next());
			else {
				Knot<T> parent = findParent(cur);
				if (parent != null) {
					parent.getSubtree().remove(elem);
				}
			}
		}
	}
	
	/**
	 * Get the Knot of this element
	 * 
	 * @param elem	the element to be looked for
	 * @return	the corresponding knot
	 */
	private Knot<T> getKnot(T elem) {
		if (root == null)
			return null;
		Knot<T> cur = root;
		do {
			if (cur.getElem().equals(elem))
				return cur;
			if (cur.hasSubtree()) {
				Knot<T> sub = cur.getSubtree().getKnot(elem); 
				if (sub != null)
					return sub;
			}
		} while(cur.hasNext() && (cur = cur.next()) != null);
		return null;
	}
	
	/**
	 * Find the predecessor of this element
	 * 
	 * @param knot	the current knot
	 * @return	the knots predecessor
	 */
	private Knot<T> findPrev(Knot<T> knot) {
		if (root == null)
			return null;
		Knot<T> cur = root;
		do {
			if (cur.next() == knot)
				return cur;
			if (cur.hasSubtree()) {
				Knot<T> sub = cur.getSubtree().findPrev(knot); 
				if (sub != null)
					return sub;
			}
		} while(cur.hasNext() && (cur = cur.next()) != null);
		return null;
	}
	
	/**
	 * Find the knots parent
	 * 
	 * @param knot	the knot
	 * @return	the knots parent
	 */
	private Knot<T> findParent(Knot<T> knot) {
		if (root == null)
			return null;
		Knot<T> cur = root;
		do {
			if (cur.hasSubtree()) {
				Knot<T> sub = cur.getSubtree().getRoot();
				if (sub == knot)
					return cur;
				else { 
					sub = findParent(sub);
					if (sub != null)
						return sub;
				}
			}
			if (cur.next() == knot)
				return cur;
		} while(cur.hasNext() && (cur = cur.next()) != null);
		return null;
	}

	/**
	 * Get the root of this Unbalanced Tree.
	 * 
	 * @return	the trees root
	 */
	public Knot<T> getRoot() {
		return root;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return treeToString(root, "UnbalancedTree: ");
	}
	
	/**
	 * Construct a String representation of this Unbalanced Tree
	 * (using recursion)
	 * 
	 * @param start		the start item for the current recursion 
	 * @param curString	the start string for the current recursion
	 * @return			the resulting string from this recursion
	 */
	private String treeToString(Knot<T> start, String curString) {
		if (start == null) {
			return curString + "empty";
		}
		String apString = curString + start.getElem();
		if (start.hasSubtree()) {
			apString = subtreeToString(start.getSubtree(), apString);
		}
		if (start.hasNext()) {
			apString = apString + ", ";
			apString = treeToString(start.next(), apString);
		}
		return apString;
	}
	
	/**
	 * Include the subtree to the String representation
	 * 
	 * @param subtree	the current element with subtree
	 * @param curString	the current String representation
	 * @return	the resulting String representation
	 */
	private String subtreeToString(UnbalancedTree<T> subtree, String curString){
		return curString + " \\" + subtree.treeToString(subtree.getRoot(), "") + "/";
	}
}

class Knot<T> {
	private T elem;
	private Knot<T> next;
	private UnbalancedTree<T> subtree;
	
	public Knot(T elem) {
		this.elem = elem;
		next = null;
		subtree = null;
	}
	
	public boolean hasNext() {
		return next != null;
	}
	
	public Knot<T> next() {
		return next;
	}
	
	public void add(T elem) {
		if (!hasNext())
			next = new Knot<>(elem);
	}
		
	public T getElem() {
		return elem;
	}
	
	public void addSubtree(T elem) {
		if (subtree == null)
			subtree = new UnbalancedTree<>(elem);
		else {
			Knot<T> cur = subtree.getRoot();
			while (cur.hasNext())
				cur = cur.next;
			cur.setNext(new Knot<>(elem));
		}
	}
		
	public void addSubtree(UnbalancedTree<T> tree) {
		if (subtree == null)
			subtree = tree;
		else
			new IllegalStateException("Element " + elem + "already has subtree");
	}
	
	public boolean hasSubtree() {
		return subtree != null;
	}
	
	public UnbalancedTree<T> getSubtree() {
		return subtree;
	}
	
	public void setNext(Knot<T> next) {
		this.next = next;
	}
	
	public void removeSubtree() {
		subtree = null;
	}
}