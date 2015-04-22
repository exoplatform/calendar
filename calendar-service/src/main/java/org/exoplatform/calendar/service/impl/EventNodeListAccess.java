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

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventNodeListAccess extends AbstractEventListAccess<Node> {

  private static Log log = ExoLogger.getLogger(EventNodeListAccess.class);

  public EventNodeListAccess(EventDAOImpl evtDAO, EventQuery eventQuery) {
    super(evtDAO, eventQuery);
  }

  public Node[] load(int offset, int limit) {
    try {
      QueryResultImpl queryResult = super.loadData(offset, limit);
      if (queryResult != null) {
        NodeIterator nodes = queryResult.getNodes();
        
        List<Node> results = new LinkedList<Node>();
        while (nodes.hasNext()) {
          results.add(nodes.nextNode());
        }      
        
        return results.toArray(new Node[results.size()]);        
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }
}