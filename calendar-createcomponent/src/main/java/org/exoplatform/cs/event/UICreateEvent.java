package org.exoplatform.cs.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
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
                                phase = org.exoplatform.webui.event.Event.Phase.DECODE
                       ),
                       @EventConfig(
                                    listeners = UICreateEvent.CancelActionListener.class,
                                    phase = org.exoplatform.webui.event.Event.Phase.DECODE
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
  static List<SelectItemOption<String>> options = new ArrayList();
  private static Log log = ExoLogger.getLogger(UICreateEvent.class);
  static String CHOIX = "Choix";

  static String TITLE = "Title";

  static String END_EVENT = "EndEvent";
  static String CALENDAR = "Calendar";
  static String Start_EVENT = "StartEvent";
  private String calType_ = "0";
  public UICreateEvent()
      throws Exception {


    addUIFormInput(new UIFormRadioBoxInput(CHOIX, "Event", options));
    addUIFormInput(new UIFormStringInput(TITLE, TITLE, null));
    addUIFormInput(new UIFormDateTimeInput(Start_EVENT, Start_EVENT, null, true));
    addUIFormInput(new UIFormDateTimeInput(END_EVENT, END_EVENT, null, true));
    addUIFormInput(new UIFormSelectBoxWithGroups(CALENDAR, CALENDAR, getCalendarOption()));


  }

  public String[] getActions() {
    return new String[]{"Next", "Cancel"};
  }


  static public class NextActionListener extends EventListener<UICreateEvent> {


    public void execute(Event<UICreateEvent> event)
        throws Exception {

      UICreateEvent uiForm = event.getSource() ;
      
      String summary = uiForm.getEventSummary();
      if (summary == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId()
            + ".msg.summary-field-required", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return;
      }
      summary = summary.trim();
      summary = enCodeTitle(summary);
      UIFormDateTimeInput fromField = uiForm.getChildById(Start_EVENT);
      UIFormDateTimeInput toField = uiForm.getChildById(END_EVENT);
      Calendar from =  fromField.getCalendar();
      Calendar to = toField.getCalendar();
      if(from == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.fromDate-format", null, ApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return ;
      }
      if(to == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.toDate-format", null, ApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return ;
      }
      if(from.after(to) || from.equals(to)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return ;
      }
      
      CalendarService calService =  getCalendarService() ;
      if(calService.isRemoteCalendar(getCurrentUser(), uiForm.getEventCalendar())) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return;
      }
      
      try {
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(summary) ;
        calEvent.setCalendarId(uiForm.getEventCalendar());
        String username = getCurrentUser() ;
        boolean isEvent = "Event".equals(((UIFormRadioBoxInput)uiForm.getUIInput(CHOIX)).getValue()) ;
        if(isEvent){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
          calEvent.setEventState(CalendarEvent.ST_BUSY) ;
           
          calEvent.setRepeatType(CalendarEvent.RP_NOREPEAT) ;
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
          calEvent.setEventState(CalendarEvent.NEEDS_ACTION) ;
          calEvent.setTaskDelegator(event.getRequestContext().getRemoteUser());
        }
        calEvent.setFromDateTime(from.getTime());
        calEvent.setToDateTime(to.getTime()) ;
        calEvent.setCalType(uiForm.calType_) ;
        
          
        if(uiForm.calType_.equals(PRIVATE_TYPE)) {
          calService.saveUserEvent(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(SHARED_TYPE)){
          calService.saveEventToSharedCalendar(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(PUBLIC_TYPE)){
          calService.savePublicEvent(calEvent.getCalendarId(), calEvent, true) ;          
        }
        
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to quick add event to the calendar", e);
        }
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-unsuccessfully", null)) ;
      }
      UICreateEvent   uisource=event.getSource();
      uisource.reset();
      WebuiRequestContext ctx = event.getRequestContext();
      Event<UIComponent> cancelEvent = uisource.createEvent("Cancel", Event.Phase.DECODE, ctx);
      if (cancelEvent != null) {
        cancelEvent.broadcast();
      }
    }
  }


  static public class CancelActionListener extends EventListener<UICreateEvent> {


    public void execute(Event<UICreateEvent> event)
        throws Exception {
      UICreateEvent   uisource=event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      Event<UIComponent> cancelEvent = uisource.<UIComponent>getParent().createEvent("Cancel", Event.Phase.DECODE, ctx);
      if (cancelEvent != null) {
        cancelEvent.broadcast();
      }


    }
  }


  static {
    options.add(new SelectItemOption("Event"));
    options.add(new SelectItemOption("Task"));
  }

  public static List<SelectItem> getCalendarOption() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    CalendarService calendarService = getCalendarService() ;
    String username = getCurrentUser();
    Map<String, String> hash = new HashMap<String, String>();
    // private calendars group
    SelectOptionGroup privGrp = new SelectOptionGroup(PRIVATE_CALENDARS);
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      if (c.getId().equals(Utils.getDefaultCalendarId(username)) && c.getName().equals(NewUserListener.defaultCalendarName)) {
        String newName = getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
        c.setName(newName);
      }
      if (!hash.containsKey(c.getId())) {
        hash.put(c.getId(), "");
        privGrp.addOption(new SelectOption(c.getName(), PRIVATE_TYPE + COLON + c.getId())) ;
      }
    }
    if(privGrp.getOptions().size() > 0) options.add(privGrp);
    // shared calendars group
    GroupCalendarData gcd = calendarService.getSharedCalendars(username, true);
    if(gcd != null) {
      SelectOptionGroup sharedGrp = new SelectOptionGroup(SHARED_CALENDARS);
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(canEdit(null, Utils.getEditPerUsers(c), username)){
          if (c.getId().equals(Utils.getDefaultCalendarId(c.getCalendarOwner())) && c.getName().equals(NewUserListener.defaultCalendarName)) {
            String newName = getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
            c.setName(newName);
          }
          String owner = "" ;
          if(c.getCalendarOwner() != null) owner = c.getCalendarOwner() + " - " ;
          if (!hash.containsKey(c.getId())) {
            hash.put(c.getId(), "");
            sharedGrp.addOption(new SelectOption(owner + c.getName(), SHARED_TYPE + COLON + c.getId())) ;
          }
        }
      }
      if(sharedGrp.getOptions().size() > 0) options.add(sharedGrp);
    }
    // public calendars group
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(getUserGroups(username), true, username) ;

    if(lgcd != null) {
      SelectOptionGroup pubGrp = new SelectOptionGroup(PUBLIC_CALENDARS);      
      String[] checkPerms = getCheckPermissionString().split(COMMA);
      for(GroupCalendarData g : lgcd) {
        String groupName = g.getName();
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(hasEditPermission(c.getEditPermission(), checkPerms)){
            if (!hash.containsKey(c.getId())) {
              hash.put(c.getId(), "");
              pubGrp.addOption(new SelectOption(getGroupCalendarName(groupName.substring(groupName.lastIndexOf("/") + 1),
                                                                     c.getName()), PUBLIC_TYPE + COLON + c.getId())) ;
            }
          }
        }
      }
      if(pubGrp.getOptions().size() > 0)  options.add(pubGrp);
    }
    return options ;
  }

  static public CalendarService getCalendarService() throws Exception {
    return (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class) ;
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
    if(savePerms != null)
      for(String sp : savePerms) {
        for (String cp : checkPerms) {
          if(sp.equals(cp)) {return true ;}      
        }
      }
    return false ;
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
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }

  public static boolean canEdit(OrganizationService oService, String[] savePerms, String username) throws Exception {
    String checkPerms = getCheckPermissionString();
    return hasEditPermission(savePerms, checkPerms.toString().split(COMMA)) ;
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
    return getUIStringInput(TITLE).getValue() ;
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
        if((int)c == 60){
          buffer.append("&lt;") ;
        } else if((int)c == 62){
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
  private String getEventCalendar() {
    String values = getUIFormSelectBoxGroup(CALENDAR).getValue() ;
    if(values != null && values.trim().length() > 0 && values.split(COLON).length > 0) {
      calType_ = values.split(COLON)[0] ;
      return values.split(COLON)[1] ;
    }
    return null ;

  }
  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id) ;
  }
}
