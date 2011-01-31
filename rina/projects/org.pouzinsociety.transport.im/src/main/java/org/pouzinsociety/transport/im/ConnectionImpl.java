/*
 * 2008 - 2010 (c) Waterford Institute of Technology
 *		   TSSG, EU ICT 4WARD
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *   - Organisation Strings updated to reflect fork.
 *
 *
 * Author        : pphelan(at)tssg.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.pouzinsociety.transport.im;

import org.apache.commons.logging.*;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

public class ConnectionImpl implements Connection, ConnectionCreationListener,
ConnectionListener {
	Log log = LogFactory.getLog(ConnectionImpl.class);
	XMPPConnection xmppConnection;
	ConnectionConfiguration xmppConfiguration;
	ConnectionStatus status;
	String server_hostname;
	Integer server_port;
	String login_name;
	String login_password;
	String login_resourceId;
	String subnet_roomname;					// subnet_a@conference.localhost
	String subnet_name;
	MultiUserChat subnet_channel;
	PacketListener listener;

	public ConnectionImpl() {
		XMPPConnection.addConnectionCreationListener(this);
		setStatus(ConnectionStatus.DISCONNECTED);
	}

	protected void finalize() throws Throwable {
			disconnect();
	}

	public void setConfiguration(String server, String port, String username,
			String password, String resourceId, String room) throws NumberFormatException {
		server_hostname = server;
		server_port = Integer.parseInt(port);
		login_name = username;
		login_password = password;
		login_resourceId = resourceId;
		subnet_roomname = room.toLowerCase();
		subnet_name = room.substring(0, room.indexOf("@"));

		log.info("loading configuration");

		xmppConfiguration = new ConnectionConfiguration(server_hostname, server_port.intValue());
		xmppConfiguration.setRosterLoadedAtLogin(false);
		xmppConfiguration.setReconnectionAllowed(true);
		xmppConfiguration.setSendPresence(true);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getName() + '@' + Integer.toHexString(hashCode()) + "\n");
		buffer.append("Server(" + server_hostname + "," + server_port.toString() + ")\n");
		buffer.append("Login(" + login_name + "," + login_password + ")\n");
		buffer.append("Resource(" + login_resourceId + "),Room(" + subnet_roomname + ")\n");
		return buffer.toString();
	}

	private boolean isConnected() {
		if (xmppConnection == null)
			return false;
		return xmppConnection.isConnected();
	}


	public boolean connect(PacketListener listener) throws Exception {
		if (!isConnected()) {
			try {
				xmppConnection = new XMPPConnection(xmppConfiguration);					
				xmppConnection.connect();					
				xmppConnection.addConnectionListener(this);					
				configure();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Cannot connect to IMServer");
			}
		}

		if (xmppConnection.isConnected() && xmppConnection.isAuthenticated() == false) {
			try {
				xmppConnection.login(login_name, login_password, login_resourceId);
				setPresenceOnline();
			} catch(Exception e) {
				log.error(e);
				throw new Exception("Cannot authenticate with IMServer");
			}
		}

		// Join the room
		try {
			subnet_channel = new MultiUserChat(xmppConnection, subnet_roomname);
		} catch (Exception e) {
			log.error(e);
			throw new Exception("Cannot join remote room");
		}
		SmackConfiguration.setPacketReplyTimeout(10000);

		DiscussionHistory history = new DiscussionHistory();
		history.setMaxStanzas(0); // no history please
		subnet_channel.join(login_resourceId, null, history, SmackConfiguration.getPacketReplyTimeout());
		this.listener = listener;
		subnet_channel.addMessageListener(listener);
		// Presence
		//subnet_channel.addParticipantListener(this);			
		return true;
	}

	private void configure() {
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
		.getInstanceFor(xmppConnection);
		if (discoManager == null) {
			discoManager = new ServiceDiscoveryManager(xmppConnection);
		}
		ProviderManager pm = ProviderManager.getInstance();
		try {
			// ==============================================================================
			pm.addIQProvider("query", "jabber:iq:private",
					new PrivateDataManager.PrivateDataIQProvider());
			pm.addIQProvider("query", "jabber:iq:time", Class
					.forName("org.jivesoftware.smackx.packet.Time"));
			pm.addExtensionProvider("x", "jabber:x:roster",
					new RosterExchangeProvider());
			pm.addExtensionProvider("x", "jabber:x:event",
					new MessageEventProvider());
			pm.addExtensionProvider("active",
					"http://jabber.org/protocol/chatstates",
					new ChatStateExtension.Provider());
			pm.addExtensionProvider("composing",
					"http://jabber.org/protocol/chatstates",
					new ChatStateExtension.Provider());
			pm.addExtensionProvider("paused",
					"http://jabber.org/protocol/chatstates",
					new ChatStateExtension.Provider());
			pm.addExtensionProvider("inactive",
					"http://jabber.org/protocol/chatstates",
					new ChatStateExtension.Provider());
			pm.addExtensionProvider("gone",
					"http://jabber.org/protocol/chatstates",
					new ChatStateExtension.Provider());
			pm.addExtensionProvider("html",
					"http://jabber.org/protocol/xhtml-im",
					new XHTMLExtensionProvider());
			pm.addExtensionProvider("x", "jabber:x:conference",
					new GroupChatInvitation.Provider());
			pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
					new DiscoverItemsProvider());
			pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
					new DiscoverInfoProvider());
			pm.addExtensionProvider("x", "jabber:x:data",
					new DataFormProvider());
			pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
					new MUCUserProvider());
			pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
					new MUCAdminProvider());
			pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
					new MUCOwnerProvider());
			pm.addExtensionProvider("x", "jabber:x:delay",
					new DelayInformationProvider());
			pm.addIQProvider("query", "jabber:iq:version", Class
					.forName("org.jivesoftware.smackx.packet.Version"));
			pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
			pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
					new OfflineMessageRequest.Provider());
			pm.addExtensionProvider("offline",
					"http://jabber.org/protocol/offline",
					new OfflineMessageInfo.Provider());
			pm.addIQProvider("query", "jabber:iq:last",
					new LastActivity.Provider());
			pm.addIQProvider("query", "jabber:iq:search",
					new UserSearch.Provider());
			pm.addIQProvider("sharedgroup",
					"http://www.jivesoftware.org/protocol/sharedgroup",
					new SharedGroupsInfo.Provider());
			pm.addExtensionProvider("x", "http://jabber.org/protocol/address",
					new MultipleAddressesProvider());
			pm.addIQProvider("si", "http://jabber.org/protocol/si",
					new StreamInitiationProvider());
			pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
					new BytestreamsProvider());
			pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
					new IBBProviders.Open());
			pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
					new IBBProviders.Close());
			pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
					new IBBProviders.Data());
			pm.addIQProvider("query", "jabber:iq:privacy",
					new org.jivesoftware.smack.provider.PrivacyProvider());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void transmit(byte[] pdu) throws XMPPException {
		Message message = new Message(subnet_roomname, Message.Type.groupchat);
		message.setBody("");
		message.setProperty("PDU", pdu);
		message.setProperty("Node", login_resourceId);
		message.setProperty("Network", subnet_name);
		subnet_channel.sendMessage(message);
	}
	
	public void transmit(Message msg) throws XMPPException {
		subnet_channel.sendMessage(msg);
	}

	private void leaveRoom()  {
		if (subnet_channel != null) {
			subnet_channel.removeMessageListener(listener);
			//subnet_channel.removeParticipantListener(this);
			subnet_channel.leave();
			subnet_channel = null;
		}
	}
	private void setPresenceOnline() {
		if (xmppConnection != null && xmppConnection.isConnected()) {
			Presence presence = new Presence(Presence.Type.available);
			presence.setPacketID("ID5037");
			presence.setMode(Presence.Mode.available);
			presence.setStatus("Online");
			xmppConnection.sendPacket(presence);
		}
	}

	private void setPresenceOffline() {
		if (xmppConnection != null && xmppConnection.isConnected()) {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setPacketID("ID5037");
			presence.setMode(Presence.Mode.away);
			presence.setStatus("Offline");
			xmppConnection.sendPacket(presence);
		}
	}

	public void disconnect() throws Exception {
		if (xmppConnection.isConnected()) {
			try {
				if (subnet_channel != null) {
					leaveRoom();
				}
				setPresenceOffline();			
				xmppConnection.disconnect();
				setStatus(ConnectionStatus.DISCONNECTED);
				XMPPConnection.removeConnectionCreationListener(this);
			} catch (Exception e) {
				log.error(e);
				throw new Exception("Cannot disconnect from IMServer");
			}
		}
	}

	public void connectionCreated(XMPPConnection arg0) {
		setStatus(ConnectionStatus.CONNECTED);
	}

	public void connectionClosed() {
		setStatus(ConnectionStatus.DISCONNECTED);		
	}

	public void connectionClosedOnError(Exception arg0) {
		setStatus(ConnectionStatus.DISCONNECTED);
	}

	public void reconnectingIn(int arg0) {
		setStatus(ConnectionStatus.CONNECTING);
	}

	public void reconnectionFailed(Exception arg0) {
		setStatus(ConnectionStatus.DISCONNECTED);
	}

	public void reconnectionSuccessful() {
		setStatus(ConnectionStatus.CONNECTED);
	}

	private void setStatus(final ConnectionStatus status) {
		this.status = status;
	}
}
