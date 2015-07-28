package org.exoplatform.calendar.service;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.calendar.service.storage.*;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.container.component.BaseComponentPlugin;

public class MockStorage extends BaseComponentPlugin implements Storage {

  public static final CalendarType MOCK_CAL_TYPE = new CalendarType(){};
  
  @Override
  public List<CalendarType> getSupportedTypes() {
    return Arrays.asList(MOCK_CAL_TYPE);
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EventDAO getEventDAO() {
    return null;
  }
}
