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

import java.util.Arrays;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.container.component.BaseComponentPlugin;

public class JCRStorage extends BaseComponentPlugin implements Storage {  
  private CalendarDAO calendarDAO;

  public JCRStorage(CalendarService service) {
    calendarDAO = new JCRCalendarDAOImpl(service, this);
  }
  
  public JCRStorage(CalendarDAO calDAO) {
    this.calendarDAO = calDAO;
  }

  @Override
  public List<CalendarType> getSupportedTypes() {
    return Arrays.asList((CalendarType)Calendar.Type.PERSONAL, (CalendarType)Calendar.Type.GROUP);
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    return calendarDAO;
  }

}
