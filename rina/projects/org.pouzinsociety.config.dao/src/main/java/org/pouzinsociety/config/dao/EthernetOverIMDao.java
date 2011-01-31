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
package org.pouzinsociety.config.dao;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class EthernetOverIMDao extends EthernetDeviceDao {
	private String im_server, im_port, im_buddyId, im_buddyPassword, im_chatroom, im_resourceId;
	private String node_name, ip_address, ip_netmask;
	
	public EthernetOverIMDao() {
		super();
		device_prefix = "im";
		im_server = im_port = im_buddyId = im_buddyPassword = im_chatroom = im_resourceId = null;
		ip_address = ip_netmask = null;
	}
	public EthernetOverIMDao(String node_name, String deviceName,
			String ethernetAddress, String ip_address, String ip_netmask, 
			String im_server, String im_port, String im_buddyId, String im_buddyPassword,
			String im_chatroom, String im_resourceId) {
		super();
		setNode_name(node_name);
		setDevice_prefix("im");
		setDevice_name(deviceName);
		setEthernetAddress(ethernetAddress);
		setIp_address(ip_address);
		setIp_netmask(ip_netmask);
		setIm_server(im_server);
		setIm_port(im_port);
		setIm_buddyId(im_buddyId);
		setIm_buddyPassword(im_buddyPassword);
		setIm_chatroom(im_chatroom);
		setIm_resourceId(im_resourceId);
	}

	public String getIm_server() {
		return im_server;
	}

	public void setIm_server(String im_server) {
		this.im_server = im_server;
	}

	public String getIm_port() {
		return im_port;
	}

	public void setIm_port(String im_port) {
		this.im_port = im_port;
	}

	public String getIm_buddyId() {
		return im_buddyId;
	}

	public void setIm_buddyId(String im_buddyId) {
		this.im_buddyId = im_buddyId;
	}

	public String getIm_buddyPassword() {
		return im_buddyPassword;
	}

	public void setIm_buddyPassword(String im_buddyPassword) {
		this.im_buddyPassword = im_buddyPassword;
	}

	public String getIm_chatroom() {
		return im_chatroom;
	}

	public void setIm_chatroom(String im_chatroom) {
		this.im_chatroom = im_chatroom;
	}

	public String getNode_name() {
		return node_name;
	}

	public void setNode_name(String node_name) {
		this.node_name = node_name;
		this.im_resourceId = node_name;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public String getIp_netmask() {
		return ip_netmask;
	}

	public void setIp_netmask(String ip_netmask) {
		this.ip_netmask = ip_netmask;
	}

	public void setIm_resourceId(String im_resourceId) {
		this.im_resourceId = im_resourceId;
	}

	public String getIm_resourceId() {
		return im_resourceId;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("EthernetDevice:(" + node_name + "," + device_name + "," +
				hardwareAddress + "," + im_server + "," + im_port + "," + im_buddyId +
				"," + im_buddyPassword + "," + im_chatroom + "," + ip_address + "," +
				ip_netmask + ")");
		return buf.toString();
	}
	static public String toXML(EthernetOverIMDao dao) {
		StringBuffer xml = new StringBuffer();
		xml.append("<interface ");
		xml.append("node=\"" + dao.getNode_name() + "\" ");
		xml.append("device=\"" + dao.getDevice_name() + "\" ");
		xml.append("ethernetAddress=\"" + dao.getEthernetAddress() + "\" ");
		xml.append("im_server=\"" + dao.getIm_server() + "\" ");
		xml.append("im_port=\"" + dao.getIm_port() + "\" ");
		xml.append("im_buddyId=\"" + dao.getIm_buddyId() + "\" ");
		xml.append("im_buddyPassword=\"" + dao.getIm_buddyPassword() + "\" ");
		xml.append("im_chatroom=\"" + dao.getIm_chatroom() + "\" ");
		xml.append("ip_address=\"" + dao.getIp_address() + "\" ");
		xml.append("ip_netmask=\"" + dao.getIp_netmask() + "\" ");
		xml.append(" />\n");
		return xml.toString();
	}
	
	static public String toXML(List<EthernetOverIMDao> list) {
		StringBuffer xml = new StringBuffer();
		xml.append("<interfaces>\n");
		for (int i = 0; i < list.size(); i++) 
			xml.append(EthernetOverIMDao.toXML(list.get(i)));
		xml.append("</interfaces>\n");
		return xml.toString();
	}
	
	static public List<EthernetOverIMDao> fromXML(String xml) throws Exception {
		List<EthernetOverIMDao> list = new LinkedList<EthernetOverIMDao>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("interface");
		for(int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element)nodes.item(i);
			
			EthernetOverIMDao iface = new EthernetOverIMDao();
			String value = cleanString(element.getAttribute("device"));
			iface.setDevice_name((value == null) ? null : value);
			value = cleanString(element.getAttribute("node"));
			iface.setNode_name((value == null) ? null : value);
			value = cleanString(element.getAttribute("ethernetAddress"));
			iface.setEthernetAddress((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_server"));
			iface.setIm_server((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_port"));
			iface.setIm_port((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_buddyId"));
			iface.setIm_buddyId((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_buddyPassword"));
			iface.setIm_buddyPassword((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_chatroom"));
			iface.setIm_chatroom((value == null) ? null : value);
			value = cleanString(element.getAttribute("ip_address"));
			iface.setIp_address((value == null) ? null : value);
			value = cleanString(element.getAttribute("ip_netmask"));
			iface.setIp_netmask((value == null) ? null : value);
			list.add(iface);
		}
		
		return list;
	}
	
	static private String cleanString(String rawXMLValue) {
		if (rawXMLValue == null)
			return null;
		rawXMLValue = rawXMLValue.trim();
		if (rawXMLValue.isEmpty())
			return null;
		return rawXMLValue;	
	}
	
}
