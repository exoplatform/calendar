/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 */

package org.exoplatform.webservice.cs.rest;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.ProviderBinder;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/org/exoplatform/services/organization/TestOrganization-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.cs.webservice.rest.test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration1.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration2.xml")
  })
public abstract class BaseTest extends AbstractKernelTest {

  protected PortalContainer container;
  
  protected ProviderBinder providers;

  protected ResourceBinder     binder;

  protected RequestHandlerImpl requestHandler;
  
  protected OrganizationService  orgService;
 
  protected ChromatticManager chromatticManager;

  public void setUp() throws Exception {
    container = PortalContainer.getInstance();
    chromatticManager = (ChromatticManager)container.getComponentInstanceOfType(ChromatticManager.class);
    orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    binder.clear();
  }
  
  protected void start() {
    ((ComponentRequestLifecycle)orgService).startRequest(container);
  }
  
  protected void stop() {
    ((ComponentRequestLifecycle)orgService).endRequest(container);
  }  
  
  public void tearDown() throws Exception {
  }

  public boolean registry(Object resource) throws Exception {
//    container.registerComponentInstance(resource);
    return binder.bind(resource);
  }

  public boolean registry(Class<?> resourceClass) throws Exception {
//    container.registerComponentImplementation(resourceClass.getName(), resourceClass);
    return binder.bind(resourceClass);
  }

  public boolean unregistry(Object resource) {
//    container.unregisterComponentByInstance(resource);
    return binder.unbind(resource.getClass());
  }

  public boolean unregistry(Class<?> resourceClass) {
//    container.unregisterComponent(resourceClass.getName());
    return binder.unbind(resourceClass);
  }

}
