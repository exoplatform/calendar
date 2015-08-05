package org.exoplatform.calendar.service;

import java.util.HashMap;
import java.util.List;

import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.security.Identity;

public class MockStorage extends BaseComponentPlugin implements Storage {

  public static final String ID = "MOCK_STORAGE";

  CalendarDAO calDAO;

  EventDAO eventDAO;

  @Override
  public String getId() {
    return ID;
  }
    
  @Override
  public CalendarDAO getCalendarDAO() {
    if (calDAO == null) {
      calDAO = new MockCalendarDAO();
    }
    return calDAO;
  }

  @Override
  public EventDAO getEventDAO() {
    if (eventDAO == null) {
      eventDAO = new MockEventDAO();
    }
    return eventDAO;
  }

  class MockCalendarDAO implements CalendarDAO {

    private HashMap<String, Calendar> cals = new HashMap<String, Calendar>();

    @Override
    public Calendar getById(String id) {
      return cals.get(id);
    }

    @Override
    public Calendar save(Calendar cal) {
      cals.put(cal.getId(), cal);
      return cal;
    }

    @Override
    public Calendar remove(String id) {
      return cals.remove(id);
    }

    @Override
    public Calendar newInstance() {
      return new MockCalendar();
    }

    @Override
    public List<Calendar> findCalendarsByIdentity(Identity identity, String[] excludeIds) {
      return null;
    }

    @Override
    public Calendar update(Calendar entity) {
      return null;
    }
  }

  class MockEventDAO implements EventDAO {

    @Override
    public CalendarEvent getById(String id) {
      return null;
    }

    @Override
    public CalendarEvent save(CalendarEvent object) {
      return null;
    }

    @Override
    public CalendarEvent remove(String id) {
      return null;
    }

    @Override
    public CalendarEvent newInstance() {
      return null;
    }

    @Override
    public ListAccess<CalendarEvent> findEventsByQuery(org.exoplatform.calendar.model.query.EventQuery eventQuery) throws CalendarException {
      return null;
    }

    @Override
    public CalendarEvent update(CalendarEvent entity) {
      return null;
    }
  }
}
