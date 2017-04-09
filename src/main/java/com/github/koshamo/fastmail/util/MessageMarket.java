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
}
