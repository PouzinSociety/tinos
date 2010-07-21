package org.xbill.DNS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This bundle's activator. Mainly used to get the bundle context
 * @author edu
 *
 */
public class Activator implements BundleActivator{

	private static BundleContext context;
	private static final Log log = LogFactory.getLog(Activator.class);

	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
	}

	public static TransportLayer getTransportLayer(String transport){
		TransportLayer transportLayer = null;
		try{
			String filterString = "(& (" + Constants.OBJECTCLASS + "=org.jnode.net.TransportLayer)(transport="+ 
				transport+"))";
			Filter filter = FrameworkUtil.createFilter(filterString);
			ServiceTracker tracker = new ServiceTracker(context, filter, null);
		    tracker.open();
		    log.debug("Looking up Service from regisrty with properties: " + filter);
		    transportLayer = (TransportLayer) tracker.waitForService(20000);
		    tracker.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return transportLayer;
	}

}
