/**
 * Copyright (C) 2003-20012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/

====================================================== 
    Release Notes - eXo Calendar - Version 4.0
======================================================
===============
1 Introduction
===============


*eXo Calendar helps you to organize and order time in your life more easily and efficiently. It
also manages your shared and public agendas in your organization. eXo Calendar provides
iCal support that you can use to exchange event/task information with other applications.
Searching for events/tasks in calendars also very convenient with full text search and advanced search with many criteria.


=============
2 What's new?
=============


    * General
                  
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6 or higher
        Application Server : jboss-5.1.0.GA
        Building Tools: Maven 2.2.1 and up
       

- Collaboration suite quick start guide
  Collaboration suite have a server need to run at same time to use:
    +) jobs: this is main jobss server include Collaboration web applications and all dependencies.     
    

Need to set the JAVA_HOME variable for run server.
+) How to start server:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.6"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
    
   
   * Start jboss server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to jboss/bin and type the command:
         run.bat start

     +) On Unix/Linux/cygwin
       Open a terminal, go to jboss/bin and type the command:
         ./run.sh
    
   
-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:
   Classic :
      http://localhost:8080/portal
      http://localhost:8080/portal/public/classic
   Demo portal  
      http://localhost:8080/csdemo

You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "gtn".

* Direct link to access applications in calendar application:
    +) Calendar application: http://localhost:8080/portal/private/classic/calendar     
   
  You will get login form if you are not yet logged in to portal

==============
4 KNOWN ISSUES
==============
 
===========
5 RESOURCES
===========

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Community gatein    http://www.jboss.org/gatein/ 
     Developers wiki     http://wiki.exoplatform.org


===========
6 CHANGELOG
===========
