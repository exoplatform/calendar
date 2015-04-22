/*
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.service.impl;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventRowListAccess extends AbstractEventListAccess<Row> {

  private static Log log = ExoLogger.getLogger(EventRowListAccess.class);

  public EventRowListAccess(EventDAOImpl evtDAO, EventQuery eventQuery) {
    super(evtDAO, eventQuery);
  }

  public Row[] load(int offset, int limit) {
    try {
      QueryResultImpl queryResult = super.loadData(offset, limit);
      if (queryResult != null) {
        RowIterator rows = queryResult.getRows();
        
        List<Row> results = new LinkedList<Row>();
        while (rows.hasNext()) {
          results.add(rows.nextRow());
        }      
        
        return results.toArray(new Row[results.size()]);        
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }
}