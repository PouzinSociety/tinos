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
package org.pouzinsociety.support.jnode.impl;

import java.util.List;
import org.jnode.driver.AbstractDeviceManager;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceToDriverMapper;

public class TssgDeviceManager extends AbstractDeviceManager {
	private List<DeviceFinder> deviceFinders;

	public TssgDeviceManager() {
		super();
		// super - Create the SystemBus
	}
	
	public void setDeviceFinders(List<DeviceFinder> deviceFinders) {
		this.deviceFinders = deviceFinders;
	}

	public List<DeviceFinder> getDeviceFinders() {
		return deviceFinders;
	}

	@Override
	protected void refreshFinders(List<DeviceFinder> finders) {
		logger.debug("refreshFinders");
		finders.clear();
		finders.addAll(deviceFinders);
	}

	@Override
	protected void refreshMappers(List<DeviceToDriverMapper> mappers) {
		mappers.clear();
	}

	@Override
	public void start() throws Exception {
		loadExtensions();
		findDevices();
	}
}
