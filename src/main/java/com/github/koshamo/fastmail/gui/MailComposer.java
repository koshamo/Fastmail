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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.github.koshamo.fastmail.mail.MailAccount;
import com.github.koshamo.fastmail.mail.MailData;
import com.github.koshamo.fastmail.util.MailTools;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Mail Composer is a class to compose a new Email using a new window.
 * <p>
 * This class is intended to be called from the main application either only
 * with account information to create a new email or via one of the specialized
 * constructors to reply to a existing email. For those constructors additional
 * information is needed to fill the mail header.
 * <p>
 * Use this class in the main application to compose a mail by calling one of 
 * the constructors. No more action is needed, as this class calls the 
 * sendMail() method from MailAccount and closes itself.
 * <p> 
 * @author jochen
 *
 */
public class MailComposer {

	private MailAccount[] accounts;
	private Stage stage;
	private Label status;
	private TextField attachmentLine;
	private List<File> attachmentList;
	private ChoiceBox<String> fromBox;
	private TextField toAddress;
	private TextField ccAddress;
	private TextField subjectText;
	private TextArea area;
	private MailData mail;
	private int curAccnt;
	
	private ResourceBundle i18n;
	
	/**
	 * Basic constructor to compose a new mail. 
	 * <p> 
	 * Only the information about the mail accounts is needed
	 * 
	 * @param accounts the array that holds the mail accounts
	 */
	public MailComposer(final MailAccount[] accounts) {
		this.accounts = accounts;
		buildGui();
	}
	
	/**
	 * This constructor is used to reply to a message
	 * @param accounts	the array that holds the mail accounts
	 * @param curAccnt	the index of the active account in the array
	 * @param mail		the MailData object to reply to
	 * @param replyAll	set this to true, if the message is reply to all
	 */
	public MailComposer(final MailAccount[] accounts, final int curAccnt, 
			final MailData mail, final boolean replyAll) {
		this.accounts = accounts;
		this.mail = mail;
		this.curAccnt = curAccnt;
		buildGui();
		toAddress.setText(mail.getFrom());
		if (replyAll)
			ccAddress.setText(mail.getCcAsLine());
		subjectText.setText(MailTools.makeSubject(mail.getSubject()));
		area.setText(MailTools.decorateMailText(mail.getContent()));
	}


	/**
	 * main method to construct the GUI
	 */
	private void buildGui() {
		i18n = SerializeManager.getLocaleMessages();
		stage = new Stage();
		VBox overallPane = new VBox();
		
		buildButtonBar(overallPane);
		buildTop(overallPane);
		buildBottom(overallPane);
		status = new Label();
		overallPane.getChildren().add(status);
		
		attachmentList = new ArrayList<File>();
		Scene scene = new Scene(overallPane, 600, 500);
		stage.setScene(scene);
		stage.show();
	}
	
	/**
	 * this method constructs the button bar at the top of the Mail Composer
	 * window.
	 * <p>
	 * all button action is done here, which does the most important validation
	 * checks
	 * 
	 * @param pane the VBox layout container
	 */
	private void buildButtonBar(final VBox pane) {
		HBox hbox = new HBox();
		Button btnSend = new Button(i18n.getString("action.send")); //$NON-NLS-1$
		btnSend.setPrefSize(90, 50);
		btnSend.setOnAction(ev -> {
			// check for valid entries in the user fields		
			if (fromBox.getSelectionModel().getSelectedItem() == null) {
				status.setText(i18n.getString("alert.selectemail"));  //$NON-NLS-1$
				return;
			}
			if (toAddress.getText().isEmpty()) {
				status.setText(i18n.getString("alert.addemail")); //$NON-NLS-1$
				return;
			}
			if (!MailTools.isValid(toAddress.getText())) {
				status.setText(i18n.getString("alert.to.addmail")); //$NON-NLS-1$
				return;
			}
			if (!ccAddress.getText().isEmpty() && !MailTools.isValid(ccAddress.getText())) {
				status.setText(i18n.getString("alert.cc.addmail")); //$NON-NLS-1$
				return;
			}
			if (subjectText.getText().isEmpty()) {
				status.setText(i18n.getString("alert.subject")); //$NON-NLS-1$
				return;
			}
			// get the values an send the mail
			if (mail == null)
				accounts[fromBox.getSelectionModel().getSelectedIndex()].
						sendMail(toAddress.getText(), ccAddress.getText(), 
						subjectText.getText(), area.getText(), attachmentList, 
						null);
			else
				accounts[fromBox.getSelectionModel().getSelectedIndex()].
						sendMail(toAddress.getText(), ccAddress.getText(), 
						subjectText.getText(), area.getText(), attachmentList, 
						mail.getMessage());
				
			stage.close();
		});

		Button btnAttach = new Button(i18n.getString("action.attachfile")); //$NON-NLS-1$
		btnAttach.setPrefSize(90, 50);
		btnAttach.setOnAction(ev -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(i18n.getString("dialog.title.attachfile")); //$NON-NLS-1$
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); //$NON-NLS-1$
			File selFile = fileChooser.showOpenDialog(stage.getOwner());
			if (selFile != null) {
				if (attachmentLine.getText().isEmpty())
					attachmentLine.setText(selFile.getName());
				else
					attachmentLine.setText(attachmentLine.getText() + ";" + selFile.getName()); //$NON-NLS-1$
				if (attachmentList == null)
					attachmentList = new ArrayList<File>();
				attachmentList.add(selFile);
			}
		});
		
		hbox.getChildren().addAll(btnSend, btnAttach);
		pane.getChildren().add(hbox);
	}
	
	
	/**
	 * this method constructs the Mail Composer window's head.
	 * 
	 * @param pane the VBox layout container
	 */
	private void buildTop(final VBox pane) {
		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setPadding(new Insets(5, 20, 5, 20));
		ColumnConstraints colLabel = new ColumnConstraints(80);
		colLabel.setHgrow(Priority.NEVER);
		colLabel.setHalignment(HPos.RIGHT);
		ColumnConstraints colField = new ColumnConstraints();
		colField.setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().addAll(colLabel, colField);
		
		Label from = new Label(i18n.getString("entry.from")); //$NON-NLS-1$
		grid.add(from, 0, 0);
		fromBox = new ChoiceBox<String>(fillList());
		fromBox.setMaxWidth(Double.MAX_VALUE);
		fromBox.setValue(accounts[curAccnt].getAccountName());
		grid.add(fromBox, 1, 0);
		Label to = new Label(i18n.getString("entry.to")); //$NON-NLS-1$
		grid.add(to, 0, 1);
		toAddress = new TextField();
		grid.add(toAddress, 1, 1);
		Label cc = new Label(i18n.getString("entry.cc")); //$NON-NLS-1$
		grid.add(cc, 0, 2);
		ccAddress = new TextField();
		grid.add(ccAddress, 1, 2);
		Label subject = new Label(i18n.getString("entry.subject")); //$NON-NLS-1$
		grid.add(subject, 0, 3);
		subjectText = new TextField();
		grid.add(subjectText, 1, 3);
		stage.setTitle(subjectText.getText());
		stage.titleProperty().bindBidirectional(subjectText.textProperty());
		Separator sep = new Separator();
		grid.add(sep, 0, 4, 2, 1);
		Label attachmentLbl = new Label(i18n.getString("entry.attachment")); //$NON-NLS-1$
		grid.add(attachmentLbl, 0, 5);
		attachmentLine = new TextField();
		attachmentLine.setEditable(false);
		grid.add(attachmentLine, 1, 5);

		pane.getChildren().add(grid);
	}


	
	/**
	 * this is a helper method to fill the items for the Choice Box.
	 * <p>
	 * this method parses all local mail accounts and populates the the 
	 * from choice box with account representation strings
	 * 
	 * @return the account list for the choice box
	 */
	private ObservableList<String> fillList() {
		final ObservableList<String> list = FXCollections.observableArrayList();
		for (MailAccount a : accounts)
			list.add(a.getAccountName());
		return list;
	}
	
	/**
	 * this method constructs the body part of the window, which is the 
	 * text area to compose the mail
	 * 
	 * @param pane the VBox layout container
	 */
	private void buildBottom(final VBox pane) {
		area = new TextArea();
		area.setWrapText(true);
		VBox.setVgrow(area, Priority.ALWAYS);
		pane.getChildren().add(area);
	}
	
}
