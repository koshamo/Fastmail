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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.github.koshamo.fastmail.mail.AttachmentData;
import com.github.koshamo.fastmail.mail.MailData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
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
		VBox vbox = new VBox();
		
		mailHeader = new GridPane();
		mailHeader.setHgap(20);
		ColumnConstraints colLabel = new ColumnConstraints(80);
		colLabel.setHgrow(Priority.NEVER);
		colLabel.setHalignment(HPos.RIGHT);
		ColumnConstraints colField = new ColumnConstraints();
		colField.setHgrow(Priority.ALWAYS);
		mailHeader.getColumnConstraints().addAll(colLabel, colField);

		Label fromLbl = new Label("From: ");
		from = new Label();
		Label subjectLbl = new Label("Subject: ");
		subject = new Label();
		Label toLbl = new Label("To: ");
		to = new Label();
		ccLbl = new Label("CC: ");
		cc = new Label();
		mailHeader.add(fromLbl, 0, 0);
		mailHeader.add(from, 1, 0);
		mailHeader.add(subjectLbl, 0, 1);
		mailHeader.add(subject, 1, 1);
		mailHeader.add(toLbl, 0, 2);
		mailHeader.add(to, 1, 2);
		attachmentPane = new VBox();
		attachmentLbl = new Label();
		attachments = FXCollections.observableArrayList();
		attachmentBox = new ChoiceBox<String>(attachments);
		HBox btnBox = new HBox();
		// SAVE AS button
		saveAsBtn = new Button("save as");
		saveAsBtn.setDisable(true);
		saveAsBtn.setOnAction(ev -> {
			int item = attachmentBox.getSelectionModel().getSelectedIndex();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save attachment as ...");
			fileChooser.setInitialDirectory(
					new File(System.getProperty("user.home")));
			fileChooser.setInitialFileName(data.getAttachments()[item].getFileName());
			File outputFile = fileChooser.showSaveDialog(getScene().getWindow());
			if (outputFile == null)
				return;
			Thread t = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try (InputStream is = (data.getAttachments())[item].getInputStream()) {
						if (!data.getMessage().getFolder().isOpen())
							data.getMessage().getFolder().open(Folder.READ_WRITE);
						if (is == null)
							return null;
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
						byte[] pipe = new byte[1000];
						while (is.read(pipe) > 0)
							bos.write(pipe);
						bos.close();
						data.getMessage().getFolder().close(true);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			});
			t.start();
		});
		// SAVE ALL button
		saveAllBtn = new Button("save all");
		saveAllBtn.setDisable(true);
		saveAllBtn.setOnAction(ev -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Save all attachments ...");
			directoryChooser.setInitialDirectory(
					new File(System.getProperty("user.home")));
			File outputDir = directoryChooser.showDialog(getScene().getWindow());
			if (outputDir == null)
				return;
			Thread t = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					for (int i = 0; i < data.getAttachments().length; i++) {
						try (InputStream is = (data.getAttachments())[i].getInputStream()) {
							if (!data.getMessage().getFolder().isOpen())
								data.getMessage().getFolder().open(Folder.READ_WRITE);
							if (is == null)
								return null;
							BufferedOutputStream bos = new BufferedOutputStream(
									new FileOutputStream(
											new File(outputDir.toString() + File.separator + 
													data.getAttachments()[i].getFileName())));
							byte[] pipe = new byte[1000];
							while (is.read(pipe) > 0)
								bos.write(pipe);
							bos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return null;
				};
			});
			t.start();
		});
		btnBox.getChildren().addAll(saveAsBtn, saveAllBtn);
		attachmentPane.getChildren().addAll(attachmentLbl, attachmentBox, btnBox);
		
		AnchorPane anchorPane = new AnchorPane();
		AnchorPane.setTopAnchor(mailHeader, 1.0);
		AnchorPane.setLeftAnchor(mailHeader, 1.0);
		AnchorPane.setBottomAnchor(mailHeader, 1.0);
		AnchorPane.setTopAnchor(attachmentPane, 1.0);
		AnchorPane.setRightAnchor(attachmentPane, 1.0);
		AnchorPane.setBottomAnchor(attachmentPane, 1.0);
		anchorPane.getChildren().addAll(mailHeader, attachmentPane);

		ScrollPane infoScroller = new ScrollPane(anchorPane);
		infoScroller.setFitToWidth(true);

		mailBody = new TextArea();
		mailBody.setEditable(false);
		mailBody.setWrapText(true);
		ScrollPane bodyScroller = new ScrollPane(mailBody);
		bodyScroller.setFitToHeight(true);
		bodyScroller.setFitToWidth(true);
		SplitPane splitter = new SplitPane(infoScroller, bodyScroller);
		splitter.setOrientation(Orientation.VERTICAL);
		splitter.setDividerPositions(0.2);
		vbox.getChildren().add(splitter);
		VBox.setVgrow(splitter, Priority.ALWAYS);
		getChildren().add(vbox);
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
		if (data.getFromName() != null)
			from.setText(data.getFromName());
		else
			from.setText(data.getFrom());
		subject.setText(data.getSubject());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.getTo().length; i++ ) {
			if (data.getToName()[i] != null)
				sb.append(data.getToName()[i]);
			else
				sb.append(data.getTo()[i]);
			sb.append("; "); //$NON-NLS-1$
		}
		to.setText(sb.toString());
		sb = new StringBuilder();
		if (data.getCc() != null) {
			for (int i = 0; i < data.getCc().length; i++) {
				if (data.getCcName()[i] != null)
					sb.append(data.getCcName()[i]);
				else
					sb.append(data.getCc()[i]);
				sb.append("; "); //$NON-NLS-1$
			}
			mailHeader.add(ccLbl, 0, 3);
			mailHeader.add(cc, 1, 3);
		}
		cc.setText(sb.toString());
		int attachCnt;
		if (data.getAttachments() != null) {
			attachCnt = data.getAttachments().length;
			if (attachCnt > 0) {
				attachmentLbl.setText(attachCnt + " attachments");
				for (AttachmentData ad : data.getAttachments())
					attachments.add(ad.getFileName() + " (" + (ad.getSize() / 1024) + "kb)"); //$NON-NLS-1$ //$NON-NLS-2$
				attachmentBox.setValue(attachments.get(0));
				saveAsBtn.setDisable(false);
				if (attachCnt > 1)
					saveAllBtn.setDisable(false);
			}
		} else {
			attachmentLbl.setText("No attachments");
		}
		mailBody.setText(data.getContent());
	}
	
	
	/*
	 * ****************************************************
	 * 					PRIVATE
	 * ****************************************************
	 */
	
	private TextArea mailBody;
	private Label from;
	private Label subject;
	private Label to;
	private Label ccLbl;
	private Label cc;
	private Label attachmentLbl;
	private ChoiceBox<String> attachmentBox;
	private Button saveAsBtn;
	private Button saveAllBtn;
	private GridPane mailHeader;
	private VBox attachmentPane;
	private ObservableList<String> attachments;
	private MailData data;
	
}
