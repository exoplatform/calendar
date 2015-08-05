/**
 * Copyright (C) 2015 eXo Platform SAS.
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
  
package org.exoplatform.calendar.service.storage.jcr;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.container.component.BaseComponentPlugin;

public class JCRStorage extends BaseComponentPlugin implements Storage {

  public static final String JCR_STORAGE = "jcr";

  private CalendarDAO calendarDAO;
  private EventDAO eventDAO;
  
  @Override
  public String getId() {
    return JCR_STORAGE;
  }

  public JCRStorage(CalendarService service) {
    calendarDAO = new JCRCalendarDAOImpl(service, this);
    eventDAO = new JCREventDAOImpl(service, this);
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    return calendarDAO;
  }

  @Override
  public EventDAO getEventDAO() {
    return eventDAO;
  }
}
