package org.pouzinsociety.example;

import org.pouzinsociety.example.InterfaceService;

public class InterfaceServiceImpl implements InterfaceService {
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World");
		return sb.toString();
	}
}
