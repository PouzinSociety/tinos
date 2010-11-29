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
package org.pouzinsociety.dns.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TSIG;
import org.xbill.glue.DNSClient;

public class DnsClientLauncher implements BootStrapCompleteAPI {
	private static Log log = LogFactory.getLog(DnsClientLauncher.class);
	private TransportLayer transportLayer;
	private DNSClient dnsClient;
	private boolean dnsClientConfigured = false;
	private boolean gotResult = false;

	public void bootstrapComplete(Object arg0) throws BootstrapException {
		log.debug("BootStrapComplete()");
		setDNSConfiguration();
		log.debug("DNS Client Configured : " + dnsClientConfigured);
	}

	public void setDNSConfiguration() {
		// Read DNS Configuration Files
		try {
			dnsClient = new DNSClient();
			dnsClient.setServer("10.0.0.1");
			dnsClient.setPort(53);
			dnsClient.setLocalAddress("10.0.0.2");

			dnsClient.setTSIGKey(new TSIG("hmac-md5", "xbill.org", "1234"));
			dnsClient.setTransportLayer(transportLayer);
			dnsClient.initialize();
			synchronized (this) {
				dnsClientConfigured = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	public String getConfigDaoClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void timerFired() {
		synchronized (this) {
			// Wait for DNS Client to be configured
			if (dnsClientConfigured != true)
				return;
			// After a successful query, no need to try again as the XBILL code
			// caches the result.
			if (gotResult == true) {
				log.debug("No more requests - cached locally");
				return;
			}

			log.debug("Processing DNS Lookup Request");
			try {
				Lookup.setDefaultResolver(dnsClient);
				// Essentially the default domain from resolv.conf
				Lookup.setDefaultSearchPath(new String[] { "linux.bogus" });

				// Test Lookup : mail, combined with default domain :
				// mail.linux.bogus
				String hostname = "mail";
				Lookup lookup = new Lookup(hostname);
				lookup.run();
				log.info("Lookup Result:\n" + printAnswer(hostname, lookup));
				if (lookup.getResult() == Lookup.SUCCESSFUL)
					gotResult = true;
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			}
		}
	}

	public String printAnswer(String hostname, Lookup lookup) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(hostname + ":");
		int result = lookup.getResult();
		if (result != Lookup.SUCCESSFUL)
			buffer.append(" " + lookup.getErrorString());
		buffer.append("\n");
		Name[] aliases = lookup.getAliases();
		if (aliases.length > 0) {
			buffer.append("# aliases: ");
			for (int i = 0; i < aliases.length; i++) {
				buffer.append(aliases[i]);
				if (i < aliases.length - 1)
					buffer.append(" ");
			}
			buffer.append("\n");
		}
		if (lookup.getResult() == Lookup.SUCCESSFUL) {
			Record[] answers = lookup.getAnswers();
			for (int i = 0; i < answers.length; i++)
				buffer.append(answers[i] + "\n");
		}
		return buffer.toString();
	}

	public void setTransportLayer(TransportLayer transportLayer) {
		this.transportLayer = transportLayer;
	}

}
