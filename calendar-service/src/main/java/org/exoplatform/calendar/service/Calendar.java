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
public class Calendar {

  public static final int      TYPE_PRIVATE  = 0;

  public static final int      TYPE_SHARED   = 1;

  public static final int      TYPE_PUBLIC   = 2;

  public static final String   OLIVE         = "Olive".intern();

  public static final String   OLIVEDRAB     = "OliveDrab".intern();

  public static final String   ORANGERED     = "OrangeRed".intern();

  public static final String   ORCHID        = "Orchid".intern();

  public static final String   PALEGOLDENROD = "PaleGoldenRod".intern();

  public static final String   PALEGREEN     = "PaleGreen".intern();

  public static final String   PALETURQUOISE = "PaleTurquoise".intern();

  public static final String   PALEVIOLETRED = "PaleVioletRed".intern();

  public static final String   PAPAYAWHIP    = "PapayaWhip".intern();

  public static final String   PEACHPUFF     = "PeachPuff".intern();

  public static final String   PERU          = "Peru".intern();

  public static final String   PINK          = "Pink".intern();

  public static final String   PLUM          = "Plum".intern();

  public static final String   POWDERBLUE    = "PowderBlue".intern();

  public static final String   PURPLE        = "Purple".intern();

  public static final String   RED           = "Red".intern();

  public static final String   ROSYBROWN     = "RosyBrown".intern();

  public static final String   ROYALBLUE     = "RoyalBlue".intern();

  public static final String   SADDLEBROWN   = "SaddleBrown".intern();

  public static final String   SALMON        = "Salmon".intern();

  public static final String   SANDYBROWN    = "SandyBrown".intern();

  public static final String   SEAGREEN      = "SeaGreen".intern();

  public static final String   SEASHELL      = "SeaShell".intern();

  public static final String   SIANNA        = "Sienna".intern();

  public static final String   SILVER        = "Silver".intern();

  public static final String   SKYBLUE       = "SkyBlue".intern();

  public static final String   THISTLE       = "Thistle".intern();

  public static final String   TOMATO        = "Tomato".intern();

  public static final String   TURQUOISE     = "Turquoise".intern();

  public static final String   VIOLET        = "Violet".intern();

  public static final String   WHEAT         = "Wheat".intern();

  public static final String   YELLOW        = "Yellow".intern();

  public static final String[] COLORS        = { POWDERBLUE, ORCHID, PALEGOLDENROD, PALEGREEN, OLIVE, OLIVEDRAB, ORANGERED, PALETURQUOISE, PALEVIOLETRED, PAPAYAWHIP, PEACHPUFF, PERU, PINK, PLUM, PURPLE, RED, ROSYBROWN, ROYALBLUE, SADDLEBROWN, SALMON, SANDYBROWN, SEAGREEN, SEASHELL, SIANNA, SILVER, SKYBLUE, THISTLE, TOMATO, TURQUOISE, VIOLET, WHEAT, YELLOW };

  private String               id;

  private String               name;

  private String               calendarPath;

  private String               calendarColor = POWDERBLUE;

  private String               description;

  private String               timeZone;

  private String               locale;

  private String               calendarOwner;

  private String[]             viewPermission;

  private String[]             editPermission;

  private boolean              isDataInit    = false;

  private boolean              isPublic      = false;

  private String               categoryId;

  private String[]             groups;

  private String               publicUrl;

  private String               privateUrl;

  public static final String   CALENDAR_PREF = "calendar".intern();

  public Calendar() {
    id = CALENDAR_PREF + IdGenerator.generate();
    timeZone = TimeZone.getDefault().getID();
    locale = Locale.getDefault().getISO3Country();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCalendarPath() {
    return calendarPath;
  }

  public void setCalendarPath(String path) {
    this.calendarPath = path;
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

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
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
    this.isDataInit = isDataInit;
  }

  public boolean isDataInit() {
    return isDataInit;
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
}
