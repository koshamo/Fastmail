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

import java.util.LinkedList;
import java.util.Queue;

import com.github.koshamo.fastmail.util.MessageItem.MessageType;

/**
 * MessageMarket represents a Producer Consumer Pattern for MessageItems.
 * <p>
 * MessageMarket itself is a Singleton and represents a Producer Consumer
 * Pattern to send messages between worker threads and the UI.
 *  
 * @author jochen
 *
 */
public class MessageMarket {

	private static MessageMarket market = null;
	private Queue<MessageItem> kanban;
	
	
	/**
	 * Private constructor only for internal use, as we are a Singleton!
	 */
	private MessageMarket() {
		// prevent external construction
		kanban = new LinkedList<MessageItem>();
	}
	
	
	/**
	 * Get the one and only instance of MessageMarket to add or remove
	 * a MessageItem. 
	 * @return	the MessageMarket instance for this application
	 */
	public static MessageMarket getInstance() {
		if (market == null)
			market = new MessageMarket();
		return market;
	}
	
	
	/**
	 * This method adds a produced MessageItem to the internal list, which then 
	 * can be consumed by a consumer.
	 * <p>
	 * The internal data storage is organized as a FIFO. 
	 * @param item	the produced MessageItem
	 */
	public synchronized void produceMessage(MessageItem item) {
		kanban.add(item);
	}
	
	
	/**
	 * This method returns the next MessageItem to consume and removes it from
	 * the internal list. So you cannot consume it twice!
	 * @return	the MessageItem to consume
	 */
	public MessageItem consumeMessage() {
		return kanban.poll();
	}
	
	/**
	 * To show high priority items before the low priority items, you need to
	 * check, if any high priority items exist. This method checks if any
	 * high priority items are stored in its queue.
	 * 
	 * @return	true, if any high priority items exist, otherwise false
	 */
	public boolean hasHighPriorityItem() {
		for (MessageItem item : kanban)
			if (item.getType() == MessageType.ERROR 
					|| item.getType() == MessageType.EXCEPTION)
				return true;
		return false;
	}
	
	/**
	 * If the Queue holds a MessageItem with high priority, which should
	 * have been checked with hasHighPriorityItem(), get this item.
	 * 
	 * @return	the first high priority MessageItem, otherwise null
	 */
	public MessageItem getNextHighPriorityItem() {
		MessageItem highPrioItem = null;
		for (MessageItem item : kanban)
			if (item.getType() == MessageType.ERROR 
					|| item.getType() == MessageType.EXCEPTION) {
				highPrioItem = item;
				break;	// check only for the first
			}
		kanban.remove(highPrioItem);
		return highPrioItem;
	}
}
