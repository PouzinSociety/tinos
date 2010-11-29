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
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

public class RouteDao {
	private String  target, netmask, gateway, device;

	public RouteDao() {
	}
	
	public RouteDao(String target, String netmask, String gateway, String device) {
		setTarget(target);
		setNetmask(netmask);
		if (gateway != null) setGateway(gateway);
		setDevice(device);
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Route:(T(" + target + "),N("+ netmask + 
				"),G(" + gateway + "),D(" + device + "))");
		return buf.toString();
	}
	
	static public String toXML(List<RouteDao> routeList) {
		StringBuffer xml = new StringBuffer();
		xml.append("<routes>\n");
		for (int i = 0; i < routeList.size(); i++) {
			xml.append(RouteDao.toXML(routeList.get(i)));
		}
		xml.append("</routes>\n");
		return xml.toString();
	}
	
	static public String toXML(RouteDao route) {
		StringBuffer xml = new StringBuffer();
		xml.append("<route ");
		xml.append("target=\"" + route.getTarget() + "\" ");
		if (route.getNetmask() != null)
			xml.append("netmask=\"" + route.getNetmask() + "\" ");
		if (route.getGateway() != null)
			xml.append("gateway=\"" + route.getGateway() + "\" ");
		if (route.getDevice() != null)
			xml.append("device=\"" + route.getDevice() + "\" ");
		xml.append("/>\n");		
		return xml.toString();		
	}
	
	static public List<RouteDao> fromXML(String xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		
		List<RouteDao> routeList = new LinkedList<RouteDao>();
		
		Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("route");
		for(int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element)nodes.item(i);
			
			RouteDao route = new RouteDao();			
			String value = cleanString(element.getAttribute("target"));
			route.setTarget((value == null) ? null : value);			
			value = cleanString(element.getAttribute("netmask"));
			route.setNetmask((value == null) ? null : value);
			value = cleanString(element.getAttribute("gateway"));
			route.setGateway((value == null) ? null : value);
			value = cleanString(element.getAttribute("device"));
			route.setDevice((value == null) ? null : value);			
			routeList.add(route);
		}
		return routeList;
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
