package rina.flowallocator.impl;

import junit.framework.TestCase;
import rina.flowallocator.impl.BundleService;
import rina.flowallocator.impl.impl.BundleServiceImpl;

public class BundleServiceImplTests extends TestCase {
    private BundleService impl;

    protected void setUp() {
        impl = new BundleServiceImpl();
    }

    protected void tearDown() {
        impl = null;
    }

    public void testGetMessage() {
        assertEquals("Hello World from Bundle (rina)",
            impl.getMessage());
    }
}
