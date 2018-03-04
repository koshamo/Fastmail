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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.github.koshamo.fastmail.mail.MailAccountData;

/**
 * The SerializeManager is responsible to load data at startup and save date at 
 * close down.
 * <p>
 * The SerializeManager is realized as a Singleton, so it will exist only once and 
 * is available throughout the application
 * 
 * @author jochen
 *
 */
public class SerializeManager {

	/**
	 * Get the only one existing instance of the Serialize Manager
	 * 
	 * @return the one and only object of SerializeManager
	 */
	public static synchronized SerializeManager getInstance() {
		if (manager == null)
			manager = new SerializeManager();
		return manager;
	}

	/**
	 * Use this method to serialize the applications data
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean serialize() {
		File file = new File(homeDir + settingsPath + settingsFile);
		if (!file.exists()) {
			new File(homeDir + settingsPath).mkdirs();
			try {
				file.createNewFile();
			} catch (@SuppressWarnings("unused") IOException e) {
				MessageItem mItem = new MessageItem(
						i18nTexts.getString("exception.createfile"),  //$NON-NLS-1$
						0.0, MessageItem.MessageType.EXCEPTION);
				MessageMarket.getInstance().produceMessage(mItem);
			}
		}
		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
		try (ObjectOutputStream oos = 
				new ObjectOutputStream(new CipherOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(file)), cipher))) {
			oos.flush();
			oos.writeObject(mailAccounts);
			oos.flush();
		} catch (@SuppressWarnings("unused") FileNotFoundException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.savefile"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
			return false;
		} catch (@SuppressWarnings("unused") IOException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.savefile"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
			return false;
		} 
		return true;
	}
	
	/**
	 * Use this method to deserialize the applications data
	 * 
	 * @return true if successful, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean deserialize() {
		File file = new File(homeDir + settingsPath + settingsFile);
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
		try (ObjectInputStream ois = 
				new ObjectInputStream(
						new CipherInputStream(
								new BufferedInputStream(
										new FileInputStream(file)), cipher))) {
			mailAccounts = (Vector<MailAccountData>) ois.readObject();
		} catch (@SuppressWarnings("unused") FileNotFoundException e) {
			// should be default case for newly installed programs
			System.out.println(getLocaleMessageBundle().getString("info.newcomer")); //$NON-NLS-1$
			return false;
		} catch (@SuppressWarnings("unused") IOException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.readfile"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
			return false;
		} catch (@SuppressWarnings("unused") ClassNotFoundException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.classnotfound"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
			return false;
		} 
		return true;
	}
	
	/**
	 * Add a mail account to serialization
	 * 
	 * @param data the MailAccountData object belonging to the mail account
	 */
	public void addMailAccount(final MailAccountData data) {
		mailAccounts.add(data);
	}
	
	/**
	 * Remove a mail account from serialization
	 * @param data the MailAccountData object belonging to the mail account
	 */
	public void removeMailAccount(final MailAccountData data) {
		mailAccounts.remove(data);
	}
	
	/**
	 * Get the mail accounts currently registered to serialize
	 * @return get the registered mail accounts
	 */
	public List<MailAccountData> getMailAccounts() {
		return mailAccounts;
	}

	/**
	 * Get the internationalized texts bundle.
	 * The bundle exists only once. So if it is not yet loaded, it will be 
	 * loaded, otherwise the reference to the data object will be returned 
	 * @return	the resource bundle containing the internationalized texts
	 */
	public static ResourceBundle getLocaleMessageBundle() {
		if (i18nTexts == null)
			i18nTexts = ResourceBundle.getBundle("messages"); //$NON-NLS-1$
		return i18nTexts;
	}
	
	
	/*
	 * *****************************************************************
	 * 						PRIVATE
	 * *****************************************************************
	 */
	/**
	 * Only one Instance of SerializeManager will exist in this application
	 */
	private static SerializeManager manager = null;
	
	private static ResourceBundle i18nTexts = null;
	
	private final String settingsPath = "/.FDE/fastmail/";	//$NON-NLS-1$
	private final String settingsFile = "fastmail.fms";	//$NON-NLS-1$
	private String homeDir;
	private Vector<MailAccountData> mailAccounts;
	
	/**
	 * The constructor is private to prevent others to instantiate this class 
	 */
	private SerializeManager() {
		homeDir = System.getProperty("user.home"); //$NON-NLS-1$
		mailAccounts = new Vector<MailAccountData>();
		
	}
	
	/**
	 * Initialize the Cipher for en-/decrypted settings stream
	 * <p>
	 * The encryption currently is only needed to hide account passwords on
	 * disk. The serialization allows anyone to read the password in plain text
	 * in the settings file, if one uses an editor capable of UTF-8 strings.
	 * 
	 * @param mode	Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @return		the Cipher for the stream
	 */
	private static Cipher getCipher(final int mode) {
		SecretKey key64 = new SecretKeySpec(
				new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07}, 
				"Blowfish"); //$NON-NLS-1$
		Cipher cipher = null; 
		try {
			cipher = Cipher.getInstance("Blowfish"); //$NON-NLS-1$
			cipher.init(mode, key64);
		} catch (@SuppressWarnings("unused") NoSuchAlgorithmException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.ciphering"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (@SuppressWarnings("unused") NoSuchPaddingException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.ciphering"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		} catch (@SuppressWarnings("unused") InvalidKeyException e) {
			MessageItem mItem = new MessageItem(
					i18nTexts.getString("exception.ciphering"),  //$NON-NLS-1$
					0.0, MessageItem.MessageType.EXCEPTION);
			MessageMarket.getInstance().produceMessage(mItem);
		}
		return cipher;
	}
	

}
