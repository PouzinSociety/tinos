/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package <%= organization %>.<%= project_name %>.integration.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Find a packaged ivy artifact starting in some root directory of a ivy
 * project.
 * 
 * @author Patsy Phelan
 * 
 * 
 */
public class IvyPackagedArtifactFinder {

	/** logger */
	private static final Log log = LogFactory
			.getLog(IvyPackagedArtifactFinder.class);

	private static final String IVY_XML = "ivy.xml";
	private static final String TARGET = "target/artifacts";
	private final String artifactName;
	private final String groupId;

	public IvyPackagedArtifactFinder(String groupId, String artifactId,
			String version, String type) {
		this.groupId = groupId;
		this.artifactName = artifactId + "-" + version + "." + type;
	}

	File findPackagedArtifact(File startingDirectory) throws IOException {
		// check ivy.xml in project root
		if (!isIvyProjectDirectory(startingDirectory)) {
			throw new IllegalStateException(startingDirectory
					+ " does not contain a " + IVY_XML + " file");
		}

		// Use the project root as Ivy root (then look for target directory)
		File rootIvyProjectDir = startingDirectory;
		if (log.isTraceEnabled())
			log.trace("Starting local artifact search from "
					+ rootIvyProjectDir.getAbsolutePath());

		File found = findInDirectoryTree(artifactName, rootIvyProjectDir);
		if (found == null) {
			throw new FileNotFoundException("Cannot find the artifact <"
					+ artifactName + "> with groupId <" + groupId + ">");
		}
		return found;
	}

	/**
	 * Check whether the folder is a ivy dir.
	 * 
	 * @param dir
	 * @return
	 */
	private boolean isIvyProjectDirectory(File dir) {
		return (dir.isDirectory() && new File(dir, IVY_XML).exists());
	}

	private File findInDirectoryTree(String fileName, File root) {
		boolean trace = log.isTraceEnabled();

		File targetDir = new File(root, TARGET);
		log.info("Looking for (" + fileName + ") in "
				+ targetDir.getAbsolutePath());
		if (targetDir.exists()) {
			File artifact = new File(targetDir, fileName);
			// found artifact
			if (artifact.exists()) {
				if (trace)
					log.trace("Found artifact at " + artifact.getAbsolutePath()
						+ ";");
				return artifact;
			}
		}
		return null;
	}
}
