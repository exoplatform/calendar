package org.exoplatform.calendar.service;

import org.exoplatform.calendar.service.MockStorage.MockCalendarDAO;
import org.exoplatform.calendar.service.MockStorage.MockEventDAO;
import org.exoplatform.calendar.service.test.BaseCalendarServiceTestCase;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestCalendarService extends BaseCalendarServiceTestCase {

  public void testInitServices() throws Exception{
    assertNotNull(calendarService_) ;
    assertNotNull(xCalService.getCalendarHandler());

    Storage src = xCalService.lookForDS(MockStorage.ID);
    assertEquals(MockStorage.class, src.getClass());
    assertEquals(MockCalendarDAO.class, src.getCalendarDAO().getClass());
    assertEquals(MockEventDAO.class, src.getEventDAO().getClass());
  }
}
