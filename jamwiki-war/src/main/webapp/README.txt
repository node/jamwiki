INTRODUCTION
============

Please see http://jamwiki.org for the latest notes and documentation.  This
file attempts to provide basic information for getting an instance of JAMWiki
running on your web application server.  In addition, instructions are
provided for those interested in building JAMWiki from source.


PREREQUISITES
=============

JAMWiki requires a web application server (such as Tomcat or Websphere) that
supports the following specifications:

  Java 5 or later
  Servlet 2.5 or later
  JSP 2.1 or later
  JDBC 3.0 or later (if using an external database)

JAMWiki can be configured to store its data either in an external database or
using an embedded version of the HSQL database that is included in the
distribution.  When running with an embedded database no additional software
or configuration is required; when using an external database JAMWiki requires
a database user with permission to create tables and sequences.  JAMWiki has
built-in support for recent versions of the following databases:

  H2
  HSQL
  MS SQL
  MySQL
  Oracle
  Postgres
  DB2 (experimental)
  DB2/400 (experimental)
  Sybase (experimental)

Note that JAMWiki should work with any ANSI compliant database.  Also note that
to support double-byte characters (such as Chinese, Hindi or Japanese) the
database should use UTF-8 encoding.


INSTALLATION
============

See http://jamwiki.org/wiki/en/Installation for the complete installation
instructions; see the UPGRADE.txt for the complete upgrade instructions - DO
NOT attempt to upgrade JAMWiki using the new installation process!

The basic steps for performing a new JAMWiki install are:

  1. Download the latest JAMWiki release from
     http://sourceforge.net/projects/jamwiki/.
  2. Deploy the JAMWiki WAR file.  See your web application server's
     documentation for specific deployment instructions.  The WAR file should
     be deployed as an exploded WAR to avoid potential issues.
  3. Update the logback.xml configuration file with appropriate log
     configuration information.  The logback.xml file can be found in the
     /WEB-INF/classes/ directory of your installation.  Note that the web
     application server must have appropriate permissions to write to the log
     file.
  4. (Optional) If using an external database create a new database instance
     that can be used by JAMWiki, or verify that a database user is available
     with permission to create tables and sequences.  If support is needed for
     double-byte character sets be sure that the database uses UTF-8 encoding.
  5. (Optional) If using an external database verify that your JDBC driver is
     in the web application server's classpath.
  6. Once the JAMWiki WAR file has been deployed and the web application
     server started, view the http://<server>/<context>/ page, where <server>
     is the application server URL, and <context> is the application server
     context.  The JAMWiki configuration process will begin automatically.

The configuration process begins automatically with the first JAMWiki pageview
after setup.  Configuration will request the following information:

  1. A directory (accessible to the application server) into which JAMWiki
     files can be written.
  2. The login and password of an administrative user.
  3. (Optional) If using an external database for storage then the database
     settings must be provided (see the "Database Settings" section below).
  4. (Optional) Once setup is complete, JAMWiki can be customized by using the
     Special:Admin page, accessible to admins by clicking on the "Admin" link
     on the top right portion of all JAMWiki pages.

Once the configuration settings have been verified JAMWiki will create the
admin user account, database tables (if using an external database), base
properties, and default topics.  Once configuration is complete JAMWiki
redirects to the wiki home page and is ready for use.  If any problems occur
during installation please review the detailed installation instructions on
http://jamwiki.org/wiki/en/Installation.  If your problem persists please
report it on http://jira.jamwiki.org/ and include any relevant information
from your log files with the problem report.


UPGRADES
========

See the UPGRADE.txt file or http://jamwiki.org/wiki/en/Installation#Upgrades
for detailed upgrade instructions.  In most cases the JAMWiki upgrade process
can be handled using an automated process.  If an upgrade fails please report
the error on jamwiki.org, and then follow the manual upgrade steps outlined in
the UPGRADE.txt document.


DATABASE SETTINGS
=================

JAMWiki can operate using a pre-configured, embedded database for storage, or
using an external database.  For larger implementations an external database
is highly recommended.  To utilize an external database the following steps
are required:

  1. Install an appropriate JDBC driver in your web application server's
     classpath.  A typical location is the WEB-INF/lib directory.  JDBC
     driver packages can normally be obtained from the database vendor.
  2. Create the JAMWiki database.  JAMWiki can also use an existing database.
     Note that sites which support double-byte character sets must use a
     database with UTF-8 support.
  3. Create a database user for JAMWiki.  The user must have permission to
     create tables and sequences.
  4. During the configuration process select your database type from the
     dropdown menu.  If your database is not listed select the "ANSI" option,
     which will use ANSI SQL for all queries.
  5. Enter the driver, url, username and password information.  Consult your
     database documentation to determine the appropriate values for each of
     these fields.  Some example values are below:

       JDBC driver class: org.postgresql.Driver
       JDBC driver class: com.mysql.jdbc.Driver
       JDBC driver class: oracle.jdbc.driver.OracleDriver
       Database type    : as appropriate
       Database URL     : jdbc:postgresql://localhost:5432/<database_name>
       Database URL     : jdbc:mysql://localhost:3306/<database_name>
       Database URL     : jdbc:oracle:thin:@//localhost:1521/<service_name>
       Database Username: as appropriate
       Database Password: as appropriate

  6. Once all configuration information has been entered JAMWiki will verify
     that a connection can be established with the database and will then
     create all required tables.  If any failures are reported check the logs
     for information about the specific failure type.


VIRTUAL WIKIS
=============

JAMWiki provides the capability to create "virtual wikis", which are distinct
wikis running under the same web application.  By default, a virtual wiki
named "en" is created during installation.  The default URL for files within
this virtual wiki is then of the form "http://<server>/<context>/en/Topic".
To create a new virtual wiki the following steps are required:

1. Access the Special:VirtualWiki page (JAMWiki 0.9.0+) or for earlier
   JAMWiki releases, access the Special:Maintenance page.
2. Scroll down to the "add virtual wiki" box, enter a name (one word, no
   spaces) and click add.  A database record for the virtual wiki will be
   created, but the virtual wiki will not yet be active.
3. Shut down the web application server.
4. Edit the web application web.xml file.  There will be a mapping of the
   form:

    <servlet-mapping>
        <servlet-name>jamwiki</servlet-name>
        <url-pattern>/en/*</url-pattern>
    </servlet-mapping>

5. Create a new servlet-mapping, replacing "en" in the above example with the
   name of the new virtual wiki.
6. Restart the web application server.
7. A new virtual wiki will now be available from URLs of the form
   http://example.com/context/virtual-wiki/


BUILDING FROM SOURCE
====================

See http://jamwiki.org/wiki/en/Building_from_Source for the latest
instructions about building the JAMWiki source.  The instructions below
provide a brief overview of the JAMWiki build process.

The JAMWiki source is available from the Subversion source repository on
Sourceforge.net.  To check out the code, first install Subversion and then
execute the following command:

  svn co https://jamwiki.svn.sourceforge.net/svnroot/jamwiki/wiki/trunk jamwiki

This command will copy the current development code (the "trunk") into a local
directory named "jamwiki".

The software can be built from the Maven build script provided.  To build the
software, install Maven (http://maven.apache.org/) and a JDK version 1.5 or
later.  Once Maven and the JDK are properly installed, run the following
commands:

  cd trunk/jamwiki
  mvn package

The software should build, and when complete a jamwiki-x.x.x.war file will be
located in the trunk/jamwiki/target/ directory.  Consult your web application
server's documentation for instructions on how to install this file.
