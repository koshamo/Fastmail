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
 * This class creates and shows a dialog to add a new MailAccount to
 * the application configuration.
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
	 * The constructor to build the dialog.
	 */
	public MailAccountDialog() {
		
		addComponents(TYPE.ADD);
		
		dialog.setResultConverter(buttonType -> {
			ButtonData data = buttonType.getButtonData();
			if (data == ButtonData.OK_DONE)
				return createAccount(false);
			return account;
		});
	}
	
	public MailAccountDialog(MailAccount account) {
		this.account = account;
		addComponents(TYPE.CHANGE);
		
		dialog.setResultConverter(buttonType -> {
			ButtonData buttonData = buttonType.getButtonData();
			if (buttonData == ButtonData.OK_DONE) {
				MailAccountData data = account.getMailAccountData();
				data.setUsername(usernameField.getText());
				data.setDisplayName(displaynameField.getText());
				data.setPassword(passwordField.getText());
				data.setInboxType(serverTypeBox.getSelectionModel().getSelectedItem());
				data.setInboxHost(imapField.getText());
				data.setSsl(sslBox.isSelected());
				data.setSmtpHost(smtpField.getText());
				data.setTls(tlsBox.isSelected());
				account.setMailAccountData(data);
			}
			return account;
		});
	}
	
	/**
	 * This methods shows the dialog and waits for it to return. Thus it is modal.
	 * 
	 * @return a new MailAccount or null
	 */
	public MailAccount showAndWait() {
		Optional<MailAccount> opt = dialog.showAndWait();
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
	private Dialog<MailAccount> dialog;
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
	private MailAccount account = null;
	
	/**
	 * create the actual GUI
	 */
	private void addComponents(TYPE type) {
		dialog = new Dialog<MailAccount>();
		ButtonType add = new ButtonType("Add Account", ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		
		if (type == TYPE.ADD) {
			dialog.setTitle("Add a new Mail Account");
			dialog.setHeaderText(
					"To add a new Mail Account, some basic information is needed.");
		}
		else {	// TYPE.CHANGE
			dialog.setTitle("Change a Mail Account");
			dialog.setHeaderText("Change the settings of your mail accoutn.");
		}

		grid = new GridPane();
		grid.setHgap(30);
		grid.setVgap(15);
		Label usernameLabel = new Label("Username: ");
		usernameLabel.setTooltip(new Tooltip("add a full qualified email address, e.g. jochen@com.github.koshamo.fastmail.org"));
		grid.add(usernameLabel, 0, 0);
		usernameField = new TextField();
		usernameField.setTooltip(new Tooltip("add a full qualified email address, e.g. jochen@com.github.koshamo.fastmail.org"));
		grid.add(usernameField, 1, 0);
		Label displaynameLabel = new Label("Displayed name: ");
		displaynameLabel.setTooltip(new Tooltip("this name is shown in most email clients"));
		grid.add(displaynameLabel, 0, 1);
		displaynameField = new TextField();
		displaynameField.setTooltip(new Tooltip("this name is shown in most email clients"));
		grid.add(displaynameField, 1, 1);
		Label passwordLabel = new Label("Password: ");
		grid.add(passwordLabel, 0, 2);
		passwordField = new PasswordField();
		grid.add(passwordField, 1, 2);
		Label serverTypeLabel = new Label("Server Type: ");
		grid.add(serverTypeLabel, 0, 3);
		serverTypeBox = new ChoiceBox<String>(FXCollections.observableArrayList("IMAP"));
		serverTypeBox.setTooltip(new Tooltip("currently only IMAP is supported"));
		serverTypeBox.setValue("IMAP");
		grid.add(serverTypeBox, 1, 3);
		Label imapLabel = new Label("IMAP / POP3 server address: ");
		imapLabel.setTooltip(new Tooltip("e.g. imap.gmail.com"));
		grid.add(imapLabel, 0, 4);
		imapField = new TextField();
		imapField.setTooltip(new Tooltip("e.g. imap.gmail.com"));
		grid.add(imapField, 1, 4);
		Label sslLabel = new Label("Secure SSL Connection: ");
		grid.add(sslLabel, 0, 5);
		sslBox = new CheckBox("SSL enable");
		sslBox.setTooltip(new Tooltip("if unsure, leave checked"));
		sslBox.setSelected(true);		
		grid.add(sslBox, 1, 5);
		Label smtpLabel = new Label("SMTP server address: ");
		smtpLabel.setTooltip(new Tooltip("e.g. smtp.gmail.com"));
		grid.add(smtpLabel, 0, 6);
		smtpField = new TextField();
		smtpField.setTooltip(new Tooltip("e.g. smtp.gmail.com"));
		grid.add(smtpField, 1, 6);
		Label tlsLabel = new Label("Secure TLS Connection: ");
		grid.add(tlsLabel, 0, 7);
		tlsBox = new CheckBox("TLS enable");
		tlsBox.setTooltip(new Tooltip("if unsure, leave checked"));
		tlsBox.setSelected(true);
		grid.add(tlsBox, 1, 7);
		testButton = new Button("test configuration");
		testButton.setOnAction(ev -> {
			createAccount(true);
		});
		testButton.setTooltip(new Tooltip("test the server settings before you add the mail account"));
		grid.add(testButton, 1, 8);
		statusLabel = new Label();
		grid.add(statusLabel, 0, 9, 2, 1);
		
		if (type == TYPE.CHANGE) {
			MailAccountData data = account.getMailAccountData();
			usernameField.setText(data.getUsername());
			displaynameField.setText(data.getDisplayName());	
			passwordField.setText(data.getPassword());	
			serverTypeBox.setValue(data.getInboxType());
			imapField.setText(data.getInboxHost());
			sslBox.setSelected(data.isSsl());
			smtpField.setText(data.getSmtpHost());
			tlsBox.setSelected(data.isTls());
		}
		
		DialogPane pane = dialog.getDialogPane();
		pane.setContent(grid);
		pane.getButtonTypes().addAll(cancel, add);
	}
	
	/**
	 * Helper method that validates the fields.
	 * <p>
	 * If test is true, it checks if the account is valid and can be added. <br>
	 * If test is false, it creates a new MailAccount and returns it.
	 * 
	 * @param test true for account testing, false for production
	 * @return a new MailAccount or null
	 */
	private MailAccount createAccount(boolean test) {
		MailAccount account = null;
		if (usernameField.getText().isEmpty()) {
			statusLabel.setText("Please enter username");
			return null;
		}
		if (!MailTools.isValid(usernameField.getText())) {
			statusLabel.setText("Username is not valid");
			return null;
		}
		if (passwordField.getText().isEmpty()) {
			statusLabel.setText("Please enter password");
			return null;
		}
		if (imapField.getText().isEmpty()) {
			statusLabel.setText("Please enter IMAP address");
			return null;
		}
		if (!MailTools.isServerValid(imapField.getText())) {
			statusLabel.setText("IMAP address doesn't seem to be valid");
			return null;
		}
		if (smtpField.getText().isEmpty()) {
			statusLabel.setText("Please enter SMTP address");
			return null;
		}
		if (!MailTools.isServerValid(smtpField.getText())) {
			statusLabel.setText("SMTP address doesn't seem to be valid");
			return null;
		}
		if (test)
			statusLabel.setText(MailAccount.testConnection(
					usernameField.getText(), passwordField.getText(), null, 
					serverTypeBox.getSelectionModel().getSelectedItem(), 
					imapField.getText(), smtpField.getText(), 
					sslBox.isSelected(), tlsBox.isSelected()));
		else
			account = new MailAccount(usernameField.getText(), passwordField.getText(), 
					displaynameField.getText(), 
					serverTypeBox.getSelectionModel().getSelectedItem(), 
					imapField.getText(), smtpField.getText(), 
					sslBox.isSelected(), tlsBox.isSelected());
		return account;
	}
}
