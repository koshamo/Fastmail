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
	
	/**
	 * Create a new MessageItem to be consumed by a consumer to show it 
	 * in the UI.
	 * <p>
	 * The String represents the actual message, which may be decorated with
	 * detailed information using a MessagerFormat tool.
	 * <p>
	 * The progress is only used to show a progress bar, e.g. to show the progress
	 * of a download process. Set it to 0.0 if not needed.
	 * <p>
	 * The type indicates how this message item will be handled. Information, Work,
	 * and Progress types are interruptible by Exceptions and Errors, which will
	 * have a higher priority. The consumer is responsible to implement
	 * interruption of low priority items.
	 * 
	 * @param message	the actual message to display
	 * @param progress	the value of the progress bar. Values between 0.0 and 
	 * 1.0 are valid. Set it to 0.0 if not used
	 * @param type		the message type
	 */
	public MessageItem(final String message, final double progress, 
			final MessageType type) {
		this.message = new SimpleStringProperty(message);
		// TODO: check range
		this.progress = new SimpleDoubleProperty(progress);
		this.type = type;
		this.processed = false;
	}
	
	/**
	 * The Message Items String representation as property will be returned. 
	 * This is due the changeable status of the String representation 
	 * @return	the message's String representation as property
	 */
	public StringProperty getMessageProperty() {
		timestamp = Instant.now();
		return message;
	}
	
	/**
	 * The progress property is used to update the progress bar.
	 * @return	the progress of the current process as double property
	 */
	public DoubleProperty getProgressProperty() {
		return progress;
	}
	
	/**
	 * The producer may set the progress property either by using the property
	 * or just set a new value with this method, which should be convenient for
	 * most cases.
	 * @param current	the current progress value, range between 0.0 and 1.0
	 */
	public void updateProgress(double current) {
		// TODO: check range
		progress.set(current);
	}
	
	/**
	 * The producer may set the message representation either by using the 
	 * property or just set a new String with this method, which should be
	 * convenient for most cases
	 * @param message	the new String representation of the message item
	 */
	public void updateMessage(String message) {
		this.message.set(message);
	}
	
	/**
	 * Get the type of the message item
	 * @return	the message tpye
	 */
	public MessageType getType() {
		return type;
	}
	
	/**
	 * Indicate that this message item is done. 
	 * <p>
	 * This method should be used for messages of type WORK and PROGRESS, 
	 * as the former is indeterminate and the latter uses double values 
	 * for the progress. Indicated file sizes of attachments may differ 
	 * from real file sizes, so use this method to be sure the message item
	 * is finalized.  
	 */
	public void done() {
		processed = true;
	}
	
	/**
	 * Check if this message item has been processed
	 * @return	the processed status 
	 */
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
