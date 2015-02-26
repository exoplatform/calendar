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

import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventListAccess implements ListAccess<Row> {

  private static Log log = ExoLogger.getLogger(EventListAccess.class);
  
  private JCRDataStorage storage;
  private EventQuery query;
  
  private Integer size;

  public EventListAccess(JCRDataStorage storage, EventQuery eventQuery) {
    this.query = eventQuery;
    this.storage = storage;
  }

  public Row[] load(int offset, int limit) {
    try {
      Session session = storage.getSession(storage.createSystemProvider());
      QueryManager queryMan = session.getWorkspace().getQueryManager();
      QueryImpl jcrQuery = (QueryImpl)queryMan.createQuery(query.getQueryStatement(), query.getQueryType());
      if (limit > 0) {
        jcrQuery.setOffset(offset);
        jcrQuery.setLimit(limit);
      }

      QueryResultImpl queryResult = (QueryResultImpl)jcrQuery.execute();
      RowIterator rows = jcrQuery.execute().getRows();
      
      List<Row> results = new LinkedList<Row>();
      while (rows.hasNext()) {
        results.add(rows.nextRow());
      }
      this.size = queryResult.getTotalSize();
      
      return results.toArray(new Row[results.size()]);
    } catch (InvalidQueryException ex) {
      if(log.isDebugEnabled()) {
        log.debug("JCRQuery is invalid", ex);
      }
      return null;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }
  
  @Override
  public int getSize() throws Exception {
    if (size == null) {
      log.debug("Querying to get Size");
      load(0, 0);
    }
    return size;
  }
}