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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.quartz.JobExecutionContext;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */
public class Utils {

  public static final String EXO_ID                     = "exo:id".intern();

  public static final String EXO_LABEL                  = "exo:label".intern();

  public static final String EXO_NAME                   = "exo:name".intern();

  public static final String EXO_DESCRIPTION            = "exo:description".intern();

  public static final String EXO_EVENT_ID               = "exo:eventId".intern();

  public static final String EXO_EVENT_CATEGORYID       = "exo:eventCategoryId".intern();

  public static final String EXO_EVENT_CATEGORY_NAME    = "exo:eventCategoryName".intern();

  public static final String EXO_TASK_DELEGATOR         = "exo:taskDelegator".intern();

  public static final String EXO_REPEAT                 = "exo:repeat".intern();

  public static final String EXO_EVENT_TYPE             = "exo:eventType".intern();

  public static final String EXO_PRIORITY               = "exo:priority".intern();

  public static final String EXO_IS_PRIVATE             = "exo:isPrivate".intern();

  public static final String EXO_EVENT_STATE            = "exo:eventState".intern();

  public static final String EXO_INVITATION             = "exo:invitation".intern();

  public static final String EXO_CALENDAR_EVENT         = "exo:calendarEvent".intern();

  public static final String EXO_REMINDER_TYPE          = "exo:reminderType".intern();

  public static final String EXO_ALARM_BEFORE           = "exo:alarmBefore".intern();

  public static final String EXO_EMAIL                  = "exo:email".intern();

  public static final String EXO_OWNER                  = "exo:creator".intern();

  public static final String EXO_REMINDER               = "exo:reminder".intern();

  public static final String EXO_FROM_DATE_TIME         = "exo:fromDateTime".intern();

  public static final String EXO_TO_DATE_TIME           = "exo:toDateTime".intern();

  public static final String EXO_SUMMARY                = "exo:summary".intern();

  public static final String EXO_IS_REPEAT              = "exo:isRepeat".intern();

  public static final String EXO_IS_OVER                = "exo:isOver".intern();

  public static final String EXO_CALENDAR_PUBLIC_EVENT  = "exo:calendarPublicEvent".intern();

  public static final String EXO_EVENT_CATEGORY         = "exo:eventCategory".intern();

  public static final String EXO_PUBLIC_URL             = "exo:publicUrl".intern();

  public static final String EXO_PRIVATE_URL            = "exo:privateUrl".intern();

  public static final String EXO_DATA                   = "exo:data".intern();

  public static final String EXO_ICAL_DATA              = "exo:iCalData".intern();

  public static final String EXO_TITLE                  = "exo:title".intern();

  public static final String EXO_CONTENT                = "exo:content".intern();

  public static final String EXO_CALENDAR_SETTING       = "exo:calendarSetting".intern();

  public static final String EXO_IS_SHOW_WORKING_TIME   = "exo:showWorkingTime".intern();

  public static final String EXO_WORKING_BEGIN          = "exo:workingTimeBegin".intern();

  public static final String EXO_WORKING_END            = "exo:workingTimeEnd".intern();

  public static final String EXO_PRIVATE_CALENDARS      = "exo:defaultPrivateCalendars".intern();

  public static final String EXO_PUBLIC_CALENDARS       = "exo:defaultPublicCalendars".intern();

  public static final String EXO_SHARED_CALENDARS       = "exo:defaultSharedCalendars".intern();

  public static final String EXO_SHARED_CALENDAR_COLORS = "exo:sharedCalendarsColors".intern();

  public static final String EXO_EVEN_TATTACHMENT       = "exo:eventAttachment".intern();

  public static final String EXO_FILE_NAME              = "exo:fileName".intern();

  public static final String EXO_CATEGORY_ID            = "exo:categoryId".intern();

  public static final String EXO_VIEW_PERMISSIONS       = "exo:viewPermissions".intern();

  public static final String EXO_EDIT_PERMISSIONS       = "exo:editPermissions".intern();

  public static final String EXO_GROUPS                 = "exo:groups".intern();

  public static final String EXO_LOCALE                 = "exo:locale".intern();

  public static final String EXO_TIMEZONE               = "exo:timeZone".intern();

  public static final String EXO_CALENDAR_ID            = "exo:calendarId".intern();

  public static final String EXO_SHARED_MIXIN           = "exo:calendarShared".intern();

  public static final String EXO_SHARED_ID              = "exo:sharedId".intern();

  public static final String EXO_PARTICIPANT            = "exo:participant".intern();

  public static final String EXO_CALENDAR               = "exo:calendar".intern();

  public static final String EXO_CALENDAR_COLOR         = "exo:calendarColor".intern();

  public static final String EXO_CALENDAR_CATEGORY      = "exo:calendarCategory".intern();

  public static final String EXO_CALENDAR_OWNER         = "exo:calendarOwner".intern();

  public static final String EXO_SHARED_COLOR           = "exo:sharedColor".intern();

  public static final String EXO_VIEW_TYPE              = "exo:viewType".intern();

  public static final String EXO_TIME_INTERVAL          = "exo:timeInterval".intern();

  public static final String EXO_WEEK_START_ON          = "exo:weekStartOn".intern();

  public static final String EXO_DATE_FORMAT            = "exo:dateFormat".intern();

  public static final String EXO_TIME_FORMAT            = "exo:timeFormat".intern();

  public static final String EXO_LOCATION               = "exo:location".intern();

  public static final String EXO_REMINDER_DATE          = "exo:remindDateTime".intern();

  public static final String EXO_ROOT_EVENT_ID          = "exo:rootEventId".intern();

  public static final String EXO_RSS_DATA               = "exo:rssData".intern();

  public static final String EXO_BASE_URL               = "exo:baseUrl".intern();

  public static final String EXO_SEND_OPTION            = "exo:sendOption".intern();

  public static final String EXO_MESSAGE                = "exo:message".intern();

  public static final String EXO_PARTICIPANT_STATUS     = "exo:participantStatus".intern();

  public static final String EXO_DATE_MODIFIED          = "exo:dateModified".intern();

  public static final String EXO_REMOTE_MIXIN           = "exo:remoteCalendar".intern();

  public static final String EXO_REMOTE_URL             = "exo:remoteUrl".intern();

  public static final String EXO_REMOTE_TYPE            = "exo:remoteType".intern();

  public static final String EXO_REMOTE_USERNAME        = "exo:username".intern();

  public static final String EXO_REMOTE_PASSWORD        = "exo:password".intern();

  public static final String EXO_REMOTE_SYNC_PERIOD     = "exo:syncPeriod".intern();

  public static final String EXO_REMOTE_LAST_UPDATED    = "exo:lastUpdated".intern();

  public static final String EXO_REMOTE_BEFORE_DATE     = "exo:beforeDate".intern();

  public static final String EXO_REMOTE_AFTER_DATE      = "exo:afterDate".intern();

  public static final String EXO_REMOTE_EVENT_MIXIN     = "exo:caldavCalendarEvent".intern();

  public static final String EXO_CALDAV_HREF            = "exo:caldavHref";

  public static final String EXO_CALDAV_ETAG            = "exo:caldavEtag";

  public static final String EXO_REPEAT_CALENDAR_EVENT  = "exo:repeatCalendarEvent";

  public static final String EXO_REPEAT_COUNT           = "exo:repeatCount";

  public static final String EXO_REPEAT_UNTIL           = "exo:repeatUntil";

  public static final String EXO_RECURRENCE_ID          = "exo:recurrenceId";

  public static final String EXO_IS_EXCEPTION           = "exo:isException";

  public static final String EXO_EXCLUDE_ID             = "exo:excludeId";

  public static final String EXO_ORIGINAL_REFERENCE     = "exo:originalReference";

  public static final String EXO_REPEAT_INTERVAL        = "exo:repeatInterval";

  public static final String EXO_REPEAT_BYDAY           = "exo:repeatByDay";

  public static final String EXO_REPEAT_BYMONTHDAY      = "exo:repeatByMonthDay";

  public static final String EXO_REPEAT_FINISH_DATE     = "exo:repeatFinishDate";
  
  public static final String EXO_DATE_CREATED           = "exo:dateCreated";

  public static final String X_STATUS                   = "X-STATUS".intern();

  public static final String ATTACHMENT_NODE            = "attachment".intern();                                                                              ;

  public static final String REMINDERS_NODE             = "reminders".intern();                                                                               ;

  public static final String NT_UNSTRUCTURED            = "nt:unstructured".intern();

  public static final String NT_FILE                    = "nt:file".intern();

  public static final String NT_RESOURCE                = "nt:resource".intern();

  public static final String MIX_REFERENCEABLE          = "mix:referenceable".intern();

  public static final String JCR_LASTMODIFIED           = "jcr:lastModified".intern();

  public static final String JCR_CONTENT                = "jcr:content".intern();

  public static final String JCR_MIMETYPE               = "jcr:mimeType".intern();

  public static final String JCR_DATA                   = "jcr:data".intern();

  public static final String JCR_SCORE                  = "jcr:score";
  
  public static final String MIMETYPE_TEXTPLAIN         = "text/plain".intern();

  public static final String MIMETYPE_ICALENDAR         = "TEXT/CALENDAR".intern();

  public static final String ATTACHMENT                 = "ATTACHMENT".intern();

  public static final String INLINE                     = "INLINE".intern();

  public static final String COMMA                      = ",".intern();

  public static final String COLON                      = ":".intern();

  public static final String SLASH                      = "/".intern();

  public static final String UNDERSCORE                 = "_".intern();

  public static final String SLASH_COLON                = "/:".intern();

  public static final String COLON_SLASH                = ":/".intern();

  public static final String ANY                        = "*.*".intern();

  public static final String ANY_OF                     = "*.".intern();

  public static final String SLASH_AST                  = "/*".intern();

  public static final String MINUS                      = "-".intern();

  final public static String CALENDAR_REMINDER          = "reminders".intern();

  final public static String CALENDAR_APP               = "CalendarApplication".intern();

  /**
   * These constants were used to indicate participant's answer or action in invitation mail  
   */
  public static final int    DENY                       = 0;

  public static final int    ACCEPT                     = 1;

  public static final int    NOTSURE                    = 2;

  public static final int    ACCEPT_IMPORT              = 3;

  public static final int    JUMP_TO_CALENDAR           = 4;

  public static final String RSS_NODE                   = "iCalendars".intern();

  public static final String CALDAV_NODE                = "WebDavCalendars".intern();

  public static final String ICS_EXT                    = ".ics".intern();

  public static final String RSS_EXT                    = ".rss".intern();

  final public static String EMPTY_STR                  = "".intern();

  final public static String STATUS_PENDING             = "pending".intern();

  final public static String STATUS_YES                 = "yes".intern();

  final public static String STATUS_NO                  = "no".intern();

  public static final int    INVALID_TYPE               = -1;

  public static final int    PRIVATE_TYPE               = 0;

  public static final int    SHARED_TYPE                = 1;

  public static final int    PUBLIC_TYPE                = 2;

  public static final String SPLITTER                   = "splitter";

  public static final String ASCENDING                  = "ascending";

  public static final String DESCENDING                 = "descending";

  public static final String SPACE                      = " ";

  public static final String RESOURCEBUNDLE_NAME        = "locale.portlet.calendar.CalendarPortlet";

  public static int          EVENT_NUMBER               = -1;

  /**
   * These constants were used to determine synchronization period of remote calendar
   */
  public static final String SYNC_AUTO                  = "auto";

  public static final String SYNC_5MINS                 = "5mins";

  public static final String SYNC_10MINS                = "10mins";

  public static final String SYNC_15MINS                = "15mins";

  public static final String SYNC_1HOUR                 = "1hour";

  public static final String SYNC_1DAY                  = "1day";

  public static final String SYNC_1WEEK                 = "1week";

  public static final String SYNC_1YEAR                 = "1year";

  public static final String MIMETYPE_TEXTHTML          = "text/html".intern();

  public static String[]     SYNC_PERIOD                = { SYNC_AUTO, SYNC_5MINS, SYNC_10MINS, SYNC_15MINS, SYNC_1HOUR, SYNC_1DAY, SYNC_1WEEK, SYNC_1YEAR };
  
  //Unified search
  public static final String DETAIL_PATH = "details";
  public static final String DUE_FOR = "Due for: ";
  public static final String ORDER_TYPE_ASCENDING                  = "ASC";
  public static final String ORDER_TYPE_DESCENDING                 = "DESC";
  public static final String DOT = ".";

  public static String  ORDERBY_RELEVANCY  = "relevancy" ;
  public static String  ORDERBY_DATE  = "date" ;
  public static String  ORDERBY_TITLE  = "title" ;
  public static String  DATE_TIME_FORMAT = "EEEEE, MMMMMMMM d, yyyy K:mm a";
  public static String  JCR_EXCERPT = "excerpt(.)";
  public static String  JCR_EXCERPT_ROW = "rep:excerpt(.)";
  
  public final static Map<String, String> SORT_FIELD_MAP = new LinkedHashMap<String, String>(){{
    put(ORDERBY_RELEVANCY, JCR_SCORE);
    put(ORDERBY_DATE, EXO_DATE_CREATED);
    put(ORDERBY_TITLE, EXO_SUMMARY);
  }};
  
  public final static String[] SELECT_FIELDS =  {JCR_EXCERPT, EXO_SUMMARY, EXO_DESCRIPTION, EXO_LOCATION,
    EXO_FROM_DATE_TIME, EXO_TO_DATE_TIME, EXO_EVENT_STATE, EXO_DATE_CREATED, JCR_SCORE, EXO_ID, EXO_CALENDAR_ID, EXO_EVENT_TYPE};
  
  public static String[] SEARCH_FIELDS = {EXO_SUMMARY, EXO_DESCRIPTION, EXO_LOCATION} ;
  
  public static String EVENT_ICON = "Icon" ;
  public static String TASK_ICON = "uiIconAct" ;
  
  /**
   * The method creates an instance of calendar object with time zone is GMT 0
   * @return GregorianCalendar
   */
  public static GregorianCalendar getInstanceTempCalendar() {
    GregorianCalendar calendar = new GregorianCalendar();
    // int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    // calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    return calendar;
  }

  /**
   * The method validates the string value is empty or not
   * @param string String input value
   * @return boolean value
   */
  public static boolean isEmpty(String string) {
    return string == null || string.trim().length() == 0;
  }

  @SuppressWarnings("unchecked")
  public static boolean canEdit(OrganizationService oService, String[] savePerms, String username) throws Exception {
    StringBuffer sb = new StringBuffer(username);
    if (oService != null) {
      Collection<Group> groups = oService.getGroupHandler().findGroupsOfUser(username);
      for (Group g : groups) {
        sb.append(COMMA).append(g.getId()).append(SLASH_COLON).append(ANY);
        sb.append(COMMA).append(g.getId()).append(SLASH_COLON).append(username);
        Collection<Membership> memberShipsType = oService.getMembershipHandler().findMembershipsByUserAndGroup(username, g.getId());
        for (Membership mp : memberShipsType) {
          sb.append(COMMA).append(g.getId()).append(SLASH_COLON).append(ANY_OF + mp.getMembershipType());
        }
      }
    }
    return hasEditPermission(savePerms, sb.toString().split(Utils.COMMA));
  }

  public static boolean isMemberShipType(Collection<Membership> mbsh, String value) {
    if (!isEmpty(value))
      for (String check : value.split(COMMA)) {
        check = check.trim();
        if (check.lastIndexOf(ANY_OF) > -1) {
          if (ANY.equals(check))
            return true;
          value = check.substring(check.lastIndexOf(ANY_OF) + ANY_OF.length());
          if (mbsh != null && !mbsh.isEmpty()) {
            for (Membership mb : mbsh) {
              if (mb.getMembershipType().equals(value))
                return true;
            }
          }
        }
      }
    return false;
  }

  public static boolean hasEditPermission(String[] savePerms, String[] checkPerms) {
    if (savePerms != null)
      for (String sp : savePerms) {
        for (String cp : checkPerms) {
          if (sp.equals(cp)) {
            return true;
          }
        }
      }
    return false;
  }

  public static String getDefaultCalendarId(String username) {
    return new StringBuilder(username).append(MINUS).append(NewUserListener.defaultCalendarId).toString();
  }

  public static PortalContainer getPortalContainer(JobExecutionContext context) {
    if (context == null)
      return null;
    String portalName = context.getJobDetail().getKey().getGroup();
    if (portalName == null)
      return null;
    if (portalName.indexOf(COLON) > 0)
      portalName = portalName.substring(0, portalName.indexOf(":"));
    return RootContainer.getInstance().getPortalContainer(portalName);
  }

  public static String getDisplaySharedCalendar(String sharedUserId, String calName) {
    return sharedUserId + MINUS + SPACE + calName;
  }

  public static String getDisplayGroupCalendar(String groupId, String calName) {
    return groupId.substring(groupId.lastIndexOf(SLASH) + 1) + MINUS + calName;
  }

  public static String[] getEditPerUsers(org.exoplatform.calendar.service.Calendar calendar) throws Exception {
    List<String> sharedUsers = new ArrayList<String>();
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    if (calendar.getEditPermission() != null)
      for (String editPer : calendar.getEditPermission()) {
        if (editPer.contains(Utils.SLASH)) {
          if (organizationService.getGroupHandler().findGroupById(editPer) != null) {
            for (User user : organizationService.getUserHandler().findUsersByGroup(editPer).getAll()) {
              sharedUsers.add(user.getUserName());
            }
          }
        } else {
          sharedUsers.add(editPer);
        }
      }
    return sharedUsers.toArray(new String[sharedUsers.size()]);
  }

  public static java.util.Calendar getGreenwichMeanTime() {
    java.util.Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
    calendar.setLenient(false);
    int gmtoffset = calendar.get(java.util.Calendar.DST_OFFSET) + calendar.get(java.util.Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
    return calendar;
  }
  
  /**
   * Check two dates are in the same day in GMT time zone  
   * @param value1
   * @param value2
   * @return
   */
  public static boolean isSameDate(Date value1, Date value2) {
    Calendar date1 = getInstanceTempCalendar();
    date1.setTime(value1);
    Calendar date2 = getInstanceTempCalendar();
    date2.setTime(value2);
    return (date1.get(java.util.Calendar.DATE) == date2.get(java.util.Calendar.DATE) &&
            date1.get(java.util.Calendar.MONTH) == date2.get(java.util.Calendar.MONTH) &&
            date1.get(java.util.Calendar.YEAR) == date2.get(java.util.Calendar.YEAR)
           );
  }

  public static boolean isRepeatEvent(CalendarEvent event) throws Exception {
    return (event.getRepeatType() != null && !CalendarEvent.RP_NOREPEAT.equals(event.getRepeatType()) && isEmpty(event.getRecurrenceId()));
  }

  public static boolean isExceptionOccurrence(CalendarEvent event) throws Exception {
    return ((event.getIsExceptionOccurrence() != null && event.getIsExceptionOccurrence() == true));
  }

  public static Node getPublicServiceHome(SessionProvider provider) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node publicApp = nodeHierarchyCreator.getPublicApplicationNode(provider);
    if (publicApp != null && publicApp.hasNode(CALENDAR_APP))
      return publicApp.getNode(CALENDAR_APP);
    return null;
  }

  public static Session getSession(SessionProvider sprovider) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository currentRepo = repositoryService.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }
  
  public static SessionProvider createSystemProvider() {
    SessionProviderService sessionProviderService = (SessionProviderService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);
    return sessionProviderService.getSystemSessionProvider(null);
  }

}
