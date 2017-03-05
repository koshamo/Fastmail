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

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
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

/**
 * @author jochen
 *
 */
public class MailView extends StackPane {

	public MailView() {
		VBox vbox = new VBox();
		
		HBox infoBox = new HBox();
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
		attachments = new VBox();
		infoBox.getChildren().addAll(mailHeader, attachments);

		ScrollPane infoScroller = new ScrollPane(infoBox);
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
	
	public void clear() {
		from.setText(null);
		subject.setText(null);
		to.setText(null);
		cc.setText(null);
		mailHeader.getChildren().removeAll(ccLbl, cc);
		mailBody.setText(null);
	}

	public void setContent(MailData data) {
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
		mailBody.setText(data.getContent());
	}
	
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
	private GridPane mailHeader;
	private VBox attachments;
}
