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
package org.exoplatform.calendar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.jcr.PathNotFoundException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletPreferences;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.popup.UIAddressForm.ContactData;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public class CalendarUtils {

  public static final String PRIVATE_CALENDARS = "privateCalendar".intern();
  public static final String SHARED_CALENDARS = "sharedCalendar".intern();
  public static final String PUBLIC_CALENDARS = "publicCalendar".intern();
  public static final String PRIVATE_TYPE = "0".intern();
  public static final String SHARED_TYPE = "1".intern();
  public static final String PUBLIC_TYPE = "2".intern();
  public static final String SEMICOLON = ";".intern();
  public static final String COLON = ":".intern();
  public static final String COMMA = ",".intern();
  public static final String STAR = "*".intern();
  public static final String PLUS = "+".intern();
  public static final String AND = "&".intern();
  public static final String OR = "|".intern();
  public static final String PERCENT = "%".intern();
  public static final String QUESTION_MARK = "?".intern();
  public static final String SINGLE_QUOTE = "'".intern();
  public static final String QUOTE = "\"".intern();
  public static final String AT = "@".intern();
  public static final String EXCLAMATION = "!".intern() ;
  public static final String SHARP = "#".intern() ;
  public static final String OPEN_PARENTHESIS = "(".intern() ;
  public static final String CLOSE_PARENTHESIS = ")".intern() ;
  public static final String OPEN_SQUARE_BRACKET = "[".intern() ;
  public static final String CLOSE_SQUARE_BRACKET = "]".intern() ;
  public static final String OPEN_SHARP_BRACKET = "{".intern() ;
  public static final String CLOSE_SHARP_BRACKET = "}".intern() ;
  public static final String MONEY_MARK = "$".intern() ;
  public static final String EXPONENT = "^".intern() ;
  public static final String MINUS = "-".intern();
  public static final String SLASH = "/".intern();
  public static final String BACKSLASH = "\\".intern();
  public static final String DOUBLESCORE = "--".intern();
  public static final String UNDERSCORE = "_".intern();
  public static final String SLASH_COLON = "/:".intern() ;
  public static final String COLON_SLASH = ":/".intern() ;
  public static final String GREATER_THAN = ">".intern() ;
  public static final String SMALLER_THAN = "<".intern() ;
  public static final String ANY = "*.*".intern();
  public static final String ANY_OF = "*.".intern();
  public static final String DOT = ".".intern();
  public static final String TIMEFORMAT  = "HH:mm".intern();
  public static final String DATEFORMAT = "MM/dd/yyyy".intern();
  public static final String[] TIMEFORMATPATTERNS = {"hh:mm a","HH:mm"} ;
  public static final String DATEFORMAT1 = "dfm0".intern();
  public static final String DATEFORMAT2 = "dfm1".intern();
  public static final String DATEFORMAT3 = "dfm2".intern();
  public static final String DATEFORMAT4 = "dfm3".intern();
  public static final String[] DATEFORMATS = {DATEFORMAT1,DATEFORMAT2,DATEFORMAT3,DATEFORMAT4} ;
  public static final String FORMATPATTERN1 = "dd/MM/yyyy".intern();
  public static final String FORMATPATTERN2 = "dd-MM-yyyy".intern();
  public static final String FORMATPATTERN3 = "MM/dd/yyyy".intern();
  public static final String FORMATPATTERN4 = "MM-dd-yyyy".intern();
  public static final String[] FORMATPATTERNS = {FORMATPATTERN1,FORMATPATTERN2,FORMATPATTERN3,FORMATPATTERN4} ;
  public static final String TWELVE_HOURS  = "12-Hours".intern() ;
  public static final String TWENTY_FOUR_HOURS  = "24-Hours".intern() ;
  public static final int DEFAULT_VALUE_UPLOAD_PORTAL = -1;

  public static final String BREAK_LINE = "\n".intern() ;

  public static final String DATETIMEFORMAT = DATEFORMAT +" " +TIMEFORMAT;   
  public static final int DEFAULT_TIMEITERVAL = 15;
  public static final long MILISECONS_OF_DAY = 24*60*59*1000;
  public static final String EXO_INVITATION = "X-Exo-Invitation".intern();
  public static final String SPECIALCHARACTER[] = {SEMICOLON,COMMA,SLASH,BACKSLASH,
    SINGLE_QUOTE,OR,GREATER_THAN,SMALLER_THAN,QUOTE, QUESTION_MARK, EXCLAMATION, 
    AT, SHARP, MONEY_MARK, PERCENT,EXPONENT,AND,STAR};
  public static final String EXTENDEDCHARACTER[] = {SEMICOLON,COMMA,COLON,SLASH,BACKSLASH,
    SINGLE_QUOTE,OR,GREATER_THAN,SMALLER_THAN,QUOTE, QUESTION_MARK, EXCLAMATION, 
    AT, SHARP, MONEY_MARK, PERCENT,EXPONENT,AND,STAR,PLUS,OPEN_SQUARE_BRACKET,CLOSE_SQUARE_BRACKET};
  public static final String EXTENDEDKEYWORD[] = {SEMICOLON,COMMA,COLON,SLASH,BACKSLASH,
    SINGLE_QUOTE,OR,GREATER_THAN,SMALLER_THAN,QUOTE, QUESTION_MARK, EXCLAMATION, 
    AT, SHARP, MONEY_MARK, PERCENT,EXPONENT,AND,STAR,PLUS,OPEN_SQUARE_BRACKET,CLOSE_SQUARE_BRACKET,
    OPEN_SHARP_BRACKET,CLOSE_SHARP_BRACKET,OPEN_PARENTHESIS,CLOSE_PARENTHESIS};
  public static final String SIMPLECHARACTER[] = {GREATER_THAN,SMALLER_THAN};

  final public static String FIELD_SEND = "send".intern();
  final public static String ITEM_ALWAYS = "always".intern();
  final public static String ITEM_NERVER = "never".intern();
  final public static String ITEM_ASK = "ask".intern();
  final public static String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+";
  /* single email format regex */
  public static final String emailFormat = "[_A-Za-z0-9-]+@[_A-Za-z0-9-.]+";
  final public static String contactRegex = ".\\("+ emailRegex + "\\)";

  public final static String INVITATION_URL = "/invitation/".intern();
  public final static String INVITATION_IMPORT_URL = "/invitation/import/".intern();
  public final static String INVITATION_DETAIL_URL = "/invitation/detail/".intern();
  public static final String DETAILS_URL = "/details/";
  public static final String DETAIL_URL = "/detail/";
  public static final String IS_CLOSING = "closingPopup";

  private static Log log = ExoLogger.getLogger(CalendarUtils.class);

  public static final String[] getUserGroups(String username) throws Exception {
    ConversationState conversationState = ConversationState.getCurrent();
    Identity identity = conversationState.getIdentity();
    Set<String> objs = identity.getGroups();
    String[] groups = new String[objs.size()];
    int i = 0;
    for (String obj : objs) {
      groups[i++] = obj;
    }
    return groups;
  }
  static public String[] getAllGroups() throws Exception {
    Object[] objs = getOrganizationService().getGroupHandler().getAllGroups().toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((Group)objs[i]).getId() ;
    }
    return groups ;
  }

  public static boolean isEmpty(String value) {
    return (value == null || value.trim().length() == 0) ;
  }

  static public CalendarService getCalendarService() throws Exception {
    return (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class);
  }

  static public OrganizationService getOrganizationService() throws Exception {
    return (OrganizationService)PortalContainer.getInstance().getComponentInstance(OrganizationService.class) ;
  }

  /**
   * calendar setting registry by user name. 
   */
  private static ConcurrentHashMap<String, CalendarSetting> calendarSettingsByUserName = new ConcurrentHashMap<String, CalendarSetting>();

  /**
   * remove current calendar setting from registry.
   * @return
   */
  public static CalendarSetting removeCurrentCalendarSetting() {
    try {
      return calendarSettingsByUserName.remove(getCurrentUser());
    } catch (Exception e) {
      log.warn("could not remove calendar setting of the user.", e);
      return null;
    }
  }

  /**
   * change value of calendar setting of user in registry.
   * 
   * @param setting = null means that current calendar setting will be removed.
   */
  public static void setCurrentCalendarSetting(CalendarSetting setting) {
    try {
      if (setting == null) {
        calendarSettingsByUserName.remove(getCurrentUser());
      } else {
        calendarSettingsByUserName.put(getCurrentUser(), setting);
      }
    } catch (Exception e) {
      log.warn("could not set calendar setting for current user", e);
    }
  }

  /**
   * 
   * @return calendar setting of current user. return null if any exception is thrown.
   */
  public static CalendarSetting getCurrentUserCalendarSetting() {

    try {
      String user = getCurrentUser();
      CalendarSetting setting = calendarSettingsByUserName.get(user);
      if (setting == null) {
        setting = getCalendarService().getCalendarSetting(user) ;
        calendarSettingsByUserName.put(user, setting);
      }
      return setting;
    } catch (Exception e) {
      log.warn("could not get calendar setting of user", e);
      return null;
    }

  }

  /**
   * @return
   * @deprecated
   */
  @Deprecated
  public static Calendar getInstanceTempCalendar() {
    return getInstanceOfCurrentCalendar();
  }

  /**
   * get calendar by user setting (timezone, first day of week)
   * @param calendarSetting
   * @return calendar object
   */
  public static Calendar getCalendarInstanceBySetting(final CalendarSetting calendarSetting) {
    Calendar  calendar = Calendar.getInstance() ;
    calendar.setLenient(false);
    calendar.setTimeZone(TimeZone.getTimeZone(calendarSetting.getTimeZone()));
    calendar.setFirstDayOfWeek(Integer.parseInt(calendarSetting.getWeekStartOn()));
    calendar.setMinimalDaysInFirstWeek(4);
    return calendar;
  }

  /**
   * 
   * @return return an instance of Calendar class which contains user's setting, such as, time zone, first day of week.
   */
  public static Calendar getInstanceOfCurrentCalendar() {
    try {
      CalendarSetting setting = getCurrentUserCalendarSetting();
      return getCalendarInstanceBySetting(setting); 
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn("Could not get calendar setting!", e);
      Calendar calendar = Calendar.getInstance() ;
      calendar.setLenient(false);
      return calendar;
    }
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    return getTimesSelectBoxOptions(timeFormat, TIMEFORMAT, CalendarSetting.DEFAULT_TIME_INTERVAL, locale);
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat) {
    return getTimesSelectBoxOptions(labelFormat, valueFormat, CalendarSetting.DEFAULT_TIME_INTERVAL);
  }

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval) {

    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    return getTimesSelectBoxOptions(labelFormat, valueFormat, timeInteval, locale);
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval, Locale locale) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("")); // get a GMT calendar
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;

    DateFormat dfLabel = new SimpleDateFormat(labelFormat, locale) ;
    dfLabel.setCalendar(cal) ;
    DateFormat dfValue = new SimpleDateFormat(valueFormat, locale) ;
    dfValue.setCalendar(cal) ;

    int day = cal.get(Calendar.DAY_OF_MONTH);
    while (day == cal.get(Calendar.DAY_OF_MONTH)) {
      options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, (int)timeInteval) ;
    }
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.MILLISECOND, 59) ;
    options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime()))) ;
    return options ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat, int timeInteval) {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    return getTimesSelectBoxOptions(timeFormat, TIMEFORMAT, timeInteval, locale);
  }

  /**
   * render time zone label for a time zone.
   * @param timeZoneID
   * @return display string for the time zone id, which contains value of GMT offset and time zone id.
   * For example, display string for "Asia/Ho_Chi_Minh" would be "(GMT +07:00) Asia/Ho_Chi_Minh".
   */
  public static String generateTimeZoneLabel(String timeZoneID) {
    String label = timeZoneID;
    if(label.lastIndexOf("/") > 0 && label.toLowerCase().lastIndexOf("etc".toLowerCase()) < 0 && label.toLowerCase().lastIndexOf("system") < 0) {
      TimeZone timeZone = TimeZone.getTimeZone(label) ;
      int rawOffset = timeZone.getRawOffset() / 60000;
      int hours = rawOffset / 60;
      int minutes = Math.abs(rawOffset) % 60;
      String hrStr = "";
      if (Math.abs(hours) < 10) {
        if (hours < 0) {
          hrStr = "-0" + Math.abs(hours);
        } else {
          hrStr = "0" + Math.abs(hours);
        }
      } else {
        hrStr = Integer.toString(hours);
      }
      String minStr = (minutes < 10) ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
      label = "(GMT " + ((timeZone.getRawOffset() >= 0) ? "+" : "") 
          + hrStr + ":" + minStr + ") " + timeZoneID ;

    }
    return label;
  }

  public static List<SelectItemOption<String>> getTimeZoneSelectBoxOptions(String[] timeZoneIds) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for (String tz : timeZoneIds){
      if(tz.lastIndexOf("/") > 0 && tz.toLowerCase().lastIndexOf("etc".toLowerCase()) < 0 && tz.toLowerCase().lastIndexOf("system") < 0) {
        String str = generateTimeZoneLabel(tz);
        options.add(new SelectItemOption<String>(str, tz)) ;
      } 
    }
    return options ;
  }

  /**
   * render display string for an ISO3 location name.
   * @param locationName - ISO3 location name
   * @return display string that contains display country name and display language name. 
   * For example, output for "VNM" would be "Vietnam(Vietnamese)".
   */
  public static String getLocationDisplayString(String locationName) {
    Locale[] avai = Locale.getAvailableLocales();
    Locale locale = null;
    for (Locale l : avai) {
      if (l.getISO3Country().equalsIgnoreCase(locationName)) {
        locale = l;
        break;
      }
    }

    if (locale != null) {
      String country = locale.getISO3Country();
      if (country != null && country.trim().length() > 0)
        return locale.getDisplayCountry() + "(" + locale.getDisplayLanguage() + ")";
    }
    return locationName;
  }

  @SuppressWarnings("unchecked")
  public static List<SelectItemOption<String>> getLocaleSelectBoxOptions(Locale[] locale) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for (Locale local : locale) {
      String country = local.getISO3Country();
      if (country != null && country.trim().length() > 0)
        options.add(new SelectItemOption<String>(local.getDisplayCountry() + "(" + local.getDisplayLanguage() + ")", country));
    }
    Collections.sort(options, new SelectComparator()) ;
    return options ;
  }
  public static String parse(Date date, String timeFormat) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    return df.format(date) ;    
  }
  public static String parse(Date date, String timeFormat, Locale locale) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat, locale) ;
    return df.format(date) ;    
  }
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }

  public static boolean isAllDayEvent(CalendarEvent eventCalendar) {
    Calendar cal1 = getInstanceOfCurrentCalendar() ;
    Calendar cal2 = getInstanceOfCurrentCalendar() ;
    cal1.setTime(eventCalendar.getFromDateTime()) ;
    cal2.setTime(eventCalendar.getToDateTime()) ;
    return (cal1.get(Calendar.HOUR_OF_DAY) == 0  && 
        cal1.get(Calendar.MINUTE) == 0 &&
        cal2.get(Calendar.HOUR_OF_DAY) == cal2.getActualMaximum(Calendar.HOUR_OF_DAY)&& 
        cal2.get(Calendar.MINUTE) == cal2.getActualMaximum(Calendar.MINUTE) );
  }

  public static boolean isSameDate(java.util.Calendar date1, java.util.Calendar date2) {
    return ( date1.get(java.util.Calendar.DATE) == date2.get(java.util.Calendar.DATE) &&
        date1.get(java.util.Calendar.MONTH) == date2.get(java.util.Calendar.MONTH) &&
        date1.get(java.util.Calendar.YEAR) == date2.get(java.util.Calendar.YEAR)
        ) ;
  }
  public static boolean isSameDate(Date value1, Date value2) {
    Calendar date1 = getInstanceOfCurrentCalendar() ;
    date1.setTime(value1) ;
    Calendar date2 = getInstanceOfCurrentCalendar() ;
    date2.setTime(value2) ;
    return isSameDate(date1, date2) ;
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

  public static Calendar getBeginDay(Date date) {    
    Calendar cal = getInstanceOfCurrentCalendar() ;
    cal.setTime(date) ;
    return getBeginDay(cal) ;
  }

  public static Calendar getEndDay(Date date)  {
    Calendar cal = getInstanceOfCurrentCalendar() ;
    cal.setTime(date) ;
    return getEndDay(cal) ;
  }


  public static String getDataSource(Attachment attach, DownloadService dservice) throws Exception {      
    if (attach != null) {
      try {
        InputStream input = attach.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, attach.getMimeType() ) ;
          dresource.setDownloadName(attach.getName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        } 
      } catch (PathNotFoundException ex) {
        if (log.isDebugEnabled()) {
          log.debug("The attachment has no data source", ex);
        }
        return null ;
      }
    }
    return null ;
  }
  /*
  public static boolean isNameValid(String name, String[] regexpression) {
    for(String c : regexpression){ if(name.contains(c)) return false ;}
    return true ;
  }*/

  public static boolean isNameEmpty(String name) {
    return (name == null || name.trim().length() == 0) ;
  }

  public static String getServerBaseUrl() {
    PortletRequestContext portletRequestContext = RequestContext.getCurrentInstance() ;
    String url = portletRequestContext.getRequest().getScheme() + "://" + 
        portletRequestContext.getRequest().getServerName() + ":" +
        String.format("%s",portletRequestContext.getRequest().getServerPort()) 
        + "/" ;
    return url ;
  }

  static public String getTimeZone(String timezone) {
    TimeZone timeZone = TimeZone.getTimeZone(timezone) ;
    int rawOffset = timeZone.getRawOffset()  ;
    return String.valueOf(0 - (rawOffset /60000 + timeZone.getDSTSavings()/60000)) ;
  }

  /**
   * 
   * @return the base URL to the current calendar application
   * @throws Exception 
   */
  public static String getCalendarURL() throws Exception {
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String url = pContext.getRequest().getRequestURL().toString();
    String selectedNode = Util.getUIPortal().getSelectedUserNode().getURI() ;
    String portalName = pContext.getPortalOwner();
    if (url.indexOf(portalName) > 0) {
      String s = new StringBuilder().append(portalName).append("/").append(selectedNode).toString();  
      if(url.indexOf(s) < 0){
        url = url.replaceFirst(portalName, s) ;
      } 
      selectedNode = s;
      url = url.substring(0, url.lastIndexOf(selectedNode) + selectedNode.length());
    } 
    
    return url;
  }

  public static boolean hasEditPermission(String[] savePerms, String[] checkPerms) {
    if(savePerms != null)
      for(String sp : savePerms) {
        for (String cp : checkPerms) {
          if(sp.equals(cp)) {return true ;}      
        }
      }
    return false ;
  } 

  public static boolean canEdit(OrganizationService oService, String[] savePerms, String username) throws Exception {
    String checkPerms = getCheckPermissionString();
    return CalendarUtils.hasEditPermission(savePerms, checkPerms.toString().split(CalendarUtils.COMMA)) ;
  }

  public static String getCheckPermissionString() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    StringBuffer sb = new StringBuffer(identity.getUserId());
    Set<String> groupsId = identity.getGroups();
    for (String groupId : groupsId) {
      sb.append(CalendarUtils.COMMA).append(groupId).append(SLASH_COLON).append(ANY);
      sb.append(CalendarUtils.COMMA).append(groupId).append(SLASH_COLON).append(identity.getUserId());
    }
    Collection<MembershipEntry> memberships = identity.getMemberships();
    for (MembershipEntry membership : memberships) {
      sb.append(CalendarUtils.COMMA).append(membership.getGroup()).append(SLASH_COLON).append(ANY_OF + membership.getMembershipType());
    }
    return sb.toString();
  }

  public static boolean isMemberShipType(Collection<Membership> mbsh, String value) {
    if(!isEmpty(value))
      for (String check : value.split(COMMA)) { 
        check = check.trim() ;
        if(check.lastIndexOf(ANY_OF) > -1) {
          if(ANY.equals(check)) return true ;
          value = check.substring(check.lastIndexOf(ANY_OF) + ANY_OF.length()) ;
          if(mbsh!= null && !mbsh.isEmpty()) {
            for(Membership mb : mbsh) {
              if(mb.getMembershipType().equals(value)) return true ; 
            }
          }
        }
      }
    return false ;
  }
  static public class SelectComparator implements Comparator{
    @Override
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((SelectItemOption) o1).getLabel() ;
      String name2 = ((SelectItemOption) o2).getLabel() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  static public class ContactComparator implements Comparator{
    @Override
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((ContactData) o1).getFullName() ;
      String name2 = ((ContactData) o2).getFullName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }

  /**
   * get all users calendars (private, public, shared)
   *
   * @return
   * @throws Exception
   */
  public static List<SelectItem> getCalendarOption() throws Exception
  {
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    CalendarService calendarService = getCalendarService() ;
    String username = getCurrentUser();
    /*
     * hash map to check existence of a calendar in the list. 
     */
    Map<String, String> hash = new HashMap<String, String>();
    /*
     * Modified by Philippe (philippe.aristote@gmail.com)
     * Uses SelectItemOptionGroup to differienciate private, shared and public groups
     */

    // private calendars group
    SelectOptionGroup privGrp = new SelectOptionGroup(CalendarUtils.PRIVATE_CALENDARS);
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      if (!hash.containsKey(c.getId())) {
        hash.put(c.getId(), "");
        privGrp.addOption(new SelectOption(c.getName(), CalendarUtils.PRIVATE_TYPE + CalendarUtils.COLON + c.getId())) ;
      }
    }
    if(privGrp.getOptions().size() > 0) options.add(privGrp);
    // shared calendars group
    GroupCalendarData gcd = calendarService.getSharedCalendars(username, true);
    if(gcd != null) {
      SelectOptionGroup sharedGrp = new SelectOptionGroup(CalendarUtils.SHARED_CALENDARS);
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, Utils.getEditPerUsers(c), username)){
          if (!hash.containsKey(c.getId())) {
            hash.put(c.getId(), "");
            sharedGrp.addOption(new SelectOption(c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }
      }
      if(sharedGrp.getOptions().size() > 0) options.add(sharedGrp);
    }
    // public calendars group
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(CalendarUtils.getUserGroups(username), true, username) ;

    if(lgcd != null) {
      SelectOptionGroup pubGrp = new SelectOptionGroup(CalendarUtils.PUBLIC_CALENDARS);      
      String[] checkPerms = getCheckPermissionString().split(CalendarUtils.COMMA);
      for(GroupCalendarData g : lgcd) {
        String groupName = g.getName();
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(hasEditPermission(c.getEditPermission(), checkPerms)){
            if (!hash.containsKey(c.getId())) {
              hash.put(c.getId(), "");
              pubGrp.addOption(new SelectOption(c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
            }
          }
        }
      }
      if(pubGrp.getOptions().size() > 0)  options.add(pubGrp);
    }
    return options ;
  }

  public static String getGroupCalendarName(String groupName, String calendarName) {
    return calendarName + Utils.SPACE + OPEN_PARENTHESIS + groupName + CLOSE_PARENTHESIS;
  }

  public static List<org.exoplatform.calendar.service.Calendar> getAllOfCurrentUserCalendars() throws Exception {
    List<org.exoplatform.calendar.service.Calendar> list = new ArrayList<org.exoplatform.calendar.service.Calendar>() ;
    CalendarService calendarService = getCalendarService() ;
    String username = getCurrentUser() ;
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      list.add(c) ;
    }
    GroupCalendarData gcd = calendarService.getSharedCalendars(username, true);
    if(gcd != null) {
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, Utils.getEditPerUsers(c), username)){
          list.add(c) ;
        }
      }
    }
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(CalendarUtils.getUserGroups(username), true, username) ;
    if(lgcd != null) {
      OrganizationService oService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(CalendarUtils.canEdit(oService, c.getEditPermission(), username)){
            list.add(c) ; 
          }
        }

      }
    }
    return list ;
  }

  public static String encodeJCRText(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
        replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
  }

  public static String encodeHTML(String htmlContent) throws Exception {
    return htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
        .replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
  }

  static public MailService getMailService() throws Exception {
    return (MailService)PortalContainer.getInstance().getComponentInstance(MailService.class) ;
  }

  public static String convertSize(long size) throws Exception {
    StringBuilder str = new StringBuilder();
    DecimalFormat df = new DecimalFormat("0.00");
    if (size > 1024 * 1024) str.append(df.format(((double) size)/(1024 * 1024))).append(" MB");
    else if (size > 1024) str.append(df.format(((double) size)/(1024))).append(" KB");
    else str.append(size).append(" B");
    return str.toString();
  }

  public static boolean isAValidEmailAddress(String email) {
    return email.matches(emailFormat);
  }

  public static boolean isValidEmailAddresses(String addressList) {
    if (isEmpty(addressList)) return true ;
    addressList = addressList.replaceAll(SEMICOLON,COMMA) ;
    List<String> emails = new ArrayList<String>() ;
    emails.addAll(Arrays.asList(addressList.split(COMMA))) ;
    for (String email : emails) {
      email = email.trim() ;
      if(!email.matches(emailRegex)) {
        return false ;
      }
    }
    return true ;
  }

  public static String invalidEmailAddresses(String addressList) {
    StringBuilder invalidEmails = new StringBuilder("") ;
    addressList = addressList.replaceAll(SEMICOLON,COMMA) ;
    List<String> emails = new ArrayList<String>() ;
    emails.addAll(Arrays.asList(addressList.split(COMMA))) ;
    for (String email : emails) {
      email = email.trim() ;
      if(!email.matches(emailRegex)) {
        if (invalidEmails.length() > 0) invalidEmails.append(", ") ;
        invalidEmails.append(email) ;
      }
    }
    if (invalidEmails.length() ==0) return addressList ;
    return invalidEmails.toString() ;
  }

  public static String invalidUsers(String userList) throws Exception {
    StringBuilder invalidNames = new StringBuilder("") ;
    userList = userList.replaceAll(SEMICOLON,COMMA) ;
    List<String> users = new ArrayList<String>() ;
    users.addAll(Arrays.asList(userList.split(COMMA))) ;
    for(String user: users){
      user = user.trim();
      if(!isUserExisted(getOrganizationService(), user)){
        if(invalidNames.length()>0) invalidNames.append(", ");
        invalidNames.append(user);
      }
    }
    return invalidNames.toString() ;
  }

  public static String parseEmailAddress(String address) {
    try {
      InternetAddress[] iAdds = InternetAddress.parse(address, true);
      return iAdds[0].getAddress() ;
    } catch (AddressException e) {
      if (log.isDebugEnabled()) {
        log.debug("The mail address is not valid", e);
      }
      return null ;
    }
  }
  public static boolean isEmailValid(String value) {
    return (value!= null && value.trim().length() > 0 && value.trim().matches(emailRegex)) ;
  }
  public static boolean isAllEmailValid(String addressList) {
    boolean isValid = true ;
    if(CalendarUtils.isEmpty(addressList)) return false ;
    addressList.replaceAll(SEMICOLON, COMMA) ;
    for(String s : addressList.split(CalendarUtils.COMMA)) {
      s = s.trim() ;
      if(!isEmailValid(s)) isValid = false ;
      break ;
    }
    return isValid  ;
  }
  public static boolean isUserExisted(OrganizationService orgSevice, String value) {
    try {
      return (!isEmpty(value) && orgSevice.getUserHandler().findUserByName(value) != null) ;
    } catch( Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to check if user exist", e);
      }
      return false ;
    }
  }
  public static String getCurrentTime(UIComponent uiCompo) throws Exception {
    return String.valueOf(System.currentTimeMillis());
  }

  public static List<SelectItemOption<String>> getSendValue(String more) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    if(more != null) 
      options.add(new SelectItemOption<String>(CalendarSetting.ACTION_BYSETTING, CalendarSetting.ACTION_BYSETTING)) ;
    options.add(new SelectItemOption<String>(CalendarSetting.ACTION_NEVER, CalendarSetting.ACTION_NEVER)) ;
    options.add(new SelectItemOption<String>(CalendarSetting.ACTION_ALWAYS, CalendarSetting.ACTION_ALWAYS)) ;
    options.add(new SelectItemOption<String>(CalendarSetting.ACTION_ASK, CalendarSetting.ACTION_ASK)) ;
    return options ;
  }

  public static String reduceSpace(String s) {
    if (isEmpty(s)) return "" ;
    String[] words = s.split(" ") ;
    StringBuilder builder = new StringBuilder() ;
    for (String word : words) {
      if (builder.length() > 0 && word.trim().length() > 0) builder.append(" ") ;
      builder.append(word.trim()) ;
    }
    return builder.toString() ;
  }

  public static String cleanValue(String values) throws Exception{
    if(isEmpty(values)) return "";
    values = values.trim().replaceAll("(,\\s*)", COMMA).replaceAll("(\\s*,)", COMMA);
    String[] tmpArr = values.split(COMMA);
    Set<String> set = new HashSet<String>(Arrays.asList(tmpArr));
    StringBuilder data = new StringBuilder();
    for (String s : set) {
      if(s.trim().length() > 0) {
        if(data.length() > 0) data.append(COMMA);
        data.append(s.trim());
      }
    }
    return data.toString();
  }

  public static String getResourceBundle(String key, String defaultValue) {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString(key);
    } catch (MissingResourceException e) {
      log.warn("Can not find the resource for key: " + key);
      return defaultValue;
    }
  }

  public static ContinuationService getContinuationService() {
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance().getComponentInstanceOfType(ContinuationService.class);
    return continuation;

  }

  public static List<org.exoplatform.calendar.service.Calendar> getAllOfCalendars(String username) throws Exception {
    List<org.exoplatform.calendar.service.Calendar> calendars = new ArrayList<org.exoplatform.calendar.service.Calendar>();
    CalendarService calendarService = getCalendarService();
    /*---- get private calendars ----*/
    List<GroupCalendarData> groupCalendars  ;
    for (org.exoplatform.calendar.service.Calendar calendar : calendarService.getUserCalendars(username, true)) {
      calendars.add(calendar);
    }

    /*---- get public calendars ----*/
    String[] groups = CalendarUtils.getUserGroups(username);
    groupCalendars = calendarService.getGroupCalendars(groups, true, CalendarUtils.getCurrentUser());
    Map<String, org.exoplatform.calendar.service.Calendar> mapCal = new HashMap<String, org.exoplatform.calendar.service.Calendar>();
    for (GroupCalendarData group : groupCalendars) {
      for (org.exoplatform.calendar.service.Calendar cal : group.getCalendars()) {
        mapCal.put(cal.getId(), cal);
      }
    }
    calendars.addAll(mapCal.values());

    /*---- get shared calendars ----*/
    GroupCalendarData groupCalendar = calendarService.getSharedCalendars(username, true);
    if (groupCalendar != null) {
      for (org.exoplatform.calendar.service.Calendar calendar : groupCalendar.getCalendars()) {
        calendars.add(calendar);
      }
    }
    return calendars;
  }   

  public static String getLabel(String componentid, String id) throws Exception
  {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String label = componentid + ".label." + id;
    try
    {
      return res.getString(label);
    }
    catch (MissingResourceException e)
    {
      return id;
    }
  }

  public static String enCodeTitle(String s) {
    StringBuffer buffer = new StringBuffer();
    if(s != null) {
      s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
      s = s.replaceFirst("(<br/>)*", "");
      s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
      s.replaceAll("&", "&amp;").replaceAll("'", "&apos;");
      for (int j = 0; j < s.trim().length(); j++) {
        char c = s.charAt(j);
        if(c == 60){
          buffer.append("&lt;") ;
        } else if(c == 62){
          buffer.append("&gt;") ;
        } else if(c == '\''){
          buffer.append("&#39") ;
        } else {
          buffer.append(c) ;
        }
      }
    }
    return buffer.toString();
  }

  public static org.exoplatform.calendar.service.Calendar getCalendar(String calType, String calendarId) throws Exception {
    CalendarService calService = CalendarUtils.getCalendarService() ;
    String currentUser = CalendarUtils.getCurrentUser() ;
    org.exoplatform.calendar.service.Calendar calendar = null;
    if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
      calendar = calService.getUserCalendar(currentUser, calendarId) ;
    } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
      GroupCalendarData gCalendarData = calService.getSharedCalendars(currentUser, true) ;
      if(gCalendarData != null) calendar = gCalendarData.getCalendarById(calendarId) ;
    } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
      calendar = calService.getGroupCalendar(calendarId) ;
    }
    return calendar;
  }

  public static List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(getCurrentUser()) ;
    for(EventCategory category : eventCategories) {
      // Check if EventCategory is default event category
      boolean isDefaultEventCategory = false;
      for (int i = 0; i < NewUserListener.defaultEventCategoryIds.length; i++) {
        if (category.getId().equals(NewUserListener.defaultEventCategoryIds[i])
            && category.getName().equals(NewUserListener.defaultEventCategoryNames[i])) {
          isDefaultEventCategory = true;
          break;
        }
      }

      if (isDefaultEventCategory) {
        String newName = getResourceBundle("UICalendarView.label." + category.getId(), category.getId());
        options.add(new SelectItemOption<String>(newName, category.getId())) ;
      } else {
        options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;        
      }
    }
    return options ;
  }

  public static int getLimitUploadSize() {
    PortletRequestContext pcontext = (PortletRequestContext) RequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    int limitMB;
    try {
      limitMB = Integer.parseInt(portletPref.getValue("uploadFileSizeLimitMB", "").trim());
    } catch (NumberFormatException e) {
      limitMB = DEFAULT_VALUE_UPLOAD_PORTAL;
    }
    return limitMB;
  }
  public static String buildSubscribeUrl(String calId, String calType, boolean isPrivate){
    try {
      String baseUrl = isPrivate ? CalendarWebservice.BASE_URL_PRIVATE : CalendarWebservice.BASE_URL_PUBLIC;
      return new StringBuffer(SLASH).append(PortalContainer.getCurrentRestContextName())
          .append(baseUrl).append(getCurrentUser()).append(SLASH)
          .append(calId).append(SLASH).append(calType).toString();
    } catch (Exception e) {
      return null;
    }
  }
  /**
   * Gets the default view in user setting
   * If the saved view is not in view types any more (for example YearView is removed in PLF 4), 
   * this method returns DayView as default view and save this view to the setting
   * @return the view 
   */
  public static String getViewInSetting() {
    CalendarSetting calendarSetting = getCurrentUserCalendarSetting();
    try {
      return UICalendarViewContainer.TYPES[Integer.parseInt(calendarSetting.getViewType())];
    } catch (ArrayIndexOutOfBoundsException e) {
      resetViewInSetting(calendarSetting);
      return UICalendarViewContainer.TYPES[0];
    } catch (NumberFormatException nfe) {
      resetViewInSetting(calendarSetting);
      return UICalendarViewContainer.TYPES[0];
    }
  }
  
  // save a default view type for Calendar Setting when cannot get the view type
  private static void resetViewInSetting(CalendarSetting calendarSetting) {
    try {
      calendarSetting.setViewType(CalendarSetting.DAY_VIEW);
      getCalendarService().saveCalendarSetting(getCurrentUser(), calendarSetting);
      setCurrentCalendarSetting(calendarSetting);
    } catch(Exception e) {
      if(log.isDebugEnabled()) {
        log.debug("Cant save Calendar Setting",e);
      }
    }
  }
}
