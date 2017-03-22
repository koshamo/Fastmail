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

package com.github.koshamo.fastmail.mail;

import java.io.Serializable;

/**
 * This class stores all fields necessary to build a connection to a mail server. 
 * It is used to serialize the account data.
 *  
 * @author jochen
 *
 */
public class MailAccountData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2095082909969332716L;
	private String username;
	private String password;
	private String displayName;
	private String inboxType;
	private String inboxHost;
	private String smtpHost;
	private boolean ssl;
	private boolean tls;
	
	/**
	 * This constructor holds all fields used for a connection to a mail server
	 *  
	 * @param username		the username for this account as fullly qualified 
	 * email address
	 * @param password		the account's password
	 * @param displayName	the display name for most email clients
	 * @param inboxType		type of inbox, currently only IMAP is supported
	 * @param inboxHost		host URL for retrieving (e.g. imap.gmail.com)
	 * @param smtpHost		host URL for sending (e.g. smtp.gmail.com)
	 * @param ssl			use SSL connection
	 * @param tls			use TLS authentification
	 */
	public MailAccountData(final String username, final String password, 
			final String displayName, final String inboxType, 
			final String inboxHost, final String smtpHost,
			final boolean ssl, final boolean tls) {
		this.username = username;
		this.password = password;
		this.displayName = displayName;
		this.inboxType = inboxType;
		this.inboxHost = inboxHost;
		this.smtpHost = smtpHost;
		this.ssl = ssl;
		this.tls = tls;
	}

	/**
	 * The default constructor is needed to deserialize this object
	 */
	public MailAccountData() {
		
	}
	
	/**
	 * Get the username 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username 
	 * @param username the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * Get the password
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password
	 * @param password the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Get the DisplayName
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the DisplayName
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Get the InboxType
	 * @return the inboxType
	 */
	public String getInboxType() {
		return inboxType;
	}

	/** 
	 * Set the InboxType
	 * @param inboxType the inboxType to set
	 */
	public void setInboxType(final String inboxType) {
		this.inboxType = inboxType;
	}

	/**
	 * Get the InboxHost
	 * @return the inboxHost
	 */
	public String getInboxHost() {
		return inboxHost;
	}

	/**
	 * Set the InboxHost
	 * @param inboxHost the inboxHost to set
	 */
	public void setInboxHost(final String inboxHost) {
		this.inboxHost = inboxHost;
	}

	/**
	 * Get the SMTPHost
	 * @return the smtpHost
	 */
	public String getSmtpHost() {
		return smtpHost;
	}

	/**
	 * Set the SMTPHost
	 * @param smtpHost the smtpHost to set
	 */
	public void setSmtpHost(final String smtpHost) {
		this.smtpHost = smtpHost;
	}

	/**
	 * Get SSL
	 * @return the ssl
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * Set SSL
	 * @param ssl the ssl to set
	 */
	public void setSsl(final boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * Get TLS
	 * @return the tls
	 */
	public boolean isTls() {
		return tls;
	}

	/**
	 * Set TLS
	 * @param tls the tls to set
	 */
	public void setTls(final boolean tls) {
		this.tls = tls;
	}
}
