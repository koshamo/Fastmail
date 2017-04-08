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
 * @author jochen
 *
 */
public class MessageConsumer extends AnimationTimer {

	/* package private */
	StringProperty text;
	DoubleProperty progress;
	MessageMarket market;
	
	MessageItem currentItem;
	MessageItem tmpItem;
	
	public MessageConsumer(StringProperty text, DoubleProperty progress) {
		this.text = text;
		this.progress = progress;
		this.market = MessageMarket.getInstance();
	}
	

	/* (non-Javadoc)
	 * @see javafx.animation.AnimationTimer#handle(long)
	 */
	@Override
	public void handle(long now) {
		if (currentItem != null && currentItem.isProcessed()) {
			text.unbind();
			text.set(""); //$NON-NLS-1$
			progress.unbind();
			progress.set(0.0);
			currentItem = null;
		}
		if (currentItem == null && tmpItem == null)
			currentItem = market.consumeMessage();
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
