eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params) {

  var ws = params.ws;
  var portal = params.portal;
  var module = new Module();

  module.version = "${project.version}";
  module.relativeMavenRepo = "org/exoplatform/calendar";
  module.relativeSRCRepo = "calendar";
  module.name = "calendar";

  var cometVersion = "${org.exoplatform.commons.version}";
  
  module.commons = {};
  module.commons.extension = 
    new Project("org.exoplatform.commons", "exo.platform.commons.extension.webapp", "war", "${org.exoplatform.commons.version}").
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.webui", "jar", "${org.exoplatform.commons.version}"));
  module.commons.extension.deployName = "commons-extension";
  
  module.comet = {};
  module.comet.cometd =
    new Project("org.exoplatform.commons", "exo.platform.commons.comet.webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "${org.mortbay.jetty.cometd-bayeux.version}")).
    addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "${org.mortbay.jetty.jetty-util.version}")).
    addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "${org.mortbay.jetty.cometd-api.version}")).
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.comet.service", "jar", cometVersion));
  module.comet.cometd.deployName = "cometd";
  
  // Calendar

  module.upgrade = new Project("org.exoplatform.commons", "exo.platform.commons.component.upgrade", "jar", "${org.exoplatform.commons.version}").
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.component.product", "jar", "${org.exoplatform.commons.version}"));

  
  module.common = new Project("org.exoplatform.calendar", "calendar-common", "jar", module.version).
     addDependency(new Project("org.exoplatform.calendar", "calendar-upgrade", "jar", module.version)).
     addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.webui.ext", "jar", "${org.exoplatform.commons.version}"));

  
    
  module.calendar =
    new Project("org.exoplatform.calendar", "calendar-webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.calendar", "calendar-service", "jar",  module.version)).
    addDependency(new Project("ical4j", "ical4j", "jar", "${ical4j.version}")).
    addDependency(new Project("org.apache.jackrabbit", "jackrabbit-webdav", "jar", "${org.apache.jackrabbit.version}")).
    addDependency(new Project("org.apache.jackrabbit", "jackrabbit-jcr-commons", "jar", "${org.apache.jackrabbit.version}"));
  module.calendar.deployName = "calendar";
    
  // Calendar resources and services
  module.web = {}
  module.web.webservice =
    new Project("org.exoplatform.calendar", "calendar-webservice", "jar",  module.version);
  module.web.resources =
    new Project("org.exoplatform.calendar", "calendar-resources", "war", module.version) ;
  module.web.resources.deployName = "csResources"
  //Calendar extension
  module.extension = {};
  module.extension.webapp =
    new Project("org.exoplatform.calendar", "calendar-extension-webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.calendar", "calendar-extension-config", "jar", module.version));
  module.extension.webapp.deployName = "calendar-extension";
  
  /**
   * Configure and add server path for chat, single sign-on
   */
  module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch = 
    new Project("org.exoplatform.calendar", "calendar-server-tomcat-patch", "jar", module.version);

  module.server.jboss = {}
  module.server.jboss.patch = 
	    new Project("org.exoplatform.calendar", "calendar-server-jboss-patch", "jar", module.version);
		
  module.server.jboss.patchear = 
	    new Project("org.exoplatform.calendar", "calendar-server-jboss-ear", "jar", module.version);
  
 
      
  // Calendar demo 
  module.demo = {};
  // demo portal
  module.demo.portal =
    new Project("org.exoplatform.calendar", "calendar-demo-webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.calendar", "calendar-injector", "jar", module.version)).
    addDependency(new Project("org.exoplatform.calendar", "calendar-demo-config", "jar", module.version));
  module.demo.portal.deployName = "csdemo";  
	   
  module.demo.cometd=
    new Project("org.exoplatform.calendar", "calendar-demo-cometd", "war", module.version);
  module.demo.cometd.deployName = "cometd-csdemo";
	
  // demo rest endpoint	
  module.demo.rest =
    new Project("org.exoplatform.calendar", "calendar-demo-rest", "war", module.version).
    addDependency(ws.frameworks.servlet);
  module.demo.rest.deployName = "rest-csdemo"; 
   
   return module;
}
