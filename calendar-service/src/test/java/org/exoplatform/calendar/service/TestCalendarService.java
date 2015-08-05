package org.exoplatform.calendar.service;

import org.exoplatform.calendar.service.MockStorage.MockCalendarDAO;
import org.exoplatform.calendar.service.MockStorage.MockEventDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.calendar.service.test.BaseCalendarServiceTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-calendar-service.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.portal-configuration.xml")
})
public class TestCalendarService extends BaseCalendarServiceTestCase {

  public void testInitServices() throws Exception{
    assertNotNull(calendarService_) ;
    assertNotNull(calendarService_.getCalendarHandler());

    Storage src = calendarService_.lookForDS(MockStorage.ID);
    assertEquals(MockStorage.class, src.getClass());
    assertEquals(MockCalendarDAO.class, src.getCalendarDAO().getClass());
    assertEquals(MockEventDAO.class, src.getEventDAO().getClass());
  }
}
