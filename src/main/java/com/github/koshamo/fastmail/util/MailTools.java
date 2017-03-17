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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * MailTools is a helper class that holds some static methods which are useful
 * throughout the application and do some basic stuff
 * 
 * @author jochen
 *
 */
public class MailTools {

	/**
	 * checks if a semicolon separated String of email addresses holds
	 * possibly valid addresses.
	 * <p>
	 * This method only checks the structure of a mail address of type
	 * <code>name@domain.toplevel</code>
	 * <p>
	 * no existence checks will be done!
	 * 
	 * @param addressLine a string with (semicolon separated) email addresses
	 * @return	true if valid, false otherwise
	 */
	public static boolean isValid(String addressLine) {
		String[] addresses = addressLine.split(";"); //$NON-NLS-1$
		for (String adr : addresses){
			int at = adr.indexOf("@"); //$NON-NLS-1$
			int tld = adr.lastIndexOf("."); //$NON-NLS-1$
			if (at < 0 || tld < 0 || tld < at)
				return false;
		}
		return true;
	}
	
	/**
	 * this method parses a given String that holds several email addresses 
	 * semicolon separated and transforms them to an array of internet email
	 * addresses.
	 * 
	 * @param addressLine a semicolon separated list of email addresses as string
	 * @return an array of internet email Addresses
	 */
	public static InternetAddress[] parseAddresses(String addressLine) {
		String[] adr = addressLine.split(";"); //$NON-NLS-1$
		InternetAddress[] inetAdr = new InternetAddress[adr.length];
		for (int i = 0; i < adr.length; i++) {
			try {
				inetAdr[i] = new InternetAddress(adr[i]);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return inetAdr;
		
	}
	
	/**
	 * prepends a subject line with Re: if not yet present
	 * 
	 * @param subject the current subject line
	 * @return the subject line starting with Re: 
	 */
	public static String makeSubject(String subject) {
		if (subject.startsWith("Re:") || subject.startsWith("RE:")) //$NON-NLS-1$ //$NON-NLS-2$
			return subject;
		return new StringBuilder("Re: ").append(subject).toString();  //$NON-NLS-1$
	}
	
	/**
	 * Decorates reply messages with the widely used > to identify cited text
	 * 
	 * @param text the email text, that will be replied
	 * @return the decorated email text
	 */
	public static String decorateMailText(String text) {
		String[] lines = text.split("\n"); //$NON-NLS-1$
		StringBuilder reply = new StringBuilder();
		for (String line : lines)
			reply.append("> ").append(line).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return reply.toString();
	}
	
	public static boolean isServerValid(String addressLine) {
		if (addressLine.contains("@")) //$NON-NLS-1$
			return false;
		String[] str = addressLine.split("\\."); //$NON-NLS-1$
		if (str.length < 3)
			return false;
		return true;
	}
}
