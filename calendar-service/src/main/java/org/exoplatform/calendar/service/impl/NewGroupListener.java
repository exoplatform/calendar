/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 14 f�vr. 08
 * 
 * This is a plugin running every time a new group is create.
 * The goal is to create a default calendar for each group.
 * The plugin configuration is defined in the portal/conf/cs/cs-plugin-configuration.xml file. 
 *
 */
public class NewGroupListener extends GroupEventListener {

  private static final Log     log                 = ExoLogger.getLogger(NewGroupListener.class);
  
  protected CalendarService calendarService_;

  private String            defaultCalendarDescription;

  private String            defaultLocale;

  private String            defaultTimeZone;

  private String[]          editPermission;                             ;

  private String[]          viewPermission;

  private List<String>      ignore_groups_;

  final public String       ST_GROUP_IGNORE = "ignoredGroups".intern();

  /**
   * 
   * @param calendarService Calendar service geeting from the Portlet Container
   * @param params  parameters defined in the cs-plugins-configuration.xml
   */
  @SuppressWarnings("unchecked")
  public NewGroupListener(CalendarService calendarService, InitParams params) {
    calendarService_ = calendarService;
    if (params.getValueParam("defaultEditPermission") != null)
      editPermission = params.getValueParam("defaultEditPermission").getValue().split(",");
    if (params.getValueParam("defaultViewPermission") != null)
      viewPermission = params.getValueParam("defaultViewPermission").getValue().split(",");
    if (params.getValueParam("defaultCalendarDescription") != null)
      defaultCalendarDescription = params.getValueParam("defaultCalendarDescription").getValue();
    if (params.getValueParam("defaultLocale") != null)
      defaultLocale = params.getValueParam("defaultLocale").getValue();
    if (params.getValueParam("defaultTimeZone") != null)
      defaultTimeZone = params.getValueParam("defaultTimeZone").getValue();
    if (params.getValuesParam(ST_GROUP_IGNORE) != null && !params.getValuesParam(ST_GROUP_IGNORE).getValues().isEmpty())
      ignore_groups_ = params.getValuesParam(ST_GROUP_IGNORE).getValues();
  }

  public void postSave(Group group, boolean isNew) throws Exception {
    if (!isNew)
      return;
    String groupId = group.getId();
    String parentId = group.getParentId();
    if (ignore_groups_ != null && !ignore_groups_.isEmpty())
      for (String g : ignore_groups_) {
        if (groupId.equalsIgnoreCase(g))
          return;
        // if(g.contains("/spaces/*") && groupId.toLowerCase().contains("spaces/")) return;
        // CS-4474: ignore create calendar for group of space
        if ((g.lastIndexOf(Utils.SLASH_AST) > -1) && ((g.substring(0, g.lastIndexOf(Utils.SLASH_AST))).equalsIgnoreCase(parentId)))
          return;
      }
    boolean isPublic = true;
    Calendar calendar = new Calendar();
    calendar.setName("Default");
    if (defaultCalendarDescription != null)
      calendar.setDescription(defaultCalendarDescription);
    calendar.setGroups(new String[] { groupId });
    calendar.setPublic(isPublic);
    if (defaultLocale != null)
      calendar.setLocale(defaultLocale);
    if (defaultTimeZone != null)
      calendar.setTimeZone(defaultTimeZone);
    calendar.setCalendarColor(Calendar.COLORS[new Random().nextInt(Calendar.COLORS.length)]);
    List<String> perms = new ArrayList<String>();
    for (String s : viewPermission) {
      if (!perms.contains(s))
        perms.add(s);
    }
    calendar.setViewPermission(perms.toArray(new String[perms.size()]));
    perms.clear();
    for (String s : editPermission) {
      String groupKey = groupId + "/:" + s;
      if (!perms.contains(groupKey))
        perms.add(groupKey);
    }
    calendar.setEditPermission(perms.toArray(new String[perms.size()]));
    calendarService_.savePublicCalendar(calendar, isNew);
  }

  @Override
  public void postDelete(Group group) throws Exception {
    try {
      List<GroupCalendarData> gCalData = calendarService_.getGroupCalendars(new String[] { group.getId() }, true, null);
      for (GroupCalendarData gc : gCalData) {
        if (gc != null && !gc.getCalendars().isEmpty()) {
          for (Calendar c : gc.getCalendars()) {
            calendarService_.removePublicCalendar(c.getId());
          }
        }
      }
      super.postDelete(group);
    } catch (Exception e) {
      // catch any exception to ensure that it is not thrown to higher level and organization service does not need to roll back data.
      if (log.isWarnEnabled()) {
        log.warn(String.format("Can not clear calendars of [%s]", group != null ? group.getGroupName() : "null"), e);
      }
    }
  }

}
