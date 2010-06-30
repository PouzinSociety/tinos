package <%= organization %>.<%= project_name %>.integration.test;

import org.springframework.osgi.test.provisioning.ArtifactLocator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LocalFileSystemIvyRepository implements ArtifactLocator {

	private static final char SLASH_CHAR = '/';
	private static final Log log = LogFactory.getLog(LocalFileSystemIvyRepository.class);

	/** local repo system property */
	private static final String SYS_PROPERTY = "IVY_CACHE";
	private static final String USER_HOME = "user.home";
	/** discovered local ivy-cache repository home */
	private String repositoryHome;


	/**
	 * Initialization method It determines the repository path by checking the
	 * existence of <code>localRepository</code> system property and falling
	 * back to the *not so* traditional <code>user.home/ivy/ivy-cache</code>.
	 * 
	 * <p/> This method is used to postpone initialization until an artifact is
	 * actually located. As the test class is instantiated on each test run, the
	 * init() method prevents repetitive, waste-less initialization.
	 * 
	 */
	private void init() {
		// already discovered a repository home, bailing out
		if (repositoryHome != null)
			return;

		boolean trace = log.isDebugEnabled();

		// check system property
		Map<String,String> envMap = System.getenv();
		String localRepository = envMap.get(SYS_PROPERTY);
		if (trace)
			log.trace("IVY_CACHE system property [" + SYS_PROPERTY + "] has value=" + localRepository);

		if (localRepository == null) {
			if (trace)
				log.trace("Unable to locate IVY_CACHE environment property [" +
					SYS_PROPERTY + "], defaulting to system property <user.home>/ivy/ivy-cache");
			String userHome = System.getProperty(USER_HOME);
			if (userHome != null) {
				localRepository = new File(userHome, "/ivy/ivy-cache").getAbsolutePath();
			}
			if (localRepository == null)
				throw (RuntimeException) new RuntimeException("Unable to locate IVY_CACHE");
		}
		repositoryHome = localRepository;
		log.info("Local IVY_CACHE repository used: [" + repositoryHome + "]");
	}

	/**
	 * Find a local ivy artifact. First tries to find the resource as a
	 * packaged artifact produced by a local ivy build, and if that fails will
	 * search the local maven repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return the String representing the URL location of this bundle
	 */
	public Resource locateArtifact(String groupId, String artifactId, String version) {
		return locateArtifact(groupId, artifactId, version, DEFAULT_ARTIFACT_TYPE);
	}

	/**
	 * Find a local ivy artifact. First tries to find the resource as a
	 * packaged artifact produced by a local ivy build, and if that fails will
	 * search the local ivy repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @param type - the extension type of the artifact
	 * @return
	 */
	public Resource locateArtifact(String groupId, String artifactId, String version, String type) {
		init();

		try {
			return localIvyBuildArtifact(groupId, artifactId, version, type);
		}
		catch (IllegalStateException illStateEx) {
			Resource localIvyBundle = localIvyBundle(groupId, artifactId, version, type);
			if (log.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("[");
				buf.append(groupId);
				buf.append("|");
				buf.append(artifactId);
				buf.append("|");
				buf.append(version);
				buf.append("]");
				log.debug(buf + " local ivy build artifact detection failed, falling back to local maven bundle "
						+ localIvyBundle.getDescription());
			}
			return localIvyBundle;
		}
	}

	/**
	 * Return the resource of the indicated bundle in the local Maven repository
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifact - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return
	 */
	protected Resource localIvyBundle(String groupId, String artifact, String version, String type) {
		StringBuffer location = new StringBuffer(SLASH_CHAR + "repository" + SLASH_CHAR);

		log.info("\n\tDependency (G:" + groupId + ",A: " + artifact + ",V: "
			+ version + ",T: " + type + ")\n\t\t=>" + artifact + "-"
			+ version + "." + type);


		location.append(groupId);
		location.append(SLASH_CHAR);
		location.append(artifact);
		location.append(SLASH_CHAR);
		location.append(version);
		location.append(SLASH_CHAR);
		location.append(artifact);
		location.append('-');
		location.append(version);
		location.append(".");
		location.append(type);

		return new FileSystemResource(new File(repositoryHome, location.toString()));
	}

	/**
	 * Find a local maven artifact in the current build tree. This searches for
	 * resources produced by the package phase of a maven build.
	 * 
	 * @param artifactId
	 * @param version
	 * @param type
	 * @return
	 */
	protected Resource localIvyBuildArtifact(String groupId, String artifactId, String version, String type) {
		try {
			File found = new IvyPackagedArtifactFinder(groupId, artifactId, version, type).findPackagedArtifact(new File(
				"."));
			Resource res = new FileSystemResource(found);
//			if (log.isDebugEnabled()) {
				log.info("[" + artifactId + "|" + version + "] resolved to " + res.getDescription()
						+ " as a local ivy artifact");
//			}
			return res;
		}
		catch (IOException ioEx) {
				log.info("[" + artifactId + "|" + version + "] failed to resolved to a local ivy build  artifact");
			throw (RuntimeException) new IllegalStateException("Artifact " + artifactId + "-" + version + "." + type
					+ " could not be found").initCause(ioEx);
		}
	}

}

