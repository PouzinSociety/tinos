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

public class IMDao {
	private String im_server, im_port, im_buddyId, im_buddyPassword, im_chatroom, im_resourceId;

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

	public String getIm_resourceId() {
		return im_resourceId;
	}

	public void setIm_resourceId(String im_resourceId) {
		this.im_resourceId = im_resourceId;
	}

	public String getIm_chatroom() {
		return im_chatroom;
	}

	public void setIm_chatroom(String im_chatroom) {
		this.im_chatroom = im_chatroom;
	}
	
	static public String toXML(IMDao dao) {
		StringBuffer xml = new StringBuffer();
		xml.append("<im_connection ");
		xml.append("im_server=\"" + dao.getIm_server() + "\" ");
		xml.append("im_port=\"" + dao.getIm_port() + "\" ");
		xml.append("im_buddyId=\"" + dao.getIm_buddyId() + "\" ");
		xml.append("im_buddyPassword=\"" + dao.getIm_buddyPassword() + "\" ");
		xml.append("im_resourceId=\"" + dao.getIm_resourceId() + "\" ");
		xml.append("im_chatroom=\"" + dao.getIm_chatroom() + "\" ");
		xml.append(" />\n");
		return xml.toString();
	}
	
	static public String toXML(List<IMDao> list) {
		StringBuffer xml = new StringBuffer();
		xml.append("<im_connections>\n");
		for (int i = 0; i < list.size(); i++) 
			xml.append(IMDao.toXML(list.get(i)));
		xml.append("</im_connections>\n");
		return xml.toString();
	}
	
	static public List<IMDao> fromXML(String xml) throws Exception {
		List<IMDao> list = new LinkedList<IMDao>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("im_connection");
		for(int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element)nodes.item(i);			
			IMDao con = new IMDao();
			String value = cleanString(element.getAttribute("im_server"));
			con.setIm_server((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_port"));
			con.setIm_port((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_buddyId"));
			con.setIm_buddyId((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_buddyPassword"));
			con.setIm_buddyPassword((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_resourceId"));
			con.setIm_resourceId((value == null) ? null : value);
			value = cleanString(element.getAttribute("im_chatroom"));
			con.setIm_chatroom((value == null) ? null : value);
			list.add(con);
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
