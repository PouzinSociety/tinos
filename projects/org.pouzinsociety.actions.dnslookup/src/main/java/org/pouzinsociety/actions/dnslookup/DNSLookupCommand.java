package org.pouzinsociety.actions.dnslookup;

import java.net.SocketException;

import jnode.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.NetworkLayerManager;
import org.pouzinsociety.config.stack.StackConfiguration;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;

/**
 * Ported from the dnsjava lookup program
 * @author eduardgrasa
 *
 */
public class DNSLookupCommand {
	private static Log log = LogFactory.getLog(DNSLookupCommand.class);
	@SuppressWarnings("unused")
	private NetworkLayerManager networkLayerManager;
	// Force the ordering
	private StackConfiguration stackConfiguration;

	public void setStackConfiguration(StackConfiguration stackConfiguration) {
		this.stackConfiguration = stackConfiguration;
	}

	public DNSLookupCommand() {
	}

	public void execute() throws SocketException, InterruptedException {
		int i = 0;
		while (stackConfiguration.Complete() == false && i < 100) {
			Thread.sleep(1000);
			i++;
			log.info("Waiting for completion");
		}

		try{
			Lookup lookup = new Lookup("ns1.internal");
			SimpleResolver simpleResolver = new SimpleResolver();
			InetAddress dnsServerAddress = InetAddress.getByName("10.0.0.1");
			simpleResolver.setAddress(dnsServerAddress);
			simpleResolver.setTCP(true);
			InetAddress localAddress = InetAddress.getByName("10.0.0.2");
			simpleResolver.setLocalAddress(localAddress, 8888);
			lookup.setResolver(simpleResolver);
			lookup.run();
			printAnswer("ns1.internal", lookup);
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
		}

	}

	private void printAnswer(String name, Lookup lookup) {
		log.info(name + ":");
		int result = lookup.getResult();
		if (result != Lookup.SUCCESSFUL)
			log.info(" " + lookup.getErrorString());
		log.info("");
		Name [] aliases = lookup.getAliases();
		if (aliases.length > 0) {
			log.info("# aliases: ");
			for (int i = 0; i < aliases.length; i++) {
				log.info(aliases[i]);
				if (i < aliases.length - 1)
					log.info(" ");
			}
			log.info("");
		}
		if (lookup.getResult() == Lookup.SUCCESSFUL) {
			Record [] answers = lookup.getAnswers();
			for (int i = 0; i < answers.length; i++)
				log.info(answers[i]);
		}
	}
}
