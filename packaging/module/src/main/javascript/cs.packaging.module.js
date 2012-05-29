eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params) {

  var ws = params.ws;
  var portal = params.portal;
  var module = new Module();

  module.version = "${project.version}";
  module.relativeMavenRepo = "org/exoplatform/cs";
  module.relativeSRCRepo = "cs";
  module.name = "cs";

  var cometVersion = "${org.exoplatform.commons.version}";
  
  module.commons = {};
  module.commons.extension = 
    new Project("org.exoplatform.commons", "exo.platform.commons.extension.webapp", "war", "${org.exoplatform.commons.version}");
  module.commons.extension.deployName = "commons-extension";
  
  module.comet = {};
  module.comet.cometd =
    new Project("org.exoplatform.commons", "exo.platform.commons.comet.webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "${org.mortbay.jetty.cometd-bayeux.version}")).
    addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "${org.mortbay.jetty.jetty-util.version}")).
    addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "${org.mortbay.jetty.cometd-api.version}")).
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.comet.service", "jar", cometVersion));
  module.comet.cometd.deployName = "cometd";
  
  // CS

  module.eXoApplication = {};
  module.eXoApplication.upgrade = new Project("org.exoplatform.commons", "exo.platform.commons.component.upgrade", "jar", "${org.exoplatform.commons.version}").
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.component.product", "jar", "${org.exoplatform.commons.version}"));

  
  module.eXoApplication.common = new Project("org.exoplatform.cs", "exo.cs.eXoApplication.common", "jar", module.version).
     addDependency(new Project("org.exoplatform.cs", "exo.cs.component.upgrade", "jar", module.version)).
     addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.webui.ext", "jar", "${org.exoplatform.commons.version}"));

  
    
  module.eXoApplication.calendar =
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.service", "jar",  module.version)).
    addDependency(new Project("ical4j", "ical4j", "jar", "${ical4j.version}")).
    addDependency(new Project("org.apache.jackrabbit", "jackrabbit-webdav", "jar", "${org.apache.jackrabbit.version}")).
    addDependency(new Project("org.apache.jackrabbit", "jackrabbit-jcr-commons", "jar", "${org.apache.jackrabbit.version}"));
  module.eXoApplication.calendar.deployName = "calendar";
    
  // CS resources and services
  module.web = {}
  module.web.webservice =
    new Project("org.exoplatform.cs", "exo.cs.web.webservice", "jar",  module.version);
  module.web.csResources =
    new Project("org.exoplatform.cs", "exo.cs.web.csResources", "war", module.version) ;
  
  //CS extension
  module.extension = {};
  module.extension.webapp =
    new Project("org.exoplatform.cs", "exo.cs.extension.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.extension.config", "jar", module.version));
  module.extension.webapp.deployName = "cs-extension";
  
  /**
   * Configure and add server path for chat, single sign-on
   */
  module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch = 
    new Project("org.exoplatform.cs", "exo.cs.server.tomcat.patch", "jar", module.version);

  module.server.jboss = {}
  module.server.jboss.patch = 
	    new Project("org.exoplatform.cs", "exo.cs.server.jboss.patch", "jar", module.version);
		
  module.server.jboss.patchear = 
	    new Project("org.exoplatform.cs", "exo.cs.server.jboss.patch-ear", "jar", module.version);
  
 
      
  // CS demo 
  module.demo = {};
  // demo portal
  module.demo.portal =
    new Project("org.exoplatform.cs", "exo.cs.demo.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.component.injector", "jar", module.version)).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.demo.config", "jar", module.version));
  module.demo.portal.deployName = "csdemo";  
	   
  module.demo.cometd=
    new Project("org.exoplatform.cs", "exo.cs.demo.cometd-war", "war", module.version);
  module.demo.cometd.deployName = "cometd-csdemo";
	
  // demo rest endpoint	
  module.demo.rest =
    new Project("org.exoplatform.cs", "exo.cs.demo.rest-war", "war", module.version).
    addDependency(ws.frameworks.servlet);
  module.demo.rest.deployName = "rest-csdemo"; 
       
   
   
   return module;
}

/**
 * Configure and deploy Openfire for integrated chat on cs 
 */


