/*
 * Copyright (C) 2017  Dr. Jochen Raﬂler
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.mail.Folder;

import com.github.koshamo.fastmail.mail.AttachmentData;
import com.github.koshamo.fastmail.mail.MailData;
import com.github.koshamo.fastmail.util.MessageItem;
import com.github.koshamo.fastmail.util.MessageMarket;
import com.github.koshamo.fastmail.util.SerializeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * This class provides a special widget to show the mail content 
 * in a typical manner.
 * <p>
 * This class provides a typical view to the mail content, including 
 * all addresses, attachments that can be stored and the actual mail
 * text.
 *  
 * @author jochen
 *
 */
public class MailView extends StackPane {

	/**
	 * The constructor that builds the GUI component
	 */
	public MailView() {
		i18n = SerializeManager.getLocaleMessages();
		VBox vbox = new VBox();
		
		Node infoScroller = buildInfoPanel();
		Node bodyScroller = buildMailBodyPanel();
		SplitPane splitter = new SplitPane(infoScroller, bodyScroller);
		splitter.setOrientation(Orientation.VERTICAL);
		splitter.setDividerPositions(0.2);

		vbox.getChildren().add(splitter);
		VBox.setVgrow(splitter, Priority.ALWAYS);
		getChildren().add(vbox);
	}
	
	/**
	 * this method builds the header panel shown on top of each mail,
	 * containing the actual header data as well as the attachment
	 * information
	 * 
	 * @return the panel including all header data
	 */
	private Node buildInfoPanel() {
		buildHeaderInfoPanel();
		Node attachmentPane = buildAttachmentPanel();

		AnchorPane anchorPane = new AnchorPane();
		AnchorPane.setTopAnchor(mailHeader, Double.valueOf(1.0));
		AnchorPane.setLeftAnchor(mailHeader, Double.valueOf(1.0));
		AnchorPane.setBottomAnchor(mailHeader, Double.valueOf(1.0));
		AnchorPane.setTopAnchor(attachmentPane, Double.valueOf(1.0));
		AnchorPane.setRightAnchor(attachmentPane, Double.valueOf(1.0));
		AnchorPane.setBottomAnchor(attachmentPane, Double.valueOf(1.0));
		anchorPane.getChildren().addAll(mailHeader, attachmentPane);
		
		ScrollPane infoScroller = new ScrollPane(anchorPane);
		infoScroller.setFitToWidth(true);
		return infoScroller;
	}
	
	/**
	 * this method builds the panel showing the relevant mail data,
	 * including from, subject, to, and cc 
	 */
	private void buildHeaderInfoPanel() {
		mailHeader = new GridPane();
		mailHeader.setHgap(20);
		ColumnConstraints colLabel = new ColumnConstraints(80);
		colLabel.setHgrow(Priority.NEVER);
		colLabel.setHalignment(HPos.RIGHT);
		ColumnConstraints colField = new ColumnConstraints();
		colField.setHgrow(Priority.ALWAYS);
		mailHeader.getColumnConstraints().addAll(colLabel, colField);

		Label fromLbl = new Label(i18n.getString("entry.from")); //$NON-NLS-1$
		from = new Label();
		from.setWrapText(true);
		Label subjectLbl = new Label(i18n.getString("entry.subject")); //$NON-NLS-1$
		subject = new Label();
		subject.setWrapText(true);
		Label toLbl = new Label(i18n.getString("entry.to")); //$NON-NLS-1$
		to = new Label();
		to.setWrapText(true);
		ccLbl = new Label(i18n.getString("entry.cc")); //$NON-NLS-1$
		cc = new Label();
		cc.setWrapText(true);
		mailHeader.add(fromLbl, 0, 0);
		GridPane.setValignment(fromLbl, VPos.TOP);
		mailHeader.add(from, 1, 0);
		mailHeader.add(subjectLbl, 0, 1);
		GridPane.setValignment(subjectLbl, VPos.TOP);
		mailHeader.add(subject, 1, 1);
		mailHeader.add(toLbl, 0, 2);
		GridPane.setValignment(toLbl, VPos.TOP);
		mailHeader.add(to, 1, 2);
	}
	
	
	/**
	 * this method builds the attachment access for the header panel
	 * 
	 * @return returns the local attachment widget
	 */
	private Node buildAttachmentPanel() {
		attachmentPane = new VBox();
		attachmentLbl = new Label();
		attachments = FXCollections.observableArrayList();
		attachmentBox = new ChoiceBox<>(attachments);
		HBox btnBox = new HBox();
		// SAVE AS button
		saveAsBtn = new Button(i18n.getString("action.saveas")); //$NON-NLS-1$
		saveAsBtn.setDisable(true);
		saveAsBtn.setOnAction(new SaveAsEventHandler());
		// SAVE ALL button
		saveAllBtn = new Button(i18n.getString("action.saveall")); //$NON-NLS-1$
		saveAllBtn.setDisable(true);
		saveAllBtn.setOnAction(new SaveAllEventHandler());
		btnBox.getChildren().addAll(saveAsBtn, saveAllBtn);
		attachmentPane.getChildren().addAll(attachmentLbl, attachmentBox, btnBox);
		return attachmentPane;
	}
	
	
	/**
	 * this method builds the actual mailbody, which is the viewing
	 * widget within a scroll panel
	 * @return
	 */
	private Node buildMailBodyPanel() {
		mailBody = new TextArea();
		mailBody.setEditable(false);
		mailBody.setWrapText(true);
		ScrollPane bodyScroller = new ScrollPane(mailBody);
		bodyScroller.setFitToHeight(true);
		bodyScroller.setFitToWidth(true);
		return bodyScroller;
	}
	
	
	/**
	 * To unset the widgets content, you may call this method
	 */
	public void clear() {
		from.setText(null);
		subject.setText(null);
		to.setText(null);
		cc.setText(null);
		mailHeader.getChildren().removeAll(ccLbl, cc);
		mailBody.setText(null);
		attachmentLbl.setText(null);
		attachments.clear();
		saveAsBtn.setDisable(true);
		saveAllBtn.setDisable(true);
		this.data = null;
	}

	/**
	 * Call this method to fill the widgets content
	 * @param data	the data object containing all relevant mail data
	 */
	public void setContent(final MailData data) {
		clear();
		this.data = data;
		// set attachments first, to get current data for header label width
		setAttachmentContent(data);
		// set from
		setFromContent(data);
		// set subject
		subject.setText(data.getSubject());
		// set to
		setToContent(data);
		// set cc
		setCcContent(data);
		// set body
		mailBody.setText(data.getContent());
	}
	
	
	/*
	 * ****************************************************
	 * 					PRIVATE
	 * ****************************************************
	 */
	
	private VBox attachmentPane;
	private TextArea mailBody;
	private Label from;
	private Label subject;
	private Label to;
	private Label ccLbl;
	private Label cc;
	private Label attachmentLbl;
	ChoiceBox<String> attachmentBox;
	private Button saveAsBtn;
	private Button saveAllBtn;
	private GridPane mailHeader;
	private ObservableList<String> attachments;
	MailData data;
	
	final ResourceBundle i18n;
	
	/**
	 * fill attachmentPane with mails content
	 * 
	 * @param data the current mail to display
	 */
	private void setAttachmentContent(final MailData data) {
		int attachCnt;
		if (data.getAttachments() != null) {
			attachCnt = data.getAttachments().length;
			if (attachCnt > 0) {
				attachmentLbl.setText(attachCnt + " attachments"); //$NON-NLS-1$
				for (AttachmentData ad : data.getAttachments())
					attachments.add(ad.getFileName() + " (" + (ad.getSize() / 1024) + "kb)"); //$NON-NLS-1$ //$NON-NLS-2$
				attachmentBox.setValue(attachments.get(0));
				saveAsBtn.setDisable(false);
				if (attachCnt > 1)
					saveAllBtn.setDisable(false);
			}
		} else {
			attachmentLbl.setText("No attachments"); //$NON-NLS-1$
		}
		// layout the attachment widget and set the max size for 
		// the header info panel
		layout();
		setHeaderWidht();
	}
	
	/**
	 *  fill to Label with mails content
	 *  
	 * @param data the current mail to display
	 */
	private void setToContent(final MailData data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.getTo().length; i++ ) {
			if (data.getToName()[i] != null)
				sb.append(data.getToName()[i]).append(" <") //$NON-NLS-1$
				.append(data.getTo()[i]).append(">"); //$NON-NLS-1$
			else
				sb.append(data.getTo()[i]);
			sb.append("; "); //$NON-NLS-1$
		}
		to.setText(sb.toString());
	}
	
	/**
	 * fill cc Label with mails content
	 * @param data the current mail to display
	 */
	private void setCcContent(final MailData data) {
		StringBuilder sb = new StringBuilder();
		if (data.getCc() != null) {
			for (int i = 0; i < data.getCc().length; i++) {
				if (data.getCcName()[i] != null)
					sb.append(data.getCcName()[i]).append(" <") //$NON-NLS-1$
					.append(data.getCc()[i]).append(">"); //$NON-NLS-1$
				else
					sb.append(data.getCc()[i]);
				sb.append("; "); //$NON-NLS-1$
			}
			mailHeader.add(ccLbl, 0, 3);
			GridPane.setValignment(ccLbl, VPos.TOP);
			mailHeader.add(cc, 1, 3);
		}
		cc.setText(sb.toString());
	}
	
	/**
	 * fill from Label with mails content 
	 * 
	 * @param data the current mail to display
	 */
	private void setFromContent(final MailData data) {
		if (data.getFromName() != null)
			from.setText(data.getFromName() + " <" + data.getFrom() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		else
			from.setText(data.getFrom());
	}
	
	/**
	 * calculate the width for header info panel and set it
	 */
	private void setHeaderWidht() {
		double width = getWidth() - attachmentPane.getWidth() - 20;
		mailHeader.setMaxWidth(width);
	}
	
	
	/*package private*/ 
	final class SaveAsEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			int item = attachmentBox.getSelectionModel().getSelectedIndex();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(i18n.getString("dialog.title.saveas")); //$NON-NLS-1$
			fileChooser.setInitialDirectory(
					new File(System.getProperty("user.home"))); //$NON-NLS-1$
			fileChooser.setInitialFileName(data.getAttachments()[item].getFileName());
			File outputFile = fileChooser.showSaveDialog(getScene().getWindow());
			if (outputFile == null)
				return;
			Thread t = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// wrong warning from eclipse, ignore it
					InputStream is = (data.getAttachments())[item].getInputStream();
					if (!data.getMessage().getFolder().isOpen())
						data.getMessage().getFolder().open(Folder.READ_WRITE);
					if (is == null)
						return null;
					// Message status
					MessageItem mItem = new MessageItem(
							MessageFormat.format(i18n.getString("entry.saveattachment"), outputFile.getName()),  //$NON-NLS-1$
							0.0, MessageItem.MessageType.PROGRESS);
					MessageMarket.getInstance().produceMessage(mItem);
					int fileSize = (data.getAttachments())[item].getSize();
					int counter = 0; 
					int cur = 0;
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) { 
						byte[] pipe = new byte[1000];
						while ((cur = is.read(pipe)) > 0) {
							counter += cur;
							bos.write(pipe);
							mItem.updateProgress((double) counter / fileSize);
						}
					} catch (FileNotFoundException e) {
						mItem.done();
						mItem = new MessageItem(
								MessageFormat.format(i18n.getString("exception.savefile"), e.getMessage()) //$NON-NLS-1$
								, 0.0, MessageItem.MessageType.EXCEPTION);
						MessageMarket.getInstance().produceMessage(mItem);
					}catch (IOException e) {
						mItem.done();
						mItem = new MessageItem(
								MessageFormat.format(i18n.getString("exception.saveattachment"), e.getMessage()) //$NON-NLS-1$
								, 0.0, MessageItem.MessageType.EXCEPTION);
						MessageMarket.getInstance().produceMessage(mItem);
					}
					mItem.done();
					is.close();
					data.getMessage().getFolder().close(true);
					return null;
				}
			});
			t.start();
		}
	}
	
	/* package private*/
	final class SaveAllEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle(i18n.getString("dialog.title.saveall")); //$NON-NLS-1$
			directoryChooser.setInitialDirectory(
					new File(System.getProperty("user.home"))); //$NON-NLS-1$
			File outputDir = directoryChooser.showDialog(getScene().getWindow());
			if (outputDir == null)
				return;
			Thread t = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// Message status
					final MessageItem mItem = 
							new MessageItem(
									i18n.getString("entry.initial.saveallattachment"),  //$NON-NLS-1$
									0.0, MessageItem.MessageType.PROGRESS);
					MessageMarket.getInstance().produceMessage(mItem);
					int totalFileSize = 0;
					int counter = 0; 
					int cur = 0;
					for (AttachmentData ad : data.getAttachments())
						totalFileSize += ad.getSize();
					for (int i = 0; i < data.getAttachments().length; i++) {
						// wrong warning from eclipse, ignore it
						InputStream is = (data.getAttachments())[i].getInputStream();
						if (!data.getMessage().getFolder().isOpen())
							data.getMessage().getFolder().open(Folder.READ_WRITE);
						if (is == null)
							return null;
						final int aCnt = i;	// final variable for inner class
						Platform.runLater(()-> {
							mItem.updateMessage(MessageFormat.format(
									i18n.getString("entry.saveallattachment"),  //$NON-NLS-1$
									(data.getAttachments())[aCnt].getFileName(), 
									Integer.valueOf(aCnt), 
									Integer.valueOf(data.getAttachments().length)));
						});
						try (BufferedOutputStream bos = new BufferedOutputStream(
								new FileOutputStream(
										new File(outputDir.toString() + File.separator + 
												data.getAttachments()[i].getFileName())))) {
							byte[] pipe = new byte[1000];
							while ((cur = is.read(pipe)) > 0) {
								counter += cur;
								bos.write(pipe);
								mItem.updateProgress((double) counter / totalFileSize);
							}
						} catch (FileNotFoundException e) {
							mItem.done();
							MessageItem meItem = new MessageItem(
									MessageFormat.format(i18n.getString("exception.savefile"), e.getMessage()) //$NON-NLS-1$
									, 0.0, MessageItem.MessageType.EXCEPTION);
							MessageMarket.getInstance().produceMessage(meItem);
						} catch (IOException e) {
							mItem.done();
							MessageItem meItem = new MessageItem(
									MessageFormat.format(i18n.getString("exception.saveattachment"), e.getMessage()) //$NON-NLS-1$
									, 0.0, MessageItem.MessageType.EXCEPTION);
							MessageMarket.getInstance().produceMessage(meItem);
						}
						mItem.done();
						is.close();
						data.getMessage().getFolder().close(true);
					}
					return null;
				}
			});
			t.start();
		}
	}
	
}
