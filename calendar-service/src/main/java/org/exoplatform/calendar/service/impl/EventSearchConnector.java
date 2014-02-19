/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service.impl;

import java.util.Collection;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 22, 2013  
 */
public class EventSearchConnector extends CalendarSearchServiceConnector {

  public EventSearchConnector(InitParams initParams) {
    super(initParams);
  }

  public Collection<SearchResult> search(SearchContext context, String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    return searchData(context, CalendarEvent.TYPE_EVENT, query, sites, offset, limit, sort, order);
  }

}
