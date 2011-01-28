package <%= organization %>.<%= project_name %>.integration.test;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.Constants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.provisioning.ArtifactLocator;
import org.springframework.osgi.test.platform.Platforms;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import <%= organization %>.<%= project_name %>.BundleService;

/**
 * Starts up an OSGi environment (equinox, knopflerfish, or felix according to
 * the profile selected) and installs the simple service bundle and the bundles
 * it depends on. The test classes in this project will be turned into a virtual
 * bundle which is also installed and the tests are then run inside the OSGi
 * runtime.
 * 
 * The tests have access to a BundleContext, which we use to test that the
 * simpleService bean was indeed published as an OSGi service.
 * 
 */
public class SimpleBundleTest extends AbstractConfigurableBundleCreatorTests {

	/**
	 * Override method - this is to allow the explicit specification of the
	 * required bundles (and versions) that the test framework is going to use.
	 * See next "commented" method for an easier option.
	 */
	protected Resource getTestingFrameworkBundlesConfiguration() {
		return new InputStreamResource(
				SimpleBundleTest.class
						.getResourceAsStream("<%= src_path %>/boot-bundles.properties"));
	}

	/**
	 * If all the internal versions were correct we could simply call this - to
	 * add the relevant bundle to test.
	 * 
	 * protected String[] getTestBundlesNames() { return new String[] {
	 * "<%= organization %>,<%= organization %>.<%= project_name %>, 1.0.0" }; }
	 */

	/**
	 * Explicitly set the OSGi Platform to Equinox. We are going to use Equinox
	 * for the moment - we have it in the ivy dependencies.
	 */
	protected String getPlatformName() {
		return Platforms.EQUINOX;
	}

	/**
	 * Explicitly specify Artifact Locator. Spring OSGi Test Framework currently
	 * only support dependency resolution via Maven. Our build framework is
	 * using ivy. So we have an implementation with Ivy support provided.
	 */
	protected ArtifactLocator getLocator() {
		return new LocalFileSystemIvyRepository();
	}

	/**
	 * Override.
	 * Allows us to check the correct bundles are in place - look in the logs.
	 */
	protected void postProcessBundleContext(BundleContext bundleContext)
			throws Exception {
		System.out.println(">TEST CASE SETUP INFO");
		System.out.println("Framework Vendor : "
				+ bundleContext.getProperty(Constants.FRAMEWORK_VENDOR));
		System.out.println("Framework Version: "
				+ bundleContext.getProperty(Constants.FRAMEWORK_VERSION));
		System.out.println("Framework ExecEnv: "
				+ bundleContext
						.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT));

		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			System.out.println("Bundle : "
					+ OsgiStringUtils.nullSafeName(bundles[i]) + " (State: "
					+ OsgiStringUtils.bundleStateAsString(bundles[i]) + ")");
			ServiceReference inUse[] = bundles[i].getServicesInUse();
			if (inUse != null) {
				for (int x = 0; x < inUse.length; x++) {
					System.out.println("\tServiceInUse : "
							+ OsgiStringUtils.nullSafeToString(inUse[x]));
				}
			}
			ServiceReference regSer[] = bundles[i].getRegisteredServices();
			if (regSer != null) {
				for (int y = 0; y < regSer.length; y++) {
					System.out.println("\tServiceReg : "
							+ OsgiStringUtils.nullSafeToString(regSer[y]));
				}
			}
		}
		System.out.println("<TEST CASE SETUP INFO");
		super.postProcessBundleContext(bundleContext);

	}

	/**
	 * Simple Test to check the Framework has initialised properly
	 */
	public void testOsgiPlatformStarts() throws Exception {
		System.out.println(">>testOsgiPlatformStarts\n");
		System.out.println("Framework Vendor : "
				+ bundleContext.getProperty(Constants.FRAMEWORK_VENDOR));
		System.out.println("Framework Version: "
				+ bundleContext.getProperty(Constants.FRAMEWORK_VERSION));
		System.out.println("Framework ExecEnv: "
				+ bundleContext
						.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT));
		System.out.println("<<testOsgiPlatformStarts\n");
	}

	/**
	 * The simple service should have been exported as an OSGi service, which we
	 * can verify using the OSGi service APIs.
	 * 
	 * In a Spring bundle, using osgi:reference is a much easier way to get a
	 * reference to a published service.
	 * 
	 */
	public void testSimpleServiceExported() {
		System.out.println(">>testSimpleServiceExported\n");
		waitOnContextCreation("<%= organization %>.<%= project_name %>");
		ServiceReference ref = bundleContext
				.getServiceReference(BundleService.class.getName());
		assertNotNull("Service Reference is null", ref);
		try {
			BundleService bundleService = (BundleService) bundleContext
					.getService(ref);
			assertNotNull("Cannot find the service", bundleService);
			assertEquals(
					"Hello World from Bundle (<%= organization %>)",
					bundleService.getMessage());
			System.out.println("<%= organization %>.<%= project_name %>.BundleService.getMessage(" + bundleService.getMessage() + ")");

		} finally {
			bundleContext.ungetService(ref);
		}
		System.out.println("<<testSimpleServiceExported\n");
	}
}
