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
package sample.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sample.api.SampleDao;
import sample.api.SampleAPI;
public class SampleAPIImpl implements SampleAPI {
	
	List<SampleDao> daoList = new ArrayList<SampleDao>();
	
	public SampleAPIImpl() {
		synchronized (daoList) {
			daoList.add(new SampleDao("PreLoaded"));
		}
	}

	public void add(SampleDao arg0) {
		synchronized (daoList) {
			daoList.add(arg0);			
		}	
	}

	public List<SampleDao> get() {
		List<SampleDao> copy;
		synchronized (daoList) {
			copy = Collections.unmodifiableList(daoList);
		}
		return copy;
	}

	public void remove(SampleDao arg0) {
		synchronized (daoList) {
			daoList.remove(arg0);
		}
	}
}
