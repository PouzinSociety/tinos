package <%= organization %>.<%= project_name %>;

import junit.framework.TestCase;
import <%= organization %>.<%= project_name %>.BundleService;
import <%= organization %>.<%= project_name %>.impl.BundleServiceImpl;

public class BundleServiceImplTests extends TestCase {
    private BundleService impl;

    protected void setUp() {
        impl = new BundleServiceImpl();
    }

    protected void tearDown() {
        impl = null;
    }

    public void testGetMessage() {
        assertEquals("Hello World from Bundle (<%= organization %>)",
            impl.getMessage());
    }
}
