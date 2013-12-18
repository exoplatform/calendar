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

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.UtcOffset;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.Daylight;
import net.fortuna.ical4j.model.component.Standard;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.TzId;
import net.fortuna.ical4j.model.property.TzName;
import net.fortuna.ical4j.model.property.TzOffsetFrom;
import net.fortuna.ical4j.model.property.TzOffsetTo;
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
import org.quartz.impl.JobDetailImpl;

import javax.jcr.Node;
import javax.jcr.Session;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */
public class Utils {

  public static final String EXO_ID                     = "exo:id";

  public static final String EXO_LABEL                  = "exo:label";

  public static final String EXO_NAME                   = "exo:name";

  public static final String EXO_DESCRIPTION            = "exo:description";

  public static final String EXO_EVENT_ID               = "exo:eventId";

  public static final String EXO_EVENT_CATEGORYID       = "exo:eventCategoryId";

  public static final String EXO_EVENT_CATEGORY_NAME    = "exo:eventCategoryName";

  public static final String EXO_TASK_DELEGATOR         = "exo:taskDelegator";

  public static final String EXO_REPEAT                 = "exo:repeat";

  public static final String EXO_EVENT_TYPE             = "exo:eventType";

  public static final String EXO_PRIORITY               = "exo:priority";

  public static final String EXO_IS_PRIVATE             = "exo:isPrivate";

  public static final String EXO_EVENT_STATE            = "exo:eventState";

  public static final String EXO_INVITATION             = "exo:invitation";

  public static final String EXO_CALENDAR_EVENT         = "exo:calendarEvent";

  public static final String EXO_REMINDER_TYPE          = "exo:reminderType";

  public static final String EXO_ALARM_BEFORE           = "exo:alarmBefore";

  public static final String EXO_EMAIL                  = "exo:email";

  public static final String EXO_OWNER                  = "exo:creator";

  public static final String EXO_REMINDER               = "exo:reminder";

  public static final String EXO_FROM_DATE_TIME         = "exo:fromDateTime";

  public static final String EXO_TO_DATE_TIME           = "exo:toDateTime";

  public static final String EXO_SUMMARY                = "exo:summary";

  public static final String EXO_IS_REPEAT              = "exo:isRepeat";

  public static final String EXO_IS_OVER                = "exo:isOver";

  public static final String EXO_CALENDAR_PUBLIC_EVENT  = "exo:calendarPublicEvent";

  public static final String EXO_EVENT_CATEGORY         = "exo:eventCategory";

  public static final String EXO_PUBLIC_URL             = "exo:publicUrl";

  public static final String EXO_PRIVATE_URL            = "exo:privateUrl";

  public static final String EXO_DATA                   = "exo:data";

  public static final String EXO_ICAL_DATA              = "exo:iCalData";

  public static final String EXO_TITLE                  = "exo:title";

  public static final String EXO_CONTENT                = "exo:content";

  public static final String EXO_CALENDAR_SETTING       = "exo:calendarSetting";

  public static final String EXO_IS_SHOW_WORKING_TIME   = "exo:showWorkingTime";

  public static final String EXO_WORKING_BEGIN          = "exo:workingTimeBegin";

  public static final String EXO_WORKING_END            = "exo:workingTimeEnd";

  public static final String EXO_PRIVATE_CALENDARS      = "exo:defaultPrivateCalendars";

  public static final String EXO_PUBLIC_CALENDARS       = "exo:defaultPublicCalendars";

  public static final String EXO_SHARED_CALENDARS       = "exo:defaultSharedCalendars";

  public static final String EXO_SHARED_CALENDAR_COLORS = "exo:sharedCalendarsColors";

  public static final String EXO_EVEN_TATTACHMENT       = "exo:eventAttachment";

  public static final String EXO_FILE_NAME              = "exo:fileName";

  public static final String EXO_CATEGORY_ID            = "exo:categoryId";

  public static final String EXO_VIEW_PERMISSIONS       = "exo:viewPermissions";

  public static final String EXO_EDIT_PERMISSIONS       = "exo:editPermissions";

  public static final String EXO_GROUPS                 = "exo:groups";

  public static final String EXO_LOCALE                 = "exo:locale";

  public static final String EXO_TIMEZONE               = "exo:timeZone";

  public static final String EXO_CALENDAR_ID            = "exo:calendarId";

  public static final String EXO_SHARED_MIXIN           = "exo:calendarShared";

  public static final String EXO_SHARED_ID              = "exo:sharedId";

  public static final String EXO_PARTICIPANT            = "exo:participant";

  public static final String EXO_CALENDAR               = "exo:calendar";

  public static final String EXO_CALENDAR_COLOR         = "exo:calendarColor";

  public static final String EXO_CALENDAR_CATEGORY      = "exo:calendarCategory";

  public static final String EXO_CALENDAR_OWNER         = "exo:calendarOwner";

  public static final String EXO_SHARED_COLOR           = "exo:sharedColor";

  public static final String EXO_VIEW_TYPE              = "exo:viewType";

  public static final String EXO_TIME_INTERVAL          = "exo:timeInterval";

  public static final String EXO_WEEK_START_ON          = "exo:weekStartOn";

  public static final String EXO_DATE_FORMAT            = "exo:dateFormat";

  public static final String EXO_TIME_FORMAT            = "exo:timeFormat";

  public static final String EXO_LOCATION               = "exo:location";

  public static final String EXO_REMINDER_DATE          = "exo:remindDateTime";

  public static final String EXO_ROOT_EVENT_ID          = "exo:rootEventId";

  public static final String EXO_RSS_DATA               = "exo:rssData";

  public static final String EXO_BASE_URL               = "exo:baseUrl";

  public static final String EXO_SEND_OPTION            = "exo:sendOption";

  public static final String EXO_MESSAGE                = "exo:message";

  public static final String EXO_PARTICIPANT_STATUS     = "exo:participantStatus";

  public static final String EXO_DATE_MODIFIED          = "exo:dateModified";

  public static final String EXO_REMOTE_MIXIN           = "exo:remoteCalendar";

  public static final String EXO_REMOTE_URL             = "exo:remoteUrl";

  public static final String EXO_REMOTE_TYPE            = "exo:remoteType";

  public static final String EXO_REMOTE_USERNAME        = "exo:username";

  public static final String EXO_REMOTE_PASSWORD        = "exo:password";

  public static final String EXO_REMOTE_SYNC_PERIOD     = "exo:syncPeriod";

  public static final String EXO_REMOTE_LAST_UPDATED    = "exo:lastUpdated";

  public static final String EXO_REMOTE_BEFORE_DATE     = "exo:beforeDate";

  public static final String EXO_REMOTE_AFTER_DATE      = "exo:afterDate";

  public static final String EXO_REMOTE_EVENT_MIXIN     = "exo:caldavCalendarEvent";

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

  public static final String X_STATUS                   = "X-STATUS";

  public static final String ATTACHMENT_NODE            = "attachment";                                                                              ;

  public static final String REMINDERS_NODE             = "reminders";                                                                               ;

  public static final String NT_UNSTRUCTURED            = "nt:unstructured";

  public static final String NT_FILE                    = "nt:file";

  public static final String NT_RESOURCE                = "nt:resource";

  public static final String MIX_REFERENCEABLE          = "mix:referenceable";

  public static final String JCR_LASTMODIFIED           = "jcr:lastModified";

  public static final String JCR_CONTENT                = "jcr:content";

  public static final String JCR_MIMETYPE               = "jcr:mimeType";

  public static final String JCR_DATA                   = "jcr:data";

  public static final String JCR_SCORE                  = "jcr:score";
  
  public static final String MIMETYPE_TEXTPLAIN         = "text/plain";

  public static final String MIMETYPE_ICALENDAR         = "TEXT/CALENDAR";

  public static final String ATTACHMENT                 = "ATTACHMENT";

  public static final String INLINE                     = "INLINE";

  public static final String COMMA                      = ",";

  public static final String COLON                      = ":";

  public static final String SLASH                      = "/";

  public static final String UNDERSCORE                 = "_";

  public static final String SLASH_COLON                = "/:";

  public static final String COLON_SLASH                = ":/";

  public static final String ANY                        = "*.*";

  public static final String ANY_OF                     = "*.";

  public static final String SLASH_AST                  = "/*";

  public static final String MINUS                      = "-";

  final public static String CALENDAR_REMINDER          = "reminders";

  final public static String CALENDAR_APP               = "CalendarApplication";

  /**
   * These constants were used to indicate participant's answer or action in invitation mail  
   */
  public static final int    DENY                       = 0;

  public static final int    ACCEPT                     = 1;

  public static final int    NOTSURE                    = 2;

  public static final int    ACCEPT_IMPORT              = 3;

  public static final int    JUMP_TO_CALENDAR           = 4;

  public static final String RSS_NODE                   = "iCalendars";

  public static final String CALDAV_NODE                = "WebDavCalendars";

  public static final String ICS_EXT                    = ".ics";

  public static final String RSS_EXT                    = ".rss";

  final public static String EMPTY_STR                  = "";

  final public static String STATUS_PENDING             = "pending";

  final public static String STATUS_YES                 = "yes";

  final public static String STATUS_NO                  = "no";

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

  public static final String MIMETYPE_TEXTHTML          = "text/html";

  public static String[]     SYNC_PERIOD                = { SYNC_AUTO, SYNC_5MINS, SYNC_10MINS, SYNC_15MINS, SYNC_1HOUR, SYNC_1DAY, SYNC_1WEEK, SYNC_1YEAR };

  /*
   * constants for sharing and deleting job
   */
  public static final String SHARE_CALENDAR_GROUP = "CS-ShareCalenar";
  
  public static final String DELETE_SHARED_GROUP = "CS-DeleteShare";

  public static final String SHARED_GROUPS        = "sharedGroups";

  public static final String USER_NAME            = "userName";

  public static final String CALENDAR_ID          = "calendarId";

  public static final String JCR_DATA_STORAGE     = "JCRDataStorage";

  public static final String SHARE_CAL_CHANEL     = "/eXo/Application/Calendar/notifyShareCalendar";
  
  public static final String REMOVED_USERS = "removedUsers";
  
  public static final String START_SHARE = "startShare";
  
  public static final String FINISH_SHARE = "finishShare";
  
  public static final String START_UN_SHARE = "startUnShare";
  
  public static final String FINISH_UN_SHARE = "finishUnShare";
  
  public static final String ERROR_SHARE = "errorShare";
  
  public static final String ERROR_UN_SHARE = "errorUnShare";
  
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
  
  public static String DATE_FORMAT_RECUR_ID = "yyyyMMdd'T'HHmmss'Z'";
  
  public final static Map<String, String> SORT_FIELD_MAP = new LinkedHashMap<String, String>(){{
    put(ORDERBY_RELEVANCY, JCR_SCORE);
    put(ORDERBY_DATE, EXO_DATE_CREATED);
    put(ORDERBY_TITLE, EXO_SUMMARY);
  }};
  
  public final static String[] SELECT_FIELDS =  {JCR_EXCERPT, EXO_SUMMARY, EXO_DESCRIPTION, EXO_LOCATION,
    EXO_FROM_DATE_TIME, EXO_TO_DATE_TIME, EXO_EVENT_STATE,EXO_IS_PRIVATE, EXO_DATE_CREATED, JCR_SCORE, EXO_ID, EXO_CALENDAR_ID, EXO_EVENT_TYPE};
  
  public static String[] SEARCH_FIELDS = {EXO_SUMMARY, EXO_DESCRIPTION, EXO_LOCATION} ;
  public static String EVENT_ICON_URL = null;
  public static String TASK_ICON_URL = "/social-resources/skin/images/Activity/status-task.png";
  
  public static final String DEFAULT_SITENAME = "intranet";
  public static final String PAGE_NAGVIGATION = "calendar";
  public static final String NONE_NAGVIGATION = "#";
  public static final String PORTLET_NAME = "CalendarPortlet";
  public static final String SPACES_GROUP = "spaces";
  public static final String SPACES_GROUP_ID_PREFIX = "/spaces/";
  public static final String SPACE_CALENDAR_ID_SUFFIX = "_space_calendar";

  /**
   * The method creates an instance of calendar object with time zone is GMT 0
   * @return GregorianCalendar
   */
  public static GregorianCalendar getInstanceTempCalendar() {
    GregorianCalendar calendar = new GregorianCalendar();
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
    String portalName = ((JobDetailImpl)context.getJobDetail()).getGroup();
    if (portalName == null)
      return null;
    if (portalName.indexOf(COLON) > 0)
      portalName = portalName.substring(0, portalName.indexOf(COLON));
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
          // edit permision has form: groupId/:membership, for ex: /platform/user/:*.* or /platform/:*.member
          sharedUsers.addAll(getUsersCanEdit(editPer));
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
    return (event.getRepeatType() != null && !CalendarEvent.RP_NOREPEAT.equals(event.getRepeatType()));
  }

  public static boolean isExceptionOccurrence(CalendarEvent event) throws Exception {
    return ((event.getIsExceptionOccurrence() != null && event.getIsExceptionOccurrence() == true));
  }

  public static boolean isOccurrence(CalendarEvent event){
    return (event.getRepeatType() != null && !CalendarEvent.RP_NOREPEAT.equals(event.getRepeatType()) && (event.getIsExceptionOccurrence() == null || !event.getIsExceptionOccurrence()));
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
  
  /**
   * build message about job sharing calendar for groups
   * @param type The type can be: share,un-share,finishShare,finishUnShare
   * @param calendarName
   * @param groups Groups that are shared/un-shared
   * @return String in form [type, calendarName, group1, group2,..]
   */
  public static String buildMessageToSend(String type, String calendarName, List<String> groups, OrganizationService oService) throws Exception {
    StringBuilder sb = new StringBuilder("");
    sb.append(type);
    sb.append(",");
    sb.append(calendarName);
    Iterator<String> it = groups.iterator();
    while (it.hasNext()) {
      sb.append(",");
      String groupId = it.next();
      Group group = oService.getGroupHandler().findGroupById(groupId);
      sb.append(group.getGroupName());
    }
    return sb.toString();
  }
  
  public static Calendar getBeginDay(Calendar cal) {
    Calendar newCal = (Calendar) cal.clone();

    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
    return newCal ;
  }
  
  public static Calendar getEndDay(Calendar cal)  {
    Calendar newCal = (Calendar) cal.clone();    
    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
    newCal.add(Calendar.HOUR_OF_DAY, 24) ;
    return newCal ;
  }
  
  /**
   * get list of user by membership id and group id
   * example of membership id: validator, group id: /platform/users
   * @param membershipId
   * @param groupId
   * @return
   * @throws Exception
   */
  public static Set<String> getUserByMembershipId(String membershipId, String groupId) throws Exception
  {
    OrganizationService organizationService = (OrganizationService)PortalContainer.getInstance().
        getComponentInstance(OrganizationService.class) ;
    List<User> usersInGroup = organizationService.getUserHandler().findUsersByGroup(groupId).getAll();
    Set<String> userIds = new HashSet<String>();
    
    if (usersInGroup == null) return userIds;
    
    if("*".equals(membershipId)) { // if membership id is "*" that means we get all users in the group
      for(User user : usersInGroup.toArray(new User[]{})) {
        userIds.add(user.getUserName());
      }
      return userIds;
    } else {
      for (User user : usersInGroup.toArray(new User[]{}))
      {
        Membership membership = organizationService.getMembershipHandler().findMembershipByUserGroupAndType(user.getUserName(),
            groupId, membershipId);
        if (membership != null) {
          userIds.add(user.getUserName());
        }
      }
      return userIds;
    }
    
  }
  /**
   * gets users by edit permission 
   * @param editPer in form groupid/:*.membershipId for ex: /platform/user/:*.*
   * @return
   * @throws Exception
   */
  public static List<String> getUsersCanEdit(String editPer) throws Exception {
    List<String> result = new ArrayList<String>();
    String[] perArr = editPer.split(":");
    String membershipId = perArr[1].substring(2);
    String groupId = perArr[0].substring(0, perArr[0].length() - 1);
    Set<String> usersCanEdit = getUserByMembershipId(membershipId, groupId);
    result.addAll(usersCanEdit);
    return result;
  }
  /**
   * Gets id for a calendar space by space group id
   * 
   * @param spaceGroupId in form  ex: /spaces/mobile_team
   * @return calendar id in form ex: mobile_team_space_calendar
   */
  public static String getCalendarIdFromSpace(String spaceGroupId) {
    StringBuilder sb = new StringBuilder(spaceGroupId.substring(SPACES_GROUP_ID_PREFIX.length()));
    sb.append(SPACE_CALENDAR_ID_SUFFIX);
    return sb.toString();
  }
  
  /**
   * Gets space group id from calendar id of a space calendar
   * 
   * @param calendarId in form ex: mobile_team_space_calendar
   * @return space group id ex: /spaces/mobile_team
   */
  public static String getSpaceGroupIdFromCalendarId(String calendarId) {
    StringBuilder sb = new StringBuilder(SPACES_GROUP_ID_PREFIX);
    sb.append(calendarId.split(SPACE_CALENDAR_ID_SUFFIX)[0]);
    return sb.toString();
  }
  
  /**
   * Gets an ical4j TimeZone object from a java.util.TimeZone object
   * @param jTz a java.util.TimeZone object
   * @return an ical4j TimeZone object
   * @throws ParseException
   */
  public static net.fortuna.ical4j.model.TimeZone getICalTimeZone(TimeZone jTz) throws ParseException {
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    Calendar calendar = Calendar.getInstance();
    String dtStartValue = dateFormat.format(calendar.getTime());
    //Properties for Standard component
    PropertyList standardTzProps = new PropertyList();
    
    TzName standardTzName = new TzName(new ParameterList(), jTz.getDisplayName(false,TimeZone.SHORT));
    
    DtStart standardTzStart = new DtStart();
    standardTzStart.setValue(dtStartValue);
    
    TzOffsetTo standardTzOffsetTo = new TzOffsetTo();
    standardTzOffsetTo.setOffset(new UtcOffset(jTz.getRawOffset()));
    
    TzOffsetFrom standardTzOffsetFrom = new net.fortuna.ical4j.model.property.TzOffsetFrom();
    standardTzOffsetFrom.setOffset(new UtcOffset(jTz.getRawOffset() +  jTz.getDSTSavings()));

    standardTzProps.add(standardTzName);
    standardTzProps.add(standardTzStart);
    standardTzProps.add(standardTzOffsetTo);
    standardTzProps.add(standardTzOffsetFrom);
    
    //Standard Component for VTimeZone
    Standard standardTz = new Standard(standardTzProps);
    
    //Components for VTimeZone
    ComponentList tzComponents = new ComponentList();
    tzComponents.add(standardTz);
    
    if(jTz.useDaylightTime()) {
      //Properties for DayLight component
      PropertyList daylightTzProps = new PropertyList();
      
      TzName daylightTzName = new TzName(jTz.getDisplayName(true, TimeZone.SHORT));
      
      DtStart daylightDtStart = new DtStart();
      daylightDtStart.setValue(dtStartValue);
      
      TzOffsetTo daylightTzOffsetTo = new TzOffsetTo();
      daylightTzOffsetTo.setOffset(new UtcOffset(jTz.getRawOffset() +  jTz.getDSTSavings()));
      
      TzOffsetFrom daylightTzOffsetFrom = new TzOffsetFrom();
      daylightTzOffsetFrom.setOffset(new UtcOffset(jTz.getRawOffset()));
      
      daylightTzProps.add(daylightTzOffsetFrom);
      daylightTzProps.add(daylightTzOffsetTo);
      daylightTzProps.add(daylightDtStart);
      daylightTzProps.add(daylightTzName);

      //Daylight Component for VTimeZone
      Daylight daylightTz = new Daylight(daylightTzProps);
      //add daylight component to VTimeZone
      tzComponents.add(daylightTz);
    }
    
    PropertyList tzProps = new PropertyList();
    TzId tzId = new TzId(null, jTz.getID());
    tzProps.add(tzId);
    
    //Construct the VTimeZone object
    VTimeZone vTz = new VTimeZone(tzProps, tzComponents);
    try {
      vTz.validate();
      return new net.fortuna.ical4j.model.TimeZone(vTz);
    } catch (ValidationException e) {
      return null;
    }
  }
  
  /**
   * adapts the repeat rule
   * because of different time zones, the repeated day of a repetitive event can be different 
   * in each user's setting time zone. We need to take into account this one.
   * @since CAL-572 
   * @param recur Recur object that contains the repeat rule
   * @param firstOccurDate The date time of the first occurrence of the series
   * @param tz User time zone
   */
  @SuppressWarnings("unchecked")
  public static void adaptRepeatRule(Recur recur, DateTime firstOccurDate, TimeZone tz) {
    WeekDay[] weekdays = {WeekDay.SU, WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR, WeekDay.SA};
    int delta;
    WeekDayList weekdayList = recur.getDayList();
    
    NumberList numberList = recur.getMonthDayList();
    
    Calendar calendar = Calendar.getInstance(tz);
    calendar.setTimeInMillis(firstOccurDate.getTime());
    
    // repeated weekly/monthly, by day of week
    if(weekdayList != null && weekdayList.size() > 0) {
      WeekDay expectedFirstWeekday = WeekDay.getWeekDay(calendar);
      WeekDay firstWeekDayOfRule = (WeekDay) weekdayList.get(0);
      delta = WeekDay.getCalendarDay(expectedFirstWeekday) - WeekDay.getCalendarDay(firstWeekDayOfRule);
      if(delta != 0) { //if there is a difference, adapt the week day list of the rule
        WeekDayList newWeekDayList = new WeekDayList();
        for(int i = 0; i < weekdayList.size(); i ++) {
          WeekDay weekdayI = (WeekDay) weekdayList.get(i);
          int index = (WeekDay.getCalendarDay(weekdayI) - 1 + delta) % 7;
          newWeekDayList.add(weekdays[index]);
        }
        recur.getDayList().removeAll(weekdayList);
        recur.getDayList().addAll(newWeekDayList);
      }
    }
    
    // repeated monthly, by day in month
    if(numberList != null && numberList.size() > 0) {
      Integer firstDayOfRule = (Integer)numberList.get(0);
      int expectedFirstDay = calendar.get(Calendar.DAY_OF_MONTH);
      delta = expectedFirstDay - firstDayOfRule.intValue();
      if(delta != 0) {
        NumberList newNumberList = new NumberList();
        for(int j = 0; j < numberList.size(); j++) {
          Integer numberI = (Integer)numberList.get(j);
          newNumberList.add(new Integer(numberI.intValue() + delta));
        }
        recur.getMonthDayList().removeAll(numberList);
        recur.getMonthDayList().addAll(newNumberList);
      }
    }
  }


  /**
   * Gets a repetitive event's occurrence date right before a given date
   * @param recurEvent The repetitive event
   * @param aDate  The date before which we find the occurrence date
   * @param tz User timezone
   * @return the occurrence date right before the given aDate
   * @throws Exception
   */
  public static Date getPreviousOccurrenceDate(CalendarEvent recurEvent, Date aDate, TimeZone tz) throws Exception {

    DateTime ical4jEventFrom = new DateTime(recurEvent.getFromDateTime());

    VEvent vevent = new VEvent(ical4jEventFrom, Utils.EMPTY_STR);

    Recur recur = getICalendarRecur(recurEvent);

    vevent.getProperties().add(new RRule(recur));

    Calendar calendar = new GregorianCalendar();
    calendar.setTimeZone(tz);

    calendar.set(Calendar.YEAR, calendar.getMinimum(Calendar.YEAR));
    DateTime ical4jFrom = new DateTime(calendar.getTime());
    calendar.setTime(aDate);
    //store the difference after applying timezone
    int delta = aDate.getDate() - calendar.get(Calendar.DATE);
    //include the selected occurrence by move calendar to beginning of the next day
    calendar.add(Calendar.DATE, 1);
    calendar.set(Calendar.HOUR_OF_DAY,0);
    calendar.set(Calendar.MINUTE,0);
    calendar.set(Calendar.SECOND,0);

    DateTime ical4jTo = new DateTime(calendar.getTime());

    Period period = new Period(ical4jFrom, ical4jTo);
    PeriodList list = vevent.calculateRecurrenceSet(period);

    if (list == null || list.size() == 0 || list.size() == 1) {
      return null;
    }
    Period last = (Period) list.last();
    list.remove(last);
    last = (Period) list.last();

    calendar.setTimeInMillis(last.getStart().getTime());
    //compensate the difference
    calendar.add(Calendar.DATE, delta);

    return calendar.getTime();
  }
  public static Recur getICalendarRecur(CalendarEvent recurEvent) throws Exception {
    String repeatType = recurEvent.getRepeatType();
    // get the repeat count property of recurrence event
    int count = (int) recurEvent.getRepeatCount();

    java.util.Calendar until = null;
    if (recurEvent.getRepeatUntilDate() != null) {
      until = Utils.getInstanceTempCalendar();
      //set until to the end of the day, to include the until date in the occurrence instances list
      until.setTimeInMillis(recurEvent.getRepeatUntilDate().getTime() + 24 * 60 * 60 * 1000 - 1);
    }

    int interval = (int) recurEvent.getRepeatInterval();
    if (interval <= 1)
      interval = 1;

    Recur recur = null;

    // daily recurrence
    if (repeatType.equals(CalendarEvent.RP_DAILY)) {
      if (until != null) {
        recur = new Recur(Recur.DAILY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.DAILY, count);
        } else
          recur = new Recur("FREQ=DAILY");
      }
      recur.setInterval(interval);
      return recur;
    }

    // weekly recurrence
    if (repeatType.equals(CalendarEvent.RP_WEEKLY)) {
      if (until != null) {
        recur = new Recur(Recur.WEEKLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.WEEKLY, count);
        } else
          recur = new Recur("FREQ=WEEKLY");
      }
      recur.setInterval(interval);

      // byday property
      String[] repeatByDay = recurEvent.getRepeatByDay();
      if (repeatByDay == null || repeatByDay.length == 0)
        return null;
      WeekDayList weekDayList = new WeekDayList();
      for (String s : repeatByDay) {
        weekDayList.add(new WeekDay(s));
      }
      recur.getDayList().addAll(weekDayList);
      return recur;
    }

    // monthly recurrence
    if (repeatType.equals(CalendarEvent.RP_MONTHLY)) {
      if (until != null) {
        recur = new Recur(Recur.MONTHLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.MONTHLY, count);
        } else
          recur = new Recur("FREQ=MONTHLY");
      }
      recur.setInterval(interval);

      long[] repeatByMonthDay = recurEvent.getRepeatByMonthDay();
      // case 1: byMonthDay: day 1, 15, 26 of month
      if (repeatByMonthDay != null && repeatByMonthDay.length > 0) {
        NumberList numberList = new NumberList();
        for (long monthDay : repeatByMonthDay) {
          numberList.add(new Integer((int) monthDay));
        }
        recur.getMonthDayList().addAll(numberList);
      } else {
        // case 2: byDay: 1SU: first Sunday of month, -1TU: last Tuesday of
        // month
        String[] repeatByDay = recurEvent.getRepeatByDay();
        if (repeatByDay != null && repeatByDay.length > 0) {
          WeekDayList weekDayList = new WeekDayList();
          for (String s : repeatByDay) {
            weekDayList.add(new WeekDay(s));
          }
          recur.getDayList().addAll(weekDayList);
        }
      }
      return recur;
    }

    // yearly recurrence
    if (repeatType.equals(CalendarEvent.RP_YEARLY)) {
      if (until != null) {
        recur = new Recur(Recur.YEARLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.YEARLY, count);
        } else
          recur = new Recur("FREQ=YEARLY");
      }
      recur.setInterval(interval);
      return recur;
    }
    return recur;
  }
}
