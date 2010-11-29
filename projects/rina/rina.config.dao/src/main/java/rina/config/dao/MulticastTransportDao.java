/*
 * 2010 (c) Pouzin Society
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
package rina.config.dao;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MulticastTransportDao {
	private String domain;
	private String node;
	private List<MulticastTransportConnectionDao> mediumList;
	
	public MulticastTransportDao() {
		mediumList = null;
	}
	public MulticastTransportDao(String domain, String node,  List<MulticastTransportConnectionDao> mediumList) {
		setDomain(domain);
		setNode(node);
		setMediumList(mediumList);
	}
	
	public List<MulticastTransportConnectionDao> getMediumList() {
		if (mediumList == null)
			mediumList = new ArrayList<MulticastTransportConnectionDao>();
		return mediumList;
	}


	public void setMediumList(List<MulticastTransportConnectionDao> mediumList) {
		if (mediumList == null)
			mediumList = new ArrayList<MulticastTransportConnectionDao>();
		this.mediumList = mediumList;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("MulticastTransport { domain: " + domain + " , node : " + node + ", ");
		for (int i = 0; i < mediumList.size(); i++) {
			buf.append(mediumList.get(i).toString());
		}
		buf.append("}");		
		return buf.toString();
	}

	static public String toXML(MulticastTransportDao dao) {
		StringBuffer xml = new StringBuffer();
		xml.append("<MulticastTransport domain=\"" + dao.getDomain() + "\" node=\"" + dao.getNode() + "\">\n");
		List<MulticastTransportConnectionDao> mediumList = dao.getMediumList();
		for (int i = 0; i < mediumList.size(); i++) {
			xml.append(MulticastTransportConnectionDao.toXML(mediumList.get(i)));
		}
		xml.append("</MulticastTransport>\n");
		return xml.toString();
	}

	static public MulticastTransportDao fromXML(String xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));

		MulticastTransportDao dao = new MulticastTransportDao();
		List<MulticastTransportConnectionDao> mediumList = new ArrayList<MulticastTransportConnectionDao>();

		Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("MulticastTransport");
		for (int i = 0; i < nodes.getLength(); i++) {
			
			Element element = (Element) nodes.item(i);
			String domain = cleanString(element.getAttribute("domain"));
			dao.setDomain(domain);
			String node = cleanString(element.getAttribute("node"));
			dao.setNode(node);
			NodeList innerNodes = element.getElementsByTagName("MulticastTransportConnection");		
			for (int j = 0; j < innerNodes.getLength(); j++) {
				Element innerElement = (Element) innerNodes.item(j);

				MulticastTransportConnectionDao medium = new MulticastTransportConnectionDao();
				String value = cleanString(innerElement.getAttribute("domain"));
				medium.setDomain((value == null) ? null : value);
				value = cleanString(innerElement.getAttribute("node"));
				medium.setNode((value == null) ? null : value);
				value = cleanString(innerElement.getAttribute("multicastAddr"));
				medium.setMulticastAddress((value == null) ? null : value);
				value = cleanString(innerElement.getAttribute("multicastPort"));
				medium.setMulticastPort((value == null) ? null : value);
				value = cleanString(innerElement.getAttribute("interface"));
				medium.setNetworkInterface((value == null) ? null : value);
				
				mediumList.add(medium);
			}
			dao.setMediumList(mediumList);
		}
		return dao;
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
