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

import java.time.Instant;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * The class MessageItem is intended to send messages within the Fastmail
 * application. It can be fed in one thread and be consumed in another
 * thread controlled by the UI to show the messages.
 * <p>
 * This class is aimed to be used in a produced consumer pattern.
 * 
 * @author jochen
 *
 */
public class MessageItem {
	
	/**
	 * MessageType specifies the type of message , which controls the
	 * behavior of the MessageItem. Message types are:
	 * <p>
	 * <li> EXCEPTTION: to inform the user of a exception occurred in the application
	 * <li> ERROR: to inform the user of a user error
	 * <li> INFORMATION: this is a pure information of the application's state
	 * <li> WORK: information to the user that some work is in progress
	 * <li> PROGRESS: informs the user of the progress of a given task and when
	 * to expect it to finish
	 * 
	 * @author jochen
	 *
	 */
	public enum MessageType {EXCEPTION, ERROR, INFORMATION, WORK, PROGRESS} 
	
	private final SimpleStringProperty message;
	private final SimpleDoubleProperty progress;
	private final MessageType type;
	
	private boolean processed;
	private Instant timestamp;
	
	public MessageItem(final String message, final double progress, 
			final MessageType type) {
		this.message = new SimpleStringProperty(message);
		this.progress = new SimpleDoubleProperty(progress);
		this.type = type;
		this.processed = false;
	}
	
	public StringProperty getMessageProperty() {
		timestamp = Instant.now();
		return message;
	}
	
	public DoubleProperty getProgressProperty() {
		return progress;
	}
	
	public void updateProgress(double current) {
		progress.set(current);
	}
	
	public MessageType getType() {
		return type;
	}
	
	public void done() {
		processed = true;
	}
	
	public boolean isProcessed() {
		if (processed)
			return processed;
		if (type == MessageType.EXCEPTION)
			if (Instant.now().isAfter(timestamp.minusSeconds(20))) {
				processed = true;
				return processed;
			}
		if (type == MessageType.ERROR)
			if (Instant.now().isAfter(timestamp.minusSeconds(15))) {
				processed = true;
				return processed;
			}
		if (type == MessageType.INFORMATION)
			if (Instant.now().isAfter(timestamp.minusSeconds(30))) {
				processed = true;
				return processed;
			}
		if (progress.get() > 0.999)
			processed = true;
		return processed;
	}
}
