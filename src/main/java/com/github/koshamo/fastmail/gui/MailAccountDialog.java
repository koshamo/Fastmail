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

import com.github.koshamo.fastmail.mail.MailAccount;
import com.github.koshamo.fastmail.mail.MailAccountData;
import com.github.koshamo.fastmail.util.MailTools;

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
		if (accountData == null)
			throw new NullPointerException(
					"You need to provide actual MailAccountData, if you use this constructor");
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
	private enum TYPE {ADD, CHANGE};
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
	
	/**
	 * create the actual GUI
	 */
	private void addComponents(final TYPE type) {
		dialog = new Dialog<MailAccountData>();
		ButtonType add = new ButtonType("Add Account", ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		
		if (type == TYPE.ADD) {
			dialog.setTitle("Add a new Mail Account");
			dialog.setHeaderText(
					"To add a new Mail Account, some basic information is needed.");
		}
		else {	// TYPE.CHANGE
			dialog.setTitle("Change a Mail Account");
			dialog.setHeaderText("Change the settings of your mail account.");
		}

		grid = new GridPane();
		grid.setHgap(30);
		grid.setVgap(15);
		// USERNAME
		Label usernameLabel = new Label("Username: ");
		usernameLabel.setTooltip(new Tooltip("add a full qualified email address, e.g. jochen@com.github.koshamo.fastmail.org"));
		grid.add(usernameLabel, 0, 0);
		usernameField = new TextField();
		usernameField.setTooltip(new Tooltip("add a full qualified email address, e.g. jochen@com.github.koshamo.fastmail.org"));
		grid.add(usernameField, 1, 0);
		// DISPLAYED NAME
		Label displaynameLabel = new Label("Displayed name: ");
		displaynameLabel.setTooltip(new Tooltip("this name is shown in most email clients"));
		grid.add(displaynameLabel, 0, 1);
		displaynameField = new TextField();
		displaynameField.setTooltip(new Tooltip("this name is shown in most email clients"));
		grid.add(displaynameField, 1, 1);
		// PASSWORD
		Label passwordLabel = new Label("Password: ");
		grid.add(passwordLabel, 0, 2);
		passwordField = new PasswordField();
		grid.add(passwordField, 1, 2);
		// SERVER TYPE
		Label serverTypeLabel = new Label("Server Type: ");
		grid.add(serverTypeLabel, 0, 3);
		serverTypeBox = new ChoiceBox<String>(FXCollections.observableArrayList("IMAP"));
		serverTypeBox.setTooltip(new Tooltip("currently only IMAP is supported"));
		serverTypeBox.setValue("IMAP");
		grid.add(serverTypeBox, 1, 3);
		// SERVER ADDRESS
		Label imapLabel = new Label("IMAP / POP3 server address: ");
		imapLabel.setTooltip(new Tooltip("e.g. imap.gmail.com"));
		grid.add(imapLabel, 0, 4);
		imapField = new TextField();
		imapField.setTooltip(new Tooltip("e.g. imap.gmail.com"));
		grid.add(imapField, 1, 4);
		// SSL CONNECTION
		Label sslLabel = new Label("Secure SSL Connection: ");
		grid.add(sslLabel, 0, 5);
		sslBox = new CheckBox("SSL enable");
		sslBox.setTooltip(new Tooltip("if unsure, leave checked"));
		sslBox.setSelected(true);		
		grid.add(sslBox, 1, 5);
		// SMTP ADDRESS
		Label smtpLabel = new Label("SMTP server address: ");
		smtpLabel.setTooltip(new Tooltip("e.g. smtp.gmail.com"));
		grid.add(smtpLabel, 0, 6);
		smtpField = new TextField();
		smtpField.setTooltip(new Tooltip("e.g. smtp.gmail.com"));
		grid.add(smtpField, 1, 6);
		// TLS CONNECTION
		Label tlsLabel = new Label("Secure TLS Connection: ");
		grid.add(tlsLabel, 0, 7);
		tlsBox = new CheckBox("TLS enable");
		tlsBox.setTooltip(new Tooltip("if unsure, leave checked"));
		tlsBox.setSelected(true);
		grid.add(tlsBox, 1, 7);
		// TEST CONFIGURATION
		testButton = new Button("test configuration");
		testButton.setOnAction(ev -> {
			fillAccountData(true);
		});
		testButton.setTooltip(new Tooltip("test the server settings before you add the mail account"));
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
			statusLabel.setText("Please enter username");
			return false;
		}
		if (!MailTools.isValid(usernameField.getText())) {
			statusLabel.setText("Username is not valid");
			return false;
		}
		if (passwordField.getText().isEmpty()) {
			statusLabel.setText("Please enter password");
			return false;
		}
		if (imapField.getText().isEmpty()) {
			statusLabel.setText("Please enter IMAP address");
			return false;
		}
		if (!MailTools.isServerValid(imapField.getText())) {
			statusLabel.setText("IMAP address doesn't seem to be valid");
			return false;
		}
		if (smtpField.getText().isEmpty()) {
			statusLabel.setText("Please enter SMTP address");
			return false;
		}
		if (!MailTools.isServerValid(smtpField.getText())) {
			statusLabel.setText("SMTP address doesn't seem to be valid");
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
		else
			return true;
	}
}
