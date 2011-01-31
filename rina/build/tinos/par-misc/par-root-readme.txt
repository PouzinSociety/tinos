================================================================================
==               SpringSource dm Server - <%= project_name %> PAR   
================================================================================

@author FIX-ME - Add your Author Name here

--------------------------------------------------------------------------------

OVERVIEW

The SpringSource dm Server - <%= project_name%> PAR distribution has been designed
as a set of OSGi bundles. In addition, the bundles have been packaged together
as a PAR for convenient deployment to the SpringSource dm Server. To run the
prototype, you will need to install version 1.0.0 or later of the SpringSource
dm Server.

For further details on installing, deploying to, and developing applications for
the SpringSource dm Server please consult the User and Programmer guides.

--------------------------------------------------------------------------------

CONTENTS

The root of the distribution contains the following folders:

 - dist:         pre-built and pre-packaged artifacts which can be deployed
                 directly on the server.
 - par-provided: the collection of OSGi bundles and library descriptors which
                 will be required to run the sample application on the server.
 - projects:     the source code and build scripts for building and packaging
                 the prototype projects from the command line.

--------------------------------------------------------------------------------

PRE-PACKAGED DISTRIBUTABLES

If you would simply like to see the sample application in action, you can hot
deploy the PAR from the command line or via drag-and-drop semantics in your
operating system's file system explorer (e.g., Windows Explorer or Mac Finder)
by copying the PAR file to the server's 'pickup' directory as outlined below.
Alternatively, you can deploy the application via the web-based admin console
(e.g., http://localhost:8080/admin) or via the SpringSource Tool Suite 
dm Server support in Eclipse.

Steps to deploy the PAR and its dependencies:

 - Copy par-provided/bundles/* to the SERVER_HOME/repository/bundles/usr
   directory.

 - Copy par-provided/libraries/* to the SERVER_HOME/repository/libraries/usr
   directory.

 - Copy dist/*.par to the SERVER_HOME/pickup directory.

 - Start the SpringSource dm Server

 - If you have a web interface within your par:

		Open http://localhost:8080/<%= project_name %>/

 
--------------------------------------------------------------------------------

MANUALLY BUILDING PROJECTS & ECLIPSE IDE SUPPORT

If you would like to manually build the projects in the sample application or
import them into Eclipse, please consult the "readme.txt" file in the projects
directory. 

--------------------------------------------------------------------------------
