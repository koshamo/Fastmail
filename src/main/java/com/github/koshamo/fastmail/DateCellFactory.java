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

package com.github.koshamo.fastmail;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/** The DateCellFactory is a general formatter class to work with Java's new Time 
 * and Date API
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
public class DateCellFactory implements 
	Callback<TableColumn<EmailTableData, String>, TableCell<EmailTableData, String>> {

	
	/**
	 * format the incoming date String using the rules described above
	 * 
	 * @param date the String representing the date 
	 * @return the formatted String using above rules
	 */
	static String formatString(String date) {
		Instant mailDate = Instant.parse(date);
		ZonedDateTime zonedDate = mailDate.atZone(ZoneId.systemDefault());
		ZonedDateTime today = ZonedDateTime.now();
		ZonedDateTime lastWeek = ZonedDateTime.now().minusWeeks(1);
		DateTimeFormatter formatter;
		// today
		if (today.getYear() == zonedDate.getYear() 
				&& today.getMonthValue() == zonedDate.getMonthValue()
				&& today.getDayOfMonth() == zonedDate.getDayOfMonth())
			formatter = DateTimeFormatter.ofPattern("HH:mm");
		// last week
		else if (zonedDate.isAfter(lastWeek))
			formatter = DateTimeFormatter.ofPattern("EEE HH:mm");
		// older dates
		else
			formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
		return zonedDate.format(formatter);
	
	}


	/* (non-Javadoc)
	 * @see javafx.util.Callback#call(java.lang.Object)
	 */
	@Override
	public TableCell<EmailTableData, String> call(TableColumn<EmailTableData, String> col) {
		TableCell<EmailTableData, String> cell = new TableCell<EmailTableData, String>() {
			@Override
			public void updateItem (final String item, boolean empty) {
				if (item != null)
					setText(DateCellFactory.formatString(item));
			}
		};
		return cell;
	}
	
}
