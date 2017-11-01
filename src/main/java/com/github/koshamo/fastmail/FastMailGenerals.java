/*
 * Copyright (C) 2017  Dr. Jochen Raﬂler
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

/**
 * This class holds general information about Fastmail.
 * 
 * @author jochen
 *
 */
public class FastMailGenerals {
	private static final String applicationName = "Fastmail"; //$NON-NLS-1$
	private static final String version = "0.1.0"; //$NON-NLS-1$
	private static final String author = "Dr.-Ing. Jochen Raﬂler"; //$NON-NLS-1$
	private static final String license = "GNU GPL 2.0 with Classpath Exception"; //$NON-NLS-1$
	
	/**
	 * Returns the name of this application
	 * @return application name as String
	 */
	public static String getApplicationName() {
		return applicationName;
	}
	
	/**
	 * Returns the application's version
	 * @return version as String
	 */
	public static String getVersion() {
		return version;
	}
	
	/**
	 * Returns the application's author(s)
	 * @return authors as String
	 */
	public static String getAuthor() {
		return author;
	}
	
	/**
	 * Returns the application's license
	 * @return license as String
	 */
	public static String getLicense() {
		return license;
	}
	
	/**
	 * Returns the applications name and the current version as one String
	 * 
	 * @return name and version String
	 */
	public static String getNameVersion() {
		return new StringBuilder(applicationName).append(" ").append(version).toString(); //$NON-NLS-1$
	}
}
