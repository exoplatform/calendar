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

import javax.jcr.query.InvalidQueryException;

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class AbstractEventListAccess<T> implements ListAccess<T> {

  private static Log log = ExoLogger.getLogger(AbstractEventListAccess.class);
  
  private EventDAOImpl evtDAO;
  private EventQuery query;
  
  private Integer size;

  public AbstractEventListAccess(EventDAOImpl evtDAO, EventQuery eventQuery) {
    this.query = eventQuery;
    this.evtDAO = evtDAO;
  }
  
  protected QueryResultImpl loadData(int offset, int limit) {
    try {
      QueryImpl jcrQuery = evtDAO.createJCRQuery(query.getQueryStatement(), query.getQueryType());
      if (limit > 0) {
        jcrQuery.setOffset(offset);
        jcrQuery.setLimit(limit);
      }

      QueryResultImpl queryResult = (QueryResultImpl)jcrQuery.execute();
      this.size = queryResult.getTotalSize();
      
      return queryResult;
    } catch (InvalidQueryException ex) {
      if(log.isDebugEnabled()) {
        log.debug("JCRQuery is invalid", ex);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }

  public abstract T[] load(int offset, int limit);
  
  @Override
  public int getSize() throws Exception {
    if (size == null) {
      log.debug("Querying to get Size");
      load(0, 0);
    }
    return size;
  }
}