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

package com.github.koshamo.fastmail.events;

import com.github.koshamo.fastmail.util.EmailTableData;
import com.github.koshamo.fiddler.DataEvent;
import com.github.koshamo.fiddler.EventHandler;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class ShowAddMailEvent extends DataEvent<FolderItemMeta, EmailTableData> {

	/**
	 * @param source
	 * @param target
	 * @param meta
	 * @param data
	 */
	public ShowAddMailEvent(EventHandler source, EventHandler target, FolderItemMeta meta, EmailTableData data) {
		super(source, target, meta, data);
	}

}
