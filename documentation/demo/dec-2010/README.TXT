Deployment Demos:
=================

1. Consult the previous simple-ping demo for the detailed setup.

2. Configure the OpenFire server with the database in db/openfire.db (Same as the Simple-Ping demo)

Virgo Web Server (Repository Configuration)

	3. Copy all the bundles in "usr" to the repository/usr directory.

	4. Copy all "tinos" bundles in the "bundles" directory to repository/usr

	5. Copy "config"/serviceability.xml to the config directory (Adds TINOS debug logging).

Demo Core:
==========

For all demos, the following plans must be put in place:
	1. Copy plans/core/*.plan to the pickup directory.
		- Adds Config.Manager
		- Adds Logger
		- Adds Snaps Support for the Console


Start the Virgo Server, with only the core plans deployed. Then proceed with the relevant demo. When the demo is complete shutdown the server
and remove the specific demo plans.


For the DNS Demo:
=================
	1. Copy the plans/dns-lookup/org.pouzinsociety.dns.server.plan into the Virgo pickup directory. This deploys the DNS Server.

	2. Copy the plans/dns-lookup/org.pouzinsociety.dns.client.plan into the Virgo pickup directory. This deploys the DNS Client & initiates a lookup.

	3. View log file : virgo/serviceability/logs/org.pouzinsociety.dns.client.plan-1/log.log

For the Web Console Demo:
=========================
	1. Copy the plans/web-console/org.pouzinsociety.console.plan into the Virgo pickup directory. This deploys a TINOS node with WebConsole.

	2. Visit http://localhost:8080/node
		Snaps:  http://localhost:8080/node/devices
			http://localhost:8080/node/sample

For the Socket Test:
====================
	1. Copy the plans/web-console/org.pouzinsociety.socket.plan into the Virgo pickup directory. This deploys a TINOS node with TCP Server/Client.

	2. View log file : virgo/serviceability/logs/org.pouzinsociety.socket.plan-1/log.log

	3. You can modify the plan for UDP (but restart the server to get the changes).
