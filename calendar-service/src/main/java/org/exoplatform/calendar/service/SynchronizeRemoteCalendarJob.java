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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.quartz.JobExecutionContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 10, 2011  
 */
public class SynchronizeRemoteCalendarJob extends MultiTenancyJob {

  public static final String  SYNCHRONIZE_REMOTE_CALENDAR_JOB   = "SynchonizeRemoteCalendarJob";

  public static final String  SYNCHRONIZE_REMOTE_CALENDAR_GROUP = "SynchonizeRemoteCalendarGroup";

  public static Boolean       isExecuting                       = false;

  private static Log          log_                              = ExoLogger.getLogger("cs.calendar.job.synchronizeremote");

  public static final String  USERNAME                          = "username";

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return SynchronizeRemoteCalendarTask.class;
  }

  public class SynchronizeRemoteCalendarTask extends MultiTenancyTask{

    public SynchronizeRemoteCalendarTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      
      if(isExecuting) { //prevent other jobs from starting.
        return;
      }
      
      super.run();
      
      synchronized (isExecuting) {
        isExecuting = true;
        PortalContainer container = Utils.getPortalContainer(context);
        if (container == null)
          return;
        ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
        ExoContainerContext.setCurrentContainer(container);
        SessionProvider provider = SessionProvider.createSystemProvider();
        CalendarService calService = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
        RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
        String currentRepo = null;
        try {
          currentRepo = repositoryService.getCurrentRepository().getConfiguration().getName();
        } catch (RepositoryException e) {
          log_.warn("Can't get current repository name", e);
        }

        int total = 0;
        int success = 0;
        int failed = 0;
        long start = System.currentTimeMillis();
        try {
          if (log_.isDebugEnabled())
            log_.debug("Remote calendar synchronization service");

          // get list of remote calendars
          String sql  = "select * from exo:remoteCalendar";
          QueryManager queryManager = getSession(provider).getWorkspace().getQueryManager();
          Query query = queryManager.createQuery(sql, Query.SQL);
          QueryResult results = query.execute();
          NodeIterator iter = results.getNodes();

          Node remoteCalendar;

          // iterate over each remote calendar, do refresh job
          while (iter.hasNext()) {
            total++;
            remoteCalendar = iter.nextNode();
            String remoteCalendarId = remoteCalendar.getProperty(Utils.EXO_ID).getString();
            String calendarOwner = remoteCalendar.getProperty(Utils.EXO_CALENDAR_OWNER).getString();

            try {
                calService.refreshRemoteCalendar(calendarOwner, remoteCalendarId);
                success++;
            } catch (Exception e) {
              log_.debug("Skip this calendar, error when reload remote calendar " + remoteCalendarId + ". Error message: " + e.getMessage());
              failed++;
              continue;
            }
          }
        } catch (RepositoryException e) {
          if (log_.isDebugEnabled())
            log_.debug("Data base not ready!");
        } catch (Exception e) {
          if (log_.isDebugEnabled()) {
            log_.debug("Exception when synchronize remote calendar. ", e);
          }
        } finally {
          provider.close(); // release sessions
          ExoContainerContext.setCurrentContainer(oldContainer);
          isExecuting = false;
          if (currentRepo != null) {
            try {
              repositoryService.setCurrentRepositoryName(currentRepo);
            } catch (RepositoryConfigurationException e) {
              log_.error(String.format("Can't set current repository name as %s", currentRepo), e);
            }
          }
        }
        long finish = System.currentTimeMillis();
        long spent = (finish - start);
        if (total > 0) {
          log_.info("Reload remote calendar completed. Total: " + total + ", Success: " + success + ", Failed: " + failed + ", Skip: " + (total - success - failed) + ". Time spent: " + spent + " ms.");
        }
      }
    }
  }

  private Session getSession(SessionProvider sprovider) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository currentRepo = repositoryService.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  public static JobInfo getJobInfo(String username) {
    JobInfo info = new JobInfo(getRemoteCalendarName(username),
                               SYNCHRONIZE_REMOTE_CALENDAR_GROUP,
                               SynchronizeRemoteCalendarJob.class);
    return info;
  }

  public static String getRemoteCalendarName(String username) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    String repoName = null;
    try {
      repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      log_.error("Repository is error", e);
    }
    StringBuilder jobNameBd = new StringBuilder().append(SYNCHRONIZE_REMOTE_CALENDAR_JOB)
        .append("_")
        .append(username)
        .append("_")
        .append(repoName);
    return jobNameBd.toString();
  }
}
