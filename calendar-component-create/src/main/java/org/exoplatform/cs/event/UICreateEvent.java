package org.exoplatform.cs.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created with IntelliJ IDEA.
 * User: Racha
 * Date: 01/11/12
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "classpath:groovy/webui/create/UICreateEvent.gtmpl",
                 events = {

                         @EventConfig(
                                 listeners = UICreateEvent.NextActionListener.class,
                                 phase = Event.Phase.DECODE
                       )   ,
                         @EventConfig(
                                 listeners = UICreateEvent.CancelActionListener.class,
                                 phase = Event.Phase.DECODE
                         )
                 }
    )

public class UICreateEvent extends UIForm {

  public static final String PRIVATE_CALENDARS = "privateCalendar";
  public static final String SHARED_CALENDARS = "sharedCalendar";
  public static final String PUBLIC_CALENDARS = "publicCalendar";
  public static final String PRIVATE_TYPE = "0";
  public static final String SHARED_TYPE = "1";
  public static final String PUBLIC_TYPE = "2";
  public static final String COLON = ":";
  public static final String COMMA = ",";
  public static final String ANY = "*.*";
  public static final String ANY_OF = "*.";
  public static final String DOT = ".";
  public static final String SLASH_COLON = "/:";
  public static final String OPEN_PARENTHESIS = "(";
  public static final String CLOSE_PARENTHESIS = ")";
  private static Log log = ExoLogger.getLogger(UICreateEvent.class);
  static String CHOIX = "Choix";

  static String TITLE = "Title";

  public static String END_EVENT = "EndEvent";
  public static String CALENDAR = "Calendar";
  public static String START_EVENT = "StartEvent";
  public static String START_TIME = "start_time";
  public static String END_TIME = "end_time";
  public static String ALL_DAY = "all-day";
  private String calType_ = "0";
  public static final String TIMEFORMAT = "HH:mm";
  public static final String DISPLAY_TIMEFORMAT = "hh:mm a";
  public static final long DEFAULT_TIME_INTERVAL = 30;

  public UICreateEvent() throws Exception {
    addUIFormInput(new UIFormRadioBoxInput(CHOIX, "Event", getTypeValue()));
    addUIFormInput(new UIFormStringInput(TITLE, TITLE, null));
    addUIFormInput(new UIFormDateTimeInput(START_EVENT, START_EVENT, getInstanceOfCurrentCalendar().getTime(), false));
    addUIFormInput(new UIFormDateTimeInput(END_EVENT, END_EVENT, getInstanceOfCurrentCalendar().getTime(), false));
    addUIFormInput(new UIFormSelectBoxWithGroups(CALENDAR, CALENDAR, getCalendarOption()));
    addUIFormInput(new UIFormSelectBox(START_TIME, START_TIME, getTimesSelectBoxOptions(DISPLAY_TIMEFORMAT)));
    addUIFormInput(new UIFormSelectBox(END_TIME, END_TIME, getTimesSelectBoxOptions(DISPLAY_TIMEFORMAT)));
  }


  protected String getDateTimeFormat(){
    UIFormDateTimeInput fromField = getChildById(START_EVENT);
    return fromField.getDatePattern_();
  }


  static public class NextActionListener extends EventListener<UICreateEvent> {
    public void execute(Event<UICreateEvent> event)
        throws Exception {
      UICreateEvent uiForm = event.getSource();
      String summary = uiForm.getEventSummary();
      if (summary == null || summary.trim().length() <= 0) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId()
                                                                                       + ".msg.summary-field-required", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      summary = summary.trim();
      summary = enCodeTitle(summary);
      UIFormDateTimeInput fromField = uiForm.getChildById(START_EVENT);
      UIFormDateTimeInput toField = uiForm.getChildById(END_EVENT);
      Date from = uiForm.getDateTime(fromField, UICreateEvent.START_TIME);
      Date to = uiForm.getDateTime(toField, UICreateEvent.END_TIME);
      if (from == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.fromDate-format", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      if (to == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.toDate-format", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      if (from.after(to) || from.equals(to)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }

      CalendarService calService = getCalendarService();
      if (calService.isRemoteCalendar(getCurrentUser(), uiForm.getEventCalendar())) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() +".msg.cant-add-event-on-remote-calendar", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }

      try {
        CalendarEvent calEvent = new CalendarEvent();
        calEvent.setSummary(summary);
        calEvent.setCalendarId(uiForm.getEventCalendar());
        String username = getCurrentUser();
        boolean isEvent = "Event".equals(((UIFormRadioBoxInput) uiForm.getUIInput(CHOIX)).getValue());
        if (isEvent) {
          calEvent.setEventType(CalendarEvent.TYPE_EVENT);
          calEvent.setEventState(CalendarEvent.ST_BUSY);

          calEvent.setRepeatType(CalendarEvent.RP_NOREPEAT);
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK);
          calEvent.setEventState(CalendarEvent.NEEDS_ACTION);
          calEvent.setTaskDelegator(event.getRequestContext().getRemoteUser());
        }
        calEvent.setFromDateTime(from);
        calEvent.setToDateTime(to);
        calEvent.setCalType(uiForm.calType_);
        String calName="";
        if(calService.getUserCalendar(username,uiForm.getEventCalendar())!=null){

          if (calService.getUserCalendar(username,uiForm.getEventCalendar()).getId().equals(Utils.getDefaultCalendarId(username)) ) {
            calName = calService.getUserCalendar(username,uiForm.getEventCalendar()).getName();

          }
        }else {
          if(calService.getGroupCalendar(uiForm.getEventCalendar())!=null){


            calName= getGroupCalendarName(calService.getGroupCalendar(uiForm.getEventCalendar()).getGroups()[0].substring(calService.getGroupCalendar(uiForm.getEventCalendar()).getGroups()[0].lastIndexOf("/") + 1),
                                          calService.getGroupCalendar(uiForm.getEventCalendar()).getName()) ;

          } else{
            if( calService.getSharedCalendars(username,true).getCalendarById(uiForm.getEventCalendar())!=null){
              if (calService.getUserCalendar(username,uiForm.getEventCalendar()).getId().equals(Utils.getDefaultCalendarId(calService.getUserCalendar(username,uiForm.getEventCalendar()).getCalendarOwner())) && calService.getUserCalendar(username,uiForm.getEventCalendar()).getName().equals(NewUserListener.defaultCalendarName)) {
                calName = getResourceBundle("UICreateEvent.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);

              }
              String owner = "";
              if (calService.getUserCalendar(username,uiForm.getEventCalendar()).getCalendarOwner() != null) owner = calService.getUserCalendar(username,uiForm.getEventCalendar()).getCalendarOwner() + " - ";
              calName= new StringBuilder(owner).append(calName).toString();
            }
          }
        }
        if (uiForm.calType_.equals(PRIVATE_TYPE)) {
          calService.saveUserEvent(username, calEvent.getCalendarId(), calEvent, true);
        } else if (uiForm.calType_.equals(SHARED_TYPE)) {
          calService.saveEventToSharedCalendar(username, calEvent.getCalendarId(), calEvent, true);
        } else if (uiForm.calType_.equals(PUBLIC_TYPE)) {
          calService.savePublicEvent(calEvent.getCalendarId(), calEvent, true);
        }
        String defaultMsg = "The {0} added to the {1}.";
        String message =  UICreateEvent.getResourceBundle(uiForm.getId()+".msg.add-successfully."+ calEvent.getEventType(),defaultMsg);
        message = message.replace("{1}", calName);
        Event<UIComponent> cancelEvent = uiForm.<UIComponent>getParent().createEvent("Cancel", Event.Phase.PROCESS, event.getRequestContext());
        if (cancelEvent != null) {
          cancelEvent.broadcast();
        }
        event.getRequestContext().getJavascriptManager().require("SHARED/navigation-toolbar", "toolbarnav").addScripts("toolbarnav.UIPortalNavigation.cancelNextClick('UICreateList','UICreatePlatformToolBarPortlet','" + message + "');");
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to quick add event to the calendar", e);
        }
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-unsuccessfully", null));
      }


    }
  }

  public Date getDateTime(UIFormDateTimeInput input, String selectId) throws Exception {
    String timeField = getUIFormSelectBox(selectId).getValue();
    boolean isAllDate = ALL_DAY.equals(timeField);
    if (END_TIME.equals(selectId)) {
      return getEndDate(isAllDate, input.getDatePattern_(), input.getValue(), TIMEFORMAT, timeField);
    } else return getBeginDate(isAllDate, input.getDatePattern_(), input.getValue(), TIMEFORMAT, timeField);
  }

  public static Date getBeginDate(boolean isAllDate, String dateFormat, String fromField, String timeFormat, String timeField) throws Exception {
    try {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    if (isAllDate) {
      DateFormat df = new SimpleDateFormat(dateFormat, locale);
      df.setCalendar(getInstanceOfCurrentCalendar());
      return getBeginDay(df.parse(fromField)).getTime();
    }
    DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE + timeFormat, locale);
    df.setCalendar(getInstanceOfCurrentCalendar());
    return df.parse(fromField + Utils.SPACE + timeField);
    } catch (Exception e) {
      return null;
    } 
  }

  public static Date getEndDate(boolean isAllDate, String dateFormat, String fromField, String timeFormat, String timeField) throws Exception {
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      Locale locale = context.getParentAppRequestContext().getLocale();
      if (isAllDate) {
        DateFormat df = new SimpleDateFormat(dateFormat, locale);
        df.setCalendar(getInstanceOfCurrentCalendar());
        Calendar temp = getEndDay(df.parse(fromField)) ;
        temp.setTimeInMillis(temp.getTimeInMillis()-1);
        return temp.getTime();
      }
      DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE + timeFormat, locale);
      df.setCalendar(getInstanceOfCurrentCalendar());
      return df.parse(fromField + Utils.SPACE + timeField);
    } catch (Exception e) {
      return null;
    }  
  }

  public static CalendarSetting getCurrentUserCalendarSetting() {
    try {
      String user = getCurrentUser();
      CalendarSetting setting = getCalendarService().getCalendarSetting(user);
      return setting;
    } catch (Exception e) {
      log.warn("could not get calendar setting of user", e);
      return null;
    }

  }

  public static Calendar getInstanceOfCurrentCalendar() {
    try {
      CalendarSetting setting = getCurrentUserCalendarSetting();
      return getCalendarInstanceBySetting(setting);
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn("Could not get calendar setting!", e);
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.setLenient(false);
      return calendar;
    }
  }

  public static Calendar getCalendarInstanceBySetting(final CalendarSetting calendarSetting) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setLenient(false);
    calendar.setTimeZone(TimeZone.getTimeZone(calendarSetting.getTimeZone()));
    calendar.setFirstDayOfWeek(Integer.parseInt(calendarSetting.getWeekStartOn()));
    // fix CS-4725
    calendar.setMinimalDaysInFirstWeek(4);
    return calendar;
  }

  public static Calendar getBeginDay(Calendar cal) {
    Calendar newCal = (Calendar) cal.clone();

    newCal.set(Calendar.HOUR_OF_DAY, 0);
    newCal.set(Calendar.MINUTE, 0);
    newCal.set(Calendar.SECOND, 0);
    newCal.set(Calendar.MILLISECOND, 0);
    return newCal;
  }

  public static Calendar getEndDay(Calendar cal) {
    Calendar newCal = (Calendar) cal.clone();
    newCal.set(Calendar.HOUR_OF_DAY, 0);
    newCal.set(Calendar.MINUTE, 0);
    newCal.set(Calendar.SECOND, 0);
    newCal.set(Calendar.MILLISECOND, 0);
    newCal.add(Calendar.HOUR_OF_DAY, 24);
    return newCal;
  }

  public static Calendar getBeginDay(Date date) {
    Calendar cal = getInstanceOfCurrentCalendar();
    cal.setTime(date);
    return getBeginDay(cal);
  }

  public static Calendar getEndDay(Date date) {
    Calendar cal = getInstanceOfCurrentCalendar();
    cal.setTime(date);
    return getEndDay(cal);
  }

  static public class CancelActionListener extends EventListener<UICreateEvent> {


    public void execute(Event<UICreateEvent> event)
        throws Exception {
      UICreateEvent uisource = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      Event<UIComponent> cancelEvent = uisource.<UIComponent>getParent().createEvent("Cancel", Event.Phase.DECODE, ctx);
      if (cancelEvent != null) {
        cancelEvent.broadcast();
      }


    }
  }



    private List<SelectItemOption<String>> getTypeValue() {
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
        options.add(new SelectItemOption<String>("Event", "Event")) ;
        options.add(new SelectItemOption<String>("Task", "Task")) ;
        return options ;
    }

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    return getTimesSelectBoxOptions(timeFormat, TIMEFORMAT, DEFAULT_TIME_INTERVAL, locale);
  }

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval) {

    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    return getTimesSelectBoxOptions(labelFormat, valueFormat, timeInteval, locale);
  }

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval, Locale locale) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(getResourceBundle("UICreateEvent.label."+ALL_DAY,"All Day"), ALL_DAY));
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("")); // get a GMT calendar
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);

    DateFormat dfLabel = new SimpleDateFormat(labelFormat, locale);
    dfLabel.setCalendar(cal);
    DateFormat dfValue = new SimpleDateFormat(valueFormat, locale);
    dfValue.setCalendar(cal);

    int day = cal.get(Calendar.DAY_OF_MONTH);
    while (day == cal.get(Calendar.DAY_OF_MONTH)) {
      options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime())));
      cal.add(java.util.Calendar.MINUTE, (int) timeInteval);
    }
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.MILLISECOND, 59);
    options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime())));
    return options;
  }

  public static List<SelectItem> getCalendarOption() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>();
    CalendarService calendarService = getCalendarService();
    String username = getCurrentUser();
    Map<String, String> hash = new HashMap<String, String>();
    // private calendars group
    SelectOptionGroup privGrp = new SelectOptionGroup(PRIVATE_CALENDARS);
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(username, true);
    for (org.exoplatform.calendar.service.Calendar c : calendars) {
      if (c.getId().equals(Utils.getDefaultCalendarId(username)) && c.getName().equals(NewUserListener.defaultCalendarName)) {
        String newName = getResourceBundle("UICreateEvent.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
        c.setName(newName);
      }
      if (!hash.containsKey(c.getId())) {
        hash.put(c.getId(), "");
        privGrp.addOption(new SelectOption(c.getName(), PRIVATE_TYPE + COLON + c.getId()));
      }
    }
    if (privGrp.getOptions().size() > 0) options.add(privGrp);
    // shared calendars group
    GroupCalendarData gcd = calendarService.getSharedCalendars(username, true);
    if (gcd != null) {
      SelectOptionGroup sharedGrp = new SelectOptionGroup(SHARED_CALENDARS);
      for (org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if (canEdit(null, Utils.getEditPerUsers(c), username)) {
          if (c.getId().equals(Utils.getDefaultCalendarId(c.getCalendarOwner())) && c.getName().equals(NewUserListener.defaultCalendarName)) {
            String newName = getResourceBundle("UICreateEvent.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
            c.setName(newName);
          }
          String owner = "";
          if (c.getCalendarOwner() != null) owner = c.getCalendarOwner() + " - ";
          if (!hash.containsKey(c.getId())) {
            hash.put(c.getId(), "");
            sharedGrp.addOption(new SelectOption(owner + c.getName(), SHARED_TYPE + COLON + c.getId()));
          }
        }
      }
      if (sharedGrp.getOptions().size() > 0) options.add(sharedGrp);
    }
    // public calendars group
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(getUserGroups(username), true, username);

    if (lgcd != null) {
      SelectOptionGroup pubGrp = new SelectOptionGroup(PUBLIC_CALENDARS);
      String[] checkPerms = getCheckPermissionString().split(COMMA);
      for (GroupCalendarData g : lgcd) {
        String groupName = g.getName();
        for (org.exoplatform.calendar.service.Calendar c : g.getCalendars()) {
          if (hasEditPermission(c.getEditPermission(), checkPerms)) {
            if (!hash.containsKey(c.getId())) {
              hash.put(c.getId(), "");
              pubGrp.addOption(new SelectOption(getGroupCalendarName(groupName.substring(groupName.lastIndexOf("/") + 1),
                                                                     c.getName()), PUBLIC_TYPE + COLON + c.getId()));
            }
          }
        }
      }
      if (pubGrp.getOptions().size() > 0) options.add(pubGrp);
    }
    return options;
  }

  static public CalendarService getCalendarService() throws Exception {
    return (CalendarService) PortalContainer.getInstance().getComponentInstance(CalendarService.class);
  }

  @SuppressWarnings("unchecked")
  public static String getCheckPermissionString() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    StringBuffer sb = new StringBuffer(identity.getUserId());
    Set<String> groupsId = identity.getGroups();
    for (String groupId : groupsId) {
      sb.append(COMMA).append(groupId).append(SLASH_COLON).append(ANY);
      sb.append(COMMA).append(groupId).append(SLASH_COLON).append(identity.getUserId());
    }
    Collection<MembershipEntry> memberships = identity.getMemberships();
    for (MembershipEntry membership : memberships) {
      sb.append(COMMA).append(membership.getGroup()).append(SLASH_COLON).append(ANY_OF + membership.getMembershipType());
    }
    return sb.toString();
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

  public static String getResourceBundle(String key, String defaultValue) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString(key);
    } catch (MissingResourceException e) {
      log.warn("Can not find the resource for key: " + key);
      return defaultValue;
    }
  }

  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }

  public static boolean canEdit(OrganizationService oService, String[] savePerms, String username) throws Exception {
    String checkPerms = getCheckPermissionString();
    return hasEditPermission(savePerms, checkPerms.toString().split(COMMA));
  }

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

  public static String getGroupCalendarName(String groupName, String calendarName) {
    return calendarName + Utils.SPACE + OPEN_PARENTHESIS + groupName + CLOSE_PARENTHESIS;
  }

  private String getEventSummary() {
    return getUIStringInput(TITLE).getValue();
  }

  public static String enCodeTitle(String s) {
    StringBuffer buffer = new StringBuffer();
    if (s != null) {
      s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
      s = s.replaceFirst("(<br/>)*", "");
      s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
      s.replaceAll("&", "&amp;").replaceAll("'", "&apos;");
      for (int j = 0; j < s.trim().length(); j++) {
        char c = s.charAt(j);
        if ((int) c == 60) {
          buffer.append("&lt;");
        } else if ((int) c == 62) {
          buffer.append("&gt;");
        } else if (c == '\'') {
          buffer.append("&#39");
        } else {
          buffer.append(c);
        }
      }
    }
    return buffer.toString();
  }

  private String getEventCalendar() {
    String values = getUIFormSelectBoxGroup(CALENDAR).getValue();
    if (values != null && values.trim().length() > 0 && values.split(COLON).length > 0) {
      calType_ = values.split(COLON)[0];
      return values.split(COLON)[1];
    }
    return null;

  }

  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id);
  }
}
