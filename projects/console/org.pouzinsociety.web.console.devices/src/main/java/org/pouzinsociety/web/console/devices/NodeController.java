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
package org.pouzinsociety.web.console.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.ethernet.EthernetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Annotation-driven <em>MultiActionController</em> that handles all non-form
 * URL's.
 *
 * Used to provide simple pages for OpenTinos Console.
 */
@Controller
public class NodeController {
	private final DeviceManager devManager;
	private static Log log = LogFactory.getLog(NodeController.class);

	@Autowired
	public NodeController(DeviceManager devMan) {
		this.devManager = devMan;
	}

	/**
	 * Custom handler for displaying device interfaces.
	 * <p>
	 * Note that this handler returns a plain {@link ModelMap} object instead of
	 * a ModelAndView, thus leveraging convention-based model attribute names.
	 * It relies on the RequestToViewNameTranslator to determine the logical
	 * view name based on the request URL: "/devices.do" -&gt; "vets".
	 *
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/devices/index")
	public ModelMap indexHandler() {
		List<DeviceDao> devList = new ArrayList<DeviceDao>();
		Collection<Device> devices = this.devManager.getDevices();
		try {
			for (Device dev : devices) {
				DeviceDao tmp = new DeviceDao();
				tmp.setName(dev.getId());
				log.debug("Device: " + dev.getId());
				final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);
				tmp.setAddress(api.getAddress().toString());
				tmp.setMtu(new Integer(api.getMTU()).toString());
				ProtocolAddressInfo protoAddrInf = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
				if (protoAddrInf != null)
					tmp.setProtocolAddress(protoAddrInf.toString());
				else
					tmp.setProtocolAddress("[IP Address - NotApplicable]");
				devList.add(tmp);
			}
		} catch (Exception e) {
			log.debug("Cannot Translate Devices retrieved from DeviceManager");
		}
		return new ModelMap().addAttribute("deviceList", devList);
	}
}
