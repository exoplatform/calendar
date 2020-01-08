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

import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.impl.*;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.webservice-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.webservice-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.webservice-local-configuration.xml"),
})
public abstract class BaseTest extends AbstractKernelTest {

  protected PortalContainer container;

  protected ProviderBinder providers;

  protected ResourceBinder     binder;

  protected RequestHandlerImpl requestHandler;

  protected OrganizationService  orgService;

  public BaseTest() {
    setForceContainerReload(true);
  }

  public void setUp() throws Exception {
    super.setUp();
    container = getContainer();
    ExoContainerContext.setCurrentContainer(container);
    orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    binder.clear();
    begin();
  }

  protected void start() {
    ((ComponentRequestLifecycle)orgService).startRequest(container);
  }

  protected void stop() {
    ((ComponentRequestLifecycle)orgService).endRequest(container);
  }  
}
