/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.util.Calendar;

import org.exoplatform.calendar.service.impl.ICalendarImportExport;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.JobDetailImpl;

/**
 * Created by The eXo Platform SAS
 * Author : vietnq
 *          vietnq@exoplatform.com
 * Apr 12, 2013  
 */
public class ImportCalendarJob implements Job, InterruptableJob {
  
  public static final String IMPORT_CALENDAR_JOB_NAME = "import_calendar_job";
  public static final String IMPORT_CALENDAR_JOB_GROUP_NAME = "import_calendar_job_group";
  public static final String IMPORT_OR_SUBSCRIBE = "import_or_subscribe"; // import uploaded file or remote url
  public static final String IMPORT_UPLOADED_FILE = "import_uploaded_file";
  public static final String IMPORT_REMOTE_CALENDAR = "import_remote_calendar";
  public static final String REMOTE_CALENDAR = "remote_calendar";
  public static final String INPUT_STREAM = "input_stream";
  public static final String CALENDAR_NAME = "calendar_name";
  public static final String IMPORT_FROM_TIME = "import_from_time";
  public static final String IMPORT_TO_TIME = "import_to_time";
  public static final String IS_IMPORT_NEW = "is_import_new";
  public static final String CALENDAR_ID = "calendarId";
  public static final String USER_NAME = "username";
  public static final String IMPORT_CALENDAR_CHANNEL = "/eXo/Application/Calendar/notifyImportCalendar";
  // Constants for continuation service
  public static final String START_MESSAGE_KEY = "startImport:%s";
  public static final String FINISH_MESSAGE_KEY = "finishImport:%s";
  public static final String ERROR_MESSAGE_KEY = "errorImport:%s";
  private static Log log = ExoLogger.getLogger(ImportCalendarJob.class);
  
  @Override
  public void interrupt() throws UnableToInterruptJobException {
    
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Start importing calendar....");
    
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
        .getComponentInstanceOfType(ContinuationService.class);
    
    CalendarService calendarService = (CalendarService) PortalContainer.getInstance()
        .getComponentInstanceOfType(CalendarService.class);
    
    // get info about job
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    
    String type = jobDataMap.getString(IMPORT_OR_SUBSCRIBE);
    String username = jobDataMap.getString(USER_NAME);
    String calendarName = jobDataMap.getString(CALENDAR_NAME);

    continuation.sendMessage(username, IMPORT_CALENDAR_CHANNEL, String.format(START_MESSAGE_KEY, calendarName));
    
    try {
      if(IMPORT_UPLOADED_FILE.equals(type)) {

        String calendarId = jobDataMap.getString(CALENDAR_ID);

        Calendar from = (Calendar)jobDataMap.get(IMPORT_FROM_TIME);
        Calendar to = (Calendar)jobDataMap.get(IMPORT_TO_TIME);
        Boolean isNew = (Boolean)jobDataMap.get(IS_IMPORT_NEW);
        InputStream icalInputStream = (InputStream)jobDataMap.get(INPUT_STREAM);

        ICalendarImportExport iCalImEx = (ICalendarImportExport) calendarService.getCalendarImportExports(CalendarService.ICALENDAR);
        iCalImEx.importCalendar(username, icalInputStream, calendarId, calendarName, from, to, isNew);
      } else {
        RemoteCalendar remoteCalendar = (RemoteCalendar)jobDataMap.get(REMOTE_CALENDAR);
        calendarService.importRemoteCalendar(remoteCalendar);
      }
      
      continuation.sendMessage(username, IMPORT_CALENDAR_CHANNEL, String.format(FINISH_MESSAGE_KEY, calendarName));
      log.info("finished importing icalendar");
    } catch (Exception e) {
      continuation.sendMessage(username, IMPORT_CALENDAR_CHANNEL, String.format(ERROR_MESSAGE_KEY, calendarName));
      log.error("Exception occurs when importing calendar",e);
    }
  }

  /* Gets job detail for importing calendar from an uploaded file */
  public static JobDetail getImportICSFileJobDetail(String username,
                             String calendarId,
                             String calendarName,
                             InputStream icalInputStream,
                             Calendar from,
                             Calendar to,
                             boolean isNew) {
    JobDetailImpl job = new JobDetailImpl();

    job.setName(calendarId);
    job.setGroup(IMPORT_CALENDAR_JOB_GROUP_NAME);
    job.setJobClass(ImportCalendarJob.class);
    job.setDescription("Import calendar by job");

    job.getJobDataMap().put(IMPORT_OR_SUBSCRIBE,IMPORT_UPLOADED_FILE);
    job.getJobDataMap().put(USER_NAME, username);
    job.getJobDataMap().put(INPUT_STREAM,icalInputStream);
    job.getJobDataMap().put(CALENDAR_ID, calendarId);
    job.getJobDataMap().put(CALENDAR_NAME, calendarName);
    job.getJobDataMap().put(IMPORT_FROM_TIME, from);
    job.getJobDataMap().put(IMPORT_TO_TIME, to);
    job.getJobDataMap().put(IS_IMPORT_NEW, isNew);
    
    return job;
  }

  /* Gets job detail for importing calendar from a remote url */
  public static JobDetail getImportRemoteCalendarJobDetail(RemoteCalendar remoteCalendar) {
    JobDetailImpl job = new JobDetailImpl();

    job.setName(remoteCalendar.getCalendarName());
    job.setGroup(IMPORT_CALENDAR_JOB_GROUP_NAME);
    job.setJobClass(ImportCalendarJob.class);
    job.setDescription("Import remote calendar by job");

    job.getJobDataMap().put(USER_NAME, remoteCalendar.getUsername());
    job.getJobDataMap().put(CALENDAR_NAME, remoteCalendar.getCalendarName());
    job.getJobDataMap().put(IMPORT_OR_SUBSCRIBE,IMPORT_REMOTE_CALENDAR);
    job.getJobDataMap().put(REMOTE_CALENDAR, remoteCalendar);
    return job;
  }
}
