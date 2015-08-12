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
  
package org.exoplatform.calendar.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.service.CalendarHandler;
import org.exoplatform.calendar.service.EventHandler;
import org.exoplatform.calendar.service.ExtendedCalendarService;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.calendar.storage.jcr.JCRStorage;
import org.exoplatform.container.component.ComponentPlugin;

public class ExtendedCalendarServiceImpl implements ExtendedCalendarService {
  
  private CalendarHandler calendarHandler;

  private EventHandler eventHandler;  
  
  private Map<String, Storage> storages = new HashMap<String, Storage>();
  
  public ExtendedCalendarServiceImpl() {
    this.calendarHandler = new CalendarHandlerImpl(this);
    this.eventHandler = new EventHandlerImpl(this);
  }
  
  @Override
  public Storage lookForDS(String id) {
    if (id != null) {
      return storages.get(id);
    }
    return storages.get(JCRStorage.JCR_STORAGE);
  }

  @Override
  public CalendarHandler getCalendarHandler() {
    return calendarHandler;
  }

  @Override
  public EventHandler getEventHandler() {
    return eventHandler;
  }
  
  public void addDataStore(ComponentPlugin dao) {
    if (dao instanceof Storage) {
      Storage storage = (Storage) dao;
      synchronized (this) {
          storages.put(storage.getId(), storage);
      }
    }
  }
  
  List<Storage> getAllStorage() {
    return new ArrayList<>(storages.values());
  }
}
