/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.calendar.service;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Calendar extends org.exoplatform.calendar.model.Calendar {

  private static final long serialVersionUID = 2638692203625602436L;
  
  private static final Log log = ExoLogger.getLogger(Calendar.class);

  public enum Type {

    PERSONAL(0),

    SHARED(1),

    GROUP(2),

    UNDEFINED(-1);

    private final int type;

    Type(int type) {
      this.type = type;
    }

    public int type() {
      return type;
    }

    public static Type getType(int type) {
      for (Type t : Type.values()) {
        if (t.type() == type) {
          return t;
        }
      }

      return UNDEFINED;
    }
  }

  public static final int      TYPE_PRIVATE  = 0;

  public static final int      TYPE_SHARED   = 1;

  public static final int      TYPE_PUBLIC   = 2;
  
  public static final int      TYPE_ALL   = -1;

  private String               _calendarPath;

  private boolean              _isDataInit    = false;

  private int                  _calType;
  
  public static final String   CALENDAR_PREF = "calendar";

  public Calendar() {
    this(CALENDAR_PREF + IdGenerator.generate());
  }

  public Calendar(String compositeId) {
    super(compositeId);
  }

  public String getCalendarPath() {
    return _calendarPath;
  }

  public void setCalendarPath(String path) {
    this._calendarPath = path;
  }

  public void setDataInit(boolean isDataInit) {
    this._isDataInit = isDataInit;
  }

  public boolean isDataInit() {
    return _isDataInit;
  }

  public int getCalType() {
    return _calType;
  }

  public void setCalType(int calType) {    
    this._calType = calType;
  }

  public boolean canEdit(String username) {
    return Utils.isCalendarEditable(username, this);
  }
  
  public boolean isShared(String username) {
    OrganizationService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    return Calendar.Type.PERSONAL.type() == this.getCalType() && username != null 
        && !username.equals(this.getCalendarOwner()) && Utils.hasPermission(service, this.getViewPermission(), username);
  }

  public static Calendar build(org.exoplatform.calendar.model.Calendar newModel) {
    Calendar cal = new Calendar();

    cal.setId(newModel.getId());
    cal.setName(newModel.getName());
    cal.setDescription(newModel.getDescription());
    cal.setLocale(newModel.getLocale());
    cal.setTimeZone(newModel.getTimeZone());
    cal.setCalendarColor(newModel.getCalendarColor());
    cal.setCalendarOwner(newModel.getCalendarOwner());
    cal.setPublicUrl(newModel.getPublicUrl());
    cal.setPrivateUrl(newModel.getPrivateUrl());
    cal.setLastModified(newModel.getLastModified());
    cal.setGroups(newModel.getGroups());
    cal.setViewPermission(newModel.getViewPermission());
    cal.setEditPermission(newModel.getEditPermission());
    cal.setRemote(newModel.isRemote());
    cal.setHasChildren(newModel.hasChildren());
    CalendarService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    try {
      cal.setCalType(service.getTypeOfCalendar(ConversationState.getCurrent().getIdentity().getUserId(), cal.getId()));
    } catch (Exception e) {
      log.error(e);
    }
    cal.setDS(newModel.getDS());

    return cal;
  }
}
