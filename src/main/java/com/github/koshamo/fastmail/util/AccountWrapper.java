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

package com.github.koshamo.fastmail.util;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class AccountWrapper implements MailTreeViewable {

	private final String accountName;
	
	/**
	 * 
	 */
	public AccountWrapper(final String accountName) {
		this.accountName = accountName;
	}
	
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fastmail.util.MailTreeViewable#getName()
	 */
	@Override
	public String getName() {
		return accountName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof AccountWrapper))
			return false;
		
		AccountWrapper other = (AccountWrapper) obj;
		return this.getName().equals(other.getName());
	}
	

}
