================================================================================
==               SpringSource dm Server - <%= project_name %> PAR 
================================================================================
#
# FIX-ME - Add Authors here
#
@author A.N. Other

--------------------------------------------------------------------------------

This directory contains a flavor of the <%= project_name %> that has been
designed as a set of OSGi bundles. In addition, the bundles have been packaged
together as a PAR for convenient deployment to the SpringSource dm Server. To
run the sample, you will need to install version 1.0.0 or later of the
SpringSource dm Server.

--------------------------------------------------------------------------------

PROJECTS

 - <%= organization %>:              build directory for the PAR
#
# FIX-ME - Update and add your included bundles under here
#
 - <%= organization %>.bundle_a:	"A bundle" module
 - <%= organization %>.bundle_b:	"B bundle" module
 - <%= organization %>.bundle_c:	"C bundle" module

--------------------------------------------------------------------------------

BUILD AND TEST

1) $> cd <%= organization %>

2) $> ant clean test


Note: Individual bundle integration tests need to run separately as they are
      not explicity tied to the PAR. Build the required bundle, then run the
      integration test.

		Ensure the correct versions are specified as a dependency within
		the integration test bundle.

      e.g 
        1) $> cd <%= organization %>.bundle_a
		
		2) $> ant clean jar test

		3) $> cd <%= organization %>.bundle_a.integration.test
	
		4) $> ant clean test

--------------------------------------------------------------------------------

BUILD AND DEPLOYMENT

1) $> cd <%= organization %>

2) $> ant clean jar collect-provided package

3) $> cp target/par-provided/bundles/* $SERVER_HOME/repository/bundles/usr

4) $> cp target/par-provided/libraries/* $SERVER_HOME/repository/libraries/usr

5) $> cp target/artifacts/*.par $SERVER_HOME/pickup

#
# FIX-ME : Update the URL if your application exposes a web interface
#
Once you have started the SpringSource dm Server, the sample will be accessible
via the following URL:

http://localhost:8080/web-<%= project_name %>/

--------------------------------------------------------------------------------

IDE SUPPORT

The <%= organization %> PAR application is packaged with Eclipse project
metadata for use with the SpringSource Tool Suite dm Server tooling support. For
detailed information on how to download, install, and use the STS dm Server
tools, consult the Tooling chapter of the SpringSource dm Server Programmer
Guide.

Before you import the projects into your Eclipse workspace, make sure that you
have configured a new Server instance for your locally installed SpringSource
dm Server. Steps 1 through 4 of the BUILD AND DEPLOYMENT section above should
also be completed before proceeding, but replace step 2 with the following
command:

$> ant clean-integration clean jar test collect-provided

Within each bundle included:
============================

$> cd <bundle_name>

$> ant eclipse

This will complete the required eclipse integration by generating the .classpath
entries for all the dependencies.

--------------------------------------------------------------------------------
