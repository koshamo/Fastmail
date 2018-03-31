/*
 * Copyright (C) 2018  Dr. Jochen Raßler
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

package com.github.koshamo.fastmail.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.koshamo.fastmail.events.FolderTreeEvent;
import com.github.koshamo.fastmail.events.MailAccountMeta;
import com.github.koshamo.fastmail.util.MailTreeViewable;
import com.github.koshamo.fastmail.util.SerializeManager;
import com.github.koshamo.fastmail.util.UnbalancedTree;
import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.EventHandler;
import com.github.koshamo.fiddler.MessageBus;
import com.github.koshamo.fiddler.MessageEvent;

/**
 * @author Dr. Jochen Raßler
 *
 */
public class MailModule implements EventHandler {

	private final MessageBus messageBus;
	private List<MailAccount> accounts;
	
	/**
	 * @param messageBus
	 */
	public MailModule(MessageBus messageBus) {
		this.messageBus = 
				Objects.requireNonNull(messageBus, "messageBus must not be null");
	}

	public void start() {
		List<MailAccountData> accountData = 
				SerializeManager.getInstance().getMailAccounts();
		accounts = new ArrayList<>();
		
		for (MailAccountData mad : accountData)
			accounts.add(new MailAccount(mad, this));
		
		for (MailAccount ma : accounts)
			ma.connect();
		
	}
	
	/*private*/ void postMessage(String message) {
		messageBus.postEvent(new MessageEvent(this, null, message));
	}
	
	/*private*/ <T> void postEvent(MailAccountMeta meta, T data) {
		Event event = null;
		switch (meta.getOrder()) {
		case FOLDERS: 
			event = createFolderTreeEvent(meta, data);
			break;
		default:
			break;
		}
		messageBus.postEvent(event);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Event createFolderTreeEvent(MailAccountMeta meta, T data) {
		UnbalancedTree<MailTreeViewable> folders = null;
		if (data instanceof UnbalancedTree) {
			UnbalancedTree<?> test = (UnbalancedTree<?>) data;
			if (test.getRootItem() instanceof MailTreeViewable)
				folders = (UnbalancedTree<MailTreeViewable>) data;
		}
		if (folders == null)
			throw new IllegalArgumentException("data must be of MailTreeViewable");
		return new FolderTreeEvent(this, null, meta, folders);
		
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(Event event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		for (MailAccount ma : accounts)
			ma.shutdown();
	}

}
