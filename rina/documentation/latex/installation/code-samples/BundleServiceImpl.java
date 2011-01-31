package org.tssg.network.stack.bundle_a.impl;

import org.tssg.network.stack.bundle_a.BundleService;

public class BundleServiceImpl implements BundleService {
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World from Bundle (org.tssg.network.stack.bundle_a)");
		return sb.toString();
	}
}
