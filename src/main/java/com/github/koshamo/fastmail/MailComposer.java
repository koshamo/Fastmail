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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Mail Composer is a class to compose a new Email using a new window.
 * <p>
 * This class is intended to be called from the main application either only
 * with account information to create a new email or via one of the specialized
 * constructors to reply to a existing email. For those constructors additional
 * information is needed to fill the mail header.
 * <p>
 * Use this class in the main application to compose a mail by calling one of the 
 * constructors. No more action is needed, as this class calls the sendMail() 
 * method from Account and closes itself.
 * <p> 
 * @author jochen
 *
 */
public class MailComposer {

	private MailAccountList accounts;
	private Stage stage;
	private Label status;
	private ChoiceBox<String> fromBox;
	private TextField toAddress;
	private TextField ccAddress;
	private TextField subjectText;
	private TextArea area;
	
	/**
	 * Basic constructor to compose a new mail. 
	 * <p> 
	 * Only the information about the mail accounts is needed
	 * 
	 * @param accounts the list that holds the mail accounts
	 */
	public MailComposer(MailAccountList accounts) {
		this.accounts = accounts;
		buildGui();
	}
	
	/**
	 * This constructor is used to reply to a message
	 * 
	 * @param accounts 	the list that holds the mail accounts
	 * @param to 		all email addresses in the TO field, semicolon separated
	 * @param subject	the subject of the existing email, which will be prepended
	 * with Re: if not already present
	 * @param text		the email's text, which will be prepended with > line by
	 * line to visualize the citing
	 */
	public MailComposer(MailAccountList accounts, String to, String subject, String text) {
		this.accounts = accounts;
		buildGui();
		toAddress.setText(to);
		subjectText.setText(MailTools.makeSubject(subject));
		area.setText(MailTools.decorateMailText(text));
	}

	public MailComposer(MailAccountList accounts, String to, String cc, String subject, String text) {
		this.accounts = accounts;
		buildGui();
		toAddress.setText(to);
		ccAddress.setText(cc);
		subjectText.setText(MailTools.makeSubject(subject));
		area.setText(MailTools.decorateMailText(text));
	}

	/**
	 * main method to construct the GUI
	 */
	private void buildGui() {
		stage = new Stage();
		VBox overallPane = new VBox();
		
		buildButtonBar(overallPane);
		buildTop(overallPane);
		buildBottom(overallPane);
		status = new Label();
		overallPane.getChildren().add(status);
		
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
	private void buildButtonBar(VBox pane) {
		HBox hbox = new HBox();
		Button btnSend = new Button("send");
		btnSend.setPrefSize(90, 50);
		btnSend.setOnAction(ev -> {
			// check for valid entries in the user fields		
			if (fromBox.getSelectionModel().getSelectedItem() == null) {
				status.setText("Select Email Account!"); 
				return;
			}
			if (toAddress.getText().isEmpty()) {
				status.setText("Add Email Address");
				return;
			}
			if (!MailTools.isValid(toAddress.getText())) {
				status.setText("Enter valid Email Address in TO field");
				return;
			}
			if (!ccAddress.getText().isEmpty() && !MailTools.isValid(ccAddress.getText())) {
				status.setText("Enter valid Email Address in CC field");
				return;
			}
			if (subjectText.getText().isEmpty()) {
				status.setText("Enter Subject");
				return;
			}
			// get the values an send the mail
			accounts.getAccount(fromBox.getSelectionModel().getSelectedItem().toString())
				.sendMail(toAddress.getText(), ccAddress.getText(), 
						subjectText.getText(), area.getText());
			stage.close();
		});
		
		hbox.getChildren().add(btnSend);
		pane.getChildren().add(hbox);
	}
	
	/**
	 * this method constructs the Mail Composer window's head.
	 * 
	 * @param pane the VBox layout container
	 */
	private void buildTop(VBox pane) {
		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setPadding(new Insets(5, 20, 5, 20));
		ColumnConstraints colLabel = new ColumnConstraints(80);
		colLabel.setHgrow(Priority.NEVER);
		colLabel.setHalignment(HPos.RIGHT);
		ColumnConstraints colField = new ColumnConstraints();
		colField.setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().addAll(colLabel, colField);
		
		Label from = new Label("from :");
		grid.add(from, 0, 0);
		fromBox = new ChoiceBox<String>(fillList());
		fromBox.setMaxWidth(Double.MAX_VALUE);
		fromBox.setValue(accounts.getCurrentAccount());
		grid.add(fromBox, 1, 0);
		Label to = new Label("to :");
		grid.add(to, 0, 1);
		toAddress = new TextField();
		grid.add(toAddress, 1, 1);
		Label cc = new Label("cc :");
		grid.add(cc, 0, 2);
		ccAddress = new TextField();
		grid.add(ccAddress, 1, 2);
		Label subject = new Label("subject :");
		grid.add(subject, 0, 3);
		subjectText = new TextField();
		grid.add(subjectText, 1, 3);
		stage.setTitle(subjectText.getText());
		stage.titleProperty().bindBidirectional(subjectText.textProperty());		
		pane.getChildren().add(grid);
	}

	/**
	 * this is a helper method to fill the items for the Choice Box.
	 * <p>
	 * this method parses all local mail accounts and populates the the from choice
	 * box with account representation strings
	 * 
	 * @return the account list for the choice box
	 */
	private ObservableList<String> fillList() {
		final ObservableList<String> list = FXCollections.observableArrayList();
		accounts
			.getList()
			.stream()
			.forEach(ma -> list.add(ma.getAccountName()));
		return list;
	}
	
	/**
	 * this method constructs the body part of the window, which is the text area 
	 * to compose the mail
	 * 
	 * @param pane the VBox layout container
	 */
	private void buildBottom(VBox pane) {
		area = new TextArea();
		VBox.setVgrow(area, Priority.ALWAYS);
		pane.getChildren().add(area);
	}
	
}
