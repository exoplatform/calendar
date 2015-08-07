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
  
package org.exoplatform.calendar.storage.jcr;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.NoSuchEntityException;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.security.Identity;

public class MockStorage extends BaseComponentPlugin implements Storage {

  public static final String MOCK_STORAGE = "mock";

  private CalendarDAO calendarDAO;
  private EventDAO eventDAO;
  
  private Calendar sample;
  
  @Override
  public String getId() {
    return MOCK_STORAGE;
  }

  public MockStorage(CalendarService service) {
    calendarDAO = new CalendarDAO() {
      
      @Override
      public Calendar update(Calendar entity) throws NoSuchEntityException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Calendar save(Calendar object) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Calendar remove(String id) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Calendar newInstance() {
        return new Calendar() {
          @Override
          public CalendarType getCalendarType() {
            return new CalendarType() {              
              @Override
              public String getName() {
                return "MOCK";
              }
            };            
          }

          @Override
          public String getName() {
            return "mock calendar";
          }

          @Override
          public String getDS() {
           return MOCK_STORAGE;
          }

          @Override
          public boolean canEdit(String username) {
            return false;
          }          
        };
      }
      
      @Override
      public Calendar getById(String id) {
        return getSample();
      }
      
      @Override
      public List<Calendar> findCalendarsByIdentity(Identity identity, String[] excludeIds) {
        return Arrays.asList(getSample());
      }
    };    
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    return calendarDAO;
  }

  @Override
  public EventDAO getEventDAO() {
    return eventDAO;
  }
  
  private Calendar getSample() {
    if (sample == null) {
      sample = getCalendarDAO().newInstance();
    }
    return sample;
  }
}
