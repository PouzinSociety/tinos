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
package org.pouzinsociety.transport.im.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.logging.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.pouzinsociety.transport.im.ConnectionImpl;
import org.pouzinsociety.config.dao.IMDao;

public class LoggerBuddy implements PacketListener {
		Log log = LogFactory.getLog(LoggerBuddy.class);
		IMDao imConnectionDetails;
		ConnectionImpl medium;
		
		// PCAP formatted dump file
		String dumpFileName;
		String dumpFileLocation = ".";
		FileOutputStream dumpFileStream;
		
		public LoggerBuddy(IMDao imConfig, String dumpFileDir) throws Exception {
			imConnectionDetails = imConfig;
			this.dumpFileLocation = dumpFileDir;
			medium = new ConnectionImpl();

			medium.setConfiguration(imConnectionDetails.getIm_server(),
					imConnectionDetails.getIm_port(),
					imConnectionDetails.getIm_buddyId(),
					imConnectionDetails.getIm_buddyPassword(),
					imConnectionDetails.getIm_resourceId(),
					imConnectionDetails.getIm_chatroom());
			medium.connect(this);
			log.info("Connected");
			
			try {
			dumpFileName = new String(dumpFileLocation + File.separator + imConnectionDetails.getIm_chatroom() + ".pcap");
			dumpFileStream = new FileOutputStream(dumpFileName);           
            writeFileHeader(dumpFileStream);
            log.info("Creating PCAP file (" + dumpFileName + ")");
			} catch (Exception e) {
				log.error("Exception : Creating PCAP dump file (" + dumpFileName +")");
				log.error(e.getStackTrace());
				dumpFileStream = null;
			}
		}
		
		public void close() {
			try {
				if (dumpFileStream != null)
					dumpFileStream.close();
				medium.disconnect();
			} catch(Exception e) {
			}
		}

		private static String toHexString(byte[] bytes) {
		    char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		    char[] hexChars = new char[10000000];
		    int c = 0;
		    int v;
		    for (int j = 0; j < bytes.length; j++ ) {
		        v = bytes[j] & 0xFF;
		        hexChars[c] = hexArray[v/16];
		        c++;
		        hexChars[c] = hexArray[v%16];
		        c++;
		    }
		    return new String(hexChars, 0, c);
		 }

	    private void writeULShort(FileOutputStream out, int v) throws IOException {
	    	out.write((v & 0x000000FF) >> 0);
	    	out.write((v & 0x0000FF00) >> 8);
	    }
	    private void writeULInt(FileOutputStream out, int v) throws IOException {
	    	int i;
	    	out.write(((i = (v & 0x000000FF) >> 0) < 0) ? i + 256 : i);
	    	out.write(((i = (v & 0x0000FF00) >> 8) < 0) ? i + 256 : i);
	    	out.write(((i = (v & 0x00FF0000) >> 16) < 0) ? i + 256 : i);
	    	out.write(((i = (v & 0xFF000000) >> 24) < 0) ? i + 256 : i);
	    }
	    private void writeFileHeader(FileOutputStream out) throws IOException {
	        /* PCAP file format MAGIC numbers/identifiers */
	        int PCAP_MAGIC_NUMBER1 = 0xa1b2c3d4;
	        // int PCAP_MAGIC_NUMBER2 = 0xa1b2cd34;

	        /* PCAP variables which are used in writting out headers. */
	        int pcapMagicNumber = PCAP_MAGIC_NUMBER1;
	        int pcapMajorVer = 2;
	        int pcapMinorVer = 4;
	        int pcapTimezone = 0;
	        int pcapTimestampAccuracy = 0;
	        int pcapSnaplen = 16384; // 16 Kbytes
	        int pcapLinktype = 1; // Ethernet
	        
	        writeULInt(out, pcapMagicNumber);
	        writeULShort(out, pcapMajorVer);
	        writeULShort(out, pcapMinorVer);
	        writeULInt(out, pcapTimezone);
	        writeULInt(out, pcapTimestampAccuracy);
	        writeULInt(out, pcapSnaplen);
	        writeULInt(out, pcapLinktype);
	    }
	    
		private void writeRecordHeader(FileOutputStream out, long milliseconds, int buflen) throws IOException {
			long seconds = milliseconds / 1000;
			long microseconds = (milliseconds % 1000) * 1000;
		    writeULInt(out, (int) seconds); // Seconds
		    writeULInt(out, (int)microseconds); // Micro-seconds
		    writeULInt(out, buflen);
		    writeULInt(out, buflen);
		}

	    		
		public void processPacket(Packet packet) {
			StringBuffer buf = new StringBuffer();
			boolean network_packet = false;
			
			// Debug Info for Log File
			buf.append("PacketId: " + packet.getPacketID() + "\n");
			buf.append("Src: " + packet.getFrom() + ", dest: " + packet.getTo() + "\n");			
			Message msg = (Message)packet;
			buf.append("Body:" + msg.getBody() + "\n");
			Collection<String> props  = msg.getPropertyNames();
			byte[] buffer = null;
			for (Iterator<String> keys = props.iterator(); keys.hasNext(); ) {
				String key = keys.next();
				if (key.equals("PDU")) {
					buf.append("PDU:\n" + toHexString((byte[])msg.getProperty(key)) + "\n");
					buffer = (byte[])msg.getProperty(key);
					network_packet = true;
				} else {
					buf.append("Key(" + key +"): " + msg.getProperty(key) + "\n");
				}
			}			
			
			if (network_packet == true) {
		        long timestamp = System.currentTimeMillis();		 
				if (dumpFileStream != null) {
					try {
					    writeRecordHeader(dumpFileStream, timestamp, buffer.length);
						dumpFileStream.write(buffer);
					} catch (IOException ioe) {
						log.error("Error: Writing record to dump file");
						log.error(ioe.getStackTrace());
					}
				}
			}					
			log.info("\n\nPacket:\n" + buf.toString() + "\n\n");
		}		
}
