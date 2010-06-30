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

public class HostEntryDao {

    private String hostname;

    private String[] ipAddress;

    public HostEntryDao() {
        this.hostname = null;
        this.ipAddress = new String[0];
    }

    public HostEntryDao(String hostname, String[] ipAddress) {
        setHostname(hostname);
        setIpAddress(ipAddress);
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String[] getIpAddress() {
        return this.ipAddress.clone();
    }

    public void setIpAddress(String[] ipAddress) {
        if (ipAddress == null) {
            this.ipAddress = new String[0];
        } else {
            this.ipAddress = ipAddress.clone();
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("HostEntry:(" + this.hostname + " - [");
        for (int i = 0; i < this.ipAddress.length; i++) {
            buf.append(this.ipAddress[i] + (i == this.ipAddress.length - 1 ? "" : ","));
        }

        buf.append(this.ipAddress.length == 0 ? "<empty>])" : "])");
        return buf.toString();
    }

    static public String toXML(HostEntryDao dao) {
        StringBuffer buf = new StringBuffer();
        buf.append("<host name=\"" + dao.getHostname() + "\" ipAddress=\"");
        String[] ipAddrs = dao.getIpAddress();
        for (int i = 0; i < ipAddrs.length; i++) {
            buf.append(ipAddrs[i] + (i == ipAddrs.length - 1 ? "" : ","));
        }
        buf.append("\" />\n");
        return buf.toString();
    }

    static public String toXML(List<HostEntryDao> list) {
        StringBuffer xml = new StringBuffer();
        xml.append("<hosts>\n");
        for (int i = 0; i < list.size(); i++) {
            xml.append(HostEntryDao.toXML(list.get(i)));
        }
        xml.append("</hosts>\n");
        return xml.toString();
    }

    static public List<HostEntryDao> fromXML(String xml) throws Exception {
        List<HostEntryDao> list = new LinkedList<HostEntryDao>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("host");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            HostEntryDao host = new HostEntryDao();
            String value = cleanString(element.getAttribute("name"));
            host.setHostname(value == null ? null : value);
            value = cleanString(element.getAttribute("ipAddress"));
            host.setIpAddress(value == null ? null : value.split(","));
            list.add(host);
        }
        return list;
    }

    static private String cleanString(String rawXMLValue) {
        if (rawXMLValue == null) {
            return null;
        }
        rawXMLValue = rawXMLValue.trim();
        if (rawXMLValue.isEmpty()) {
            return null;
        }
        return rawXMLValue;
    }

}
