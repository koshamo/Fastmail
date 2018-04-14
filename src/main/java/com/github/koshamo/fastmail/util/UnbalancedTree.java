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
 * The UnbalancedTree is a simple tree representation of data, that needs
 * to be in same order as added to tree.
 * 
 * Javas own trees are optimized, so that they perform a guaranteed maximum
 * of log(n) while searching for a item at the cost of data rearrangement.
 * In some cases (e.g. file system representation, mail folder representation)
 * you need to save the data in a tree structure without an optimization of
 * internal data arrangement, because you need to receive the data as is.
 * This is not possible with Javas optimized tree structures. And therefore
 * this class has been written, to be able to get the data as is at the cost 
 * of low performance.
 * 
 * @param <T>	This is a container class so it is possible to store any
 * data within this container.
 * 
 * @author Dr. Jochen Raßler
 *
 */
public class UnbalancedTree<T> {
	private Node<T> root;
	
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
	public UnbalancedTree(final T elem) {
		root = new Node<>(elem);
	}
	
	/**
	 * Add an element to the main tree
	 * 
	 * @param elem	the element to be added
	 */
	public void add(final T elem) {
		if (root == null) {
			root = new Node<>(elem);
		}
		else {
			Node<T> cur = root;
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
	public void add(final T elem, final T parent) {
		if (root == null)
			throw new NoSuchElementException("No Element " + parent + " in Tree");
		
		final Node<T> cur = getKnot(parent);
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
	public void addSubtree(final UnbalancedTree<T> tree, final T parent) {
		if (root == null)
			throw new NoSuchElementException("No Element " + parent + " in Tree");
		
		final Node<T> cur = getKnot(parent);
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
	public void remove(final T elem) {
		final Node<T> cur = getKnot(elem);
		if (cur == null)
			throw new NoSuchElementException("No Element " + elem + " in Tree");
		
		if (cur == root)
			root = cur.next();
		else {
			final Node<T> prev = findPrev(cur);
			if (prev != null)
				prev.setNext(cur.next());
			else {
				final Node<T> parent = findParent(cur);
				if (parent != null) {
					parent.getSubtree().remove(elem);
				}
			}
		}
	}
	
	/**
	 * Has this element a successor
	 * 
	 * @param elem	element to check
	 * @return	true, if it has a successor
	 */
	public boolean hasNext(final T elem) {
		final Node<T> knot = getKnot(elem);
		if (knot == null) 
			throw new NoSuchElementException("Element " + elem + " not in this Unbalanced Tree");
		return knot.hasNext();
	}
	
	/**
	 * Get the successor of this element
	 * 
	 * @param elem	element
	 * @return	successor of element
	 */
	public T next(final T elem) {
		final Node<T> knot = getKnot(elem);
		if (knot == null) 
			throw new NoSuchElementException("Element " + elem + " not in this Unbalanced Tree");
		return knot.next().getElem();
	}
	
	/**
	 * Has this element a subtree
	 * 
	 * @param elem	element to check
	 * @return	true, if this element has a subtree
	 */
	public boolean hasSubtree(final T elem) {
		final Node<T> knot = getKnot(elem);
		if (knot == null) 
			throw new NoSuchElementException("Element " + elem + " not in this Unbalanced Tree");
		return knot.hasSubtree();
	}
	
	/**
	 * Get the subtree of this element
	 * @param elem	element
	 * @return	subtree of element
	 */
	public UnbalancedTree<T> getSubtree(final T elem) {
		final Node<T> knot = getKnot(elem);
		if (knot == null) 
			throw new NoSuchElementException("Element " + elem + " not in this Unbalanced Tree");
		return knot.getSubtree();
	}
	
	/**
	 * Get the root element
	 * 
	 * @return	the root element
	 */
	public T getRootItem() {
		if (root == null)
			return null;
		return root.getElem();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return treeToString(root, "UnbalancedTree: ");
	}
	/**
	 * Get the root of this Unbalanced Tree.
	 * 
	 * @return	the trees root
	 */
	/*private*/ Node<T> getRoot() {
		return root;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof UnbalancedTree<?>))
			return false;

		UnbalancedTree<T> other = (UnbalancedTree<T>)obj;
		if (getRootItem().getClass() != other.getRootItem().getClass())
			return false;
		
		T here = getRootItem();
		T there = other.getRootItem();
		
		boolean more = true;
		
		do {
			if (!here.equals(there))
				return false;
			
			// check subtree
			if (this.hasSubtree(here)) {
				if (!other.hasSubtree(there)) {
					return false;
				} else {
					boolean same = 
							this.getSubtree(here).equals(other.getSubtree(there));
					if (!same)
						return false;
				}
			} else {
				if (other.hasSubtree(there)) {
					return false;
				}
			}
			
			// check next item
			if (this.hasNext(here)) {
				if (!other.hasNext(there)) {
					return false;
				}
				here = this.next(here);
				there = other.next(there);
			} else {
				if (other.hasNext(there)) {
					return false;
				}
				more = false;
			}
		
		} while (more);
		return true;
	}
	
	
	/**
	 * Get the Knot of this element
	 * 
	 * @param elem	the element to be looked for
	 * @return	the corresponding knot
	 */
	private Node<T> getKnot(final T elem) {
		if (root == null)
			return null;
		Node<T> cur = root;
		do {
			if (cur.getElem().equals(elem))
				return cur;
			if (cur.hasSubtree()) {
				final Node<T> sub = cur.getSubtree().getKnot(elem); 
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
	private Node<T> findPrev(final Node<T> knot) {
		if (root == null)
			return null;
		Node<T> cur = root;
		do {
			if (cur.next() == knot)
				return cur;
			if (cur.hasSubtree()) {
				Node<T> sub = cur.getSubtree().findPrev(knot); 
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
	private Node<T> findParent(final Node<T> knot) {
		if (root == null)
			return null;
		Node<T> cur = root;
		do {
			if (cur.hasSubtree()) {
				Node<T> sub = cur.getSubtree().getRoot();
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
	 * Construct a String representation of this Unbalanced Tree
	 * (using recursion)
	 * 
	 * @param start		the start item for the current recursion 
	 * @param curString	the start string for the current recursion
	 * @return			the resulting string from this recursion
	 */
	private String treeToString(final Node<T> start, final String curString) {
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
	private String subtreeToString(final UnbalancedTree<T> subtree, 
			final String curString){
		return curString + " \\" + subtree.treeToString(subtree.getRoot(), "") + "/";
	}
}