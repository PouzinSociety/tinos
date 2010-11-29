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
package org.pouzinsociety.bootstrap.api;


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.*;

public class BootstrapNotifications {

	private static final Log log = LogFactory.getLog(BootstrapNotifications.class);
	private List<BootStrapCompleteAPI> notificationList;
	
	public void setNotificationList(List<BootStrapCompleteAPI> notificationList) {
		if (notificationList != null)
			this.notificationList = notificationList;
		else
			this.notificationList = new ArrayList<BootStrapCompleteAPI>();
	}
	
	public void notifyBootstrapComplete() {
		for (int i = 0; i < notificationList.size(); i++) {
			BootStrapCompleteAPI bundle = notificationList.get(i);
			try {
				bundle.bootstrapComplete(null);
			} catch (Exception e) {
				log.error("Exception : Unable to Notify(" +
				 bundle.getConfigDaoClassName() + ") : " +
				 e.getMessage());
				log.error(e);
			}
		}
	}		
}
