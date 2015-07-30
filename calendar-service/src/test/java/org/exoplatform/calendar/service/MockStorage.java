package org.exoplatform.calendar.service;

import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.BaseComponentPlugin;

public class MockStorage extends BaseComponentPlugin implements Storage {

  public static final CalendarType MOCK_CAL_TYPE = new CalendarType(){};
  
  @Override
  public boolean isTypeSupported(CalendarType type) {
    return MOCK_CAL_TYPE.equals(type);
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    return new MockCalendarDAO();
  }

  @Override
  public EventDAO getEventDAO() {
    return new MockEventDAO();
  }

  class MockCalendarDAO implements CalendarDAO {

    @Override
    public Calendar getById(String id, CalendarType calType) {
      return null;
    }

    @Override
    public Calendar save(Calendar object, boolean isNew) {
      return null;
    }

    @Override
    public Calendar remove(String id, CalendarType calType) {
      return null;
    }

    @Override
    public Calendar newInstance(CalendarType type) {
      return null;
    }

    @Override
    public ListAccess<Calendar> findCalendarsByQuery(CalendarQuery query) {
      return null;
    }
  }

  class MockEventDAO implements EventDAO {

    @Override
    public CalendarEvent getById(String id, CalendarType calType) {
      return null;
    }

    @Override
    public CalendarEvent save(CalendarEvent object, boolean isNew) {
      return null;
    }

    @Override
    public CalendarEvent remove(String id, CalendarType calType) {
      return null;
    }

    @Override
    public CalendarEvent newInstance(CalendarType type) {
      return null;
    }

    @Override
    public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException {
      return null;
    }
  }
}
