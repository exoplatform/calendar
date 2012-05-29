/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarUpdateEventListener;
import org.exoplatform.calendar.service.CsObjectParam;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 1, 2008  
 */
public class UpdateCalendarVersion extends CalendarUpdateEventListener {

  private CalendarService   cservice_;

  private RepositoryService repositorySerivce_;

  CsObjectParam             csObj_;

  public UpdateCalendarVersion(CalendarService cservice, InitParams params, RepositoryService repositorySerivce) throws Exception {
    cservice_ = cservice;
    repositorySerivce_ = repositorySerivce;
    csObj_ = (CsObjectParam) params.getObjectParam("cs.calendar.update.object").getObject();
  }

  @Override
  public void preUpdate() {
  }

}
