/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class EventPageList extends JCRPageList {

  private List<CalendarEvent> eventList_ = null;

  public EventPageList(List<CalendarEvent> eventList, long pageSize) throws Exception {
    super(pageSize);
    eventList_ = eventList;
    setAvailablePage(eventList_.size());
  }

  protected void populateCurrentPage(long page, String username) throws Exception {
    setAvailablePage(eventList_.size());
    long pageSize = getPageSize();
    long position = 0;
    if (page == 1)
      position = 0;
    else {
      position = (page - 1) * pageSize;
    }
    currentListPage_ = new ArrayList<CalendarEvent>();
    Long objPos = position;
    if (position + pageSize > eventList_.size()) {
      currentListPage_ = eventList_.subList(objPos.intValue(), eventList_.size());
    } else {
      Long objPageSize = pageSize;
      currentListPage_ = eventList_.subList(objPos.intValue(), objPos.intValue() + objPageSize.intValue());
    }
  }

  @Override
  public List<CalendarEvent> getAll() throws Exception {
    return eventList_;
  }

}
