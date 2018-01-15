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

import com.github.koshamo.fastmail.gui.FastGui;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.application.Application;
import javafx.application.Platform;

public class Fastmail {

	public static void main(String[] args) {
		SerializeManager manager = SerializeManager.getInstance();
		manager.deserialize();
		Application.launch(FastGui.class, args);
		manager.serialize();
		Platform.exit();
	}

}
