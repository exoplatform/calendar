/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
 */
package org.exoplatform.calendar.service;

import javax.jcr.*;
import javax.jcr.query.*;

import org.quartz.*;

import org.exoplatform.container.*;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jan
 * 10, 2011
 */
@DisallowConcurrentExecution
public class SynchronizeRemoteCalendarJob implements Job {

  private static final Log   LOG                               = ExoLogger.getLogger(SynchronizeRemoteCalendarJob.class);

  public static final String SYNCHRONIZE_REMOTE_CALENDAR_JOB   = "SynchonizeRemoteCalendarJob";

  public static final String SYNCHRONIZE_REMOTE_CALENDAR_GROUP = "SynchonizeRemoteCalendarGroup";

  public static final String CALENDARS                         = "calendars".intern();

  public static final String USERNAME_PARAMETER                = "username";

  private DataStorage        dataStorage;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    PortalContainer container = Utils.getPortalContainer(context);
    if (container == null) {
      return;
    }
    ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    SessionProvider provider = SessionProvider.createSystemProvider();
    CalendarService calService = container.getComponentInstanceOfType(CalendarService.class);
    RepositoryService repositoryService = container.getComponentInstanceOfType(RepositoryService.class);
    String currentRepo = null;
    try {
      currentRepo = repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      LOG.warn("Can't get current repository name", e);
    }

    int total = 0;
    int success = 0;
    int failed = 0;
    long start = System.currentTimeMillis();
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("Remote calendar synchronization service");

      // get info from data map
      JobDetail jobDetail = context.getJobDetail();
      JobDataMap dataMap = jobDetail.getJobDataMap();
      String username = dataMap.getString(USERNAME_PARAMETER);
      if (username == null) {
        return;
      }
      // get list of remote calendar of current user
      Node userCalendarHome = getDataStorage().getUserCalendarHome(username);
      if (userCalendarHome == null) {
        throw new IllegalStateException("Can't get user calendar home node");
      }
      StringBuilder path = new StringBuilder("/jcr:root");
      path.append(userCalendarHome.getPath());
      path.append("//element(*,exo:remoteCalendar)");
      QueryManager queryManager = getSession(provider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(path.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();

      Node remoteCalendar;

      // iterate over each remote calendar, do refresh job
      while (iter.hasNext()) {
        total++;
        remoteCalendar = iter.nextNode();
        String remoteCalendarId = remoteCalendar.getProperty(Utils.EXO_ID).getString();
        String syncPeriod = remoteCalendar.getProperty(Utils.EXO_REMOTE_SYNC_PERIOD).getString();

        // skip iCalendar type
        try {
          // case 1: if auto refresh calendar, do refresh this calendar
          if (syncPeriod.equals(Utils.SYNC_AUTO)) {
            calService.refreshRemoteCalendar(username, remoteCalendarId);
            success++;
          } else {
            long lastUpdate = remoteCalendar.getProperty(Utils.EXO_REMOTE_LAST_UPDATED).getDate().getTimeInMillis();
            long now = Utils.getGreenwichMeanTime().getTimeInMillis();
            long interval = 0;
            if (Utils.SYNC_5MINS.equals(syncPeriod))
              interval = 5 * 60 * 1000L;
            if (Utils.SYNC_10MINS.equals(syncPeriod))
              interval = 10 * 60 * 1000L;
            if (Utils.SYNC_15MINS.equals(syncPeriod))
              interval = 15 * 60 * 1000L;
            if (Utils.SYNC_1HOUR.equals(syncPeriod))
              interval = 60 * 60 * 1000L;
            if (Utils.SYNC_1DAY.equals(syncPeriod))
              interval = 24 * 60 * 60 * 1000L;
            if (Utils.SYNC_1WEEK.equals(syncPeriod))
              interval = 7 * 24 * 60 * 60 * 1000L;
            if (Utils.SYNC_1YEAR.equals(syncPeriod))
              interval = 365 * 7 * 24 * 60 * 60 * 1000L;

            // if this remote calendar has expired
            if (lastUpdate + interval < now) {
              calService.refreshRemoteCalendar(username, remoteCalendarId);
              success++;
            }
          }
        } catch (Exception e) {
          LOG.debug("Skip this calendar, error when reload remote calendar " + remoteCalendarId + ". Error message: "
              + e.getMessage());
          failed++;
        }
      }
    } catch (RepositoryException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Data base not ready!");
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Exception when synchronize remote calendar. ", e);
      }
    } finally {
      provider.close(); // release sessions
      ExoContainerContext.setCurrentContainer(oldContainer);
      if (currentRepo != null) {
        try {
          repositoryService.setCurrentRepositoryName(currentRepo);
        } catch (RepositoryConfigurationException e) {
          LOG.error(String.format("Can't set current repository name as %s", currentRepo), e);
        }
      }
    }
    long finish = System.currentTimeMillis();
    long spent = (finish - start);
    if (total > 0) {
      LOG.info("Reload remote calendar completed. Total: " + total + ", Success: " + success + ", Failed: " + failed
          + ", Skip: " + (total - success - failed) + ". Time spent: " + spent + " ms.");
    }

  }

  private Session getSession(SessionProvider sprovider) throws RepositoryException {
    ManageableRepository currentRepo = SessionProviderService.getRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  public static JobInfo getJobInfo(String username) {
    return new JobInfo(getRemoteCalendarName(username),
                       SYNCHRONIZE_REMOTE_CALENDAR_GROUP,
                       SynchronizeRemoteCalendarJob.class);
  }

  public static String getRemoteCalendarName(String username) {
    String repoName = SessionProviderService.getRepository().getConfiguration().getName();
    StringBuilder jobNameBd = new StringBuilder().append(SYNCHRONIZE_REMOTE_CALENDAR_JOB)
                                                 .append("_")
                                                 .append(username)
                                                 .append("_")
                                                 .append(repoName);
    return jobNameBd.toString();
  }

  public DataStorage getDataStorage() {
    if (dataStorage == null) {
      dataStorage = ExoContainerContext.getService(DataStorage.class);
    }
    return dataStorage;
  }
}
