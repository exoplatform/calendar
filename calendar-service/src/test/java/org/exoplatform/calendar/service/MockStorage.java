package org.exoplatform.calendar.service;

import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.calendar.service.storage.jcr.AbstractStorage;
import org.exoplatform.commons.utils.ListAccess;

public class MockStorage extends AbstractStorage {

  public static final CalendarType MOCK_CAL_TYPE = new CalendarType(){
    @Override
    public String getName() {
      return "mock";
    }};
  
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
    public Calendar save(Calendar object) {
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

    @Override
    public Calendar update(Calendar entity) {
      return null;
    }
  }

  class MockEventDAO implements EventDAO {

    @Override
    public CalendarEvent getById(String id, CalendarType calType) {
      return null;
    }

    @Override
    public CalendarEvent save(CalendarEvent object) {
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
    public ListAccess<CalendarEvent> findEventsByQuery(EventQueryCondition eventQuery) throws CalendarException {
      return null;
    }

    @Override
    public CalendarEvent update(CalendarEvent entity) {
      return null;
    }
  }


}
