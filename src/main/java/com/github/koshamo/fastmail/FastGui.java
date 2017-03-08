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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * class FastGui is the main class for the GUI
 * 
 * @author jochen
 *
 */
public class FastGui extends Application {

	/* start is the starting point of a JafaFX application
	 * all we do here is to call methods to build the GUI
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle(FastMailGenerals.getApplicationName() + " " 
				+ FastMailGenerals.getVersion());
		buildGUI(primaryStage);
		primaryStage.show();
	}

	// fields to build the GUI
	private MenuBar menuBar;
	private Button btnReply;
	private Button btnReplyAll;
	private Button btnDelete;
	
	// fields for handling accounts and mails
	private TreeItem<String> rootItem;
	String rootItemString = "Mail Accounts";
	MailView mailBody;
	TableView<EmailTableData> folderMailTable;

	
	// global fields for account & folder to get access in the table view
	MailAccountList accounts;

	/**
	 * buildGUI is the main method to set up general GUI, 
	 * that is generating the main window
	 * 
	 * @param primaryStage the Stage item that contains the main windows stage 
	 */
	private void buildGUI(Stage primaryStage) {
		VBox overallPane = new VBox();

		buildMenu(overallPane);
		buildButtonPane(overallPane);
		buildBody(overallPane);
		buildStatusLine(overallPane);
		
		accounts = new MailAccountList(rootItem);
		List<MailAccountData> mad = SerializeManager.getInstance().getMailAccounts();
		for (MailAccountData d : mad) 
			accounts.add(new MailAccount(d));

		Scene scene = new Scene(overallPane, 1300, 800);
		primaryStage.setScene(scene);
	}

	/**
	 * buildMenu builds the main menu
	 * 
	 * @param overallPane this is the main windows layout container
	 */
	private void buildMenu(VBox overallPane) {
		menuBar = new MenuBar();
		// Account Menu
		Menu accountMenu = new Menu("Account");
		MenuItem addAccountItem = new MenuItem("Add Account");
		addAccountItem.setOnAction(ev -> {
			MailAccountDialog dialog = new MailAccountDialog();
			MailAccount account = dialog.showAndWait();
			if (account == null)
				return;
			accounts.add(account);
			SerializeManager.getInstance().addMailAccount(account.getMailAccountData());
		});
		MenuItem editAccountItem = new MenuItem("Edit Accounts");
		editAccountItem.setOnAction(ev -> {
			if (accounts.getCurrentAccount() == null)
				return;
			MailAccountDialog dialog = new MailAccountDialog(accounts.getAccount(accounts.getCurrentAccount()));
			dialog.showAndWait();
		});
		MenuItem removeAccountItem = new MenuItem("Remove Account");
		removeAccountItem.setOnAction(ev -> {
			if (accounts.getCurrentAccount() == null)
				return;
			Alert alert = new Alert(Alert.AlertType.WARNING,
					"Are you sure you want to remove the account\n" +
					accounts.getCurrentAccount(), 
					ButtonType.YES, ButtonType.CANCEL);
			alert.setTitle("Remove current account");
			alert.setHeaderText("You are about to remove an account from Fastmail");
			Optional<ButtonType> opt = alert.showAndWait();
			if (opt.get().equals(ButtonType.CANCEL))
				return;
			if (opt.get().equals(ButtonType.YES)) {
				SerializeManager.getInstance().removeMailAccount(
						accounts.getAccount(accounts.getCurrentAccount()).getMailAccountData());
				accounts.removeAccount(accounts.getCurrentAccount());
				accounts.setCurrentAccount(null);
				accounts.setCurrentFolder(null);
				folderMailTable.getItems().clear();
				accounts.setCurrentMail(0);
				mailBody.clear();
			}
			return;
		});
		accountMenu.getItems().addAll(addAccountItem, editAccountItem, removeAccountItem);
		
		// Help Menu
		Menu helpMenu = new Menu("Fastmail");
		MenuItem aboutHelpItem = new MenuItem("About");
		aboutHelpItem.setOnAction(ev -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION,
					"Written by " + FastMailGenerals.getAuthor() + "\n\n" + 
							"Licensed under the " + FastMailGenerals.getLicense() + "\n\n" + 
							"Version: " + FastMailGenerals.getVersion(),
							ButtonType.OK
					);
			alert.setTitle("About");
			alert.setHeaderText(FastMailGenerals.getNameVersion());

			alert.showAndWait();
		});
		helpMenu.getItems().addAll(aboutHelpItem);
		
		// build all together
		menuBar.getMenus().addAll(accountMenu, helpMenu);
		overallPane.getChildren().add(menuBar);
	}

	/**
	 * buildButtonPane builds the button menu below the main menu
	 * we want to have modern styled large buttons
	 * 
	 * @param overallPane this is the main windows layout container
	 */
	private void buildButtonPane(VBox overallPane) {
		HBox hbox = new HBox();
		Button btnNew = new Button("New");
		btnNew.setPrefSize(90, 50);
		btnNew.setMinSize(90, 50);
		btnNew.setOnAction(ev -> {
			new MailComposer(accounts);
		});
		btnReply = new Button("Reply");
		btnReply.setPrefSize(90, 50);
		btnReply.setMinSize(90, 50);
		btnReply.setOnAction(ev -> {
			if (accounts.getCurrentMessage() == 0)
				return;
			new MailComposer(accounts, 
					folderMailTable.getSelectionModel().getSelectedItem().getFromAddress(), 
					folderMailTable.getSelectionModel().getSelectedItem().getSubject(),
					mailBody.getMailContent().getContent());
		});
		btnReply.setDisable(true);
		btnReplyAll = new Button("Reply All");
		btnReplyAll.setPrefSize(90, 50);
		btnReplyAll.setMinSize(90, 50);
		btnReplyAll.setDisable(true);
		btnDelete = new Button("Delete");
		btnDelete.setPrefSize(90, 50);
		btnDelete.setMinSize(90, 50);
		btnDelete.setOnAction(ev -> {
			accounts.getAccount(
				accounts.getCurrentAccount()).
				deleteMessage(folderMailTable.getSelectionModel().getSelectedItem(), 
				accounts.getCurrentFolder());
			mailBody.clear();
		});
		btnDelete.setDisable(true);
		
		
		hbox.getChildren().addAll(btnNew, btnReply, btnReplyAll, btnDelete);
		overallPane.getChildren().add(hbox);
	}

	/**
	 * buildBody builds the main working window area
	 * in a email program this is the account tree view, the message folders content,
	 * an the actual email text area
	 * 
	 * @param overallPane this is the main windows layout container
	 */
	private void buildBody(VBox overallPane) {
		// the body must be build upside down, as we use Splitter

		// local fields
		rootItem = new TreeItem<String>(rootItemString);
		rootItem.setExpanded(true);
		
		// right side: folder content and message body
		// upper side: folder
		folderMailTable = new TableView<EmailTableData>();
		folderMailTable.setEditable(true);	// need to check, that only few fields can be modyfied
		folderMailTable.setPlaceholder(new Label("choose Folder on the left side to show Emails"));
		TableColumn<EmailTableData, String> subjectCol = new TableColumn<>("Subject");
		subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
		subjectCol.setMinWidth(400);
		TableColumn<EmailTableData, String> fromCol = new TableColumn<>("From");
		fromCol.setCellValueFactory(new PropertyValueFactory<>("from"));
		fromCol.setMinWidth(250);
		TableColumn<EmailTableData, String> dateCol = new TableColumn<>("Date");
		dateCol.setMinWidth(150);
		dateCol.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));
		dateCol.setCellFactory(new DateCellFactory());
		dateCol.setComparator(new DateCellComparator());
		TableColumn<EmailTableData, Boolean> readCol = new TableColumn<>("R");
		readCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("read"));
		readCol.setCellFactory(CheckBoxTableCell.forTableColumn(readCol));
		readCol.setEditable(true);
		readCol.setMaxWidth(30);
		TableColumn<EmailTableData, Boolean> attachmentCol = new TableColumn<>("A");
		attachmentCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("attachment"));
		attachmentCol.setCellFactory(CheckBoxTableCell.forTableColumn(attachmentCol));
		attachmentCol.setEditable(false);
		attachmentCol.setMaxWidth(30);
		TableColumn<EmailTableData, Boolean> markerCol = new TableColumn<>("M");
		markerCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("marked"));
		markerCol.setCellFactory(CheckBoxTableCell.forTableColumn(markerCol));
		markerCol.setEditable(true);
		markerCol.setMaxWidth(30);
		// idCol is only for mail selection
		TableColumn<EmailTableData, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		idCol.setVisible(false);
		folderMailTable.getColumns().addAll(Arrays.asList(
				subjectCol, fromCol, dateCol, readCol, attachmentCol, 
				markerCol, idCol));
		// local field representing the mails in a folder, that should be shown
//		folderMailTable.setItems(emailList);
		folderMailTable.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
					if (newVal != null) {
						MailContentLister mcl = 
								new MailContentLister(accounts.getAccount(accounts.getCurrentAccount()), accounts.getCurrentFolder(), newVal.getId());
						accounts.setCurrentMail(newVal.getId());
						btnReply.setDisable(false);
//						btnReplyAll.setDisable(false);
						btnDelete.setDisable(false);
						mailBody.setContent(mcl.getMessage(newVal.getId()));
					}
				});

		ScrollPane folderScroller = new ScrollPane(folderMailTable);
		folderScroller.setFitToHeight(true);
		folderScroller.setFitToWidth(true);
		
		// lower side: message body
		mailBody = new MailView();

		// build right side
		SplitPane mailfolderSplitter = new SplitPane(folderScroller, mailBody);
		mailfolderSplitter.setDividerPosition(0, 0.4);
		mailfolderSplitter.setOrientation(Orientation.VERTICAL);
		
		// left side: Mailboxes and their folders
		

		TreeView<String> accountTree = new TreeView<String>(rootItem);
		accountTree.setEditable(true);
		accountTree.setCellFactory((TreeView<String> p) -> new TreeCellFactory());
		accountTree.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<TreeItem<String>>() {
					@Override
					public void changed(ObservableValue<? extends TreeItem<String>> observedItem, TreeItem<String> oldVal,
							TreeItem<String> newVal) {
						if (newVal == null) {
							mailBody.clear();
							return;	// this should only be the case if a account has been removed
						}
						if (newVal.getValue().equals(rootItem.getValue()))
							return;
						TreeItem<String> upItem = newVal;
						while (upItem.getParent() != null &&
								!upItem.getParent().getValue().equals(rootItemString))
							upItem = upItem.getParent();
						accounts.setCurrentAccount(upItem.getValue());
						accounts.setCurrentFolder(newVal.getValue());
						accounts.setCurrentMail(0);
						btnReply.setDisable(true);
						btnReplyAll.setDisable(true);
						btnDelete.setDisable(true);
						folderMailTable.setItems(
								accounts.getAccount(accounts.getCurrentAccount())
								.getMessages(accounts.getCurrentFolder()));
					}
				});
		
		// ScrollPane below should then use AccountPane...
		ScrollPane mailboxScroller = new ScrollPane(accountTree);
		mailboxScroller.setFitToHeight(true);
		mailboxScroller.setFitToWidth(true);
		
		// building it all together
		SplitPane mainSplitter = new SplitPane(mailboxScroller, mailfolderSplitter);
		mainSplitter.setDividerPosition(0, 0.25);
		VBox.setVgrow(mainSplitter, Priority.ALWAYS);
		overallPane.getChildren().add(mainSplitter);
	}

	/**
	 * buildStatusLine is a simple status line, where we can provide some
	 * current messages for the user, that don't have to be prompted.
	 * 
	 * @param overallPane this is the main windows layout container
	 */
	private void buildStatusLine(VBox overallPane) {
		HBox hbox = new HBox(8);
		Label lbl = new Label("Status");
		hbox.getChildren().addAll(lbl);
		overallPane.getChildren().add(hbox);
	}

}
