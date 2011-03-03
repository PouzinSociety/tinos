/* *
 * 2011 (c) Pouzin Society
 *
 * Author        : pphelan
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
package rina.idd.api;

import java.util.List;

public class IDDRecordResponse {
	String requestedApplicationProcessName;
	Integer requestedApplicationProcessInstanceId;
	String difName;
	String ipcProcessName;
	List<String> notThereDifNames;
	
	public String getRequestedApplicationProcessName() {
		return requestedApplicationProcessName;
	}
	public void setRequestedApplicationProcessName(
			String requestedApplicationProcessName) {
		this.requestedApplicationProcessName = requestedApplicationProcessName;
	}
	public Integer getRequestedApplicationProcessInstanceId() {
		return requestedApplicationProcessInstanceId;
	}
	public void setRequestedApplicationProcessInstanceId(
			Integer requestedApplicationProcessInstanceId) {
		this.requestedApplicationProcessInstanceId = requestedApplicationProcessInstanceId;
	}
	public String getDifName() {
		return difName;
	}
	public void setDifName(String difName) {
		this.difName = difName;
	}
	public String getIpcProcessName() {
		return ipcProcessName;
	}
	public void setIpcProcessName(String ipcProcessName) {
		this.ipcProcessName = ipcProcessName;
	}
	public List<String> getNotThereDifNames() {
		return notThereDifNames;
	}
	public void setNotThereDifNames(List<String> notThereDifNames) {
		this.notThereDifNames = notThereDifNames;
	}
}
