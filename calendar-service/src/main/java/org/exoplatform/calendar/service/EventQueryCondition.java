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
  
package org.exoplatform.calendar.service;

public class EventQueryCondition extends QueryCondition {
  public static enum CONDITIONS {
    TYPE, TEXT, CATEGORY_IDS, CALENDAR_COMPOSITE_IDS, CALENDAR_TYPE, FROM_DATE, TO_DATE, PRIORITY, STATE, PARTICIPANTS,  NONE_REPEAT,
    EXCLUDE_PRV_EVENT_CALENDAR_IDS
  }

  @Override
  public QueryCondition with(Expression<?> expression) {
    if (expression.getName().equals(CONDITIONS.CALENDAR_TYPE) && parent != null) {
      throw new IllegalStateException();
    }
    
    return super.with(expression);
  }
  
}
