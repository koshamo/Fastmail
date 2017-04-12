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

import com.github.koshamo.fastmail.util.MessageItem.MessageType;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

/**
 * The MessageConsumer is a JavaFX thread doing the update of the status bar.
 * <p>
 * This class is intended to consume the in-application messages put in the
 * MessageMarket by producers and update the status bar. This class shows the
 * messages stored in MessageItems and handles updating of the status bar.
 * 
 * @author jochen
 *
 */
public class MessageConsumer extends AnimationTimer {

	private StringProperty text;
	private DoubleProperty progress;
	private MessageMarket market;
	
	private MessageItem currentItem;
	private MessageItem interruptedItem;
	
	/**
	 * The constructor needs the properties of the status label and the status
	 * bar, which will be bind to the MessageItem to be able to be updated
	 * by producers on the fly. This in particular will be used by saving
	 * several attachments, where the progress bar needs to be updated and 
	 * the current downloaded file will be named.
	 *   
	 * @param text		the property of the status label
	 * @param progress	the property of the status progress bar
	 */
	public MessageConsumer(StringProperty text, DoubleProperty progress) {
		this.text = text;
		this.progress = progress;
		this.market = MessageMarket.getInstance();
	}
	

	/* (non-Javadoc)
	 * The handle method processes the current MessageItem in every single
	 * UI frame.
	 * @see javafx.animation.AnimationTimer#handle(long)
	 */
	@Override
	public void handle(long now) {
		// if current item is processed, clear status bar
		if (currentItem != null && currentItem.isProcessed()) {
			text.unbind();
			text.set(""); //$NON-NLS-1$
			progress.unbind();
			progress.set(0.0);
			currentItem = null;
		}
		// if current item is processed and a high priority items exists
		if (currentItem == null && market.hasHighPriorityItem())
			currentItem = market.getNextHighPriorityItem();
		// if a low priority item is shown and a high priority item exists
		if (currentItem != null && currentItem.isInterruptible() 
				&& market.hasHighPriorityItem()) {
			interruptedItem = currentItem;
			currentItem = market.getNextHighPriorityItem();
		}
		// if no high priority item exists and a low priority item has been interrupted
		if (currentItem == null && interruptedItem != null) {
			currentItem = interruptedItem;
			interruptedItem = null;
		}
		// if no item is shown
		if (currentItem == null && interruptedItem == null)
			currentItem = market.consumeMessage();
		// display current item
		if (currentItem != null) {
			text.bind(currentItem.getMessageProperty());
			if (currentItem.getType() != MessageType.WORK)
				progress.bind(currentItem.getProgressProperty());
			else
				// set indeterminate
				progress.set(-1.0);
		}
	}

}
