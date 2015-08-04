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

import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Calendar extends AbstractBean {

  private static final long serialVersionUID = 2638692203625602436L;

  public enum Type implements CalendarType {

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

    @Override
    public String getName() {
      return name();
    }
  }

  public static final int      TYPE_PRIVATE  = 0;

  public static final int      TYPE_SHARED   = 1;

  public static final int      TYPE_PUBLIC   = 2;
  
  public static final int      TYPE_ALL   = -1;

  private String               name;

  private String               calendarColor = Constants.N_POWDER_BLUE;

  private String               description;

  private String               timeZone;

  private String               locale;

  private String               calendarOwner;

  private String[]             viewPermission;

  private String[]             editPermission;

  private String[]             groups;

  private String               publicUrl;

  private String               privateUrl;

  private String               _calendarPath;

  private boolean              _isDataInit    = false;

  private boolean              _isPublic      = false;

  @Deprecated
  private int                  _calType;
  
  private CalendarType calendarType;
  
  private boolean       remote = false;
  
  private boolean hasChildren = false;

  public static final String   CALENDAR_PREF = "calendar";

  public Calendar() {
    this(CALENDAR_PREF + IdGenerator.generate());
  }

  public Calendar(String compositeId) {
    this(compositeId, null);
  }

  public Calendar(String id, CalendarType type) {
    super(id);
    timeZone = TimeZone.getDefault().getID();
    locale = Locale.getDefault().getISO3Country();
    if (type != null) {
      setCalendarType(type);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCalendarPath() {
    return _calendarPath;
  }

  public void setCalendarPath(String path) {
    this._calendarPath = path;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getEditPermission() {
    return editPermission;
  }

  public void setEditPermission(String[] editPermission) {
    this.editPermission = editPermission;
  }

  public String[] getViewPermission() {
    return viewPermission;
  }

  public void setViewPermission(String[] viewPermission) {
    this.viewPermission = viewPermission;
  }

  public String[] getGroups() {
    return groups;
  }

  public void setGroups(String[] groups) {
    this.groups = groups;
  }

  public boolean isPublic() {
    return _isPublic;
  }

  public void setPublic(boolean isPublic) {
    this._isPublic = isPublic;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLocale() {
    return locale;
  }

  public void setCalendarColor(String calendarColor) {
    this.calendarColor = calendarColor;
  }

  public String getCalendarColor() {
    return calendarColor;
  }

  public void setDataInit(boolean isDataInit) {
    this._isDataInit = isDataInit;
  }

  public boolean isDataInit() {
    return _isDataInit;
  }

  public void setCalendarOwner(String calendarOwner) {
    this.calendarOwner = calendarOwner;
  }

  public String getCalendarOwner() {
    return calendarOwner;
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = removeDomainName(publicUrl);
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public void setPrivateUrl(String privateUrl) {
    this.privateUrl = removeDomainName(privateUrl);
  }

  public String getPrivateUrl() {
    return privateUrl;
  }
  
  // This method used to back compatible with old url data contain domain name
  private String removeDomainName(String url) {
    if (url != null && url.indexOf("http") == 0) {
      url = url.substring(url.indexOf(":") + 3);
      url = url.substring(url.indexOf("/"));
    }
    return url;
  }

  /**
   * used to compare 2 calendars or between a calendar and an object
   *
   * @param o a particular object
   * @return true false
   */
  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof Calendar)) return false;
    return getId().equals(((Calendar) o).getId());
  }

  @Override
  public int hashCode()
  {
    return getId().hashCode();
  }

  @Deprecated
  public int getCalType() {
    return _calType;
  }

  @Deprecated
  public void setCalType(int calType) {    
    this._calType = calType;
  }

  public boolean isRemote() {
    return remote;
  }

  public void setRemote(boolean remote) {
    this.remote = remote;
  }

  public boolean hasChildren() {
    return hasChildren;
  }

  public void setHasChildren(boolean children) {
    this.hasChildren = children;
  }

  public void setCalendarType(CalendarType type) {
    this.calendarType = type;
  }
  
  public CalendarType getCalendarType() {
    return calendarType;
  }
}
