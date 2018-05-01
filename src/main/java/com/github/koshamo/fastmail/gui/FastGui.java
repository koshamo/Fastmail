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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import com.github.koshamo.fastmail.FastMailGenerals;
import com.github.koshamo.fastmail.events.EditAccountEvent;
import com.github.koshamo.fastmail.events.EditType;
import com.github.koshamo.fastmail.events.FolderItemMeta;
import com.github.koshamo.fastmail.events.FolderItemOrders;
import com.github.koshamo.fastmail.events.MailAccountOrders;
import com.github.koshamo.fastmail.events.PropagateFolderTreeEvent;
import com.github.koshamo.fastmail.events.RequestFolderItemEvent;
import com.github.koshamo.fastmail.events.ShowMailListEvent;
import com.github.koshamo.fastmail.gui.utils.DateCellComparator;
import com.github.koshamo.fastmail.gui.utils.DateCellFactory;
import com.github.koshamo.fastmail.gui.utils.TreeViewUtils;
import com.github.koshamo.fastmail.mail.MailAccountData;
import com.github.koshamo.fastmail.util.AccountWrapper;
import com.github.koshamo.fastmail.util.EmailTableData;
import com.github.koshamo.fastmail.util.FolderWrapper;
import com.github.koshamo.fastmail.util.MailTreeViewable;
import com.github.koshamo.fastmail.util.MessageConsumer;
import com.github.koshamo.fastmail.util.SerializeManager;
import com.github.koshamo.fastmail.util.UnbalancedTreeUtils;
import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.ExitEvent;
import com.github.koshamo.fiddler.MessageBus.ListenerType;
import com.github.koshamo.fiddler.jfx.FiddlerFxApp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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
public class FastGui extends FiddlerFxApp {

	/* start is the starting point of a JavaFX application
	 * all we do here is to call methods to build the GUI
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(final Stage primaryStage) {
		i18n = SerializeManager.getLocaleMessageBundle();
		primaryStage.setTitle(FastMailGenerals.getApplicationName() + " "  //$NON-NLS-1$
				+ FastMailGenerals.getVersion());
		buildGUI(primaryStage);
		primaryStage.show();
		
		SerializeManager.getLocaleMessageBundle();
		i18n = SerializeManager.getLocaleMessageBundle();
		
		getMessageBus().registerAllEvents(this, ListenerType.ANY);
	}

	// the resource bundle containing the internationalized strings
	ResourceBundle i18n;
	
	// fields to build the GUI
	Button btnReply;
	Button btnReplyAll;
	Button btnDelete;
	ContextMenu treeContextMenu;
	ContextMenu tableContextMenu;
	
	// fields for handling accounts and mails
	private TreeItem<MailTreeViewable> rootItem;
	MailView mailBody;
	TableView<EmailTableData> folderMailTable;
	TreeView<MailTreeViewable> accountTree;

	

	/**
	 * buildGUI is the main method to set up general GUI, 
	 * that is generating the main window
	 * 
	 * @param primaryStage the Stage item that contains the main windows stage 
	 */
	private void buildGUI(final Stage primaryStage) {
		final VBox overallPane = new VBox();

		overallPane.getChildren().addAll(
				buildMenu(), 
				buildButtonPane(),
				buildBody(),
				buildStatusLine());

		final Scene scene = new Scene(overallPane, 1300, 800);
		primaryStage.setScene(scene);
		// set on close operation
		primaryStage.setOnCloseRequest(
				ev -> {
					SerializeManager.getInstance().serialize();
					getMessageBus().postEvent(new ExitEvent(this, null));
					});
	}

	/**
	 * buildMenu builds the main menu
	 * 
	 * @return the full configured menu bar to be added to the stage
	 */
	private MenuBar buildMenu() {
		final MenuBar menuBar = new MenuBar();
		
		menuBar.getMenus().addAll(
				buildAccountMenu(), 
				buildHelpMenu());
		return menuBar;
	}

	/**
	 * buildAccountMenu is the method to build up the menu and its
	 * entries for the accounts
	 * 
	 * @return the account Menu
	 */
	private Menu buildAccountMenu() {
		final Menu accountMenu = new Menu(i18n.getString("entry.account")); //$NON-NLS-1$
		
		accountMenu.getItems().addAll(
				addAddAccountItem(), 
				addEditAccountItem(), 
				addRemoveAccountItem());
		
		return accountMenu;
	}
	
	private MenuItem addAddAccountItem() {
		final MenuItem addAccountItem = new MenuItem(i18n.getString("action.addaccount")); //$NON-NLS-1$
		addAccountItem.setOnAction(ev -> {
			final MailAccountDialog dialog = new MailAccountDialog();
			final MailAccountData accountData = dialog.showAndWait();
			if (accountData == null)
				return;
			SerializeManager.getInstance().addMailAccount(accountData);
			getMessageBus().postEvent(new EditAccountEvent(this, null, EditType.ADD, accountData));
		});
		return addAccountItem;
	}
	
	/*package private*/ MenuItem addEditAccountItem() {
		final MenuItem editAccountItem = new MenuItem(i18n.getString("action.editaccount")); //$NON-NLS-1$
		editAccountItem.setOnAction(ev -> {
			if (accountTree.getSelectionModel().getSelectedItem() == null)
				return;
			final TreeItem<MailTreeViewable> curItem = 
					accountTree.getSelectionModel().getSelectedItem(); 
			while (!curItem.getValue().isAccount())
				curItem.getParent();
			final MailAccountDialog dialog = new MailAccountDialog(
					((AccountWrapper) curItem.getValue()).getMailAccountData());
			final MailAccountData accountData = dialog.showAndWait();
			if (accountData == null)
				return;
			SerializeManager.getInstance().changeMailAccount(
					((AccountWrapper) curItem.getValue()).getMailAccountData(), 
					accountData);
			getMessageBus().postEvent(new EditAccountEvent(this, null, EditType.EDIT, accountData));
		});
		return editAccountItem;
	}

	private MenuItem addRemoveAccountItem() {
		final MenuItem removeAccountItem = new MenuItem(i18n.getString("action.removeaccount")); //$NON-NLS-1$
		removeAccountItem.setOnAction(ev -> {
			if (accountTree.getSelectionModel().getSelectedItem() == null)
				return;
			final TreeItem<MailTreeViewable> curItem = 
					accountTree.getSelectionModel().getSelectedItem(); 
			while (!curItem.getValue().isAccount())
				curItem.getParent();
			final Alert alert = new Alert(Alert.AlertType.WARNING,
					MessageFormat.format(i18n.getString("alert.message.removeaccount"),  //$NON-NLS-1$
							curItem.getValue().getName()), 
					ButtonType.YES, ButtonType.CANCEL);
			alert.setTitle(i18n.getString("alert.title.removeaccount")); //$NON-NLS-1$
			alert.setHeaderText(i18n.getString("alert.header.removeaccount")); //$NON-NLS-1$
			final Optional<ButtonType> opt = alert.showAndWait();
			if (opt.get().equals(ButtonType.CANCEL))
				return;
			if (opt.get().equals(ButtonType.YES)) {
				SerializeManager.getInstance().removeMailAccount(
						((AccountWrapper) curItem.getValue()).getMailAccountData());
				getMessageBus().postEvent(new EditAccountEvent(this, null, EditType.REMOVE, 
						((AccountWrapper) curItem.getValue()).getMailAccountData()));
				mailBody.clear();
			}
			return;
		});
		return removeAccountItem;
	}


	private Menu buildHelpMenu() {
		final Menu helpMenu = new Menu(FastMailGenerals.getApplicationName());
		final MenuItem aboutHelpItem = new MenuItem(i18n.getString("entry.about")); //$NON-NLS-1$
		aboutHelpItem.setOnAction(ev -> {
			final Alert alert = new Alert(Alert.AlertType.INFORMATION,
					MessageFormat.format(i18n.getString("entry.message.about"), //$NON-NLS-1$
							FastMailGenerals.getAuthor(), 
							FastMailGenerals.getLicense(),
							FastMailGenerals.getVersion()),
							ButtonType.OK
					);
			alert.setTitle(i18n.getString("entry.about")); //$NON-NLS-1$
			alert.setHeaderText(FastMailGenerals.getNameVersion());

			alert.showAndWait();
		});
		helpMenu.getItems().addAll(aboutHelpItem);
		
		return helpMenu;
	}
	/**
	 * buildButtonPane builds the button menu below the main menu
	 * we want to have modern styled large buttons
	 * 
	 * @return the layouted button pane ready to be added to stage
	 */
	@SuppressWarnings("unused")
	private Node buildButtonPane() {
		final HBox hbox = new HBox();
		// NEW button
		final Button btnNew = new Button(i18n.getString("action.new")); //$NON-NLS-1$
		btnNew.setPrefSize(90, 50);
		btnNew.setMinSize(90, 50);
//		btnNew.setOnAction(ev -> {
//			ObservableList<TreeItem<MailTreeViewable>> accountList = 
//					rootItem.getChildren();
//			// TODO:
////			final MailAccount[] ma = new MailAccount[accountList.size()];
////			for (int i = 0; i < ma.length; i++)
////				ma[i] = (MailAccount)accountList.get(i).getValue();
//			// this is where a unused warning comes from
//			// as MailComposer does everything on its own
////			new MailComposer(ma);
//		});
		// REPLY button
		btnReply = new Button(i18n.getString("action.reply")); //$NON-NLS-1$
		btnReply.setPrefSize(90, 50);
		btnReply.setMinSize(90, 50);
		btnReply.setOnAction(ev -> {
			replyMail();
		});
		btnReply.setDisable(true);
		// REPLY ALL button
		btnReplyAll = new Button(i18n.getString("action.replyall")); //$NON-NLS-1$
		btnReplyAll.setPrefSize(90, 50);
		btnReplyAll.setMinSize(90, 50);
		btnReplyAll.setOnAction(ev -> {
			replyAllMail();
		});
		btnReplyAll.setDisable(true);
		// DELETE button
		btnDelete = new Button(i18n.getString("action.delete")); //$NON-NLS-1$
		btnDelete.setPrefSize(90, 50);
		btnDelete.setMinSize(90, 50);
		btnDelete.setOnAction(ev -> {
			deleteMail();
		});
		btnDelete.setDisable(true);
		
		hbox.getChildren().addAll(btnNew, btnReply, btnReplyAll, btnDelete);
		return hbox;
	}

	
	/**
	 * buildBody builds the main working window area
	 * in a email program this is the account tree view, the message folders 
	 * content, and the actual email text area
	 * 
	 * @return the body to be added to the stage
	 */
	private Node buildBody() {
		// the body must be build upside down, as we use Splitter

		// upper right side:
		final ScrollPane folderScroller = buildTableView();
		
		// lower side: message body
		mailBody = new MailView();

		// build right side
		final SplitPane mailfolderSplitter = new SplitPane(folderScroller, mailBody);
		mailfolderSplitter.setDividerPosition(0, 0.4);
		mailfolderSplitter.setOrientation(Orientation.VERTICAL);
		
		// left side: Mailboxes and their folders
		final ScrollPane mailboxScroller = buildTreeView();
		
		// building it all together
		final SplitPane mainSplitter = new SplitPane(mailboxScroller, mailfolderSplitter);
		mainSplitter.setDividerPosition(0, 0.25);
		VBox.setVgrow(mainSplitter, Priority.ALWAYS);
		return mainSplitter;
	}

	
	/**
	 * Builds the TableView representing the mail folders content
	 * and returns the content in a ScrollPane
	 * @return	the ScrollPane representing the TableView
	 */
	private ScrollPane buildTableView() {
		// upper right side: folder
		folderMailTable = new TableView<>();
		folderMailTable.setEditable(true);	
		folderMailTable.setPlaceholder(new Label(i18n.getString("entry.default.mailtable"))); //$NON-NLS-1$
		tableContextMenu = new ContextMenu();
		final Menu moveTo = new Menu(i18n.getString("action.moveto")); //$NON-NLS-1$
		tableContextMenu.getItems().addAll(
				addReplyItem(), addReplyAllItem(), addDeleteItem(),
				new SeparatorMenuItem(), moveTo);
		folderMailTable.setContextMenu(tableContextMenu);
		folderMailTable.getColumns().addAll(Arrays.asList(
						buildSubjectTableCol(), 
						buildFromTableCol(), 
						buildDateTableCol(), 
						buildReadTableCol(), 
						buildAttachmentTableCol(), 
						buildMarkerTableCol()));
		// local field representing the mails in a folder, that should be shown
//		folderMailTable.getSelectionModel().selectedItemProperty().addListener(
//				(obs, oldVal, newVal) -> {
//					if (newVal != null) {
//						// context Menu
//						moveTo.getItems().clear();
//						TreeItem<FolderWrapper> treeItem = 
//								accountTree.getSelectionModel().getSelectedItem();
//						TreeItem<FolderWrapper> accountItem = treeItem;
//						while (!accountItem.getValue().isAccount())
//							accountItem = accountItem.getParent();
//						ObservableList<TreeItem<FolderWrapper>> folders = 
//								accountItem.getChildren();
//						folders	.stream()
//								.filter(p -> p != treeItem)
//								.forEach(p -> moveTo.getItems().add(addMoveToItem(newVal, (FolderItem)p.getValue())));
//						// disable buttons and show mail
//						btnReply.setDisable(false);
//						btnReplyAll.setDisable(false);
//						btnDelete.setDisable(false);
//						mailBody.setContent(newVal.getMailData());
//						// analyze mail content to get real content
////						MailTools.analyzeContent(newVal.getMessage());
//					}
//				});

		final ScrollPane folderScroller = new ScrollPane(folderMailTable);
		folderScroller.setFitToHeight(true);
		folderScroller.setFitToWidth(true);
		
		return folderScroller;
	}

	/**
	 * @return
	 */
	private TableColumn<EmailTableData, String> buildSubjectTableCol() {
		// SUBJECT column
		final TableColumn<EmailTableData, String> subjectCol = new TableColumn<>(i18n.getString("entry.subject")); //$NON-NLS-1$
		subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject")); //$NON-NLS-1$
		subjectCol.setMinWidth(400);
		return subjectCol;
	}

	/**
	 * @return
	 */
	private TableColumn<EmailTableData, String> buildFromTableCol() {
		// FROM column
		final TableColumn<EmailTableData, String> fromCol = new TableColumn<>(i18n.getString("entry.from")); //$NON-NLS-1$
		fromCol.setCellValueFactory(new PropertyValueFactory<>("fromName")); //$NON-NLS-1$
		fromCol.setMinWidth(250);
		return fromCol;
	}
	/**
	 * @return
	 */
	private TableColumn<EmailTableData, String> buildDateTableCol() {
		// DATE column
		final TableColumn<EmailTableData, String> dateCol = new TableColumn<>(i18n.getString("entry.date")); //$NON-NLS-1$
		dateCol.setMinWidth(150);
		dateCol.setCellValueFactory(new PropertyValueFactory<>("sentDate")); //$NON-NLS-1$
		dateCol.setCellFactory((TableColumn<EmailTableData, String> p) -> new DateCellFactory());
		dateCol.setComparator(new DateCellComparator());
		return dateCol;
	}

	/**
	 * @return
	 */
	private TableColumn<EmailTableData, Boolean> buildReadTableCol() {
		// MESSAGE READ column
		final TableColumn<EmailTableData, Boolean> readCol = new TableColumn<>(i18n.getString("entry.short.read")); //$NON-NLS-1$
		readCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("read")); //$NON-NLS-1$
		readCol.setCellFactory((TableColumn<EmailTableData, Boolean> p) -> new TableCellFactory(readCol));
		readCol.setEditable(true);
		readCol.setMaxWidth(30);
		return readCol;
	}

	/**
	 * @return
	 */
	private TableColumn<EmailTableData, Boolean> buildAttachmentTableCol() {
		// ATTACHMENT column
		final TableColumn<EmailTableData, Boolean> attachmentCol = new TableColumn<>(i18n.getString("entry.short.attachment")); //$NON-NLS-1$
		attachmentCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("attachment")); //$NON-NLS-1$
		attachmentCol.setCellFactory(CheckBoxTableCell.forTableColumn(attachmentCol));
		attachmentCol.setEditable(false);
		attachmentCol.setMaxWidth(30);
		return attachmentCol;
	}

	/**
	 * @return
	 */
	private TableColumn<EmailTableData, Boolean> buildMarkerTableCol() {
		// MESSAGE MARKED column
		final TableColumn<EmailTableData, Boolean> markerCol = new TableColumn<>(i18n.getString("entry.short.marked")); //$NON-NLS-1$
		markerCol.setCellValueFactory(new PropertyValueFactory<EmailTableData,Boolean>("marked")); //$NON-NLS-1$
		markerCol.setCellFactory(CheckBoxTableCell.forTableColumn(markerCol));
		markerCol.setEditable(true);
		markerCol.setMaxWidth(30);
		return markerCol;
	}
	
	
	/** Builds the TreeView representing the mail accounts with its folders
	 * and returns it within a ScrollPane
	 * @return	the ScrollPane representing the TableView 
	 */
	private ScrollPane buildTreeView() {
		/* As the JavaFX TreeView does not allow multiple root items,
		 * we need to define the one and only root item, which has only
		 * one function: be the root of all mail accounts. Actually we
		 * do not need the root for any other function. So the root item
		 * is hidden (and provided with an empty interface implementation).
		 */
		rootItem = new TreeItem<>(null);
		rootItem.setExpanded(true);

		accountTree = new TreeView<>(rootItem);
		accountTree.setEditable(true);
		treeContextMenu = new ContextMenu();
		accountTree.setContextMenu(treeContextMenu);
		accountTree.setCellFactory((TreeView<MailTreeViewable> p) -> new TreeCellFactory(this));
		accountTree.getSelectionModel().selectedItemProperty().addListener(
				new TreeViewChangeListener(this));
		// we do not want to see the root item: simulate the Accounts as 
		// multiple roots
		accountTree.setShowRoot(false);
		
		// ScrollPane below should then use AccountPane...
		final ScrollPane mailboxScroller = new ScrollPane(accountTree);
		mailboxScroller.setFitToHeight(true);
		mailboxScroller.setFitToWidth(true);

		return mailboxScroller;
	}
	
	private MenuItem addReplyItem() {
		final MenuItem reply = new MenuItem(i18n.getString("action.reply")); //$NON-NLS-1$
//		reply.setOnAction(ev -> {
//			replyMail();
//		});
		return reply;
	}
	
	private MenuItem addReplyAllItem() {
		final MenuItem replyAll = new MenuItem(i18n.getString("action.replyall")); //$NON-NLS-1$
//		replyAll.setOnAction(ev -> {
//			replyAllMail();
//		});
		return replyAll;
	}
	
	private MenuItem addDeleteItem() {
		final MenuItem delete = new MenuItem(i18n.getString("action.delete")); //$NON-NLS-1$
//		delete.setOnAction(ev -> {
//			deleteMail();
//		});
		return delete;
	}
	
	@SuppressWarnings("static-method")
	private MenuItem addMoveToItem(EmailTableData mail, FolderWrapper target) {
		final MenuItem move = new MenuItem(target.getName());
//		move.setOnAction(ev -> {
//			target.moveMessage(mail);
//		});
		return move;
	}
	
	/**
	 * Creates a MenuItem with functionality to add a folder to this account.
	 * 
	 * @return a full featured MenuItem to add a folder to this account
	 */
	MenuItem addAddFolderItem() {
		final MenuItem add = new MenuItem(i18n.getString("action.addfolder")); //$NON-NLS-1$
		add.setOnAction(p -> {
			TreeItem<MailTreeViewable> curItem = 
					accountTree.getSelectionModel().getSelectedItem();
			String curFolder = curItem.getValue().getFullName();
			String account = getAccountName(curItem);
			FolderItemMeta meta = new FolderItemMeta(account, curFolder, FolderItemOrders.NEW);
			propagateEvent(new RequestFolderItemEvent(this, null, meta));
		});
		return add;
	}

	
	/**
	 * Creates a MenuItem with functionality to delete this folder with any 
	 * content recursively. A warning dialog will be shown prior to deleting
	 * the folder
	 * 
	 * @return a full featured MenuItem to delete this folder
	 */
	MenuItem addDeleteFolderItem() {
		final MenuItem delete = new MenuItem(i18n.getString("action.deletefolder")); //$NON-NLS-1$
		delete.setOnAction(p -> {
			TreeItem<MailTreeViewable> curItem = 
					accountTree.getSelectionModel().getSelectedItem();
			final Alert alert = new Alert(Alert.AlertType.WARNING,
					MessageFormat.format(i18n.getString("alert.message.removefolder"), //$NON-NLS-1$
							curItem.getValue().getName()), 
					ButtonType.YES, ButtonType.CANCEL);
			alert.setTitle(i18n.getString("alert.title.removefolder")); //$NON-NLS-1$
			alert.setHeaderText(i18n.getString("alert.header.removefolder")); //$NON-NLS-1$
			final Optional<ButtonType> opt = alert.showAndWait();
			if (opt.get().equals(ButtonType.CANCEL))
				return;

			if (opt.get().equals(ButtonType.YES)) {
				String curFolder = curItem.getValue().getFullName();
				String account = getAccountName(curItem);
				FolderItemMeta meta = new FolderItemMeta(account, curFolder, FolderItemOrders.REMOVE);
				propagateEvent(new RequestFolderItemEvent(this, null, meta));
			}
		});
		return delete;
	}
	
	/**
	 * Reply Mail functionality, used for button and context menu
	 */
	private void replyMail() {
		// TODO:
/*		TreeItem<MailTreeViewable> treeItem = 
				accountTree.getSelectionModel().getSelectedItem(); 
		if (treeItem == null)
			return;
		final EmailTableData tableData = 
				folderMailTable.getSelectionModel().getSelectedItem(); 
		if (tableData == null)
			return;
		while (!treeItem.getValue().isAccount())
			treeItem = treeItem.getParent();
		final ObservableList<TreeItem<MailTreeViewable>> accountList = 
				rootItem.getChildren();
		final MailAccount[] ma = new MailAccount[accountList.size()];
		for (int i = 0; i < ma.length; i++)
			ma[i] = (MailAccount)accountList.get(i).getValue();
		final int index = rootItem.getChildren().indexOf(treeItem);
		new MailComposer(ma, index, tableData.getMailData(), false);
		*/
	}


	/**
	 * Reply All functionality, used by button and context menu
	 */
	private void replyAllMail() {
		// TODO:
		/*
		TreeItem<MailTreeViewable> treeItem = 
				accountTree.getSelectionModel().getSelectedItem(); 
		if (treeItem == null)
			return;
		final EmailTableData tableData = 
				folderMailTable.getSelectionModel().getSelectedItem(); 
		if (tableData == null)
			return;
		while (!treeItem.getValue().isAccount())
			treeItem = treeItem.getParent();
		final ObservableList<TreeItem<MailTreeViewable>> accountList = 
				rootItem.getChildren();
		final MailAccount[] ma = new MailAccount[accountList.size()];
		for (int i = 0; i < ma.length; i++)
			ma[i] = (MailAccount)accountList.get(i).getValue();
		final int index = rootItem.getChildren().indexOf(treeItem);
		new MailComposer(ma, index, tableData.getMailData(), true);
		*/
	}
	
	/**
	 * Delete Mail functionality, used by button and context menu
	 */
	private void deleteMail() {
		// TODO:
		/*
		final TreeItem<MailTreeViewable> treeItem = accountTree.getSelectionModel().getSelectedItem(); 
		if (treeItem == null || treeItem.getValue().isAccount())
			return;
		final EmailTableData tableData = folderMailTable.getSelectionModel().getSelectedItem();
		if (tableData == null)
			return;
		((FolderItem) treeItem.getValue()).deleteMessage(tableData);
		// after mail has been deleted, clear selection and mail view
		// and disable buttons, as nothing is selected
		// TODO: test and decide explicit selection of new item with the same (updated) index
		folderMailTable.getSelectionModel().clearSelection();
		btnReply.setDisable(false);
		btnReplyAll.setDisable(false);
		btnDelete.setDisable(false);
		mailBody.clear();
		*/
	}
	
	
	/**
	 * buildStatusLine is a simple status line, where we can provide some
	 * current messages for the user, that don't have to be prompted.
	 * 
	 * @return the fully configured status line
	 */
	@SuppressWarnings("static-method")
	private Node buildStatusLine() {
		final AnchorPane anchor = new AnchorPane();
		final Label lbl = new Label();
		final ProgressBar progressBar = new ProgressBar();
		progressBar.setProgress(0.0);
		AnchorPane.setLeftAnchor(lbl, Double.valueOf(5.0));
		AnchorPane.setRightAnchor(progressBar, Double.valueOf(5.0));
		anchor.getChildren().addAll(lbl, progressBar);
		// the message consumer thread to populate the status bar
		final MessageConsumer consumer = 
				new MessageConsumer(lbl.textProperty(), progressBar.progressProperty());
		consumer.start();
		return anchor;
	}

	/*private*/ static void propagateEvent(Event ev) {
		getMessageBus().postEvent(ev);
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(Event event) {
		if (event instanceof PropagateFolderTreeEvent) {
			handleFolderTreeEvent((PropagateFolderTreeEvent) event);
		}
		if (event instanceof ShowMailListEvent) {
			handleShowMailListEvent((ShowMailListEvent) event);
		}
	}

	/**
	 * @param fte
	 */
	private void handleFolderTreeEvent(PropagateFolderTreeEvent fte) {
		MailAccountOrders mao = fte.getMetaInformation().getOrder();
		if (mao == MailAccountOrders.FOLDER_NEW) {
			TreeItem<MailTreeViewable> item = 
					UnbalancedTreeUtils.unbalancedTreeToJfxTreeItems(fte.getData());
			ObservableList<TreeItem<MailTreeViewable>> accountItems = item.getChildren();
			TreeViewUtils.sortFolders(accountItems.get(0).getChildren());
		
			// TODO: setting of changed tree is untested
			Optional<TreeItem<MailTreeViewable>> optItem = 
					rootItem.getChildren().stream().filter((it) -> 
					it.getValue().getName().equals(accountItems.get(0).getValue().getName())).
					findFirst(); 
			if (optItem.isPresent()) {
				int index = rootItem.getChildren().indexOf(optItem.get());
				Platform.runLater(() -> rootItem.getChildren().set(index, accountItems.get(0)));
			} else
				Platform.runLater(() -> rootItem.getChildren().addAll(accountItems));
		}
		if (mao == MailAccountOrders.FOLDER_REMOVE) {
			TreeItem<MailTreeViewable> item = 
					UnbalancedTreeUtils.unbalancedTreeToJfxTreeItems(fte.getData());
			ObservableList<TreeItem<MailTreeViewable>> accountItems = item.getChildren();

			Optional<TreeItem<MailTreeViewable>> optItem = 
					rootItem.getChildren().stream().filter((it) -> 
					it.getValue().getName().equals(accountItems.get(0).getValue().getName())).
					findFirst(); 
			if (optItem.isPresent()) 
				Platform.runLater(() -> rootItem.getChildren().remove(optItem.get()));
		}
	}

	/**
	 * @param smle
	 */
	private void handleShowMailListEvent(ShowMailListEvent smle) {
		MailTreeViewable mtv = 
				accountTree.getSelectionModel().getSelectedItem().getValue();
		// check, if folder still selected
		if (mtv.getFullName().endsWith(smle.getMetaInformation().getOriginalFolder())) {
			if (smle.getData() == null) {
				Platform.runLater(
						() -> folderMailTable.setPlaceholder(new Label("Mails still loading")));
			} else {
				ObservableList<EmailTableData> mailList = 
						FXCollections.observableArrayList(smle.getData());
				mailList.sort(null);
				folderMailTable.setItems(mailList);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		Platform.exit();
	}

	/**
	 * 
	 */
	void setComponentsForNoMailSelected() {
		mailBody.clear();
		btnReply.setDisable(true);
		btnReplyAll.setDisable(true);
		btnDelete.setDisable(true);
	}

	/**
	 * @return
	 */
	ObservableList<MenuItem> getTreeContextMenuItems() {
		return treeContextMenu.getItems();
	}
	
	/**
	 * 
	 */
	void propagateFolderSelected() {
		TreeItem<MailTreeViewable> curItem = 
				accountTree.getSelectionModel().getSelectedItem();
		String curFolder = curItem.getValue().getFullName();
		String account = getAccountName(curItem);
		FolderItemMeta meta = new FolderItemMeta(account, curFolder, FolderItemOrders.SHOW);
		propagateEvent(new RequestFolderItemEvent(this, null, meta));

	}
	
	private static String getAccountName(TreeItem<MailTreeViewable> item) {
		TreeItem<MailTreeViewable> curItem = item;
		while (!curItem.getValue().isAccount())
			curItem = curItem.getParent();
		return curItem.getValue().getFullName();
	}

}
