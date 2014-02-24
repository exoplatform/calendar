/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 */
package org.exoplatform.webservice.cs;

import java.util.GregorianCalendar;
import javax.jcr.Node;
import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 29, 2009  
 */
public class LastUpdateAction implements Action {

  public boolean execute(Context context) throws Exception {
    try {
      Node node = (Node)context.get("currentItem");
      if(!node.isNodeType("exo:datetime")){
        if(node.canAddMixin("exo:datetime")) {
          node.addMixin("exo:datetime");            
        }
        node.setProperty("exo:dateCreated",new GregorianCalendar());
        node.setProperty("exo:dateModified",new GregorianCalendar());  
      }
      else
        node.setProperty("exo:dateModified",new GregorianCalendar());  
    } catch (ClassCastException e){
      PropertyImpl property = (PropertyImpl)context.get("currentItem");
      NodeImpl parent = (NodeImpl)property.getParent();
      if(!parent.isNodeType("exo:calendarEvent"))
        throw new RuntimeException("incoming node is not exo:calendarEvent");
      parent.setProperty("exo:dateModified",new GregorianCalendar());
    }
    return false;
  }

}
