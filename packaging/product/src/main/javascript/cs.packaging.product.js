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
  var cs = Module.GetModule("cs", {portal:portal, ws:ws});
  
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
  

//cometd (requried for CS)
  product.addDependencies(cs.comet.cometd);
   
  // CS extension
  product.addDependencies(cs.eXoApplication.upgrade);
  product.addDependencies(cs.eXoApplication.common);
  product.addDependencies(cs.eXoApplication.calendar);
  product.addDependencies(cs.web.csResources); 
  product.addDependencies(cs.web.webservice);
  product.addDependencies(cs.commons.extension);

  // CS demo
  product.addDependencies(cs.demo.portal);
  product.addDependencies(cs.demo.cometd);
  product.addDependencies(cs.demo.rest);
  
  product.addDependencies(new Project("org.exoplatform.commons", "exo.platform.commons.component", "jar", "${org.exoplatform.commons.version}"));
  
  product.addServerPatch("tomcat", cs.server.tomcat.patch) ;
  //product.addServerPatch("jboss",  cs.server.jboss.patch) ;
  product.addServerPatch("jbossear", cs.server.jboss.patchear) ;

  /* cleanup duplicated lib */
  //product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));

  product.module = cs ;
  product.dependencyModule = [ kernel, core, ws, eXoJcr];

  return product ;
}
