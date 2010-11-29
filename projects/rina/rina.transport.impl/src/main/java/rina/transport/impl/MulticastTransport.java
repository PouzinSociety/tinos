package rina.transport.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import jnode.net.DatagramSocket;
import jnode.net.DatagramSocketImplFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import rina.config.dao.MulticastTransportConnectionDao;
import rina.config.dao.MulticastTransportDao;
import rina.transport.api.AbstractTransport;
import rina.transport.api.TransportConstants;
import rina.transport.api.TransportEvent;


public class MulticastTransport extends AbstractTransport implements Runnable, BootStrapCompleteAPI, SocketMediumCallback  { 
	private static final Log log = LogFactory.getLog(MulticastTransport.class);
	private TransportLayer udpTransport;
	private MulticastTransportDao dao;
	HashMap<String, SocketMedium> knownDomains;
	SocketMedium external;

	public MulticastTransport() {
		super("MULTICAST");
		knownDomains = new HashMap<String, SocketMedium>();
		external = null;
	}
	
	public void setUdpTransport(TransportLayer udpTransport) {
		this.udpTransport = udpTransport;
	}

	@Override
	public void process(TransportEvent event) throws Exception {
		String destination = event.getKeyValue(TransportConstants.KEY_DEST_DOMAIN);
		if (destination == null) {
			// Default to our own domain
			destination = dao.getDomain();
			event.setKeyValue(TransportConstants.KEY_DEST_DOMAIN, dao.getDomain());
		}

		
		if (destination.equals(TransportConstants.DEST_DOMAIN_EXTERNAL)) {
			if (external != null) {
				external.write(event);
				return;
			}
			throw new Exception("No External Connection Available");
		}
		
		if (destination.equals(TransportConstants.DEST_DOMAIN_LOOPBACK)) {
			String timestamp = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date());			
			event.setKeyValue(TransportConstants.KEY_SRC_DOMAIN, TransportConstants.DEST_DOMAIN_LOOPBACK);
			event.setKeyValue(TransportConstants.KEY_TX_DOMAIN, dao.getDomain());
			event.setKeyValue(TransportConstants.KEY_RX_NODE, dao.getNode());
			event.setKeyValue(TransportConstants.KEY_TX_NODE, dao.getNode());
			event.setKeyValue(TransportConstants.KEY_RX_TIME, timestamp);
			event.setKeyValue(TransportConstants.KEY_TX_TIME, timestamp);
			postEvent(event);
			return;
		}
		
		
		log.info("Looking for Domain : " + destination);
		SocketMedium domainConnection = knownDomains.get(destination);
		if (domainConnection != null) {
			domainConnection.write(event);
			return;
		}
		throw new Exception("No Connection for Domain (" + destination + ")");
	}

	public void bootstrapComplete(Object cfgDao) throws BootstrapException {
		log.info("bootstrapComplete(Transport:" + this.getTransportName() + ")");		
		try {
			dao = (MulticastTransportDao)cfgDao;
			log.info("bootStrapComplete(" + dao.toString() +")");			
			Thread.sleep(1000);
			
			DatagramSocketImplFactory sFactory = udpTransport.getDatagramSocketImplFactory();
	    	DatagramSocket.setDatagramSocketImplFactory(sFactory);
	    	
	    	
			new Thread(this).start();
		} catch(Exception e) {
			log.error("Exception: " + e.getMessage());
			log.error(e);
		}
	}

	public String getConfigDaoClassName() {
		return MulticastTransportDao.class.getName();
	}

	public void run(){
		log.info(this.getClass().toString() + " : Started ");
		
		List<MulticastTransportConnectionDao> list = dao.getMediumList();
		SocketMedium connection;
		
		for (int i = 0; i < list.size(); i++) {
			MulticastTransportConnectionDao medium = list.get(i);
			try  {
				connection = new SocketMedium(medium, this);
				if (medium.getDomain().equals("External")) {
					external = connection;
				} else {
					knownDomains.put(medium.getDomain(), connection);
				}
				log.info("Adding Connection : " + medium.getDomain());
			} catch (Exception e) {
				log.error("Failed to Open Connection : " + medium.toString());
				log.error("Exception : " + e.getMessage());
				log.error(e);
			}
		}
		// Start the Transmit Processor
		startTxThread();
	}

	public void receive(TransportEvent event) {
			log.info("MulticastTransportRx: " + event.toString());
			postEvent(event);
	}
}
