package <%= organization %>.<%= project_name %>.impl;

import <%= organization %>.<%= project_name %>.BundleService;
import org.apache.commons.logging.*;

public class BundleServiceImpl implements BundleService {
	private static final Log log = LogFactory.getLog(BundleServiceImpl.class);

	public String getMessage() {
		log.info("getMessage() entry");
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World from Bundle (<%= organization %>)");
		log.info("getMessage() exit");
		return sb.toString();
	}
}
