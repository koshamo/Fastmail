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

package com.github.koshamo.fastmail.gui.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.github.koshamo.fastmail.util.EmailTableData_NEW;

import javafx.scene.control.TableCell;

/** The DateCellFactory is a table cell formatter class to work with Java's 
 * new Time and Date API
 * <p>
 * The design is, that the current date and time is shown using the following 
 * rules
 * <br>
 * today: only the time is shown 
 * <br>
 * last week: name of day and time is shown
 * <br> 
 * older dates: full date is shown
 * <p>
 * 
 * @author jochen
 *
 */
public class DateCellFactory extends TableCell<EmailTableData_NEW, String> {

	@Override
	protected void updateItem(final String item, final boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else {
			setText(formatString(item));
		}
	}
	
	
	/**
	 * format the incoming date String using the rules described above
	 * 
	 * @param date the String representing the date 
	 * @return the formatted String using above rules
	 */
	private static String formatString(final String date) {
		Instant mailDate = Instant.parse(date);
		ZonedDateTime zonedDate = mailDate.atZone(ZoneId.systemDefault());
		ZonedDateTime today = ZonedDateTime.now();
		ZonedDateTime lastWeek = ZonedDateTime.now().minusWeeks(1);
		DateTimeFormatter formatter;
		// today
		if (today.getYear() == zonedDate.getYear() 
				&& today.getMonthValue() == zonedDate.getMonthValue()
				&& today.getDayOfMonth() == zonedDate.getDayOfMonth())
			formatter = DateTimeFormatter.ofPattern("HH:mm"); //$NON-NLS-1$
		// last week
		else if (zonedDate.isAfter(lastWeek))
			formatter = DateTimeFormatter.ofPattern("EEE HH:mm"); //$NON-NLS-1$
		// older dates
		else
			formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"); //$NON-NLS-1$
		return zonedDate.format(formatter);
	
	}


	
}
