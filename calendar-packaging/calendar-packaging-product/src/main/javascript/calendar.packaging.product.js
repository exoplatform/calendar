eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoPortal" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "portal" ;//module in modules/portal/module.js
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ; // CHANGED for CS to match portal version. It was ${project.version}

  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws", {kernel : kernel, core : core});
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr});
  var calendar = Module.GetModule("calendar", {portal:portal, ws:ws});
  
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.dashboard) ;
  product.addDependencies(portal.eXoGadgetServer) ;
  product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.webui.portal);
  
  product.addDependencies(portal.web.eXoResources);

  product.addDependencies(portal.web.portal);
  
  //portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  //portal.starter.deployName = "starter";
  //product.addDependencies(portal.starter);  
  

//cometd (requried for Calendar)
  product.addDependencies(calendar.comet.cometd);
   
  // Calendar extension
  product.addDependencies(calendar.upgrade);
  product.addDependencies(calendar.common);
  product.addDependencies(calendar.calendar);
  product.addDependencies(calendar.web.resources); 
  product.addDependencies(calendar.web.webservice);
  product.addDependencies(calendar.commons.extension);

  // Calendar demo
  product.addDependencies(calendar.demo.portal);
  product.addDependencies(calendar.demo.cometd);
  product.addDependencies(calendar.demo.rest);
  
  product.addDependencies(new Project("org.exoplatform.commons", "exo.platform.commons.component", "jar", "${org.exoplatform.commons.version}"));
  
  product.addServerPatch("tomcat", calendar.server.tomcat.patch) ;
  //product.addServerPatch("jboss",  cs.server.jboss.patch) ;
  product.addServerPatch("jbossear", calendar.server.jboss.patchear) ;

  /* cleanup duplicated lib */
  //product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));

  product.module = calendar ;
  product.dependencyModule = [ kernel, core, ws, eXoJcr];

  return product ;
}
