/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.calendar.service.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;

public class ImportExportTestCase extends BaseCalendarServiceTestCase {

    public void testImportExportIcs() throws Exception {
        CalendarImportExport calIE = calendarService_.getCalendarImportExports(CalendarService.ICALENDAR);        
        String calendarId = createPrivateCalendar(username, "IcsCalendar", "abcd").getId();

        // event with high priority
        InputStream icalInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated.ics");
        // event with medium priority
        InputStream icalInputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated_p2.ics");
        // event with low priority
        InputStream icalInputStream3 = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated_p3.ics");

        // test non-standard ics file, cf CAL-514, CAL-524
        InputStream icalInputStream4 = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("nonstandard.ics");
        InputStream icalInputStream5 = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("png_attachment.ics");

        calIE.importCalendar(username, icalInputStream, calendarId, calendarId, null, null, false);
        calIE.importCalendar(username, icalInputStream2, calendarId, calendarId, null, null, false);
        calIE.importCalendar(username, icalInputStream3, calendarId, calendarId, null, null, false);
        calIE.importCalendar(username, icalInputStream4, calendarId, calendarId, null, null, false);
        calIE.importCalendar(username, icalInputStream5, calendarId, calendarId, null, null, false);

        List<String> calendarIds = new ArrayList<String>();
        calendarIds.add(calendarId);
        List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);

        assertEquals(5, events.size());

        CalendarEvent event = events.get(0) ;
        assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_HIGH], event.getPriority());

        CalendarEvent event2 = events.get(1) ;
        assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_MEDIUM],  event2.getPriority());

        CalendarEvent event3 = events.get(2) ;
        assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_LOW],  event3.getPriority());

        assertNotNull(event.getFromDateTime());
        assertNotNull(event.getToDateTime());
        assertNotNull(event.getSummary());

        //export single event by id
        OutputStream icalOutputStream  =  calIE.exportEventCalendar(username, calendarId, CalendarImportExport.PRIVATE_TYPE, event.getId());
        assertNotNull(icalOutputStream);

        //export events in set of calendar
        icalOutputStream.close();
        icalOutputStream  =  calIE.exportCalendar(username, calendarIds, CalendarImportExport.PRIVATE_TYPE, 3);
        assertNotNull(icalOutputStream);

        icalOutputStream.close();
        icalInputStream.close();
    }

    public void testImportCSVFile() throws Exception{
        CalendarImportExport calIE = calendarService_.getCalendarImportExports(CalendarService.EXPORTEDCSV);
        String calendarId = createPrivateCalendar(username, "CSVCalendar", "abcd").getId();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sunbird_calendar.csv");
        calIE.importCalendar(username, in, calendarId, calendarId, null, null, false);
        List<String> calendarIds = new ArrayList<String>();
        calendarIds.add(calendarId);
        List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
        assertEquals(3, events.size());

    }
}
