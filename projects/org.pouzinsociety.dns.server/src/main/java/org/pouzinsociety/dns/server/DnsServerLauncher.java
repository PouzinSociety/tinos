package org.pouzinsociety.dns.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import org.xbill.DNS.Address;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.glue.DNSServer;

public class DnsServerLauncher implements BootStrapCompleteAPI {
	private static Log log = LogFactory.getLog(DnsServerLauncher.class);
	private TransportLayer udpTransportLayer;
	private TransportLayer tcpTransportLayer;
	private DNSServer dnsServer;
	
	public void bootstrapComplete(Object arg0) throws BootstrapException {
		log.debug("BootStrapComplete()");
		setDNSConfiguration();
	}
	
	public void setDNSConfiguration() {
		// Read DNS Configuration Files
		
		dnsServer = new DNSServer();
		dnsServer.setTransportLayer(tcpTransportLayer);
		
		/**
		 * Same as - dns.conf:
		 * primary internal /DnsConfig/internal.db
		 * cache /DnsConfig/cache.db
		 * key xbill.org 1234
		 * address 10.0.0.1
		 * port 53
		 */
		try {
		String primaryZoneDatabase = readResourceAsString("/DnsConfig/internal.db");
		log.info("<PrimaryZone>\n" + primaryZoneDatabase + "\n</PrimaryZone>");
		dnsServer.addPrimaryZone("0.0.127.in-addr.arpa", primaryZoneDatabase);
		
		String cacheDatabase = readResourceAsString("/DnsConfig/cache.db");
		log.info("<CacheDatabase>\n" + cacheDatabase + "\n</CacheDatabase>");
		dnsServer.setCache(cacheDatabase);
		
		log.info("<TSIG>hmac-md5,xbill.org,1234</TSIG>");
		dnsServer.addTSIG("hmac-md5", "xbill.org", "1234");
		
		log.info("<ServerInterface>10.0.0.1,53</ServerInterface>");
		dnsServer.setServerInterface("10.0.0.1", "53");
		
		dnsServer.initialize();
		
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	public String getConfigDaoClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUdpTransportLayer(TransportLayer udpTransportLayer) {
		this.udpTransportLayer = udpTransportLayer;
	}

	public void setTcpTransportLayer(TransportLayer tcpTransportLayer) {
		this.tcpTransportLayer = tcpTransportLayer;
	}
	
	
	public String readResourceAsString(String resourceName) throws IOException {	
		InputStream inStream = this.getClass().getResourceAsStream(resourceName);
		if (inStream != null) {
			String resoureContents = convertStreamToString(inStream);
			inStream.close();
			return resoureContents;
		}
		return "";
	}
	
	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {       
			return "";
		}
	}

}
