package org.exoplatform.calendar.service;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.calendar.service.storage.*;
import org.exoplatform.calendar.service.storage.EventDAO;
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
    public Calendar getById(String id) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Calendar getById(String id, CalendarType calType) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Calendar save(Calendar object, boolean isNew) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Calendar remove(String id, CalendarType calType) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Calendar newInstance(CalendarType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ListAccess<Calendar> findCalendarsByQuery(CalendarQuery query) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  class MockEventDAO implements EventDAO {

    @Override
    public CalendarEvent getById(String id) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public CalendarEvent getById(String id, CalendarType calType) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public CalendarEvent save(CalendarEvent object, boolean isNew) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public CalendarEvent remove(String id, CalendarType calType) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public CalendarEvent newInstance(CalendarType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ListAccess<Invitation> findInvitationsByQuery(EventQuery query) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Invitation getInvitationById(String invitationID) throws CalendarException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void removeInvitation(String id) throws CalendarException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void updateInvitation(String id, String status) throws CalendarException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public Invitation createInvitation(String eventId, String participant, String status) throws CalendarException {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
}
