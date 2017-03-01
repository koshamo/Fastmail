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
import java.util.Comparator;

/**
 * The DateCellComparator is designed to work with the DateCellFactory.
 * <p>
 * As the date uses the rules: today only time is shown, last week we
 * see the day of the week plus the time, else we see the whole date.
 * We need a comparator to work with this date format to get a correct sorting 
 * order.
 * @author jochen
 *
 */
public class DateCellComparator implements Comparator<String> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String o1, String o2) {
		Instant date1 = Instant.parse(o1);
		Instant date2 = Instant.parse(o2);
		if (date1.isBefore(date2))
			return -1;
		if (date1.isAfter(date2))
			return 1;
		return 0;
	}

}
