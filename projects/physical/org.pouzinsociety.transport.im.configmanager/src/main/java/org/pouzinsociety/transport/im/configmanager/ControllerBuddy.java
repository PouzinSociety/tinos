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
package org.pouzinsociety.transport.im.configmanager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;
import org.apache.commons.logging.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.pouzinsociety.bootstrap.api.BootstrapConstants;
import org.pouzinsociety.bootstrap.api.BootstrapEvent;
import org.pouzinsociety.config.dao.IMDao;
import org.pouzinsociety.transport.im.ConnectionImpl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.GraphMLReader;


public class ControllerBuddy implements PacketListener, BundleContextAware {
	Log log = LogFactory.getLog(ControllerBuddy.class);
	final String CONFIGURATION_MODE_SPECIFIER = "Configuration-Assignment";
	final String DYNAMIC_MODE = "dynamic";
	final String CONFIGURATION_FILE_SPECIFIER = "Configuration-File";
	final String CONFIGURATION_MAPPING_KEY = "name";

	private BundleContext bundleContext;
	boolean configurationModeDynamic = true;
	boolean configurationLoaded = false;
	IMDao imConnectionDetails;
	ConnectionImpl medium;

	// Name Mapping to Configuration Nodes (Name Assigned)
	HashMap<String, HashMap<String, String>> nodeNameMap = new HashMap<String, HashMap<String,String>>();
	// Array of Configuration Nodes (Dynamic Mode) - Names for the entries in the nodeNameMap
	List<String> nodeArray = new ArrayList<String>();
	// Assigned Nodes ("requester", nodeArray Idx)
	HashMap<String,Integer> nodeNames = new HashMap<String,Integer>();
	

	int indexCounter = 0;

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		Bundle bundle = this.bundleContext.getBundle();
		try {
			String requestOpMode =
				(String)bundle.getHeaders().get(CONFIGURATION_MODE_SPECIFIER);
			if (requestOpMode != null) {
				if (!requestOpMode.isEmpty()) {
					configurationModeDynamic =
						requestOpMode.equals(DYNAMIC_MODE);
				}
			}
		} catch (Exception e) {
			configurationModeDynamic = true;
			log.error("Cannot determine configuration mode - default dynamic");
			log.error(e);

		}

		try {
			String configurationFileLocation =
				(String)bundle.getHeaders().get(CONFIGURATION_FILE_SPECIFIER);
			loadConfiguration(configurationFileLocation);
			configurationLoaded = true;
		} catch (Exception e) {
			configurationLoaded= false;
			log.error("Cannot load configuration file");
			log.error(e);
		}
		
		log.info("ConfigurationMode : " + 
				((configurationModeDynamic == true) ? "Dynamic" : "NameAssigned"));
	}

	public void loadConfiguration(String configFileLocation) throws Exception {
		Graph configGraph;
		log.info("Loading Configuration File : " + configFileLocation);
		try {
			configGraph = new GraphMLReader().readGraph(configFileLocation);
		} catch(Exception e) {
			log.error("Exception : " + e.getMessage());
			log.error("Unable to load configuration : " + configFileLocation);
			throw e;
		}
		log.info("Configuration Graph : Nodes (" + configGraph.getNodeCount() +
				"), Edges(" + configGraph.getEdgeCount() + ")");
		
		HashMap<String, String> node;
		Table t = configGraph.getNodeTable();		
		int rowCount = t.getRowCount();
		Tuple row;
		for (int idx = 0; idx < rowCount; idx++) {
			row = t.getTuple(idx);
			int columnCount = row.getColumnCount();
			node = new HashMap<String, String>();
			for (int i = 0; i < columnCount; ++i)
				node.put(row.getColumnName(i).toLowerCase(), (String)row.get(i));	
			if (node.containsKey(CONFIGURATION_MAPPING_KEY)) {
					String mappingValue = node.get(CONFIGURATION_MAPPING_KEY);
					if (mappingValue == null)
						continue;
					if (mappingValue.isEmpty())
						continue;
					nodeNameMap.put(node.get(CONFIGURATION_MAPPING_KEY), node);
					nodeArray.add(node.get(CONFIGURATION_MAPPING_KEY));
			}
		}
		log.info("Configuration Loaded: ConfigNodes (" + nodeArray.size() + ")");
		log.info("NodeNames : " + nodeNameMap.keySet().toString());
	}

	public ControllerBuddy(IMDao imConfig) throws Exception {
		imConnectionDetails = imConfig;
		medium = new ConnectionImpl();

		medium.setConfiguration(imConnectionDetails.getIm_server(),
				imConnectionDetails.getIm_port(),
				imConnectionDetails.getIm_buddyId(),
				imConnectionDetails.getIm_buddyPassword(),
				imConnectionDetails.getIm_resourceId(),
				imConnectionDetails.getIm_chatroom());
		medium.connect(this);
		log.info("Connected");
	}

	protected void finalize() throws Throwable {
		medium.disconnect();
	}


	public void processPacket(Packet packet) {
		String id = (String)packet.getProperty("BootStrap");
		if (id != null) {
			log.info("Incoming Bootstrap Event");
			BootstrapEvent event = new BootstrapEvent();
			StringBuffer buf = new StringBuffer();
			Collection<String> propertyNames = packet.getPropertyNames();
			for (Iterator<String> iter = propertyNames.iterator(); iter.hasNext();) {
				String propName = (String)iter.next();
				if (propName.equals("BootStrap")) {
					continue;
				}
				event.setKeyValue(propName, (String)packet.getProperty(propName));
				buf.append("Key(" + propName + ") V(" + (String)packet.getProperty(propName)  + ")");
			}
			log.info("Event: " + buf.toString());
			processBootstrapEvent(event);
		}
	}

	private void processBootstrapEvent(BootstrapEvent event) {
		log.info("processBootstrapEvent: (" + event.getEventId() + ")");
		
		if (configurationLoaded == false) {
			log.error("Cannot process incoming request: No Configuration Loaded");
			return;
		}
		
		
		if (event.getEventId() == BootstrapConstants.CONFIG_REQUEST) {
			
			// Requesting Node
			String requester = event.getKeyValue("src");
			// Node Name ?
			String requestedConfigName = event.getKeyValue("node");
			String configName = null;
			// Has this requester already got a configuration ?
			Integer index = nodeNames.get(requester);
			HashMap<String,String> node = null;
			if (index == null) {
				// Nope - then lets find a configuration for this request.
				synchronized (this) {
					if (configurationModeDynamic == true) {
						if (requestedConfigName != null) 
							log.info("Configuration Name specified is ignored in Dynamic Mode");
						if (indexCounter < nodeArray.size()) {
							configName = nodeArray.get(indexCounter);
							nodeNames.put(requester, Integer.valueOf(indexCounter));
							indexCounter++;
						}
					} else { // Assign by requested node-name
						if (requestedConfigName == null) {
							log.error("In NameAssigned configuration request mode - no node" +
									" name specified by requester : " + requester);
						} else {
							if (requestedConfigName.isEmpty()) {
								log.error("In NameAssigned configuration request mode - no node" +
										" name specified by requester : " + requester);
							} else {
								log.info("Configuration Request for NodeName : " + requestedConfigName);
								if (nodeNameMap.containsKey(requestedConfigName) == false) {
									log.error("NameAssigned configuration request mode - no configuration node" +
											" for the name (" + requestedConfigName + ") specified by requester : " +
											requester);
								} else {
									configName = requestedConfigName;
									Integer idx = nodeArray.indexOf(configName);
									if (nodeNames.containsValue(idx) == true) {
										log.warn("!!WARNING!! - NameAssigned Mode : Configuration already assigned to another requester," +
												" you might not want this");
									}
									nodeNames.put(requester, idx);
								}
								
							}
						}
					}
				}
			} else {
				// Yes, then send the same again
				configName = nodeArray.get(index);
			}
			
			if (configName != null)
				node = nodeNameMap.get(configName);

			if (node == null) {
				log.error("No Configuration available for this request.");
				return;
			}

			BootstrapEvent response = new BootstrapEvent();
			response.setEventId(BootstrapConstants.CONFIG_RESPONSE);
			response.setKeyValue("src", imConnectionDetails.getIm_resourceId());
			response.setKeyValue("dest", requester);
			response.setKeyValue("LocalTime", getDateTime());
			for (String key : node.keySet())
				response.setKeyValue(key, node.get(key));
			sendBootstrapEvent(response);
		}

	}

	private void sendBootstrapEvent(BootstrapEvent event) {
		Message message = new Message(imConnectionDetails.getIm_chatroom(),
				Message.Type.groupchat);
		message.setBody("EventId(" + event.getEventId() + ") -> " + event.getKeyValue("dest"));
		StringBuffer buf = new StringBuffer();
		buf.append("Body: " + message.getBody() + "\n");
		for (String key : event.keySet()) {
			buf.append("Key(" + key + ")\nValue(" + event.getKeyValue(key) + ")\n");
			message.setProperty(key, event.getKeyValue(key));
		}
		log.info("Sending Configuration : \n" + buf.toString());

		try {
			medium.transmit(message);
		} catch (Exception e) {
			log.error("Exception - Cannot TX");
		}
	}

	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
