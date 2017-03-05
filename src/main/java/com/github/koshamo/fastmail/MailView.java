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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
		saveAsBtn = new Button("save as");
		saveAsBtn.setDisable(true);
		saveAllBtn = new Button("save all");
		saveAllBtn.setDisable(true);
		saveAsBtn.setOnAction(ev -> {
			int item = attachmentBox.getSelectionModel().getSelectedIndex();
			AttachmentData[] attachData = data.getAttachments();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save attachment as ...");
			fileChooser.setInitialDirectory(
					new File(System.getProperty("user.home")));
			fileChooser.setInitialFileName(attachData[item].getFileName());
			File outputFile = fileChooser.showSaveDialog(getScene().getWindow());
			if (outputFile == null)
				return;
			try (InputStream is = (data.getAttachments())[item].getInputStream()) {
				if (is == null)
					return;
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
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
	public void setContent(MailData data) {
		clear();
		this.data = data;
		from.setText(data.getFrom());
		subject.setText(data.getSubject());
		StringBuilder sb = new StringBuilder();
		for (String s : data.getTo())
			sb.append(s).append("; "); //$NON-NLS-1$
		to.setText(sb.toString());
		sb = new StringBuilder();
		if (data.getCc() != null) {
			for (String s : data.getCc())
				sb.append(s).append("; "); //$NON-NLS-1$
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
	
	/**
	 * Get the actual mail text
	 * @return	the mail text as String
	 */
	public String getMailText() {
		return mailBody.getText();
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
