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
import org.exoplatform.calendar.service.CompositID;
import org.exoplatform.calendar.service.Calendar.Type;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class AbstractStorage extends BaseComponentPlugin implements Storage {

  @Override
  public CompositID parse(String compositID) {
    if (compositID != null) {
      String[] composit = compositID.split(CompositID.SEPARATOR);      
      try {
        if (composit.length == 2) {
          String id = composit[0];
          Type type = Calendar.Type.valueOf(composit[1]);

          return new CompositID(id, type);          
        } else if (composit.length == 1){
          return new CompositID(null, Type.valueOf(composit[0]));
        }
      } catch(Exception ex) {
        return null;
      }
    }
    
    return null;
  }

}
