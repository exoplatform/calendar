package org.exoplatform.calendar.indexing.listener;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Indexing with :
 * - collection : "calendar"
 * - type : (event|task)
 * - name : object id
 *
 * TODO Listeners only for public events. What about others events ?
 */
public class UnifiedSearchCalendarListener extends CalendarEventListener {

  private static Log log = ExoLogger.getLogger(UnifiedSearchCalendarListener.class);

  private final IndexingService indexingService;

  public UnifiedSearchCalendarListener(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  @Override
  public void savePublicEvent(CalendarEvent event, String calendarId) {
    // TODO No need of the calendarId argument as it is already contained in the event object

    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("event", event);
      SearchEntry searchEntry = new SearchEntry("calendar", event.getEventType().toLowerCase(), event.getId(), content);
      indexingService.add(searchEntry);
    }
  }

  @Override
  public void updatePublicEvent(CalendarEvent event, String calendarId) {
    // TODO No need of the calendarId argument as it is already contained in the event object

    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("event", event);
      SearchEntryId searchEntryId = new SearchEntryId("calendar", event.getEventType().toLowerCase(), event.getId());
      indexingService.update(searchEntryId, content);
    }
  }

  @Override
  public void deletePublicEvent(CalendarEvent event, String calendarId) {
    if(indexingService != null) {
      SearchEntryId searchEntryId = new SearchEntryId("calendar", event.getEventType().toLowerCase(), event.getId());
      indexingService.delete(searchEntryId);
    }
  }

  @Override
  public void updatePublicEvent(CalendarEvent oldEvent, CalendarEvent event, String calendarId) {
    // TODO Why 2 methods for an event update ?

    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("event", event);
      SearchEntryId searchEntryId = new SearchEntryId("calendar", event.getEventType().toLowerCase(), event.getId());
      indexingService.update(searchEntryId, content);
    }
  }
}
