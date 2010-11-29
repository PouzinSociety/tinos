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
package rina.transport.api;

public interface TransportConstants {

		public static final String KEY_DEST_DOMAIN = "DestDomain";
			public static final String DEST_DOMAIN_EXTERNAL = "External";
			public static final String DEST_DOMAIN_LOOPBACK = "Loopback";
			
		public static final String KEY_SRC_DOMAIN = "SrcDomain";	// Set by Transport (on Rx)
		public static final String KEY_TX_DOMAIN = "TxDomain";	// Set by Transport (on Tx)
	
		public static final String KEY_RX_TIME = "RxTime";			// Set by Transport (on Rx)
		public static final String KEY_TX_TIME = "TxTime";			// Set by Transport (on Tx)
		public static final String KEY_TX_NODE = "TxNode";			// Set by Transport (on Tx)
		public static final String KEY_RX_NODE = "RxNode";			// Set by Transport (on Rx)
		public static final String KEY_SRC_IP = "SrcIP";			// Set by Transport (on Rx)

		public static final String KEY_APPLICATION_NAME = "AppName";
		public static final String KEY_DEST_APPLICATION_NAME = "DestAppName";
		public static final String KEY_APPLICATION_EVENT = "AppEvent";
}
