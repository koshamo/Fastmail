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

package com.github.koshamo.fastmail.gui;

import java.util.Optional;
import java.util.ResourceBundle;

import com.github.koshamo.fastmail.mail.MailAccount;
import com.github.koshamo.fastmail.mail.MailAccountData;
import com.github.koshamo.fastmail.util.MailTools;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

/**
 * This class creates and shows a dialog to add or modify a MailAccount 
 * for the application configuration.
 * <p>
 * This class is a wrapper to the JavaFX Dialog to hide the implementation
 * in the main GUI to keep that one clean. 
 * <p>
 * This class provides a showAndWait()-Method as known from Dialogs.
 *  
 * @author jochen
 *
 */
public class MailAccountDialog {

	/**
	 * The constructor to build the dialog. The default constructor is 
	 * used to add a new account, which means that to this point no account 
	 * data is available
	 */
	public MailAccountDialog() {
		i18n = SerializeManager.getLocaleMessages();
		accountData = new MailAccountData();
		
		addComponents(TYPE.ADD);
		
		dialog.setResultConverter(buttonType -> {
			ButtonData buttonData = buttonType.getButtonData();
			if (buttonData == ButtonData.OK_DONE) 
				if (fillAccountData(false))
					return accountData;
			return null;
		});
	}
	
	/**
	 * This constructor builds the dialog and fills it with available
	 * account data. Thus this constructor is best used to modify an 
	 * existing mail account.
	 * 
	 * @param accountData the data object of an existing mail account
	 * @throws NullPointerException	if the MailAccountData object given
	 * as parameter is null, this constructor throws a NullPointerException
	 */
	public MailAccountDialog(final MailAccountData accountData) {
		i18n = SerializeManager.getLocaleMessages();

		if (accountData == null)
			throw new NullPointerException(i18n.getString("error.mailaccountdialog")); //$NON-NLS-1$
		this.accountData = accountData;
		addComponents(TYPE.CHANGE);
		
		dialog.setResultConverter(buttonType -> {
			ButtonData buttonData = buttonType.getButtonData();
			if (buttonData == ButtonData.OK_DONE) 
				if (fillAccountData(false))
					return accountData;
			return null;
		});
	}
	
	/**
	 * This methods shows the dialog and waits for it to return. 
	 * Thus it is modal.
	 * 
	 * @return a new or modified MailAccountData object or null, if
	 * Button.CANCEL has been hit 
	 */
	public MailAccountData showAndWait() {
		Optional<MailAccountData> opt = dialog.showAndWait();
		if (opt.isPresent())
			return opt.get();
		return null;
	}
	/*
	 * ******************************************************
	 * 					PRIVATE
	 * ******************************************************
	 */
	private enum TYPE {ADD, CHANGE}
	private Dialog<MailAccountData> dialog;
	private GridPane grid;
	private TextField usernameField;
	private TextField displaynameField;
	private PasswordField passwordField;
	private ChoiceBox<String> serverTypeBox;
	private TextField imapField;
	private CheckBox sslBox;
	private TextField smtpField;
	private CheckBox tlsBox;
	private Button testButton;
	private Label statusLabel;
	private MailAccountData accountData = null;
	
	private ResourceBundle i18n;
	
	/**
	 * create the actual GUI
	 */
	private void addComponents(final TYPE type) {
		dialog = new Dialog<MailAccountData>();
		ButtonType add = new ButtonType(i18n.getString("action.addaccount"), ButtonData.OK_DONE); //$NON-NLS-1$
		ButtonType cancel = new ButtonType(i18n.getString("action.cancel"), ButtonData.CANCEL_CLOSE); //$NON-NLS-1$
		
		if (type == TYPE.ADD) {
			dialog.setTitle(i18n.getString("dialog.title.addnewaccount")); //$NON-NLS-1$
			dialog.setHeaderText(i18n.getString("dialog.header.addnewaccount")); //$NON-NLS-1$
		}
		else {	// TYPE.CHANGE
			dialog.setTitle(i18n.getString("dialog.title.changeaccount")); //$NON-NLS-1$
			dialog.setHeaderText(i18n.getString("dialog.header.changeaccount")); //$NON-NLS-1$
		}

		grid = new GridPane();
		grid.setHgap(30);
		grid.setVgap(15);
		// USERNAME
		Label usernameLabel = new Label(i18n.getString("entry.username")); //$NON-NLS-1$
		usernameLabel.setTooltip(new Tooltip(i18n.getString("tooltip.username"))); //$NON-NLS-1$
		grid.add(usernameLabel, 0, 0);
		usernameField = new TextField();
		usernameField.setTooltip(new Tooltip(i18n.getString("tooltip.username"))); //$NON-NLS-1$
		grid.add(usernameField, 1, 0);
		// DISPLAYED NAME
		Label displaynameLabel = new Label(i18n.getString("entry.displayname")); //$NON-NLS-1$
		displaynameLabel.setTooltip(new Tooltip(i18n.getString("tooltip.displayname"))); //$NON-NLS-1$
		grid.add(displaynameLabel, 0, 1);
		displaynameField = new TextField();
		displaynameField.setTooltip(new Tooltip(i18n.getString("tooltip.displayname"))); //$NON-NLS-1$
		grid.add(displaynameField, 1, 1);
		// PASSWORD
		Label passwordLabel = new Label(i18n.getString("entry.password")); //$NON-NLS-1$
		grid.add(passwordLabel, 0, 2);
		passwordField = new PasswordField();
		grid.add(passwordField, 1, 2);
		// SERVER TYPE
		Label serverTypeLabel = new Label(i18n.getString("entry.servertype")); //$NON-NLS-1$
		grid.add(serverTypeLabel, 0, 3);
		serverTypeBox = new ChoiceBox<String>(FXCollections.observableArrayList(i18n.getString("entry.IMAP"))); //$NON-NLS-1$
		serverTypeBox.setTooltip(new Tooltip(i18n.getString("info.IMAP"))); //$NON-NLS-1$
		serverTypeBox.setValue(i18n.getString("entry.IMAP")); //$NON-NLS-1$
		grid.add(serverTypeBox, 1, 3);
		// SERVER ADDRESS
		Label imapLabel = new Label(i18n.getString("entry.addresstype")); //$NON-NLS-1$
		imapLabel.setTooltip(new Tooltip(i18n.getString("tooltip.addresstype"))); //$NON-NLS-1$
		grid.add(imapLabel, 0, 4);
		imapField = new TextField();
		imapField.setTooltip(new Tooltip(i18n.getString("tooltip.addresstype"))); //$NON-NLS-1$
		grid.add(imapField, 1, 4);
		// SSL CONNECTION
		Label sslLabel = new Label(i18n.getString("entry.SSL")); //$NON-NLS-1$
		grid.add(sslLabel, 0, 5);
		sslBox = new CheckBox(i18n.getString("entry.SSLenable")); //$NON-NLS-1$
		sslBox.setTooltip(new Tooltip(i18n.getString("tooltip.SSL"))); //$NON-NLS-1$
		sslBox.setSelected(true);		
		grid.add(sslBox, 1, 5);
		// SMTP ADDRESS
		Label smtpLabel = new Label(i18n.getString("entry.SMTP")); //$NON-NLS-1$
		smtpLabel.setTooltip(new Tooltip(i18n.getString("tooltip.SMTP"))); //$NON-NLS-1$
		grid.add(smtpLabel, 0, 6);
		smtpField = new TextField();
		smtpField.setTooltip(new Tooltip(i18n.getString("tooltip.SMTP"))); //$NON-NLS-1$
		grid.add(smtpField, 1, 6);
		// TLS CONNECTION
		Label tlsLabel = new Label(i18n.getString("entry.TLS")); //$NON-NLS-1$
		grid.add(tlsLabel, 0, 7);
		tlsBox = new CheckBox(i18n.getString("entry.TLSenable")); //$NON-NLS-1$
		tlsBox.setTooltip(new Tooltip(i18n.getString("tooltip.TLS"))); //$NON-NLS-1$
		tlsBox.setSelected(true);
		grid.add(tlsBox, 1, 7);
		// TEST CONFIGURATION
		testButton = new Button(i18n.getString("action.testconfiguration")); //$NON-NLS-1$
		testButton.setOnAction(ev -> {
			fillAccountData(true);
		});
		testButton.setTooltip(new Tooltip(i18n.getString("tooltip.testconfiguration"))); //$NON-NLS-1$
		grid.add(testButton, 1, 8);
		statusLabel = new Label();
		grid.add(statusLabel, 0, 9, 2, 1);
		
		if (type == TYPE.CHANGE) {
			usernameField.setText(accountData.getUsername());
			displaynameField.setText(accountData.getDisplayName());	
			passwordField.setText(accountData.getPassword());	
			serverTypeBox.setValue(accountData.getInboxType());
			imapField.setText(accountData.getInboxHost());
			sslBox.setSelected(accountData.isSsl());
			smtpField.setText(accountData.getSmtpHost());
			tlsBox.setSelected(accountData.isTls());
		}
		
		DialogPane pane = dialog.getDialogPane();
		pane.setContent(grid);
		pane.getButtonTypes().addAll(cancel, add);
	}
	
	/**
	 * Helper method that validates the fields.
	 * <p>
	 * If test is true, it checks if the account is valid and can be added. <br>
	 * If test is false, it fills the internal MailAccountData object that
	 * can be returned by this class.
	 * 
	 * @param test true for account testing, false for production
	 * @return true, if all fields are valid, false otherwise
	 */
	private boolean fillAccountData(final boolean test) {
		if (usernameField.getText().isEmpty()) {
			statusLabel.setText(i18n.getString("info.username")); //$NON-NLS-1$
			return false;
		}
		if (!MailTools.isValid(usernameField.getText())) {
			statusLabel.setText(i18n.getString("alert.username")); //$NON-NLS-1$
			return false;
		}
		if (passwordField.getText().isEmpty()) {
			statusLabel.setText(i18n.getString("info.password")); //$NON-NLS-1$
			return false;
		}
		if (imapField.getText().isEmpty()) {
			statusLabel.setText(i18n.getString("info.IMAP")); //$NON-NLS-1$
			return false;
		}
		if (!MailTools.isServerValid(imapField.getText())) {
			statusLabel.setText(i18n.getString("alert.IMAP")); //$NON-NLS-1$
			return false;
		}
		if (smtpField.getText().isEmpty()) {
			statusLabel.setText(i18n.getString("info.SMTP")); //$NON-NLS-1$
			return false;
		}
		if (!MailTools.isServerValid(smtpField.getText())) {
			statusLabel.setText(i18n.getString("alert.SMTP")); //$NON-NLS-1$
			return false;
		}
		accountData.setUsername(usernameField.getText());
		accountData.setDisplayName(displaynameField.getText());
		accountData.setPassword(passwordField.getText());
		accountData.setInboxType(serverTypeBox.getSelectionModel().getSelectedItem());
		accountData.setInboxHost(imapField.getText());
		accountData.setSsl(sslBox.isSelected());
		accountData.setSmtpHost(smtpField.getText());
		accountData.setTls(tlsBox.isSelected());

		if (test) {
			statusLabel.setText(MailAccount.testConnection(accountData));
			return false;
		}
		return true;
	}
}
