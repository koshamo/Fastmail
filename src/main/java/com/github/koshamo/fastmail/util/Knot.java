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
 * This class represents the Knot / leaf of a UnbalancedTree.
 * @see UnbalancedTree
 *  
 * @author Dr. Jochen Raßler
 *
 * @param <T>	UnbalancedTree is a container class, so you are able to store
 * any data in this container
 */
/*private*/ class Knot<T> {
	private T elem;
	private Knot<T> next;
	private UnbalancedTree<T> subtree;
	
	/**
	 * Create a new Knot with this element
	 * 
	 * @param elem	the element to be stored in the knot
	 */
	public Knot(T elem) {
		this.elem = elem;
		next = null;
		subtree = null;
	}
	
	/**
	 * Has this Knot a successor
	 * 
	 * @return	true, if this knot hass a successor
	 */
	public boolean hasNext() {
		return next != null;
	}
	
	/**
	 * Get the successor of this knot
	 * 
	 * @return	the knots successor
	 */
	public Knot<T> next() {
		return next;
	}
	
	/**
	 * Append this element as a new knot 
	 * 
	 * @param elem	a element
	 */
	public void add(T elem) {
		if (!hasNext())
			next = new Knot<>(elem);
	}
		
	/**
	 * Get the element stored in this knot
	 * 
	 * @return	the element in this knot
	 */
	public T getElem() {
		return elem;
	}
	
	/**
	 * Add this element as a subtree or append it to the subtree
	 * 
	 * @param elem
	 */
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
		
	/**
	 * Add the subtree as subtree for this knot
	 * 
	 * @param tree	the subtree to be added
	 */
	public void addSubtree(UnbalancedTree<T> tree) {
		if (subtree == null)
			subtree = tree;
		else
			throw new IllegalStateException("Element " + elem + "already has subtree");
	}
	
	/**
	 * Has this knot a subtree
	 * 
	 * @return	true, if it has a subtree
	 */
	public boolean hasSubtree() {
		return subtree != null;
	}
	
	/**
	 * Get the subtree of this knot
	 * 
	 * @return	the subtree, may return null
	 */
	public UnbalancedTree<T> getSubtree() {
		return subtree;
	}
	
	/**
	 * Change the successor of this knot (used for knot removal)
	 * 
	 * @param next	the next knot for this knot
	 */
	public void setNext(Knot<T> next) {
		this.next = next;
	}
	
	/**
	 * Remove the subtree and all its elements and subtrees from this knot
	 */
	public void removeSubtree() {
		subtree = null;
	}
}