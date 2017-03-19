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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.mail.Folder;

import com.github.koshamo.fastmail.FastMailGenerals;
import com.github.koshamo.fastmail.mail.EmailTableData;
import com.github.koshamo.fastmail.mail.FolderItem;
import com.github.koshamo.fastmail.mail.MailAccount;
import com.github.koshamo.fastmail.mail.MailAccountData;
import com.github.koshamo.fastmail.mail.MailContentLister;
import com.github.koshamo.fastmail.mail.MailTreeViewable;
import com.github.koshamo.fastmail.util.DateCellComparator;
import com.github.koshamo.fastmail.util.DateCellFactory;
import com.github.koshamo.fastmail.util.SerializeManager;
import com.github.koshamo.fastmail.util.TreeCellFactory;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
	private TreeItem<MailTreeViewable> rootItem;
	MailView mailBody;
	TableView<EmailTableData> folderMailTable;
	TreeView<MailTreeViewable> accountTree;

	
	// global fields for account & folder to get access in the table view
//	MailAccountList accounts;

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
		
		List<MailAccountData> mad = SerializeManager.getInstance().getMailAccounts();
		for (MailAccountData d : mad) {
			TreeItem<MailTreeViewable> account = new TreeItem<MailTreeViewable>(new MailAccount(d));
			rootItem.getChildren().add(account);
		}

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
			MailAccountData accountData = dialog.showAndWait();
			if (accountData == null)
				return;
			TreeItem<MailTreeViewable> account = new TreeItem<MailTreeViewable>(new MailAccount(accountData));
			rootItem.getChildren().add(account);
			SerializeManager.getInstance().addMailAccount(accountData);
		});
		MenuItem editAccountItem = new MenuItem("Edit Account");
		editAccountItem.setOnAction(ev -> {
			if (accountTree.getSelectionModel().getSelectedItem() == null)
				return;
			TreeItem<MailTreeViewable> curItem = accountTree.getSelectionModel().getSelectedItem(); 
			while (!curItem.getValue().isAccount())
				curItem.getParent();
			MailAccountDialog dialog = new MailAccountDialog(
					((MailAccount) curItem.getValue()).getMailAccountData());
			dialog.showAndWait();
		});
		MenuItem removeAccountItem = new MenuItem("Remove Account");
		removeAccountItem.setOnAction(ev -> {
			if (accountTree.getSelectionModel().getSelectedItem() == null)
				return;
			TreeItem<MailTreeViewable> curItem = accountTree.getSelectionModel().getSelectedItem(); 
			while (!curItem.getValue().isAccount())
				curItem.getParent();
			Alert alert = new Alert(Alert.AlertType.WARNING,
					"Are you sure you want to remove the account\n" +
							((MailAccount) curItem.getValue()).getAccountName(), 
					ButtonType.YES, ButtonType.CANCEL);
			alert.setTitle("Remove current account");
			alert.setHeaderText("You are about to remove an account from Fastmail");
			Optional<ButtonType> opt = alert.showAndWait();
			if (opt.get().equals(ButtonType.CANCEL))
				return;
			if (opt.get().equals(ButtonType.YES)) {
				SerializeManager.getInstance().removeMailAccount(
						((MailAccount) curItem.getValue()).getMailAccountData());
				((MailAccount) curItem.getValue()).remove();
				folderMailTable.getItems().clear();
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
			new MailComposer(rootItem.getChildren().toArray(new MailAccount[0]));
		});
		btnReply = new Button("Reply");
		btnReply.setPrefSize(90, 50);
		btnReply.setMinSize(90, 50);
		btnReply.setOnAction(ev -> {
			TreeItem<MailTreeViewable> treeItem = accountTree.getSelectionModel().getSelectedItem(); 
			if (treeItem == null)
				return;
			EmailTableData tableData = folderMailTable.getSelectionModel().getSelectedItem(); 
			if (tableData == null)
				return;
			while (!treeItem.getValue().isAccount())
				treeItem = treeItem.getParent();
			int index = rootItem.getChildren().indexOf(treeItem);
			new MailComposer(rootItem.getChildren().toArray(new MailAccount[0]),
					index, tableData.getMailData(), false);
		});
		btnReply.setDisable(true);
		btnReplyAll = new Button("Reply All");
		btnReplyAll.setPrefSize(90, 50);
		btnReplyAll.setMinSize(90, 50);
		btnReplyAll.setOnAction(ev -> {
			TreeItem<MailTreeViewable> treeItem = accountTree.getSelectionModel().getSelectedItem(); 
			if (treeItem == null)
				return;
			EmailTableData tableData = folderMailTable.getSelectionModel().getSelectedItem(); 
			if (tableData == null)
				return;
			while (!treeItem.getValue().isAccount())
				treeItem = treeItem.getParent();
			int index = rootItem.getChildren().indexOf(treeItem);
			new MailComposer(rootItem.getChildren().toArray(new MailAccount[0]),
					index, tableData.getMailData(), true);
		});
		btnReplyAll.setDisable(true);
		btnDelete = new Button("Delete");
		btnDelete.setPrefSize(90, 50);
		btnDelete.setMinSize(90, 50);
		btnDelete.setOnAction(ev -> {
			TreeItem<MailTreeViewable> treeItem = accountTree.getSelectionModel().getSelectedItem(); 
			if (treeItem == null)
				return;
			EmailTableData tableData = folderMailTable.getSelectionModel().getSelectedItem(); 
			if (tableData == null)
				return;
			TreeItem<MailTreeViewable> accountItem = treeItem;
			while (!accountItem.getValue().isAccount())
				accountItem = accountItem.getParent();
			((MailAccount) accountItem.getValue()).deleteMessage((FolderItem) treeItem.getValue(), tableData);
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

		// upper right side:
		ScrollPane folderScroller = buildTableView();
		
		// lower side: message body
		mailBody = new MailView();

		// build right side
		SplitPane mailfolderSplitter = new SplitPane(folderScroller, mailBody);
		mailfolderSplitter.setDividerPosition(0, 0.4);
		mailfolderSplitter.setOrientation(Orientation.VERTICAL);
		
		// left side: Mailboxes and their folders
		ScrollPane mailboxScroller = buildTreeView();
		
		// building it all together
		SplitPane mainSplitter = new SplitPane(mailboxScroller, mailfolderSplitter);
		mainSplitter.setDividerPosition(0, 0.25);
		VBox.setVgrow(mainSplitter, Priority.ALWAYS);
		overallPane.getChildren().add(mainSplitter);
	}

	
	/**
	 * Builds the TableView representing the mail folders content
	 * and returns the content in a ScrollPane
	 * @return	the ScrollPane representing the TableView
	 */
	private ScrollPane buildTableView() {
		// upper right side: folder
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
		folderMailTable.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
					if (newVal != null) {
						MailContentLister mcl = new MailContentLister(newVal);
						btnReply.setDisable(false);
						btnReplyAll.setDisable(false);
						btnDelete.setDisable(false);
						mailBody.setContent(mcl.getMessage());
					}
				});

		ScrollPane folderScroller = new ScrollPane(folderMailTable);
		folderScroller.setFitToHeight(true);
		folderScroller.setFitToWidth(true);
		
		return folderScroller;
	}
	
	
	/** Builds the TreeView representing the mail accounts and returns it
	 * within a ScrollPane
	 * @return	the ScrollPane representing the TableView 
	 */
	private ScrollPane buildTreeView() {
		/* As the JavaFX TreeView does not allow multiple root items,
		 * we need to define the one and only root item, which has only
		 * one function: be the root of all mail accounts. Actually we
		 * do not need the root for any other function. So the root item
		 * is hidden and provided with an empty interface implementation.
		 */
		rootItem = new TreeItem<MailTreeViewable>(new MailTreeViewable() {
			@Override
			public boolean isAccount() {
				return false;
			}
			@Override
			public ObservableList<EmailTableData> getFolderContent() {
				return null;
			}
			@Override
			public Folder getParentFolder() {
				return null;
			}
			@Override
			public String getName() {
				return null;
			}
		});
		rootItem.setExpanded(true);

		accountTree = new TreeView<MailTreeViewable>(rootItem);
		accountTree.setEditable(true);
		accountTree.setCellFactory((TreeView<MailTreeViewable> p) -> new TreeCellFactory());
		accountTree.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<TreeItem<MailTreeViewable>>() {
					@Override
					public void changed(
							ObservableValue<? extends TreeItem<MailTreeViewable>> observedItem, 
							TreeItem<MailTreeViewable> oldVal,
							TreeItem<MailTreeViewable> newVal) {
						// this should only be the case if a account has been removed
						if (newVal == null) {
							mailBody.clear();
							return;	
						}
						// TODO: is the folders account information really needed?
//						TreeItem<MailTreeViewable> upItem = newVal;
//						while (!upItem.getValue().isAccount()
//								&& upItem.getParent() != null)
//							upItem = upItem.getParent();
						btnReply.setDisable(true);
						btnReplyAll.setDisable(true);
						btnDelete.setDisable(true);
						folderMailTable.setItems(newVal.getValue().getFolderContent());
					}
				});
		// we do not want to see the root item: simulate the Accounts as 
		// multiple roots
		accountTree.setShowRoot(false);
		
		// ScrollPane below should then use AccountPane...
		ScrollPane mailboxScroller = new ScrollPane(accountTree);
		mailboxScroller.setFitToHeight(true);
		mailboxScroller.setFitToWidth(true);

		return mailboxScroller;
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
