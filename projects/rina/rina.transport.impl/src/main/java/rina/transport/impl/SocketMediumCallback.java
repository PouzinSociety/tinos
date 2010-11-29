package rina.transport.impl;

import rina.transport.api.TransportEvent;

public interface SocketMediumCallback {
	public void receive(TransportEvent event);
}
